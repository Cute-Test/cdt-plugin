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
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTOperatorName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.ui.refactoring.RefactoringASTCache;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;

import ch.hsr.ifs.cute.core.CuteCorePlugin;

/**
 * @author Emanuel Graf IFS
 * @author Thomas Corbat IFS
 */
public class UnregisteredTestChecker extends AbstractIndexAstChecker {

	public void processAst(IASTTranslationUnit ast) {
		final TestFunctionFinderVisitor testFunctionFinder = new TestFunctionFinderVisitor();
		ast.accept(testFunctionFinder);
		markUnregisteredFunctions(testFunctionFinder.getTestFunctions());
	}

	private void markUnregisteredFunctions(List<IASTDeclaration> testFunctions) {
		RefactoringASTCache astCache = new RefactoringASTCache();
		try {
			final IIndex index = assembleIndex();
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
			astCache.dispose();
		}
	}

	private void markFunctionIfUnregistered(RefactoringASTCache astCache, final IIndex index, final RegisteredTestFunctionFinderVisitor registeredFunctionFinder,
			IASTDeclaration iastDeclaration) {
		final IBinding toBeRegisteredBinding = getToBeRegisteredBinding(iastDeclaration);

		try {
			updateRegisteredTests(astCache, index, registeredFunctionFinder, toBeRegisteredBinding);

			if (!(registeredFunctionFinder.getRegisteredFunctionNames().contains(index.adaptBinding(toBeRegisteredBinding)))) {
				reportProblem("ch.hsr.ifs.cute.unregisteredTestMarker", iastDeclaration); //$NON-NLS-1$
			}
		} catch (CoreException e) {
			CuteCorePlugin.log(e);
		}
	}

	private void updateRegisteredTests(RefactoringASTCache astCache, final IIndex index, final RegisteredTestFunctionFinderVisitor registeredFunctionFinder,
			final IBinding toBeRegisteredBinding) throws CoreException {
		if (toBeRegisteredBinding instanceof ICPPClassType) {
			final ICPPConstructor[] constructors = ((ICPPClassType) toBeRegisteredBinding).getConstructors();
			for (ICPPConstructor constructor : constructors) {
				final IIndexName[] constructorReferences = index.findReferences(constructor);
				updateRegisteredTestsOfReferencedTUs(registeredFunctionFinder, constructorReferences, astCache);
			}
		} else {
			final IIndexName[] references = index.findReferences(toBeRegisteredBinding);
			updateRegisteredTestsOfReferencedTUs(registeredFunctionFinder, references, astCache);
		}
	}

	private IIndex assembleIndex() throws CoreException {
		ArrayList<ICProject> projects = new ArrayList<ICProject>();

		ICProject cproject = CoreModel.getDefault().create(getProject());
		projects.add(cproject);
		IProject[] referencedProjects = getProject().getReferencedProjects();
		for (IProject refProject : referencedProjects) {
			final ICProject refCProject = CoreModel.getDefault().create(refProject);
			projects.add(refCProject);
		}

		final ICProject[] projectArray = new ICProject[projects.size()];
		final IIndex index = CCorePlugin.getIndexManager().getIndex(projects.toArray(projectArray));
		return index;
	}

	private void updateRegisteredTestsOfReferencedTUs(RegisteredTestFunctionFinderVisitor registeredFunctionFinder, IIndexName[] references, RefactoringASTCache astCache)
			throws CoreException {
		for (IIndexName testReference : references) {
			final String file = testReference.getFileLocation().getFileName();
			final Path path = new Path(file);
			final ICElement celement = CoreModel.getDefault().create(path);
			final ITranslationUnit tu = (ITranslationUnit) celement.getAdapter(ITranslationUnit.class);

			if (tu != null) {
				IASTTranslationUnit ast;
				if (getModelCache().getTranslationUnit().getResource().equals(tu.getResource())) {
					ast = getModelCache().getAST();
				} else {
					ast = astCache.getAST(tu, new NullProgressMonitor());
				}
				ast.accept(registeredFunctionFinder);
			}
		}
	}

	private IBinding getToBeRegisteredBinding(IASTDeclaration iastDeclaration) {
		if (iastDeclaration instanceof IASTFunctionDefinition) {
			IASTFunctionDefinition funcDef = (IASTFunctionDefinition) iastDeclaration;
			if (isFunctor(funcDef)) {
				IASTNode n = funcDef;
				n = findSurroundingTypeSpecifier(n);
				if (n != null) {
					ICPPASTCompositeTypeSpecifier compType = (ICPPASTCompositeTypeSpecifier) n;
					return compType.getName().resolveBinding();
				}
			} else {
				final IASTFunctionDeclarator declarator = funcDef.getDeclarator();
				if (declarator != null) {
					final IASTName funcName = declarator.getName();
					return funcName.resolveBinding();
				}
			}
		}
		return null;
	}

	private IASTNode findSurroundingTypeSpecifier(IASTNode n) {
		while (n != null && !(n instanceof ICPPASTCompositeTypeSpecifier)) {
			n = n.getParent();
		}
		return n;
	}

	protected boolean isFunctor(IASTFunctionDefinition funcDef) {
		return funcDef.getDeclarator().getName() instanceof ICPPASTOperatorName;
	}

}
