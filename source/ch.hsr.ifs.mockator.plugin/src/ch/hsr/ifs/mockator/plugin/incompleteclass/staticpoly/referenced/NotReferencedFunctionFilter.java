package ch.hsr.ifs.mockator.plugin.incompleteclass.staticpoly.referenced;

import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.unorderedMap;

import java.util.Map;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.ICProject;

import ch.hsr.ifs.mockator.plugin.base.functional.F1;
import ch.hsr.ifs.mockator.plugin.incompleteclass.StaticPolyMissingMemFun;
import ch.hsr.ifs.mockator.plugin.refsupport.functions.FunctionEquivalenceVerifier;
import ch.hsr.ifs.mockator.plugin.refsupport.utils.AstUtil;


public class NotReferencedFunctionFilter implements F1<StaticPolyMissingMemFun, Boolean> {

   private final Map<ICPPASTFunctionDefinition, Boolean> cache;
   private final FunctionCalleeReferenceResolver         calleeReferenceResolver;
   private final ICPPASTFunctionDefinition               testFunction;

   public NotReferencedFunctionFilter(IIndex index, ICProject cProject, ICPPASTFunctionDefinition testFunction) {
      this.testFunction = testFunction;
      calleeReferenceResolver = new FunctionCalleeReferenceResolver(index, cProject);
      cache = unorderedMap();
   }

   @Override
   public Boolean apply(StaticPolyMissingMemFun memFunCall) {
      ICPPASTFunctionDefinition sutFunction = memFunCall.getContainingFunction();

      if (!shouldConsider(sutFunction)) return true;

      Boolean called = cache.get(sutFunction);

      if (called == null) {
         called = isCalled(sutFunction);
         cache.put(sutFunction, called);
      }

      return called;
   }

   private static boolean shouldConsider(ICPPASTFunctionDefinition sutFunction) {
      return sutFunction != null && !AstUtil.isConstructor(sutFunction);
   }

   private boolean isCalled(ICPPASTFunctionDefinition sutFunction) {
      IBinding sutBinding = sutFunction.getDeclarator().getName().resolveBinding();

      for (IASTName caller : calleeReferenceResolver.findCallers(sutBinding, sutFunction)) {
         if (matches(testFunction, getFunctionDefinition(caller))) return true;
      }

      return false;
   }

   private ICPPASTFunctionDefinition getFunctionDefinition(IASTName caller) {
      return AstUtil.getAncestorOfType(caller, ICPPASTFunctionDefinition.class);
   }

   private static boolean matches(ICPPASTFunctionDefinition functionInUse, ICPPASTFunctionDefinition missingMemFun) {
      if (functionInUse == null || missingMemFun == null) return false;

      FunctionEquivalenceVerifier checker = new FunctionEquivalenceVerifier((ICPPASTFunctionDeclarator) functionInUse.getDeclarator());
      return checker.isEquivalent((ICPPASTFunctionDeclarator) missingMemFun.getDeclarator());
   }
}
