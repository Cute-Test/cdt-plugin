/*******************************************************************************
 * Copyright (c) 2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 *  
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *******************************************************************************/
package ch.hsr.ifs.cute.tdd.addArgument;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNode.CopyStyle;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameter;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTIdExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTName;
import org.eclipse.cdt.internal.ui.refactoring.ModificationCollector;
import org.eclipse.cdt.internal.ui.refactoring.RefactoringASTCache;
import org.eclipse.cdt.internal.ui.refactoring.togglefunction.ToggleNodeHelper;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;

import ch.hsr.ifs.cute.tdd.Activator;
import ch.hsr.ifs.cute.tdd.CRefactoring3;
import ch.hsr.ifs.cute.tdd.TypeHelper;

public class AddArgumentRefactoring extends CRefactoring3 {

	private static final String PLACEHOLDER = "_"; //$NON-NLS-1$
	private ITextSelection selection;
	private int candidatenr;

	public AddArgumentRefactoring(ISelection selection, RefactoringASTCache astCache, int candidatenr) {
		super(selection, astCache);
		this.selection = (ITextSelection) selection;
		this.candidatenr = candidatenr;
	}

	@Override
	protected void collectModifications(IProgressMonitor pm,
			ModificationCollector collector) throws CoreException {
		IASTTranslationUnit unit = astCache.getAST(tu, pm);
		IASTFunctionCallExpression call = findSelectedCall(unit);
		ASTRewrite rewrite = collector.rewriterForTranslationUnit(unit);
		rewrite.replace(call, createCorrectFunctionCall(call, candidatenr), null);
	}

	private IASTFunctionCallExpression findSelectedCall(IASTTranslationUnit unit) {
		IASTFunctionCallExpression call = findFunctionCall(unit, selection);
		if (call == null) {
			// Needed if the user types fast and their marker's position is outdated
			selection = Activator.getDefault().getEditorSelection();
			call = findFunctionCall(unit, selection);
			if (call == null) {
				throw new OperationCanceledException(Messages.AddArgumentRefactoring_1);
			}
		}
		return call;
	}

	private IASTFunctionCallExpression findFunctionCall(IASTTranslationUnit unit,
			ITextSelection selection) {
		IASTNode selectedNode = unit.getNodeSelector(null).findEnclosingNode(
				selection.getOffset(), selection.getLength());
		return ToggleNodeHelper.getAncestorOfType(selectedNode, IASTFunctionCallExpression.class);
	}

	private IASTFunctionCallExpression createCorrectFunctionCall(
			IASTFunctionCallExpression call, int candidatenr) {
		List<ICPPParameter> parameters = getParametersOf(call, candidatenr);
		List<IASTInitializerClause> newArgs = getNewArguments(
				Arrays.asList(call.getArguments()), parameters);
		IASTFunctionCallExpression newCall = call.copy(CopyStyle.withLocations);
		newCall.setArguments(newArgs.toArray(new IASTInitializerClause[] {}));
		return newCall;
	}

	private List<ICPPParameter> getParametersOf(IASTFunctionCallExpression call, int candidateNr) {
		ICPPFunction candidate = getNthCandidate(call, candidateNr);
		List<ICPPParameter> parameters = Arrays.asList(candidate.getParameters());
		return parameters;
	}

	private ICPPFunction getNthCandidate(IASTFunctionCallExpression caller, int nr) {
		IBinding[] candidates = getProblemBinding(caller).getCandidateBindings();
		if (candidates.length > nr && candidates[nr] instanceof ICPPFunction) {
			return (ICPPFunction) candidates[nr];
		}
		throw new OperationCanceledException(Messages.AddArgumentRefactoring_2);
	}

	private IProblemBinding getProblemBinding(IASTFunctionCallExpression caller) {
		IASTExpression fnameexpr = caller.getFunctionNameExpression();
		IBinding binding = findProblemName(fnameexpr);
		if (binding == null || !(binding instanceof IProblemBinding)) {
			throw new OperationCanceledException(Messages.AddArgumentRefactoring_3);
		}
		return (IProblemBinding) binding;
	}

	private IBinding findProblemName(IASTExpression fnameexpr) {
		NameCollector visitor = new NameCollector();
		fnameexpr.accept(visitor);
		for (IASTName n : visitor.getNames()) {
			IBinding binding = n.resolveBinding();
			if (binding instanceof IProblemBinding) {
				return binding;
			}
		}
		throw new OperationCanceledException(Messages.AddArgumentRefactoring_4);
	}

	public static List<IASTInitializerClause> getNewArguments(
			List<IASTInitializerClause> oldArgs, List<ICPPParameter> parameters) {
		List<IASTInitializerClause> newArgs = new ArrayList<IASTInitializerClause>();
		for (int i = 0; i < parameters.size(); i++) {
			if (i < oldArgs.size() && !TypeHelper.haveSameType(oldArgs.get(i), parameters.get(i))) {
				return null;
			}
			newArgs.add(getNewArgument(oldArgs, i));
		}
		return newArgs;
	}

	private static IASTInitializerClause getNewArgument(List<IASTInitializerClause> oldArgs, int position) {
		if (position < oldArgs.size()) {
			return (oldArgs.get(position)).copy(CopyStyle.withLocations);
		}
		return createEmptyArgument();
	}

	private static IASTIdExpression createEmptyArgument() {
		return new CPPASTIdExpression(new CPPASTName(PLACEHOLDER.toCharArray()));
	}
}
