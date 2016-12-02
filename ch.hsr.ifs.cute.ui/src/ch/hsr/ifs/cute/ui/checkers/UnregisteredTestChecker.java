/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 * 
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.cute.ui.checkers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.codan.core.cxx.model.AbstractIndexAstChecker;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ClassTypeHelper;
import org.eclipse.cdt.internal.core.model.ASTCache;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;

import ch.hsr.ifs.cute.core.CuteCorePlugin;
import ch.hsr.ifs.cute.ui.ASTUtil;
import ch.hsr.ifs.cute.ui.FunctorFinderVisitor;
import ch.hsr.ifs.cute.ui.IndirectAssertStatementCheckVisitor;
import ch.hsr.ifs.cute.ui.sourceactions.ASTHelper;

/**
 * @author Emanuel Graf IFS
 * @author Thomas Corbat IFS
 */
public class UnregisteredTestChecker extends AbstractIndexAstChecker {

	public void processAst(IASTTranslationUnit ast) {
		final FunctorFinderVisitor functorFinderVisitor = new FunctorFinderVisitor();
		ast.accept(functorFinderVisitor);
		if(functorFinderVisitor.getFunctor() == null) {
			final TestFunctionFinderVisitor testFunctionFinder = new TestFunctionFinderVisitor();
			ast.accept(testFunctionFinder);
			markUnregisteredFunctions(testFunctionFinder.getTestFunctions());
		} else {
			markUnregisteredFunctor(functorFinderVisitor.getFunctor(), ast.getDeclarations());
		}
	}

	private void markUnregisteredFunctions(List<IASTDeclaration> testFunctions) {
		ASTCache astCache = new ASTCache();
		try {
			final IIndex index = assembleIndex(getProject());
			try {
				index.acquireReadLock();
				final RegisteredTestFunctionFinderVisitor registeredFunctionFinder = new RegisteredTestFunctionFinderVisitor(index);
				for (IASTDeclaration iastDeclaration : testFunctions) {
					markFunctionIfUnregistered(astCache, index, registeredFunctionFinder, iastDeclaration);
				}
			} catch (InterruptedException e) {
				CuteCorePlugin.log(e);
			} finally {
				index.releaseReadLock();
			}
		} catch (CoreException e) {
			CuteCorePlugin.log(e);
		} finally {
			astCache.disposeAST();
		}
	}

	private void markFunctionIfUnregistered(ASTCache astCache, final IIndex index, final RegisteredTestFunctionFinderVisitor registeredFunctionFinder,
			IASTDeclaration iastDeclaration) {
		final IBinding toBeRegisteredBinding = getToBeRegisteredBinding(iastDeclaration);

		try {
			updateRegisteredTests(astCache, index, registeredFunctionFinder, toBeRegisteredBinding, iastDeclaration);
			if (iastDeclaration instanceof IASTFunctionDefinition) {
				IASTFunctionDefinition iastFunctionDefinition = (IASTFunctionDefinition) iastDeclaration;
				if (!(registeredFunctionFinder.getRegisteredFunctionNames().contains(index.adaptBinding(toBeRegisteredBinding)))) {
					reportProblem("ch.hsr.ifs.cute.unregisteredTestMarker", iastFunctionDefinition.getDeclarator());
				}
			}
		} catch (CoreException e) {
			CuteCorePlugin.log(e);
		}
	}

	private void updateRegisteredTests(ASTCache astCache, final IIndex index, final RegisteredTestFunctionFinderVisitor registeredFunctionFinder,
			final IBinding toBeRegisteredBinding, IASTDeclaration point) throws CoreException {
		if (toBeRegisteredBinding instanceof ICPPClassType) {
			final ICPPConstructor[] constructors = ClassTypeHelper.getConstructors((ICPPClassType) toBeRegisteredBinding, point);
			for (ICPPConstructor constructor : constructors) {
				final IIndexName[] constructorReferences = index.findReferences(constructor); // Here we don't get any references, only if default constructor is called
				updateRegisteredTestsOfReferencedTUs(registeredFunctionFinder, constructorReferences, astCache, index);
			}
		} else {
			final IIndexName[] references = index.findReferences(toBeRegisteredBinding);
			updateRegisteredTestsOfReferencedTUs(registeredFunctionFinder, references, astCache, index);
		}
	}

	private IIndex assembleIndex(IProject project) throws CoreException {
		ArrayList<ICProject> projects = assembleProjects(project);

		final ICProject[] projectArray = new ICProject[projects.size()];
		final IIndex index = CCorePlugin.getIndexManager().getIndex(projects.toArray(projectArray));
		return index;
	}

	private static ArrayList<ICProject> assembleProjects(IProject project) throws CoreException {
		ArrayList<ICProject> projects = new ArrayList<ICProject>();

		ICProject cproject = CoreModel.getDefault().create(project);
		projects.add(cproject);
		IProject[] referencedProjects = project.getReferencingProjects();
		for (IProject refProject : referencedProjects) {
			final ICProject refCProject = CoreModel.getDefault().create(refProject);
			projects.add(refCProject);
		}
		return projects;
	}

	private void updateRegisteredTestsOfReferencedTUs(RegisteredTestFunctionFinderVisitor registeredFunctionFinder, IIndexName[] references, ASTCache astCache,
			IIndex index) throws CoreException {
		for (IIndexName testReference : references) {
			final ITranslationUnit tu = ASTHelper.getTranslationUnitFromIndexName(testReference);
			if (tu != null) {
				IASTTranslationUnit ast;
				if (getModelCache().getTranslationUnit().getResource().equals(tu.getResource())) {
					ast = getModelCache().getAST();
				} else {
					ast = astCache.acquireSharedAST(tu, index, true, new NullProgressMonitor());
				}
				ast.accept(registeredFunctionFinder);
			}
		}
	}

	private IBinding getToBeRegisteredBinding(IASTDeclaration iastDeclaration) {
		if (iastDeclaration instanceof IASTFunctionDefinition) {
			IASTFunctionDefinition funcDef = (IASTFunctionDefinition) iastDeclaration;
			IASTFunctionDeclarator declarator = funcDef.getDeclarator();
			IASTName functionName = declarator.getName();
			IBinding functionBinding = functionName.resolveBinding();
			if (declarator != null) {
				if (ASTHelper.isFunctor(funcDef.getDeclarator())) {
					if (functionBinding instanceof ICPPMethod) {
						ICPPMethod memberFunctionBinding = (ICPPMethod)functionBinding;
						return memberFunctionBinding.getClassOwner();
					}
				}
				return functionBinding;
			}
		}
		return null;
	}
	
	private void markUnregisteredFunctor(IASTDeclaration functor, IASTDeclaration[] declarations) {
		if (functor instanceof IASTFunctionDefinition) {
			IASTFunctionDefinition functionDefinition = (IASTFunctionDefinition)functor;
			if(ASTUtil.containsAssert(functionDefinition)) {
				List<IASTDeclaration> functorList = new ArrayList<IASTDeclaration>();
				functorList.add(functor);
				markUnregisteredFunctions(functorList);
			} else {
				final IndirectAssertStatementCheckVisitor indirectAssertStatementCheckVisitor = new IndirectAssertStatementCheckVisitor();
				functor.accept(indirectAssertStatementCheckVisitor);
				if(indirectAssertStatementCheckVisitor.hasIndirectAssertStatement()) {
					List<IASTDeclaration> functorList = new ArrayList<IASTDeclaration>();
					functorList.add(functor);
					markUnregisteredFunctions(functorList);
				}
			}
		}
	}

}
