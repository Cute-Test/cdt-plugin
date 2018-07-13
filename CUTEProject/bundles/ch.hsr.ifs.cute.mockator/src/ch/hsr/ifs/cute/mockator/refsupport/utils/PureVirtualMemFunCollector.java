package ch.hsr.ifs.cute.mockator.refsupport.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.parser.util.ObjectSet;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ClassTypeHelper;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil;


// Taken and adapted from CDT 8.2
@SuppressWarnings("restriction")
public class PureVirtualMemFunCollector {

   private final ICPPClassType clazz;

   public PureVirtualMemFunCollector(final ICPPClassType clazz) {
      this.clazz = clazz;
   }

   public ICPPMethod[] collectPureVirtualMemFuns() {
      final Map<String, List<ICPPMethod>> result = getPureVirtualMemFuns(clazz, new HashMap<ICPPClassType, Map<String, List<ICPPMethod>>>());

      int resultArraySize = 0;
      for (final List<ICPPMethod> methods : result.values()) {
         resultArraySize += methods.size();
      }

      final ICPPMethod[] resultArray = new ICPPMethod[resultArraySize];
      int resultArrayIdx = 0;

      for (final List<ICPPMethod> methods : result.values()) {
         for (final ICPPMethod method : methods) {
            resultArray[resultArrayIdx++] = method;
         }
      }
      return resultArray;
   }

   private Map<String, List<ICPPMethod>> getPureVirtualMemFuns(final ICPPClassType classType,
         final Map<ICPPClassType, Map<String, List<ICPPMethod>>> cache) {
      Map<String, List<ICPPMethod>> result = cache.get(classType);
      if (result != null) { return result; }

      result = new HashMap<>();
      cache.put(classType, result);

      // Look at the pure virtual methods of the base classes
      final Set<IBinding> handledBaseClasses = new HashSet<>();
      for (final ICPPClassType baseClass : ClassTypeHelper.getAllBases(classType)) {

         if (baseClass instanceof ICPPClassType && handledBaseClasses.add(baseClass)) {
            final Map<String, List<ICPPMethod>> pureVirtuals = getPureVirtualMemFuns(baseClass, cache);
            // Merge derived pure virtual methods
            for (final String key : pureVirtuals.keySet()) {
               List<ICPPMethod> list = result.get(key);
               if (list == null) {
                  list = new ArrayList<>();
                  result.put(key, list);
               }
               list.addAll(pureVirtuals.get(key));
            }
         }
      }

      // Remove overridden pure-virtual methods and add in new pure virtuals.
      final ObjectSet<ICPPMethod> methods = ClassTypeHelper.getOwnMethods(classType);
      for (final ICPPMethod method : methods) {
         final String key = getMethodNameForOverrideKey(method);
         List<ICPPMethod> list = result.get(key);

         if (list != null) {
            final ICPPFunctionType methodType = method.getType();

            for (final Iterator<ICPPMethod> it = list.iterator(); it.hasNext();) {
               final ICPPMethod pureVirtual = it.next();
               if (functionTypesAllowOverride(methodType, pureVirtual.getType())) {
                  it.remove();
               }
            }
         }
         if (method.isPureVirtual()) {
            if (list == null) {
               list = new ArrayList<>();
               result.put(key, list);
            }
            list.add(method);
         } else if (list != null && list.isEmpty()) {
            result.remove(key);
         }
      }
      return result;
   }

   private static boolean functionTypesAllowOverride(final ICPPFunctionType a, final ICPPFunctionType b) {
      if (a.isConst() != b.isConst() || a.isVolatile() != b.isVolatile() || a.takesVarArgs() != b.takesVarArgs()) { return false; }

      final IType[] paramsA = a.getParameterTypes();
      final IType[] paramsB = b.getParameterTypes();

      if (paramsA.length == 1 && paramsB.length == 0) {
         if (!SemanticUtil.isVoidType(paramsA[0])) { return false; }
      } else if (paramsB.length == 1 && paramsA.length == 0) {
         if (!SemanticUtil.isVoidType(paramsB[0])) { return false; }
      } else if (paramsA.length != paramsB.length) {
         return false;
      } else {
         for (int i = 0; i < paramsA.length; i++) {
            if (paramsA[i] == null || !paramsA[i].isSameType(paramsB[i])) { return false; }
         }
      }
      return true;
   }

   private static String getMethodNameForOverrideKey(final ICPPMethod method) {
      if (method.isDestructor()) {
         // Destructor's names may differ but they will override each other.
         return "~";
      } else {
         return method.getName();
      }
   }
}
