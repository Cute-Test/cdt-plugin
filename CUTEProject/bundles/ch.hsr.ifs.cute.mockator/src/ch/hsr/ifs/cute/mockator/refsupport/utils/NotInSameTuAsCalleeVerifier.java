package ch.hsr.ifs.cute.mockator.refsupport.utils;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;


public class NotInSameTuAsCalleeVerifier {

   private final RefactoringStatus   status;
   private final IASTTranslationUnit ast;

   public NotInSameTuAsCalleeVerifier(final RefactoringStatus status, final IASTTranslationUnit ast) {
      this.status = status;
      this.ast = ast;
   }

   public void assurehasDefinitionNotInSameTu(final IBinding candidate) {
      if (hasDefinitionInCurrentTu(candidate)) {
         status.addFatalError("Definition must not be in the same " + "translation unit as its calling origin");
      }
   }

   private boolean hasDefinitionInCurrentTu(final IBinding candidate) {
      final IASTName[] definitions = ast.getDefinitionsInAST(candidate);
      return definitions.length > 0;
   }
}
