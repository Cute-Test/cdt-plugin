/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 *
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.cute.ui.sourceactions;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarationStatement;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamedTypeSpecifier;

import ch.hsr.ifs.cute.ui.checkers.RegisteredTestFunctionFinderVisitor;


public class SuitePushBackFinder extends ASTVisitor {

   private IASTName suiteDeclName = null;
   private IASTNode suiteLocation = null;

   {
      shouldVisitStatements = true;
   }

   //save the name of the last AST element containing "cute::suite"
   //unable to handle multi-suite in a file
   @Override
   public int leave(IASTStatement statement) {
      if (statement instanceof IASTDeclarationStatement) {
         IASTDeclarationStatement declStmt = (IASTDeclarationStatement) statement;
         IASTDeclaration decl = declStmt.getDeclaration();
         if (decl instanceof IASTSimpleDeclaration) {
            IASTSimpleDeclaration sDecl = (IASTSimpleDeclaration) decl;
            IASTDeclSpecifier declSpec = sDecl.getDeclSpecifier();
            if (declSpec instanceof ICPPASTNamedTypeSpecifier) {
               ICPPASTNamedTypeSpecifier nDeclSpec = (ICPPASTNamedTypeSpecifier) declSpec;
               if (RegisteredTestFunctionFinderVisitor.isCuteSuite(nDeclSpec)) { // PS: Hack, should check if suite is in namespace cute!
                  suiteDeclName = sDecl.getDeclarators()[0].getName();
                  suiteLocation = nDeclSpec;
               }

            }
         }
      }
      return super.leave(statement);
   }

   public IASTName getSuiteDeclName() {
      return suiteDeclName;
   }

   public IASTNode getSuiteNode() {
      return suiteLocation;
   }
}
