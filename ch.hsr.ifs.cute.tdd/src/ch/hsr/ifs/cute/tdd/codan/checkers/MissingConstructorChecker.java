/*******************************************************************************
 * Copyright (c) 2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 *  
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *******************************************************************************/
package ch.hsr.ifs.cute.tdd.codan.checkers;

import org.eclipse.cdt.codan.core.cxx.model.AbstractIndexAstChecker;
import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTImplicitName;
import org.eclipse.cdt.core.dom.ast.IASTImplicitNameOwner;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamedTypeSpecifier;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTDeclarationStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTNamedTypeSpecifier;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTSimpleDeclaration;
import org.eclipse.cdt.internal.ui.refactoring.RefactoringASTCache;
import org.eclipse.cdt.internal.ui.refactoring.togglefunction.ToggleNodeHelper;
import org.eclipse.core.runtime.Path;

import ch.hsr.ifs.cute.tdd.CodanArguments;
import ch.hsr.ifs.cute.tdd.TddHelper;

@SuppressWarnings("restriction")
public class MissingConstructorChecker extends AbstractIndexAstChecker {

	public static final String ERR_ID_MissingConstructorResolutionProblem_HSR = "ch.hsr.ifs.cute.tdd.codan.checkers.MissingConstructorResolutionProblem_HSR"; //$NON-NLS-1$

	@Override
	public void processAst(final IASTTranslationUnit ast) {
		RefactoringASTCache astCache = new RefactoringASTCache();
		try {
			final ICProject project = CoreModel.getDefault().create(new Path(ast.getContainingFilename())).getCProject();
			ast.accept(new MissingConstructorVisitor(project, ast, astCache));
		} finally {
			astCache.dispose();
		}
	}

	private final class MissingConstructorVisitor extends ASTVisitor {
		{
			shouldVisitDeclarations = true;
		}

		private MissingConstructorVisitor(ICProject project, IASTTranslationUnit ast, RefactoringASTCache astCache) {
		}

		@Override
		public int visit(IASTDeclaration declaration) {
			if (declaration instanceof CPPASTSimpleDeclaration) {
				CPPASTSimpleDeclaration simpledec = (CPPASTSimpleDeclaration) declaration;
				CPPASTDeclarationStatement declstmt = ToggleNodeHelper.getAncestorOfType(simpledec, CPPASTDeclarationStatement.class);
				ICPPASTNamedTypeSpecifier typespec = TddHelper.getChildofType(simpledec, CPPASTNamedTypeSpecifier.class);
				ICPPASTDeclarator declarator = TddHelper.getChildofType(simpledec, ICPPASTDeclarator.class);
				IASTImplicitNameOwner nameowner = TddHelper.getChildofType(simpledec, IASTImplicitNameOwner.class);
				boolean hasPointerOrRefType = TddHelper.hasPointerOrRefType(declarator);
				
				if (declstmt != null && typespec != null && declarator != null && nameowner != null && !hasPointerOrRefType ) {
					if ((simpledec.getDeclSpecifier() instanceof CPPASTNamedTypeSpecifier)) {
						IBinding typeBinding = typespec.getName().resolveBinding();
						if(typeBinding instanceof IType){
							String typeName = ASTTypeUtil.getType((IType)typeBinding, true);
							if(!(typeName.contains("&") || typeName.contains("*"))){
								reportUnresolvableConstructorCalls(simpledec, typeName);
							}
						}
					}
				}
			}
			return PROCESS_CONTINUE;
		}

		private void reportUnresolvableConstructorCalls(CPPASTSimpleDeclaration simpledec, String typename) {
			for (IASTDeclarator ctorDecl : simpledec.getDeclarators()) {
				if (ctorDecl instanceof IASTImplicitNameOwner) {
					IASTImplicitName[] implicitNames = ((IASTImplicitNameOwner) ctorDecl).getImplicitNames();
					if (implicitNames.length == 0) {
						IASTName reportedNode = ctorDecl.getName();
						String message = Messages.MissingConstructorChecker_1 + typename;
						CodanArguments ca = new CodanArguments(typename, message, ":ctor"); //$NON-NLS-1$
						reportProblem(ERR_ID_MissingConstructorResolutionProblem_HSR, reportedNode, ca.toArray());
					}
				}
			}
		}
	}
}
