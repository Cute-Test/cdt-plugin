package ch.hsr.ifs.cute.mockator.extractinterface.postconditions;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.osgi.util.NLS;

import ch.hsr.ifs.iltis.core.core.exception.ILTISException;
import ch.hsr.ifs.iltis.cpp.core.ast.ASTUtil;
import ch.hsr.ifs.iltis.cpp.core.wrappers.CPPVisitor;

import ch.hsr.ifs.cute.mockator.base.i18n.I18N;
import ch.hsr.ifs.cute.mockator.extractinterface.context.ExtractInterfaceContext;
import ch.hsr.ifs.cute.mockator.refsupport.functions.params.MethodParamEquivalenceTester;
import ch.hsr.ifs.cute.mockator.refsupport.utils.SubClassFinder;


public class ShadowedMemFunVerifier {

   private final ExtractInterfaceContext context;

   public ShadowedMemFunVerifier(final ExtractInterfaceContext context) {
      this.context = context;
   }

   public void checkForShadowedFunctions(final RefactoringStatus status) throws CoreException {
      final Collection<ICPPClassType> subClasses = getSubClassesOfChosenClass();

      for (final ICPPMethod chosenMemFun : getChosenNonVirtualMemFuns()) {
         final Set<ICPPMethod> shadowingMemFuns = new HashSet<>();

         for (final ICPPClassType clazz : subClasses) {
            for (final ICPPMethod memFun : clazz.getDeclaredMethods()) {
               if (haveSameSignature(chosenMemFun, memFun)) {
                  shadowingMemFuns.add(memFun);
               }
            }
         }

         for (final ICPPMethod shadowing : shadowingMemFuns) {
            status.addWarning(createShadowWarning(chosenMemFun, shadowing));
         }
      }
   }

   private static boolean haveSameSignature(final ICPPMethod chosenMemFun, final ICPPMethod scMemFun) {
      return chosenMemFun.getName().equals(scMemFun.getName()) && haveSameParameters(chosenMemFun, scMemFun);
   }

   private Collection<ICPPMethod> getChosenNonVirtualMemFuns() {
      return context.getChosenMemFuns().stream().map((final IASTDeclaration decl) -> getCppMethodIn(decl)).collect(Collectors.toList()).stream()
            .filter((final ICPPMethod memFun) -> !memFun.isVirtual()).collect(Collectors.toList());
   }

   private static ICPPMethod getCppMethodIn(final IASTDeclaration declaration) {
      final ICPPASTFunctionDeclarator funDecl = CPPVisitor.findChildWithType(declaration, ICPPASTFunctionDeclarator.class).orElse(null);
      final IBinding binding = funDecl.getName().resolveBinding();
      ILTISException.Unless.assignableFrom("Chosen member function is not valid", ICPPMethod.class, binding);
      return (ICPPMethod) binding;
   }

   private static String createShadowWarning(final ICPPMethod chosenMemFun, final ICPPMethod shadowingMemFun) {
      final String chosenFunQfName = ASTUtil.getQfName(chosenMemFun);
      final String shadowFunQfName = ASTUtil.getQfName(shadowingMemFun);
      return NLS.bind(I18N.ExtractInterfaceShadowedFunction, chosenFunQfName, shadowFunQfName);
   }

   private Collection<ICPPClassType> getSubClassesOfChosenClass() throws CoreException {
      final IIndex index = context.getCRefContext().getIndex();
      final ICPPClassType clazz = getChosenClassType();
      return new SubClassFinder(index).getSubClasses(clazz);
   }

   private ICPPClassType getChosenClassType() {
      final ICPPASTCompositeTypeSpecifier chosenClass = context.getChosenClass();
      final IBinding b = chosenClass.getName().resolveBinding();
      ILTISException.Unless.assignableFrom("Not a valid class to extract an interface for", ICPPClassType.class, b);
      return (ICPPClassType) b;
   }

   private static boolean haveSameParameters(final ICPPMethod chosenMemFun, final ICPPMethod subclassMemFun) {
      return new MethodParamEquivalenceTester(chosenMemFun).hasSameParameters(subclassMemFun);
   }
}
