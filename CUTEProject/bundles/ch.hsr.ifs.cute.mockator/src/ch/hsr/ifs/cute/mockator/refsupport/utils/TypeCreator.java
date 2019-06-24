package ch.hsr.ifs.cute.mockator.refsupport.utils;

import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.ALLCVQ;
import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.TDEF;

import org.eclipse.cdt.core.dom.ast.IASTArrayDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTArrayModifier;
import org.eclipse.cdt.core.dom.ast.IASTAttribute;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTPointer;
import org.eclipse.cdt.core.dom.ast.IASTPointerOperator;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.IArrayType;
import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConversionName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTPointerToMember;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTReferenceOperator;
import org.eclipse.cdt.core.parser.util.AttributeUtil;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.internal.core.dom.parser.ASTQueries;
import org.eclipse.cdt.internal.core.dom.parser.SizeofCalculator;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPArrayType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPBasicType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPFunctionType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPParameterPackType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPPointerToMemberType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPPointerType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPReferenceType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CVQualifier;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil;


// parts copied and adapted from org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CppVisitor
@SuppressWarnings("restriction")
public abstract class TypeCreator extends ASTQueries {

    public static IType byParamDeclaration(final ICPPASTParameterDeclaration pdecl) {
        final IASTDeclSpecifier pDeclSpec = pdecl.getDeclSpecifier();
        final ICPPASTDeclarator pDtor = pdecl.getDeclarator();
        IType pt = CPPVisitor.createType(pDeclSpec);

        if (pDtor != null) {
            pt = createType(pt, pDtor);
        }
        pt = adjustParameterType(pt);

        if (pDtor != null && CPPVisitor.findInnermostDeclarator(pDtor).declaresParameterPack()) {
            pt = new CPPParameterPackType(pt);
        }

        return pt;
    }

    public static IType byDeclarator(final IASTDeclarator declarator) {
        return CPPVisitor.createType(declarator);
    }

    private static IType createType(IType returnType, final ICPPASTFunctionDeclarator fnDtor) {
        final IType[] pTypes = createParameterTypes(fnDtor);

        IASTName name = fnDtor.getName();
        if (name instanceof ICPPASTQualifiedName) {
            name = ((ICPPASTQualifiedName) name).getLastName();
        }
        if (name instanceof ICPPASTConversionName) {
            returnType = createType(((ICPPASTConversionName) name).getTypeId());
        } else {
            returnType = applyAttributes(returnType, fnDtor);
            returnType = getPointerTypes(returnType, fnDtor);
        }

        final CPPFunctionType type = new CPPFunctionType(returnType, pTypes, fnDtor.getNoexceptEvaluation(), fnDtor.isConst(), fnDtor.isVolatile(),
                false, false, fnDtor.takesVarArgs());
        final IASTDeclarator nested = fnDtor.getNestedDeclarator();
        if (nested != null) {
            return createType(type, nested);
        }
        return type;
    }

    private static IType applyAttributes(IType type, final IASTDeclarator declarator) {
        if (type instanceof IBasicType) {
            final IBasicType basicType = (IBasicType) type;
            if (basicType.getKind() == IBasicType.Kind.eInt) {
                final IASTAttribute[] attributes = declarator.getAttributes();
                for (final IASTAttribute attribute : attributes) {
                    final char[] name = attribute.getName();
                    if (CharArrayUtils.equals(name, "__mode__") || CharArrayUtils.equals(name, "mode")) { //$NON-NLS-2$
                        final char[] mode = AttributeUtil.getSimpleArgument(attribute);
                        if (CharArrayUtils.equals(mode, "__QI__") || CharArrayUtils.equals(mode, "QI")) { //$NON-NLS-2$
                            type = new CPPBasicType(IBasicType.Kind.eChar, basicType.isUnsigned() ? IBasicType.IS_UNSIGNED : IBasicType.IS_SIGNED);
                        } else if (CharArrayUtils.equals(mode, "__HI__") || CharArrayUtils.equals(mode, "HI")) { //$NON-NLS-2$
                            type = new CPPBasicType(IBasicType.Kind.eInt, IBasicType.IS_SHORT | getSignModifiers(basicType));
                        } else if (CharArrayUtils.equals(mode, "__SI__") || CharArrayUtils.equals(mode, "SI")) { //$NON-NLS-2$
                            type = new CPPBasicType(IBasicType.Kind.eInt, getSignModifiers(basicType));
                        } else if (CharArrayUtils.equals(mode, "__DI__") || CharArrayUtils.equals(mode, "DI")) { //$NON-NLS-2$
                            final SizeofCalculator sizeofs = new SizeofCalculator(declarator.getTranslationUnit());
                            int modifier;
                            if (sizeofs.sizeof_long != null && sizeofs.sizeof_int != null && sizeofs.sizeof_long.size == 2 *
                                                                                                                         sizeofs.sizeof_int.size) {
                                modifier = IBasicType.IS_LONG;
                            } else {
                                modifier = IBasicType.IS_LONG_LONG;
                            }
                            type = new CPPBasicType(IBasicType.Kind.eInt, modifier | getSignModifiers(basicType));
                        } else if (CharArrayUtils.equals(mode, "__word__") || CharArrayUtils.equals(mode, "word")) { //$NON-NLS-2$
                            type = new CPPBasicType(IBasicType.Kind.eInt, IBasicType.IS_LONG | getSignModifiers(basicType));
                        }
                    }
                }
            }
        }
        return type;
    }

    private static int getSignModifiers(final IBasicType type) {
        return type.getModifiers() & (IBasicType.IS_SIGNED | IBasicType.IS_UNSIGNED);
    }

    private static IType createType(final IASTTypeId typeid) {
        return byDeclarator(typeid.getAbstractDeclarator());
    }

    private static IType[] createParameterTypes(final ICPPASTFunctionDeclarator fnDtor) {
        final ICPPASTParameterDeclaration[] params = fnDtor.getParameters();
        final IType[] pTypes = new IType[params.length];

        for (int i = 0; i < params.length; i++) {
            pTypes[i] = byParamDeclaration(params[i]);
        }

        return pTypes;
    }

    private static IType adjustParameterType(final IType pt) {
        final IType t = SemanticUtil.getNestedType(pt, TDEF);

        if (t instanceof IArrayType) {
            final IArrayType at = (IArrayType) t;
            return new CPPPointerType(at.getType());
        }

        if (t instanceof IFunctionType) {
            return new CPPPointerType(pt);
        }

        if (SemanticUtil.getCVQualifier(t) != CVQualifier.NONE) {
            return SemanticUtil.getNestedType(t, TDEF | ALLCVQ);
        }

        return pt;
    }

    private static IType getPointerTypes(IType type, final IASTDeclarator declarator) {
        final IASTPointerOperator[] ptrOps = declarator.getPointerOperators();
        for (final IASTPointerOperator ptrOp : ptrOps) {
            if (ptrOp instanceof ICPPASTPointerToMember) {
                type = new CPPPointerToMemberType(type, (ICPPASTPointerToMember) ptrOp);
            } else if (ptrOp instanceof IASTPointer) {
                type = new CPPPointerType(type, (IASTPointer) ptrOp);
            } else if (ptrOp instanceof ICPPASTReferenceOperator) {
                final ICPPASTReferenceOperator refOp = (ICPPASTReferenceOperator) ptrOp;
                type = new CPPReferenceType(type, refOp.isRValueReference());
            }
        }
        return type;
    }

    private static IType getArrayTypes(IType type, final IASTArrayDeclarator declarator) {
        final IASTArrayModifier[] mods = declarator.getArrayModifiers();

        for (int i = mods.length - 1; i >= 0; i--) {
            final IASTArrayModifier mod = mods[i];
            type = new CPPArrayType(type, mod.getConstantExpression());
        }

        return type;
    }

    private static IType createType(final IType baseType, final IASTDeclarator declarator) {
        if (declarator instanceof ICPPASTFunctionDeclarator) {
            return createType(baseType, (ICPPASTFunctionDeclarator) declarator);
        }

        IType type = baseType;
        type = getPointerTypes(type, declarator);

        if (declarator instanceof IASTArrayDeclarator) {
            type = getArrayTypes(type, (IASTArrayDeclarator) declarator);
        }

        final IASTDeclarator nested = declarator.getNestedDeclarator();

        if (nested != null) {
            return createType(type, nested);
        }
        return type;
    }
}
