package ch.hsr.ifs.mockator.plugin.refsupport.utils;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;


public class NotInSameTuAsCalleeVerifier {

   private final RefactoringStatus   status;
   private final IASTTranslationUnit ast;

   public NotInSameTuAsCalleeVerifier(RefactoringStatus status, IASTTranslationUnit ast) {
      this.status = status;
      this.ast = ast;
   }

   public void assurehasDefinitionNotInSameTu(IBinding candidate) {
      if (hasDefinitionInCurrentTu(candidate)) {
         status.addFatalError("Definition must not be in the same " + "translation unit as its calling origin");
      }
   }

   private boolean hasDefinitionInCurrentTu(IBinding candidate) {
      IASTName[] definitions = ast.getDefinitionsInAST(candidate);
      return definitions.length > 0;
   }
}
