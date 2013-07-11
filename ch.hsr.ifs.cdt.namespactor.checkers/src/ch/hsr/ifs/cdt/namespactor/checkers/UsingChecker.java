/******************************************************************************
* Copyright (c) 2012 Institute for Software, HSR Hochschule fuer Technik 
* Rapperswil, University of applied sciences and others.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html 
*
* Contributors:
* 	Ueli Kunz <kunz@ideadapt.net>, Jules Weder <julesweder@gmail.com> - initial API and implementation
******************************************************************************/
package ch.hsr.ifs.cdt.namespactor.checkers;

import org.eclipse.cdt.codan.core.cxx.model.AbstractIndexAstChecker;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIncludeStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUsingDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUsingDirective;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;

/**
 * @author kunz@ideadapt.net
 * */
@SuppressWarnings("restriction")
public class UsingChecker extends AbstractIndexAstChecker {

	private static final String UDIR_IN_HEADER_PROBLEM_ID      = "ch.hsr.ifs.cdt.namespactor.UDIRInHeader"; //$NON-NLS-1$
	private static final String UDIR_UNQUALIFIED_PROBLEM_ID    = "ch.hsr.ifs.cdt.namespactor.UDIRUnqualified"; //$NON-NLS-1$
	private static final String UDEC_IN_HEADER_PROBLEM_ID      = "ch.hsr.ifs.cdt.namespactor.UDECInHeader"; //$NON-NLS-1$
	private static final String UDIR_BEFORE_INCLUDE_PROBLEM_ID = "ch.hsr.ifs.cdt.namespactor.UDIRBeforeInclude"; //$NON-NLS-1$
	private static final String UDEC_BEFORE_INCLUDE_PROBLEM_ID = "ch.hsr.ifs.cdt.namespactor.UDECBeforeInclude"; //$NON-NLS-1$

	@Override
	public void processAst(IASTTranslationUnit ast) {
		
		if(ast.isHeaderUnit()){
			checkUsingInHeader(ast);
		}
		checkUsingBeforeInclude(ast);
		checkUDIRWithUnqualifiedName(ast);
	}

	private void checkUDIRWithUnqualifiedName(IASTTranslationUnit ast) {

		for(IASTDeclaration decl : ast.getDeclarations()){

			if(decl instanceof ICPPASTUsingDirective){
				
				ICPPASTUsingDirective udir = (ICPPASTUsingDirective) decl;
				IASTName udirName = udir.getQualifiedName();
				String[] qname    = CPPVisitor.getQualifiedName(udirName.resolveBinding());

				if(!(udirName instanceof ICPPASTQualifiedName) && qname.length > 1){
					reportProblem(UDIR_UNQUALIFIED_PROBLEM_ID, decl);
				}
			}
		}	
	}

	private void checkUsingBeforeInclude(IASTTranslationUnit ast) {
		IASTPreprocessorIncludeStatement[] includes = ast.getIncludeDirectives();
		if(includes.length > 0){
			
			int lastIncludeOffset = includes[includes.length - 1].getFileLocation().getNodeOffset();
			
			for(IASTDeclaration decl : ast.getDeclarations()){
				
				if(decl.getFileLocation().getNodeOffset() < lastIncludeOffset){
					
					if(decl instanceof ICPPASTUsingDirective){
						reportProblem(UDIR_BEFORE_INCLUDE_PROBLEM_ID, decl);
						
					}else if(decl instanceof ICPPASTUsingDeclaration){
						reportProblem(UDEC_BEFORE_INCLUDE_PROBLEM_ID, decl);
					}
				}else{
					break;
				}
			}
		}
	}

	private void checkUsingInHeader(final IASTTranslationUnit ast) {
		
		ast.accept(new ASTVisitor() {
			
			{
				shouldVisitDeclarations = true;
			}
			
			@Override
			public int visit(IASTDeclaration decl) {
				
				if(decl.getParent().equals(ast)){
					
					if(decl instanceof ICPPASTUsingDirective){
						reportProblem(UDIR_IN_HEADER_PROBLEM_ID, decl);
					}
				}
				
				if(decl instanceof ICPPASTUsingDeclaration){
					reportProblem(UDEC_IN_HEADER_PROBLEM_ID, decl);
				}
				
				return super.visit(decl);
			}
		});
	}
}
