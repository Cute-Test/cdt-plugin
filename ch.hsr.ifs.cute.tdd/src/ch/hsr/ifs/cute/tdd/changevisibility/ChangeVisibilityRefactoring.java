/*******************************************************************************
 * Copyright (c) 2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 *  
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *******************************************************************************/
package ch.hsr.ifs.cute.tdd.changevisibility;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMember;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.cdt.internal.ui.refactoring.ModificationCollector;
import org.eclipse.cdt.internal.ui.refactoring.NodeContainer;
import org.eclipse.cdt.internal.ui.refactoring.RefactoringASTCache;
import org.eclipse.cdt.internal.ui.refactoring.togglefunction.ToggleNodeHelper;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;

import ch.hsr.ifs.cute.tdd.CRefactoring3;
import ch.hsr.ifs.cute.tdd.TddHelper;
import ch.hsr.ifs.cute.tdd.TypeHelper;

@SuppressWarnings("restriction")
public class ChangeVisibilityRefactoring extends CRefactoring3 {

	private String nameToSearch;
	private TextSelection selection;

	public ChangeVisibilityRefactoring(ISelection selection,
			String name, RefactoringASTCache astCache) {
		super(selection, astCache);
		this.nameToSearch = name;
		this.selection = (TextSelection) selection;
	}

	@Override
	protected void collectModifications(IProgressMonitor pm,
			ModificationCollector collector) throws CoreException,
			OperationCanceledException {
		IASTTranslationUnit localunit = astCache.getAST(tu, pm);
		IASTNode selectedNode = localunit.getNodeSelector(null).findEnclosingNode(selection.getOffset(), selection.getLength());
		ICPPASTCompositeTypeSpecifier typeSpec = TypeHelper.getTargetTypeOfField(localunit, (IASTName) selectedNode, astCache);
		MethodFindVisitor memberfinder = new MethodFindVisitor(nameToSearch);
		typeSpec.accept(memberfinder);
		IASTNode function = memberfinder.getFoundNode();
		if (function == null) {
			TddHelper.showErrorOnStatusLine("Cannot change visibility here.");
			return;
		}

		ASTRewrite rewrite = collector.rewriterForTranslationUnit(typeSpec.getTranslationUnit());
		rewrite.remove(function, null);

		TddHelper.insertMember(function, typeSpec, rewrite);
	}
	
	class MethodFindVisitor extends ASTVisitor {
		private final NodeContainer container;
		private String nameToSearch;
		{
			shouldVisitNames = true;
		}

		public MethodFindVisitor(String nameToSearch) {
			this.container = new NodeContainer();
			this.nameToSearch = nameToSearch;
		}

		@Override
		public int visit(IASTName name) {
			IBinding binding = name.resolveBinding();
			if (binding instanceof ICPPMember) {
				String bindingname = binding.getName().replaceAll("\\(\\w*\\)", "");
				if (bindingname.equals(nameToSearch)) {
					ICPPASTCompositeTypeSpecifier typeSpec = ToggleNodeHelper
							.getAncestorOfType(name, ICPPASTCompositeTypeSpecifier.class);
					if (typeSpec != null)
					{
						IASTNode function = ToggleNodeHelper
							.getAncestorOfType(name, ICPPASTFunctionDefinition.class);
						if (function == null) {
							function = ToggleNodeHelper.getAncestorOfType(name, IASTSimpleDeclaration.class);
						}
						container.add(function);
						return PROCESS_ABORT;
					}
				}
			}
			return PROCESS_CONTINUE;
		}

		public IASTNode getFoundNode() {
			if (container.size() > 0) {
				return container.getNodesToWrite().get(0);
			}
			return null;
		}
	}
}
