package ch.hsr.ifs.cute.mockator.refsupport.functions.params;

import static ch.hsr.ifs.iltis.core.collections.CollectionUtil.list;

import java.util.Collection;

import org.eclipse.cdt.core.dom.ast.IASTArrayDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTArrayModifier;
import org.eclipse.cdt.core.dom.ast.IASTArraySubscriptExpression;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTCastExpression;
import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTConditionalExpression;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTEqualsInitializer;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpressionList;
import org.eclipse.cdt.core.dom.ast.IASTFieldDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFieldReference;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTInitializerList;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTPointer;
import org.eclipse.cdt.core.dom.ast.IASTPointerOperator;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStandardFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.IASTTypeIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.c.ICASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.c.ICASTTypeIdInitializerExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCastExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorInitializer;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeleteExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNameSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNewExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTPackExpansionExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTPointerToMember;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTReferenceOperator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleTypeConstructorExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTypeId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTypeIdExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.gnu.IGNUASTCompoundStatementExpression;
import org.eclipse.cdt.core.dom.ast.gnu.c.ICASTKnRFunctionDeclarator;
import org.eclipse.cdt.core.parser.Keywords;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;

import ch.hsr.ifs.iltis.core.exception.ILTISException;

import ch.hsr.ifs.iltis.cpp.core.wrappers.CPPVisitor;


// Taken and adapted from org.eclipse.cdt.internal.core.model.ASTStringUtil;
// ASTStringUtil has problems with formatting template ids like std::map<int, int>
// TODO contribute changes to CDT
public class ParameterSignatureHandler {

    private static final String                  SPACE       = " ";
    private static final String                  COMMA_SPACE = ", ";
    private final IASTStandardFunctionDeclarator funDecl;

    public ParameterSignatureHandler(final IASTStandardFunctionDeclarator funDecl) {
        this.funDecl = funDecl;
    }

    public Collection<String> getParameterSignatures() {
        final IASTParameterDeclaration[] parameters = funDecl.getParameters();
        final boolean takesVarArgs = funDecl.takesVarArgs();
        final String[] parameterStrings = new String[parameters.length + (takesVarArgs ? 1 : 0)];
        int i;
        for (i = 0; i < parameters.length; ++i) {
            parameterStrings[i] = getParameterSignatureString(parameters[i]);
        }
        if (takesVarArgs) {
            parameterStrings[i] = new String(Keywords.cpELLIPSIS);
        }
        return list(parameterStrings);
    }

    private static String getExpressionString(final IASTExpression expression) {
        final StringBuilder buf = new StringBuilder();
        return appendExpressionString(buf, expression).toString();
    }

    private static String getParameterSignatureString(final IASTParameterDeclaration parameterDeclaration) {
        return trimRight(appendParameterDeclarationString(new StringBuilder(), parameterDeclaration)).toString();
    }

    private static StringBuilder appendSignatureString(final StringBuilder buffer, IASTDeclarator declarator) {
        IASTNode node = declarator.getParent();
        while (node instanceof IASTDeclarator) {
            declarator = (IASTDeclarator) node;
            node = node.getParent();
        }

        final IASTDeclSpecifier declSpec;
        if (node instanceof IASTParameterDeclaration) {
            declSpec = ((IASTParameterDeclaration) node).getDeclSpecifier();
        } else if (node instanceof IASTSimpleDeclaration) {
            declSpec = ((IASTSimpleDeclaration) node).getDeclSpecifier();
        } else if (node instanceof IASTFunctionDefinition) {
            declSpec = ((IASTFunctionDefinition) node).getDeclSpecifier();
        } else if (node instanceof IASTTypeId) {
            declSpec = ((IASTTypeId) node).getDeclSpecifier();
        } else {
            declSpec = null;
        }

        return appendDeclarationString(buffer, declSpec, declarator, null);
    }

    private static StringBuilder appendDeclarationString(final StringBuilder buffer, final IASTDeclSpecifier declSpecifier,
            final IASTDeclarator declarator, final IASTFunctionDeclarator returnTypeOf) {
        if (declSpecifier != null) {
            appendDeclSpecifierString(buffer, declSpecifier);
            trimRight(buffer);
        }
        appendDeclaratorString(buffer, declarator, false, returnTypeOf);
        return buffer;
    }

    private static StringBuilder appendDeclaratorString(final StringBuilder buffer, final IASTDeclarator declarator, boolean protectPointers,
            final IASTFunctionDeclarator returnTypeOf) {
        if (declarator == null) {
            return buffer;
        }
        final IASTPointerOperator[] ptrs = declarator.getPointerOperators();
        final boolean useParenthesis = protectPointers && ptrs.length > 0;
        if (useParenthesis) {
            buffer.append(Keywords.cpLPAREN);
            protectPointers = false;
        }

        appendPointerOperatorsString(buffer, ptrs);

        if (declarator != returnTypeOf) {
            final IASTDeclarator nestedDeclarator = declarator.getNestedDeclarator();
            if (nestedDeclarator != null) {
                protectPointers = protectPointers || declarator instanceof IASTArrayDeclarator || declarator instanceof IASTFunctionDeclarator ||
                                  declarator instanceof IASTFieldDeclarator;
                appendDeclaratorString(buffer, nestedDeclarator, protectPointers, returnTypeOf);
            } else if (declarator instanceof ICPPASTDeclarator && ((ICPPASTDeclarator) declarator).declaresParameterPack()) {
                buffer.append(Keywords.cpELLIPSIS);
            }

            if (declarator instanceof IASTArrayDeclarator) {
                appendArrayQualifiersString(buffer, (IASTArrayDeclarator) declarator);
            } else if (declarator instanceof IASTFunctionDeclarator) {
                final IASTFunctionDeclarator functionDecl = (IASTFunctionDeclarator) declarator;
                appendParameterSignatureString(buffer, functionDecl);
                if (declarator instanceof ICPPASTFunctionDeclarator) {
                    final ICPPASTFunctionDeclarator cppFunctionDecl = (ICPPASTFunctionDeclarator) declarator;
                    if (cppFunctionDecl.isConst()) {
                        buffer.append(Keywords.CONST).append(' ');
                    }
                    if (cppFunctionDecl.isVolatile()) {
                        buffer.append(Keywords.VOLATILE).append(' ');
                    }
                    if (cppFunctionDecl.isPureVirtual()) {
                        buffer.append("=0 ");
                    }
                    final IASTTypeId[] exceptionTypeIds = cppFunctionDecl.getExceptionSpecification();
                    if (exceptionTypeIds != ICPPASTFunctionDeclarator.NO_EXCEPTION_SPECIFICATION) {
                        buffer.append(Keywords.THROW).append(" (");
                        for (int i = 0; i < exceptionTypeIds.length; i++) {
                            if (i > 0) {
                                buffer.append(COMMA_SPACE);
                            }
                            appendTypeIdString(buffer, exceptionTypeIds[i]);
                        }
                        buffer.append(')');
                    }
                }
            } else if (declarator instanceof IASTFieldDeclarator) {
                final IASTFieldDeclarator fieldDeclarator = (IASTFieldDeclarator) declarator;
                final IASTExpression bitFieldSize = fieldDeclarator.getBitFieldSize();
                if (bitFieldSize != null) {
                    buffer.append(Keywords.cpCOLON);
                    appendExpressionString(buffer, bitFieldSize);
                }
            } else if (CPPVisitor.findAncestorWithType(declarator, ICPPASTTemplateId.class).orElse(null) != null) {
                buffer.append(declarator.getName());
            }
        }
        if (useParenthesis) {
            trimRight(buffer);
            buffer.append(Keywords.cpRPAREN);
        }
        return buffer;
    }

    private static StringBuilder appendInitializerString(final StringBuilder buffer, final IASTInitializer initializer) {
        if (initializer instanceof IASTEqualsInitializer) {
            final IASTEqualsInitializer initializerExpression = (IASTEqualsInitializer) initializer;
            buffer.append(Keywords.cpASSIGN);
            appendInitClauseString(buffer, initializerExpression.getInitializerClause());
        } else if (initializer instanceof IASTInitializerList) {
            final IASTInitializerList initializerList = (IASTInitializerList) initializer;
            final IASTInitializerClause[] initializers = initializerList.getClauses();
            buffer.append(Keywords.cpASSIGN);
            buffer.append(Keywords.cpLBRACE);
            for (int i = 0; i < initializers.length; i++) {
                if (i > 0) {
                    buffer.append(COMMA_SPACE);
                }
                appendInitClauseString(buffer, initializers[i]);
            }
            trimRight(buffer);
            buffer.append(Keywords.cpRBRACE);
        } else if (initializer instanceof ICPPASTConstructorInitializer) {
            final ICPPASTConstructorInitializer constructorInitializer = (ICPPASTConstructorInitializer) initializer;
            final IASTInitializerClause[] clauses = constructorInitializer.getArguments();
            buffer.append(Keywords.cpLPAREN);
            for (int i = 0; i < clauses.length; i++) {
                if (i > 0) {
                    buffer.append(COMMA_SPACE);
                }
                appendInitClauseString(buffer, clauses[i]);
            }
            trimRight(buffer);
            buffer.append(Keywords.cpRPAREN);
        } else if (initializer != null) {
            throw new ILTISException("Not impplemented: " + initializer.getClass().getName()).rethrowUnchecked();
        }
        return buffer;
    }

    private static StringBuilder appendInitClauseString(final StringBuilder buffer, final IASTInitializerClause initializerClause) {
        if (initializerClause instanceof IASTExpression) {
            return appendExpressionString(buffer, (IASTExpression) initializerClause);
        }
        if (initializerClause instanceof IASTInitializer) {
            return appendInitializerString(buffer, (IASTInitializer) initializerClause);
        }
        return buffer;
    }

    private static StringBuilder appendTypeIdString(final StringBuilder buffer, final IASTTypeId typeId) {
        appendDeclSpecifierString(buffer, typeId.getDeclSpecifier());
        appendDeclaratorString(buffer, typeId.getAbstractDeclarator(), false, null);
        if (typeId instanceof ICPPASTTypeId && ((ICPPASTTypeId) typeId).isPackExpansion()) {
            buffer.append(Keywords.cpELLIPSIS);
        }
        return buffer;
    }

    private static StringBuilder trimRight(final StringBuilder buffer) {
        int length = buffer.length();
        while (length > 0 && buffer.charAt(length - 1) == ' ') {
            --length;
        }
        buffer.setLength(length);
        return buffer;
    }

    private static StringBuilder appendArrayQualifiersString(final StringBuilder buffer, final IASTArrayDeclarator declarator) {
        final IASTArrayModifier[] modifiers = declarator.getArrayModifiers();
        final int count = modifiers.length;
        for (int i = 0; i < count; i++) {
            buffer.append(Keywords.cpLBRACKET).append(Keywords.cpRBRACKET);
        }
        return buffer;
    }

    private static StringBuilder appendPointerOperatorsString(final StringBuilder buffer, final IASTPointerOperator[] pointerOperators) {
        for (final IASTPointerOperator pointerOperator : pointerOperators) {
            if (pointerOperator instanceof IASTPointer) {
                final IASTPointer pointer = (IASTPointer) pointerOperator;
                if (pointer instanceof ICPPASTPointerToMember) {
                    final ICPPASTPointerToMember pointerToMember = (ICPPASTPointerToMember) pointer;
                    appendQualifiedNameString(buffer, pointerToMember.getName());
                }
                buffer.append(Keywords.cpSTAR);
                if (pointer.isConst()) {
                    buffer.append(' ').append(Keywords.CONST);
                }
                if (pointer.isVolatile()) {
                    buffer.append(' ').append(Keywords.VOLATILE);
                }
                if (pointer.isRestrict()) {
                    buffer.append(' ').append(Keywords.RESTRICT);
                }
            } else if (pointerOperator instanceof ICPPASTReferenceOperator) {
                buffer.append(Keywords.cpAMPER);
            }
        }
        return buffer;
    }

    private static StringBuilder appendParameterSignatureString(final StringBuilder buffer, final IASTFunctionDeclarator functionDeclarator) {
        if (functionDeclarator instanceof IASTStandardFunctionDeclarator) {
            final IASTStandardFunctionDeclarator standardFunctionDecl = (IASTStandardFunctionDeclarator) functionDeclarator;
            final IASTParameterDeclaration[] parameters = standardFunctionDecl.getParameters();
            final boolean takesVarArgs = standardFunctionDecl.takesVarArgs();
            buffer.append(Keywords.cpLPAREN);
            for (int i = 0; i < parameters.length; i++) {
                if (i > 0) {
                    buffer.append(COMMA_SPACE);
                }
                appendParameterDeclarationString(buffer, parameters[i]);
            }
            if (takesVarArgs) {
                if (parameters.length > 0) {
                    buffer.append(COMMA_SPACE);
                }
                buffer.append(Keywords.cpELLIPSIS);
            }
            trimRight(buffer);
            buffer.append(Keywords.cpRPAREN);
        } else if (functionDeclarator instanceof ICASTKnRFunctionDeclarator) {
            buffer.append(Keywords.cpLPAREN);
            final ICASTKnRFunctionDeclarator knrDeclarator = (ICASTKnRFunctionDeclarator) functionDeclarator;
            final IASTName[] names = knrDeclarator.getParameterNames();
            for (int i = 0; i < names.length; i++) {
                if (i > 0) {
                    buffer.append(COMMA_SPACE);
                }
                if (names[i] != null) {
                    final IASTDeclarator declaratorForParameterName = knrDeclarator.getDeclaratorForParameterName(names[i]);
                    if (declaratorForParameterName != null) {
                        appendSignatureString(buffer, declaratorForParameterName);
                    }
                }
            }
            trimRight(buffer);
            buffer.append(Keywords.cpRPAREN);
        }
        return buffer;
    }

    private static StringBuilder appendParameterDeclarationString(final StringBuilder buffer, final IASTParameterDeclaration parameter) {
        final IASTDeclSpecifier declSpecifier = parameter.getDeclSpecifier();
        if (declSpecifier != null) {
            appendDeclSpecifierString(buffer, declSpecifier);
            trimRight(buffer);
        }
        final IASTDeclarator declarator = parameter.getDeclarator();
        if (declarator != null) {
            appendDeclaratorString(buffer, declarator, false, null);
            appendInitializerString(buffer, declarator.getInitializer());
        }
        return buffer;
    }

    public static StringBuilder appendDeclSpecifierString(final StringBuilder builder, final IASTDeclSpecifier declSpecifier) {
        if (declSpecifier.isConst()) {
            builder.append(Keywords.CONST).append(' ');
        }
        if (declSpecifier.isVolatile()) {
            builder.append(Keywords.VOLATILE).append(' ');
        }

        if (declSpecifier instanceof IASTCompositeTypeSpecifier) {
            final IASTCompositeTypeSpecifier compositeTypeSpec = (IASTCompositeTypeSpecifier) declSpecifier;
            final int key = compositeTypeSpec.getKey();
            switch (key) {
            case IASTCompositeTypeSpecifier.k_struct:
                builder.append(Keywords.STRUCT).append(' ');
                break;
            case IASTCompositeTypeSpecifier.k_union:
                builder.append(Keywords.UNION).append(' ');
                break;
            case ICPPASTCompositeTypeSpecifier.k_class:
                builder.append(Keywords.CLASS).append(' ');
                break;
            default:
                break;
            }
            appendQualifiedNameString(builder, compositeTypeSpec.getName());
        } else if (declSpecifier instanceof IASTElaboratedTypeSpecifier) {
            final IASTElaboratedTypeSpecifier elaboratedTypeSpec = (IASTElaboratedTypeSpecifier) declSpecifier;
            switch (elaboratedTypeSpec.getKind()) {
            case IASTElaboratedTypeSpecifier.k_enum:
                builder.append(Keywords.ENUM).append(' ');
                break;
            case IASTElaboratedTypeSpecifier.k_struct:
                builder.append(Keywords.STRUCT).append(' ');
                break;
            case IASTElaboratedTypeSpecifier.k_union:
                builder.append(Keywords.UNION).append(' ');
                break;
            case ICPPASTElaboratedTypeSpecifier.k_class:
                builder.append(Keywords.CLASS).append(' ');
                break;
            default:
                assert false;
            }
            appendQualifiedNameString(builder, elaboratedTypeSpec.getName());
        } else if (declSpecifier instanceof IASTEnumerationSpecifier) {
            final IASTEnumerationSpecifier enumerationSpec = (IASTEnumerationSpecifier) declSpecifier;
            builder.append(Keywords.ENUM).append(' ');
            appendQualifiedNameString(builder, enumerationSpec.getName());
        } else if (declSpecifier instanceof IASTSimpleDeclSpecifier) {
            final IASTSimpleDeclSpecifier simpleDeclSpec = (IASTSimpleDeclSpecifier) declSpecifier;
            if (simpleDeclSpec.isSigned()) {
                builder.append(Keywords.SIGNED).append(' ');
            }
            if (simpleDeclSpec.isUnsigned()) {
                builder.append(Keywords.UNSIGNED).append(' ');
            }
            if (simpleDeclSpec.isShort()) {
                builder.append(Keywords.SHORT).append(' ');
            }
            if (simpleDeclSpec.isLong()) {
                builder.append(Keywords.LONG).append(' ');
            }
            if (simpleDeclSpec.isLongLong()) {
                builder.append(Keywords.LONG_LONG).append(' ');
            }
            if (simpleDeclSpec.isComplex()) {
                builder.append(Keywords._COMPLEX).append(' ');
            }
            if (simpleDeclSpec.isImaginary()) {
                builder.append(Keywords._IMAGINARY).append(' ');
            }
            switch (simpleDeclSpec.getType()) {
            case IASTSimpleDeclSpecifier.t_void:
                builder.append(Keywords.VOID).append(' ');
                break;
            case IASTSimpleDeclSpecifier.t_char:
                builder.append(Keywords.CHAR).append(' ');
                break;
            case IASTSimpleDeclSpecifier.t_int:
                builder.append(Keywords.INT).append(' ');
                break;
            case IASTSimpleDeclSpecifier.t_float:
                builder.append(Keywords.FLOAT).append(' ');
                break;
            case IASTSimpleDeclSpecifier.t_double:
                builder.append(Keywords.DOUBLE).append(' ');
                break;
            case IASTSimpleDeclSpecifier.t_bool:
                if (simpleDeclSpec instanceof ICASTSimpleDeclSpecifier) {
                    builder.append(Keywords.cBOOL).append(' ');
                } else {
                    builder.append(Keywords.BOOL).append(' ');
                }
                break;
            case IASTSimpleDeclSpecifier.t_wchar_t:
                builder.append(Keywords.WCHAR_T).append(' ');
                break;
            case IASTSimpleDeclSpecifier.t_char16_t:
                builder.append(Keywords.CHAR16_T).append(' ');
                break;
            case IASTSimpleDeclSpecifier.t_char32_t:
                builder.append(Keywords.CHAR32_T).append(' ');
                break;
            default:
                break;
            }
        } else if (declSpecifier instanceof IASTNamedTypeSpecifier) {
            final IASTNamedTypeSpecifier namedTypeSpec = (IASTNamedTypeSpecifier) declSpecifier;
            appendQualifiedNameString(builder, namedTypeSpec.getName());
        }
        return builder;
    }

    private static StringBuilder appendQualifiedNameString(final StringBuilder buffer, final IASTName name) {
        return appendNameString(buffer, name, true);
    }

    private static StringBuilder appendNameString(final StringBuilder buffer, final IASTName name, final boolean qualified) {
        if (name instanceof ICPPASTQualifiedName) {
            final ICPPASTQualifiedName qualifiedName = (ICPPASTQualifiedName) name;
            if (qualified) {
                final ICPPASTNameSpecifier[] names = qualifiedName.getAllSegments();
                for (int i = 0; i < names.length; i++) {
                    if (i > 0) {
                        buffer.append(Keywords.cpCOLONCOLON);
                    }
                    if (names[i] instanceof IASTName) {
                        appendQualifiedNameString(buffer, (IASTName) names[i]);
                    } else {
                        buffer.append(names[i].getRawSignature());
                    }
                }
            } else {
                buffer.append(qualifiedName.getLastName());
            }
        } else if (name instanceof ICPPASTTemplateId) {
            final ICPPASTTemplateId templateId = (ICPPASTTemplateId) name;
            appendQualifiedNameString(buffer, templateId.getTemplateName());
            final IASTNode[] templateArguments = templateId.getTemplateArguments();
            buffer.append(Keywords.cpLT);
            for (int i = 0; i < templateArguments.length; i++) {
                if (i > 0) {
                    buffer.append(Keywords.cpCOMMA);
                }
                final IASTNode argument = templateArguments[i];
                if (argument instanceof IASTTypeId) {
                    appendTypeIdString(buffer, (IASTTypeId) argument);
                } else if (argument instanceof IASTExpression) {
                    final IASTExpression expression = (IASTExpression) argument;
                    appendExpressionString(buffer, expression);
                }
                trimRight(buffer);
            }
            buffer.append(Keywords.cpGT);
        } else if (name != null) {
            buffer.append(name.getSimpleID());
        }
        return buffer;
    }

    private static StringBuilder appendExpressionString(final StringBuilder buffer, final IASTExpression expression) {
        if (expression instanceof IASTIdExpression) {
            final IASTIdExpression idExpression = (IASTIdExpression) expression;
            return appendQualifiedNameString(buffer, idExpression.getName());
        }
        if (expression instanceof IASTExpressionList) {
            final IASTExpressionList expressionList = (IASTExpressionList) expression;
            final IASTExpression[] expressions = expressionList.getExpressions();
            for (int i = 0; i < expressions.length; i++) {
                if (i > 0) {
                    buffer.append(COMMA_SPACE);
                }
                appendExpressionString(buffer, expressions[i]);
            }
            return buffer;
        }
        if (expression instanceof ICPPASTSimpleTypeConstructorExpression) {
            final ICPPASTSimpleTypeConstructorExpression typeCast = (ICPPASTSimpleTypeConstructorExpression) expression;
            appendDeclSpecifierString(buffer, typeCast.getDeclSpecifier());
            trimRight(buffer);
            return appendInitializerString(buffer, typeCast.getInitializer());
        }
        if (expression instanceof IASTArraySubscriptExpression) {
            return appendArraySubscriptExpression(buffer, (IASTArraySubscriptExpression) expression);
        }
        if (expression instanceof IASTBinaryExpression) {
            return appendBinaryExpression(buffer, (IASTBinaryExpression) expression);
        }
        if (expression instanceof IASTCastExpression) {
            return appendCastExpression(buffer, (IASTCastExpression) expression);
        }
        if (expression instanceof IASTConditionalExpression) {
            return appendConditionalExpression(buffer, (IASTConditionalExpression) expression);
        }
        if (expression instanceof IASTExpressionList) {
            return appendExpressionList(buffer, (IASTExpressionList) expression);
        }
        if (expression instanceof IASTFieldReference) {
            return appendFieldReference(buffer, (IASTFieldReference) expression);
        }
        if (expression instanceof IASTFunctionCallExpression) {
            return appendFunctionCallExpression(buffer, (IASTFunctionCallExpression) expression);
        }
        if (expression instanceof IASTLiteralExpression) {
            return appendLiteralExpression(buffer, (IASTLiteralExpression) expression);
        }
        if (expression instanceof IASTTypeIdExpression) {
            return appendTypeIdExpression(buffer, (IASTTypeIdExpression) expression);
        }
        if (expression instanceof IASTUnaryExpression) {
            return appendUnaryExpression(buffer, (IASTUnaryExpression) expression);
        }
        if (expression instanceof ICASTTypeIdInitializerExpression) {
            return appendTypeIdInitializerExpression(buffer, (ICASTTypeIdInitializerExpression) expression);
        }
        if (expression instanceof ICPPASTDeleteExpression) {
            return appendDeleteExpression(buffer, (ICPPASTDeleteExpression) expression);
        }
        if (expression instanceof ICPPASTNewExpression) {
            return appendNewExpression(buffer, (ICPPASTNewExpression) expression);
        }
        if (expression instanceof IGNUASTCompoundStatementExpression) {
            return appendCompoundStatementExpression(buffer);
        }
        if (expression instanceof ICPPASTPackExpansionExpression) {
            return appendPackExpansionExpression(buffer, (ICPPASTPackExpansionExpression) expression);
        }

        return buffer;
    }

    private static StringBuilder appendArraySubscriptExpression(final StringBuilder buffer, final IASTArraySubscriptExpression expression) {
        appendExpressionString(buffer, expression.getArrayExpression());
        buffer.append(Keywords.cpLBRACKET);
        appendInitClauseString(buffer, expression.getArgument());
        return buffer.append(Keywords.cpRBRACKET);
    }

    private static StringBuilder appendCastExpression(final StringBuilder buffer, final IASTCastExpression expression) {
        if (expression.getOperator() == IASTCastExpression.op_cast) {
            buffer.append(Keywords.cpLPAREN);
            appendTypeIdString(buffer, expression.getTypeId());
            buffer.append(Keywords.cpRPAREN);
            return appendExpressionString(buffer, expression.getOperand());
        }

        buffer.append(getCastOperatorString(expression));
        buffer.append(Keywords.cpLT);
        appendTypeIdString(buffer, expression.getTypeId());
        trimRight(buffer);
        buffer.append(Keywords.cpGT);
        buffer.append(Keywords.cpLPAREN);
        appendExpressionString(buffer, expression.getOperand());
        return buffer.append(Keywords.cpRPAREN);
    }

    private static StringBuilder appendFieldReference(final StringBuilder buffer, final IASTFieldReference expression) {
        appendExpressionString(buffer, expression.getFieldOwner());
        buffer.append(expression.isPointerDereference() ? Keywords.cpARROW : Keywords.cpDOT);

        return appendNameString(buffer, expression.getFieldName(), true);
    }

    private static StringBuilder appendFunctionCallExpression(final StringBuilder buffer, final IASTFunctionCallExpression expression) {
        appendExpressionString(buffer, expression.getFunctionNameExpression());
        buffer.append(Keywords.cpLPAREN);
        final IASTInitializerClause[] clauses = expression.getArguments();
        for (int i = 0; i < clauses.length; i++) {
            if (i > 0) {
                buffer.append(COMMA_SPACE);
            }
            appendInitClauseString(buffer, clauses[i]);
        }
        return buffer.append(Keywords.cpRPAREN);
    }

    private static StringBuilder appendTypeIdInitializerExpression(final StringBuilder buffer, final ICASTTypeIdInitializerExpression expression) {
        buffer.append(Keywords.cpLPAREN);
        appendTypeIdString(buffer, expression.getTypeId());
        buffer.append(Keywords.cpRPAREN);
        return appendInitializerString(buffer, expression.getInitializer());
    }

    private static StringBuilder appendDeleteExpression(final StringBuilder buffer, final ICPPASTDeleteExpression expression) {
        buffer.append(Keywords.DELETE);
        buffer.append(SPACE);
        return appendExpressionString(buffer, expression.getOperand());
    }

    private static StringBuilder appendCompoundStatementExpression(final StringBuilder buffer) {
        buffer.append(Keywords.cpLPAREN).append(Keywords.cpLBRACE);
        buffer.append(Keywords.cpELLIPSIS);
        return buffer.append(Keywords.cpRBRACE).append(Keywords.cpRPAREN);
    }

    private static StringBuilder appendTypeIdExpression(final StringBuilder buffer, final IASTTypeIdExpression expression) {
        buffer.append(getTypeIdExpressionOperator(expression));
        buffer.append(Keywords.cpLPAREN);
        appendTypeIdString(buffer, expression.getTypeId());
        trimRight(buffer);
        return buffer.append(Keywords.cpRPAREN);
    }

    private static StringBuilder appendExpressionList(final StringBuilder buffer, final IASTExpressionList expression) {
        final IASTExpression[] exps = expression.getExpressions();
        if (exps != null) {
            for (int i = 0; i < exps.length; i++) {
                if (i > 0) {
                    buffer.append(COMMA_SPACE);
                }
                appendExpressionString(buffer, exps[i]);
            }
        }
        return buffer;
    }

    private static StringBuilder appendLiteralExpression(final StringBuilder buffer, final IASTLiteralExpression expression) {
        return buffer.append(expression.toString());
    }

    private static StringBuilder appendConditionalExpression(final StringBuilder buffer, final IASTConditionalExpression expression) {
        appendExpressionString(buffer, expression.getLogicalConditionExpression());
        buffer.append(SPACE);
        buffer.append(Keywords.cpQUESTION);
        buffer.append(SPACE);
        appendExpressionString(buffer, expression.getPositiveResultExpression());
        buffer.append(SPACE);
        buffer.append(Keywords.cpCOLON);
        buffer.append(SPACE);
        return appendExpressionString(buffer, expression.getNegativeResultExpression());
    }

    private static StringBuilder appendNewExpression(final StringBuilder buffer, final ICPPASTNewExpression expression) {
        buffer.append(Keywords.NEW);
        buffer.append(SPACE);
        final IASTInitializerClause[] args = expression.getPlacementArguments();
        if (args != null) {
            buffer.append(Keywords.cpLPAREN);
            for (int i = 0; i < args.length; i++) {
                if (i != 0) {
                    buffer.append(COMMA_SPACE);
                }
                appendInitClauseString(buffer, args[i]);
            }
            buffer.append(Keywords.cpRPAREN);
        }
        appendTypeIdString(buffer, expression.getTypeId());
        return appendInitializerString(buffer, expression.getInitializer());
    }

    private static StringBuilder appendBinaryExpression(final StringBuilder buffer, final IASTBinaryExpression expression) {
        appendExpressionString(buffer, expression.getOperand1());
        buffer.append(SPACE);
        buffer.append(getBinaryOperatorString(expression));
        buffer.append(SPACE);
        return appendExpressionString(buffer, expression.getOperand2());
    }

    private static StringBuilder appendUnaryExpression(final StringBuilder buffer, final IASTUnaryExpression expression) {
        boolean postOperator = false;
        boolean primaryBracketed = false;

        switch (expression.getOperator()) {
        case IASTUnaryExpression.op_postFixDecr:
        case IASTUnaryExpression.op_postFixIncr:
            postOperator = true;
            break;
        case IASTUnaryExpression.op_bracketedPrimary:
            primaryBracketed = true;
            break;
        default:
            postOperator = false;
            break;
        }

        if (!postOperator && !primaryBracketed) {
            buffer.append(getUnaryOperatorString(expression));
        }

        switch (expression.getOperator()) {
        case IASTUnaryExpression.op_sizeof:
        case ICPPASTUnaryExpression.op_throw:
        case ICPPASTUnaryExpression.op_typeid:
            buffer.append(SPACE);
            break;
        default:
            break;
        }

        if (primaryBracketed) {
            buffer.append(Keywords.cpLPAREN);
        }
        buffer.append(getExpressionString(expression.getOperand()));
        if (primaryBracketed) {
            buffer.append(Keywords.cpRPAREN);
        }
        if (postOperator && !primaryBracketed) {
            buffer.append(getUnaryOperatorString(expression));
        }

        return buffer;
    }

    private static String getCastOperatorString(final IASTCastExpression expression) {
        final int op = expression.getOperator();
        switch (op) {
        case ICPPASTCastExpression.op_const_cast:
            return Keywords.CONST_CAST;
        case ICPPASTCastExpression.op_dynamic_cast:
            return Keywords.DYNAMIC_CAST;
        case ICPPASTCastExpression.op_reinterpret_cast:
            return Keywords.REINTERPRET_CAST;
        case ICPPASTCastExpression.op_static_cast:
            return Keywords.STATIC_CAST;
        }
        return "";
    }

    private static char[] getUnaryOperatorString(final IASTUnaryExpression ue) {
        final int op = ue.getOperator();
        switch (op) {
        case IASTUnaryExpression.op_throw:
            return Keywords.cTHROW;
        case IASTUnaryExpression.op_typeid:
            return Keywords.cTYPEID;
        case IASTUnaryExpression.op_alignOf:
            return Keywords.cALIGNOF;
        case IASTUnaryExpression.op_amper:
            return Keywords.cpAMPER;
        case IASTUnaryExpression.op_minus:
            return Keywords.cpMINUS;
        case IASTUnaryExpression.op_not:
            return Keywords.cpNOT;
        case IASTUnaryExpression.op_plus:
            return Keywords.cpPLUS;
        case IASTUnaryExpression.op_postFixDecr:
        case IASTUnaryExpression.op_prefixDecr:
            return Keywords.cpDECR;
        case IASTUnaryExpression.op_postFixIncr:
        case IASTUnaryExpression.op_prefixIncr:
            return Keywords.cpINCR;
        case IASTUnaryExpression.op_sizeof:
            return Keywords.cSIZEOF;
        case IASTUnaryExpression.op_sizeofParameterPack:
            return Keywords.cSIZEOFPACK;
        case IASTUnaryExpression.op_star:
            return Keywords.cpSTAR;
        case IASTUnaryExpression.op_tilde:
            return Keywords.cpCOMPL;
        }

        return CharArrayUtils.EMPTY;
    }

    private static char[] getBinaryOperatorString(final IASTBinaryExpression be) {
        switch (be.getOperator()) {
        case IASTBinaryExpression.op_multiply:
            return Keywords.cpSTAR;
        case IASTBinaryExpression.op_divide:
            return Keywords.cpDIV;
        case IASTBinaryExpression.op_modulo:
            return Keywords.cpMOD;
        case IASTBinaryExpression.op_plus:
            return Keywords.cpPLUS;
        case IASTBinaryExpression.op_minus:
            return Keywords.cpMINUS;
        case IASTBinaryExpression.op_shiftLeft:
            return Keywords.cpSHIFTL;
        case IASTBinaryExpression.op_shiftRight:
            return Keywords.cpSHIFTR;
        case IASTBinaryExpression.op_lessThan:
            return Keywords.cpLT;
        case IASTBinaryExpression.op_greaterThan:
            return Keywords.cpGT;
        case IASTBinaryExpression.op_lessEqual:
            return Keywords.cpLTEQUAL;
        case IASTBinaryExpression.op_greaterEqual:
            return Keywords.cpGTEQUAL;
        case IASTBinaryExpression.op_binaryAnd:
            return Keywords.cpAMPER;
        case IASTBinaryExpression.op_binaryXor:
            return Keywords.cpXOR;
        case IASTBinaryExpression.op_binaryOr:
            return Keywords.cpBITOR;
        case IASTBinaryExpression.op_logicalAnd:
            return Keywords.cpAND;
        case IASTBinaryExpression.op_logicalOr:
            return Keywords.cpOR;
        case IASTBinaryExpression.op_assign:
            return Keywords.cpASSIGN;
        case IASTBinaryExpression.op_multiplyAssign:
            return Keywords.cpSTARASSIGN;
        case IASTBinaryExpression.op_divideAssign:
            return Keywords.cpDIVASSIGN;
        case IASTBinaryExpression.op_moduloAssign:
            return Keywords.cpMODASSIGN;
        case IASTBinaryExpression.op_plusAssign:
            return Keywords.cpPLUSASSIGN;
        case IASTBinaryExpression.op_minusAssign:
            return Keywords.cpMINUSASSIGN;
        case IASTBinaryExpression.op_shiftLeftAssign:
            return Keywords.cpSHIFTLASSIGN;
        case IASTBinaryExpression.op_shiftRightAssign:
            return Keywords.cpSHIFTRASSIGN;
        case IASTBinaryExpression.op_binaryAndAssign:
            return Keywords.cpAMPERASSIGN;
        case IASTBinaryExpression.op_binaryXorAssign:
            return Keywords.cpXORASSIGN;
        case IASTBinaryExpression.op_binaryOrAssign:
            return Keywords.cpBITORASSIGN;
        case IASTBinaryExpression.op_equals:
            return Keywords.cpEQUAL;
        case IASTBinaryExpression.op_notequals:
            return Keywords.cpNOTEQUAL;
        case IASTBinaryExpression.op_max:
            return Keywords.cpMAX;
        case IASTBinaryExpression.op_min:
            return Keywords.cpMIN;
        case IASTBinaryExpression.op_pmarrow:
            return Keywords.cpARROW;
        case IASTBinaryExpression.op_pmdot:
            return Keywords.cpDOT;
        }

        return CharArrayUtils.EMPTY;
    }

    private static String getTypeIdExpressionOperator(final IASTTypeIdExpression expression) {
        switch (expression.getOperator()) {
        case IASTTypeIdExpression.op_alignof:
            return Keywords.ALIGNOF;
        case IASTTypeIdExpression.op_typeof:
            return Keywords.TYPEOF;
        case ICPPASTTypeIdExpression.op_typeid:
            return Keywords.TYPEID;
        case IASTTypeIdExpression.op_sizeof:
            return Keywords.SIZEOF;
        }
        return "";
    }

    private static StringBuilder appendPackExpansionExpression(final StringBuilder buffer, final ICPPASTPackExpansionExpression expression) {
        appendExpressionString(buffer, expression.getPattern());
        return buffer.append(Keywords.cpELLIPSIS);
    }
}
