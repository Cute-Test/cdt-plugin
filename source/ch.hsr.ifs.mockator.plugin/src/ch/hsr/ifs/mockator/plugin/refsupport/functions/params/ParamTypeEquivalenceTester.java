package ch.hsr.ifs.mockator.plugin.refsupport.functions.params;

import static ch.hsr.ifs.mockator.plugin.MockatorConstants.BASIC_STRING_CHAR;
import static ch.hsr.ifs.mockator.plugin.MockatorConstants.STD_STRING;

import java.util.Collection;

import org.eclipse.cdt.codan.core.cxx.CxxAstUtils;
import org.eclipse.cdt.core.dom.ast.IArrayType;
import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.IQualifierType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPDeferredClassInstance;

import ch.hsr.ifs.mockator.plugin.base.collections.ParallelIterator;
import ch.hsr.ifs.mockator.plugin.base.data.Pair;
import ch.hsr.ifs.mockator.plugin.base.functional.F2;
import ch.hsr.ifs.mockator.plugin.refsupport.utils.AstUtil;

@SuppressWarnings("restriction")
public class ParamTypeEquivalenceTester {

  private final Collection<IType> caller;
  private final Collection<IType> receiver;
  private final F2<Integer, IType, Boolean> filter;

  public ParamTypeEquivalenceTester(final Collection<IType> caller, final Collection<IType> receiver) {
    this(caller, receiver, null);
  }

  public ParamTypeEquivalenceTester(final Collection<IType> caller, final Collection<IType> receiver, final F2<Integer, IType, Boolean> filter) {
    this.caller = caller;
    this.receiver = receiver;
    this.filter = filter;
  }

  public boolean areParametersEquivalent() {
    if (caller.size() != receiver.size())
      return false;

    final ParallelIterator<IType, IType> it = getIterator();

    for (int i = 0; it.hasNext(); i++) {
      final Pair<IType, IType> types = it.next();
      IType callerType = types.first();
      IType receiverType = types.second();

      if (filter != null && filter.apply(i, receiverType)) {
        continue;
      }

      final IType underlyingCaller = getUnderlyingType(callerType);

      // passing this which is an instantiated class template yields a
      // deferred instance
      if (isClassInstantiationType(callerType, underlyingCaller) && receiverType instanceof IPointerType) {
        final IType type = unwindQualifierType(((IPointerType) receiverType).getType());

        if (type instanceof ICPPTemplateInstance) {
          final ICPPTemplateDefinition callerDef = ((ICPPDeferredClassInstance) underlyingCaller).getTemplateDefinition();
          final ICPPTemplateDefinition receiverDef = ((ICPPTemplateInstance) type).getTemplateDefinition();
          return receiverDef.equals(callerDef);
        }
      }

      if (callerType instanceof IArrayType && receiverType instanceof IArrayType) {
        callerType = ((IArrayType) callerType).getType();
        receiverType = ((IArrayType) receiverType).getType();
      }

      if (isPointerType(callerType) ^ isPointerType(receiverType))
        return false;

      if (isConstCharArray(callerType) && isString(receiverType)) {
        continue;
      }

      if (!AstUtil.isSameType(getUnderlyingType(receiverType), getUnderlyingType(callerType)))
        return false;
    }

    return true;
  }

  private static boolean isClassInstantiationType(final IType callerType, final IType unwindedCallerType) {
    return callerType instanceof IPointerType && unwindedCallerType instanceof ICPPDeferredClassInstance;
  }

  private static boolean isPointerType(final IType type) {
    return type instanceof IPointerType;
  }

  private ParallelIterator<IType, IType> getIterator() {
    return new ParallelIterator<>(caller.iterator(), receiver.iterator());
  }

  private static IType getUnderlyingType(IType type) {
    type = CxxAstUtils.unwindTypedef(type);
    return AstUtil.asNonConst(AstUtil.unwindPointerOrRefType(type));
  }

  private static boolean isString(IType type) {
    type = AstUtil.unwindPointerOrRefType(CxxAstUtils.unwindTypedef(type));
    final String unwoundQualifierType = unwindQualifierType(type).toString();
    return unwoundQualifierType.contains(STD_STRING) || unwoundQualifierType.contains(BASIC_STRING_CHAR);
  }

  private static IType unwindQualifierType(IType type) {
    while (type instanceof IQualifierType) {
      type = ((IQualifierType) type).getType();
    }
    return type;
  }

  private static boolean isConstCharArray(final IType type) {
    return new ConstArrayVerifier(type).isConstCharArray();
  }
}
