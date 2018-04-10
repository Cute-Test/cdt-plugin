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
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTStatement;


public class NodeAtCaretFinder extends ASTVisitor {

   int              selOffset;
   private IASTNode matchingNode;

   {
      shouldVisitDeclarations = true;
      shouldVisitStatements = true;
   }

   public NodeAtCaretFinder(int offset) {
      selOffset = offset;
   }

   @Override
   public int leave(IASTDeclaration declaration) {
      if (containsOffset(declaration)) {
         this.matchingNode = declaration;
         return PROCESS_ABORT;
      }
      return super.leave(declaration);
   }

   @Override
   public int leave(IASTStatement statement) {
      if (containsOffset(statement)) {
         this.matchingNode = statement;
         return PROCESS_ABORT;
      }
      return super.leave(statement);
   }

   private boolean containsOffset(IASTNode node) {
      IASTFileLocation location = node.getFileLocation();
      if (location == null) { return false; }
      int nodeOffset = location.getNodeOffset();
      int nodeLength = location.getNodeLength();
      return selOffset >= nodeOffset && selOffset <= (nodeOffset + nodeLength);
   }

   public IASTNode getMatchingNode() {
      return matchingNode;
   }

}
