package ch.hsr.ifs.mockator.plugin.linker;

import java.util.Optional;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import ch.hsr.ifs.iltis.cpp.ast.ASTUtil;

import ch.hsr.ifs.mockator.plugin.refsupport.utils.NotInSameTuAsCalleeVerifier;


public class LinkerFunctionPreconVerifier {

   private final RefactoringStatus   status;
   private final IASTTranslationUnit ast;

   public LinkerFunctionPreconVerifier(final RefactoringStatus status, final IASTTranslationUnit ast) {
      this.status = status;
      this.ast = ast;
   }

   public void assureSatisfiesLinkSeamProperties(final Optional<IASTName> selectedFunName) {
      if (!selectedFunName.isPresent()) {
         status.addFatalError("Selection does not contain a function name");
         return;
      }

      final IBinding candidate = selectedFunName.get().resolveBinding();

      if (!(candidate instanceof ICPPFunction)) {
         status.addFatalError("Selected name does not refer to a valid function");
         return;
      }

      if (isFunctionTemplate(candidate)) {
         status.addFatalError("Function templates are not supported");
         return;
      }

      final ICPPFunction cppFunction = (ICPPFunction) candidate;

      if (cppFunction.isInline()) {
         status.addFatalError("Inline functions are not supported");
         return;
      }

      if (isPartOfFunCall(selectedFunName.get())) {
         assureHasDefinitionNotInSameTu(candidate);
      }
   }

   private static boolean isFunctionTemplate(final IBinding candidate) {
      return candidate instanceof ICPPTemplateInstance || candidate instanceof ICPPFunctionTemplate;
   }

   private void assureHasDefinitionNotInSameTu(final IBinding candidate) {
      final NotInSameTuAsCalleeVerifier verifier = new NotInSameTuAsCalleeVerifier(status, ast);
      verifier.assurehasDefinitionNotInSameTu(candidate);
   }

   private static boolean isPartOfFunCall(final IASTName functionName) {
      return ASTUtil.getAncestorOfType(functionName, ICPPASTFunctionCallExpression.class) != null;
   }
}
