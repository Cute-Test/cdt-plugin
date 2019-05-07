package ch.hsr.ifs.cute.mockator.refsupport.functions.params;

import static ch.hsr.ifs.cute.mockator.MockatorConstants.BASIC_STRING_CHAR;

import java.util.Collection;
import java.util.Iterator;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.eclipse.cdt.codan.core.cxx.CxxAstUtils;
import org.eclipse.cdt.core.dom.ast.IArrayType;
import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.IQualifierType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPDeferredClassInstance;

import ch.hsr.ifs.iltis.core.core.functional.Functional;
import ch.hsr.ifs.iltis.core.core.functional.StreamTriple;
import ch.hsr.ifs.iltis.core.core.functional.functions.Function2;

import ch.hsr.ifs.iltis.cpp.core.ast.ASTUtil;
import ch.hsr.ifs.iltis.cpp.core.ast.utilities.ASTTypeUtil;
import ch.hsr.ifs.iltis.cpp.core.util.constants.CommonCPPConstants;


@SuppressWarnings("restriction")
public class ParamTypeEquivalenceTester {

    private final Collection<IType>                  caller;
    private final Collection<IType>                  receiver;
    private final Function2<Integer, IType, Boolean> filter;

    public ParamTypeEquivalenceTester(final Collection<IType> caller, final Collection<IType> receiver) {
        this(caller, receiver, null);
    }

    public ParamTypeEquivalenceTester(final Collection<IType> caller, final Collection<IType> receiver,
                                      final Function2<Integer, IType, Boolean> filter) {
        this.caller = caller;
        this.receiver = receiver;
        this.filter = filter;
    }

    public boolean areParametersEquivalent() {
        if (caller.size() != receiver.size()) {
            return false;
        }

        final Iterator<StreamTriple<IType, IType, Integer>> it = getZipedStream().iterator();

        while (it.hasNext()) {
            final StreamTriple<IType, IType, Integer> triple = it.next();

            IType callerType = triple.first();
            IType receiverType = triple.second();
            final Integer argNo = triple.third();

            if (filter != null && filter.apply(argNo, receiverType)) {
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

            if (isPointerType(callerType) ^ isPointerType(receiverType)) {
                return false;
            }

            if (isConstCharArray(callerType) && isString(receiverType)) {
                continue;
            }

            if (!ASTUtil.isSameType(getUnderlyingType(receiverType), getUnderlyingType(callerType))) {
                return false;
            }
        }

        return true;
    }

    private static boolean isClassInstantiationType(final IType callerType, final IType unwindedCallerType) {
        return callerType instanceof IPointerType && unwindedCallerType instanceof ICPPDeferredClassInstance;
    }

    private static boolean isPointerType(final IType type) {
        return type instanceof IPointerType;
    }

    private Stream<StreamTriple<IType, IType, Integer>> getZipedStream() {
        return Functional.zip(caller.stream(), receiver.stream(), IntStream.rangeClosed(0, Math.max(caller.size(), receiver.size())).boxed());
    }

    private static IType getUnderlyingType(IType type) {
        type = CxxAstUtils.unwindTypedef(type);
        return ASTTypeUtil.asNonConst(ASTTypeUtil.unwindPointerOrRefType(type));
    }

    private static boolean isString(IType type) {
        type = ASTTypeUtil.unwindPointerOrRefType(CxxAstUtils.unwindTypedef(type));
        final String unwoundQualifierType = unwindQualifierType(type).toString();
        return unwoundQualifierType.contains(CommonCPPConstants.STD_STRING) || unwoundQualifierType.contains(BASIC_STRING_CHAR);
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
