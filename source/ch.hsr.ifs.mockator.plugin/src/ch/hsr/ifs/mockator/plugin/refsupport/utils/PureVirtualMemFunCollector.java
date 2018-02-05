package ch.hsr.ifs.mockator.plugin.refsupport.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBase;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.parser.util.ObjectSet;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ClassTypeHelper;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil;

// Taken and adapted from CDT 8.2
@SuppressWarnings("restriction")
public class PureVirtualMemFunCollector {
  private final ICPPClassType klass;

  public PureVirtualMemFunCollector(ICPPClassType klass) {
    this.klass = klass;
  }

  public ICPPMethod[] collectPureVirtualMemFuns() {
    Map<String, List<ICPPMethod>> result =
        getPureVirtualMemFuns(klass, new HashMap<ICPPClassType, Map<String, List<ICPPMethod>>>());

    int resultArraySize = 0;
    for (List<ICPPMethod> methods : result.values()) {
      resultArraySize += methods.size();
    }

    ICPPMethod[] resultArray = new ICPPMethod[resultArraySize];
    int resultArrayIdx = 0;

    for (List<ICPPMethod> methods : result.values()) {
      for (ICPPMethod method : methods) {
        resultArray[resultArrayIdx++] = method;
      }
    }
    return resultArray;
  }

  private Map<String, List<ICPPMethod>> getPureVirtualMemFuns(ICPPClassType classType,
      Map<ICPPClassType, Map<String, List<ICPPMethod>>> cache) {
    Map<String, List<ICPPMethod>> result = cache.get(classType);
    if (result != null)
      return result;

    result = new HashMap<String, List<ICPPMethod>>();
    cache.put(classType, result);

    // Look at the pure virtual methods of the base classes
    Set<IBinding> handledBaseClasses = new HashSet<IBinding>();
    for (ICPPClassType baseClass : ClassTypeHelper.getAllBases(classType)) {

      if (baseClass instanceof ICPPClassType && handledBaseClasses.add(baseClass)) {
        Map<String, List<ICPPMethod>> pureVirtuals =
            getPureVirtualMemFuns((ICPPClassType) baseClass, cache);
        // Merge derived pure virtual methods
        for (String key : pureVirtuals.keySet()) {
          List<ICPPMethod> list = result.get(key);
          if (list == null) {
            list = new ArrayList<ICPPMethod>();
            result.put(key, list);
          }
          list.addAll(pureVirtuals.get(key));
        }
      }
    }

    // Remove overridden pure-virtual methods and add in new pure virtuals.
    final ObjectSet<ICPPMethod> methods = ClassTypeHelper.getOwnMethods(classType);
    for (ICPPMethod method : methods) {
      String key = getMethodNameForOverrideKey(method);
      List<ICPPMethod> list = result.get(key);

      if (list != null) {
        final ICPPFunctionType methodType = method.getType();

        for (Iterator<ICPPMethod> it = list.iterator(); it.hasNext();) {
          ICPPMethod pureVirtual = it.next();
          if (functionTypesAllowOverride(methodType, pureVirtual.getType())) {
            it.remove();
          }
        }
      }
      if (method.isPureVirtual()) {
        if (list == null) {
          list = new ArrayList<ICPPMethod>();
          result.put(key, list);
        }
        list.add(method);
      } else if (list != null && list.isEmpty()) {
        result.remove(key);
      }
    }
    return result;
  }

  private static boolean functionTypesAllowOverride(ICPPFunctionType a, ICPPFunctionType b) {
    if (a.isConst() != b.isConst() || a.isVolatile() != b.isVolatile()
        || a.takesVarArgs() != b.takesVarArgs())
      return false;

    IType[] paramsA = a.getParameterTypes();
    IType[] paramsB = b.getParameterTypes();

    if (paramsA.length == 1 && paramsB.length == 0) {
      if (!SemanticUtil.isVoidType(paramsA[0]))
        return false;
    } else if (paramsB.length == 1 && paramsA.length == 0) {
      if (!SemanticUtil.isVoidType(paramsB[0]))
        return false;
    } else if (paramsA.length != paramsB.length)
      return false;
    else {
      for (int i = 0; i < paramsA.length; i++) {
        if (paramsA[i] == null || !paramsA[i].isSameType(paramsB[i]))
          return false;
      }
    }
    return true;
  }

  private static String getMethodNameForOverrideKey(ICPPMethod method) {
    if (method.isDestructor())
      // Destructor's names may differ but they will override each other.
      return "~";
    else
      return method.getName();
  }
}
