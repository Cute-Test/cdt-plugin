package ch.hsr.ifs.mockator.plugin.refsupport.qf;

import org.eclipse.cdt.codan.core.cxx.model.AbstractIndexAstChecker;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.core.runtime.CoreException;

import ch.hsr.ifs.iltis.cpp.resources.CProjectUtil;

import ch.hsr.ifs.iltis.core.exception.ILTISException;


public abstract class MockatorIndexAstChecker extends AbstractIndexAstChecker {

   @Override
   public void processAst(final IASTTranslationUnit ast) {
      ast.accept(getAstVisitor());
   }

   protected abstract ASTVisitor getAstVisitor();

   protected IIndex getIndex() {
      try {
         return getModelCache().getIndex();
      }
      catch (final CoreException e) {
         throw new ILTISException(e).rethrowUnchecked();
      }
   }

   protected IASTTranslationUnit getAst() {
      try {
         return getModelCache().getAST();
      }
      catch (final CoreException e) {
         throw new ILTISException(e).rethrowUnchecked();
      }
   }

   protected ICProject getCProject() {
      return CProjectUtil.getCProject(getFile());
   }
}
