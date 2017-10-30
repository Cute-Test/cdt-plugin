package ch.hsr.ifs.mockator.plugin.extractinterface.postconditions;

import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.unorderedSet;
import static ch.hsr.ifs.mockator.plugin.base.functional.HigherOrder.filter;
import static ch.hsr.ifs.mockator.plugin.base.functional.HigherOrder.map;

import java.util.Collection;
import java.util.Set;

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

import ch.hsr.ifs.mockator.plugin.base.dbc.Assert;
import ch.hsr.ifs.mockator.plugin.base.functional.F1;
import ch.hsr.ifs.mockator.plugin.base.i18n.I18N;
import ch.hsr.ifs.mockator.plugin.extractinterface.context.ExtractInterfaceContext;
import ch.hsr.ifs.mockator.plugin.refsupport.functions.params.MethodParamEquivalenceTester;
import ch.hsr.ifs.mockator.plugin.refsupport.utils.AstUtil;
import ch.hsr.ifs.mockator.plugin.refsupport.utils.SubClassFinder;


public class ShadowedMemFunVerifier {

   private final ExtractInterfaceContext context;

   public ShadowedMemFunVerifier(ExtractInterfaceContext context) {
      this.context = context;
   }

   public void checkForShadowedFunctions(RefactoringStatus status) throws CoreException {
      Collection<ICPPClassType> subClasses = getSubClassesOfChosenClass();

      for (ICPPMethod chosenMemFun : getChosenNonVirtualMemFuns()) {
         Set<ICPPMethod> shadowingMemFuns = unorderedSet();

         for (ICPPClassType klass : subClasses) {
            for (ICPPMethod memFun : klass.getDeclaredMethods()) {
               if (haveSameSignature(chosenMemFun, memFun)) {
                  shadowingMemFuns.add(memFun);
               }
            }
         }

         for (ICPPMethod shadowing : shadowingMemFuns) {
            status.addWarning(createShadowWarning(chosenMemFun, shadowing));
         }
      }
   }

   private static boolean haveSameSignature(ICPPMethod chosenMemFun, ICPPMethod scMemFun) {
      return chosenMemFun.getName().equals(scMemFun.getName()) && haveSameParameters(chosenMemFun, scMemFun);
   }

   private Collection<ICPPMethod> getChosenNonVirtualMemFuns() {
      return filter(map(context.getChosenMemFuns(), new F1<IASTDeclaration, ICPPMethod>() {

         @Override
         public ICPPMethod apply(IASTDeclaration decl) {
            return getCppMethodIn(decl);
         }
      }), new F1<ICPPMethod, Boolean>() {

         @Override
         public Boolean apply(ICPPMethod memFun) {
            return !memFun.isVirtual();
         }
      });
   }

   private static ICPPMethod getCppMethodIn(IASTDeclaration declaration) {
      ICPPASTFunctionDeclarator funDecl = AstUtil.getChildOfType(declaration, ICPPASTFunctionDeclarator.class);
      IBinding binding = funDecl.getName().resolveBinding();
      Assert.instanceOf(binding, ICPPMethod.class, "Chosen member function is not valid");
      return (ICPPMethod) binding;
   }

   private static String createShadowWarning(ICPPMethod chosenMemFun, ICPPMethod shadowingMemFun) {
      String chosenFunQfName = AstUtil.getQfName(chosenMemFun);
      String shadowFunQfName = AstUtil.getQfName(shadowingMemFun);
      return NLS.bind(I18N.ExtractInterfaceShadowedFunction, chosenFunQfName, shadowFunQfName);
   }

   @SuppressWarnings("restriction")
   private Collection<ICPPClassType> getSubClassesOfChosenClass() throws CoreException {
      IIndex index = context.getCRefContext().getIndex();
      ICPPClassType klass = getChosenClassType();
      return new SubClassFinder(index).getSubClasses(klass);
   }

   private ICPPClassType getChosenClassType() {
      ICPPASTCompositeTypeSpecifier chosenClass = context.getChosenClass();
      IBinding b = chosenClass.getName().resolveBinding();
      Assert.instanceOf(b, ICPPClassType.class, "Not a valid class to extract an interface for");
      return (ICPPClassType) b;
   }

   private static boolean haveSameParameters(ICPPMethod chosenMemFun, ICPPMethod subclassMemFun) {
      return new MethodParamEquivalenceTester(chosenMemFun).hasSameParameters(subclassMemFun);
   }
}
