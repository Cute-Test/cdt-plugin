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

/**
 * @author Emanuel Graf IFS
 * 
 */
public class UnregisteredTestChecker extends AbstractIndexAstChecker {

	public void processAst(IASTTranslationUnit ast) {
		final TestFunctionFinderVisitor testFunctionFinder = new TestFunctionFinderVisitor();
		ast.accept(testFunctionFinder);

		//		ast.accept(registeredFunctionFinder);
		markUnregisteredFunctions(testFunctionFinder.getTestFunctions());
	}

	private void markUnregisteredFunctions(List<IASTDeclaration> testFunctions) {
		RefactoringASTCache astCache = new RefactoringASTCache();
		try {
			ArrayList<ICProject> projects = new ArrayList<ICProject>();

			ICProject cproject = CoreModel.getDefault().create(getProject());
			projects.add(cproject);
			IProject[] referencedProjects = getProject().getReferencedProjects();
			for (IProject refProject : referencedProjects) {
				projects.add(CoreModel.getDefault().create(refProject));
			}

			final IIndex index = CCorePlugin.getIndexManager().getIndex(projects.toArray(new ICProject[projects.size()]));
			//			final IIndex index = astCache.getIndex();
			final RegisteredTestFunctionFinderVisitor registeredFunctionFinder = new RegisteredTestFunctionFinderVisitor(index);

			for (IASTDeclaration iastDeclaration : testFunctions) {
				final IBinding toBeRegisteredBinding = getToBeRegisteredBinding(iastDeclaration);

				try {
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
					if (!(registeredFunctionFinder.getRegisteredFunctionNames().contains(index.adaptBinding(toBeRegisteredBinding)))) {
						reportProblem("ch.hsr.ifs.cute.unregisteredTestMarker", iastDeclaration); //$NON-NLS-1$
					}
				} catch (CoreException e) {
					e.printStackTrace();
				}

			}
		} catch (CoreException e) {
			e.printStackTrace();
		} finally {
			astCache.dispose();
		}
	}

	private void updateRegisteredTestsOfReferencedTUs(RegisteredTestFunctionFinderVisitor registeredFunctionFinder, IIndexName[] references, RefactoringASTCache astCache)
			throws CoreException {
		for (IIndexName testReference : references) {
			final String file = testReference.getFileLocation().getFileName();
			final Path path = new Path(file);
			final ICElement celement = CoreModel.getDefault().create(path);
			final ITranslationUnit tu = (ITranslationUnit) celement.getAdapter(ITranslationUnit.class);

			if (tu != null) {

				final IASTTranslationUnit ast = astCache.getAST(tu, new NullProgressMonitor());
				ast.accept(registeredFunctionFinder);
			}
		}
	}

	private IBinding getToBeRegisteredBinding(IASTDeclaration iastDeclaration) {
		if (iastDeclaration instanceof IASTFunctionDefinition) {
			IASTFunctionDefinition funcDef = (IASTFunctionDefinition) iastDeclaration;
			if (isFunctor(funcDef)) {
				IASTNode n = funcDef;
				while (n != null && !(n instanceof ICPPASTCompositeTypeSpecifier)) {
					n = n.getParent();
				}
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

	protected boolean isFunctor(IASTFunctionDefinition funcDef) {
		return funcDef.getDeclarator().getName() instanceof ICPPASTOperatorName;
	}

}
