package ch.hsr.ifs.cute.mockator.incompleteclass.staticpoly.memfun;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.codan.core.cxx.CxxAstUtils;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTArraySubscriptExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPVariable;
import org.eclipse.cdt.internal.core.dom.parser.cpp.OverloadableOperator;

import ch.hsr.ifs.iltis.cpp.core.ast.ASTUtil;
import ch.hsr.ifs.iltis.cpp.core.wrappers.CPPVisitor;

import ch.hsr.ifs.cute.mockator.incompleteclass.TestDoubleMemFunImplStrategy;
import ch.hsr.ifs.cute.mockator.project.properties.CppStandard;
import ch.hsr.ifs.cute.mockator.project.properties.LinkedEditModeStrategy;
import ch.hsr.ifs.cute.mockator.refsupport.functions.FunctionEquivalenceVerifier.ConstStrategy;
import ch.hsr.ifs.cute.mockator.refsupport.functions.params.FunctionCallParameterCollector;
import ch.hsr.ifs.cute.mockator.refsupport.functions.params.ParamDeclCreator;
import ch.hsr.ifs.cute.mockator.refsupport.functions.returntypes.ReturnStatementCreator;
import ch.hsr.ifs.cute.mockator.refsupport.functions.returntypes.ReturnTypeDeducer;


// partially inspired by TDD
@SuppressWarnings("restriction")
class Operator extends AbstractStaticPolyMissingMemFun {

    private final IASTExpression       operatorExpr;
    private final OverloadableOperator operator;
    private final IType                injectedType;
    private final String               memberClassName;

    public Operator(final IASTExpression operatorExpr, final OverloadableOperator operator, final IType injectedType, final String memberClassName) {
        this.operatorExpr = operatorExpr;
        this.operator = operator;
        this.injectedType = injectedType;
        this.memberClassName = memberClassName;
    }

    @Override
    protected IASTExpression getUnderlyingExpression() {
        return operatorExpr;
    }

    @Override
    protected ICPPASTFunctionDeclarator createFunDecl() {
        final IASTName opName = nodeFactory.newName(getOperatorName().toCharArray());
        final ICPPASTFunctionDeclarator funDecl = nodeFactory.newFunctionDeclarator(opName);
        funDecl.setConst(isConstMemberFun());

        if (isBinaryExpression()) {
            addParamForRightHandSide(funDecl);
        }

        if (isArraySubscript()) {
            addParameterForArrayIndex(funDecl);
        }

        if (isUnaryExpression() && isPostfixOperator()) {
            addEmptyIntParameter(funDecl);
        } else if (shouldYieldByReference()) {
            addReferenceOperator(funDecl);
        } else if (shouldYieldByPointer()) {
            addPointerOperator(funDecl);
        }

        if (isFunCallOperator()) {
            addParamsForFunArgs(funDecl);
        }

        return funDecl;
    }

    @Override
    protected ICPPASTDeclSpecifier createReturnType(final ICPPASTFunctionDeclarator funDecl) {
        if (isComparisonOperator() || isLogicalOperator()) {
            return boolType();
        } else if (isArithmeticOperator() || isBitwiseOpterator() || isCompoundAssignOperator()) {
            return classType();
        } else {
            return lookupReturnType(funDecl);
        }
    }

    private ICPPASTDeclSpecifier lookupReturnType(final ICPPASTFunctionDeclarator funDecl) {
        return new ReturnTypeDeducer(funDecl, injectedType, memberClassName).determineReturnType(operatorExpr);
    }

    @Override
    protected IASTCompoundStatement createFunBody(final TestDoubleMemFunImplStrategy strategy, final ICPPASTFunctionDeclarator funDecl,
            final ICPPASTDeclSpecifier returnType, final CppStandard cppStd) {
        final IASTCompoundStatement funBody = createEmptyFunBody();
        strategy.addCallVectorRegistration(funBody, funDecl, false);
        addReturnStmtToFunBody(cppStd, returnType, funBody, funDecl);
        return funBody;
    }

    private void addReturnStmtToFunBody(final CppStandard cppStd, final ICPPASTDeclSpecifier returnType, final IASTCompoundStatement funBody,
            final ICPPASTFunctionDeclarator funDecl) {
        final ReturnStatementCreator creator = new ReturnStatementCreator(cppStd, memberClassName);
        funBody.addStatement(creator.createReturnStatement(funDecl, returnType));
    }

    private void addParameterForArrayIndex(final ICPPASTFunctionDeclarator funDecl) {
        final IASTInitializerClause argument = ((ICPPASTArraySubscriptExpression) operatorExpr).getArgument();
        final IASTExpression expr = CPPVisitor.findChildWithType(argument, IASTExpression.class).orElse(null);
        funDecl.addParameterDeclaration(ParamDeclCreator.createParameterFrom(expr, new HashMap<String, Boolean>()));
    }

    private boolean isArraySubscript() {
        return operatorExpr instanceof ICPPASTArraySubscriptExpression;
    }

    private boolean isConstMemberFun() {
        if (isCompoundAssignOperator()) {
            return false;
        }

        if (isLogicalOperator() || isBinaryExpression()) {
            return true;
        }

        switch (operator) {
        case NOT:
        case PLUS:
        case MINUS:
            return true;
        default:
            return false;
        }
    }

    private void addParamsForFunArgs(final ICPPASTFunctionDeclarator funDecl) {
        final FunctionCallParameterCollector ex = new FunctionCallParameterCollector((ICPPASTFunctionCallExpression) operatorExpr);

        for (final ICPPASTParameterDeclaration param : ex.getFunctionParameters()) {
            funDecl.addParameterDeclaration(param);
        }
    }

    private boolean isFunCallOperator() {
        return operator == OverloadableOperator.PAREN;
    }

    private static void addReferenceOperator(final ICPPASTFunctionDeclarator funDecl) {
        final boolean isRValueReference = false;
        funDecl.addPointerOperator(nodeFactory.newReferenceOperator(isRValueReference));
    }

    private static void addPointerOperator(final ICPPASTFunctionDeclarator funDecl) {
        funDecl.addPointerOperator(nodeFactory.newPointer());
    }

    private void addParamForRightHandSide(final ICPPASTFunctionDeclarator funDecl) {
        final IASTExpression binOp2Expr = ((ICPPASTBinaryExpression) operatorExpr).getOperand2();
        final Map<String, Boolean> nameHistory = new HashMap<>();

        if (binOp2Expr instanceof IASTIdExpression) {
            handleNamedParam(funDecl, binOp2Expr, nameHistory);
        } else if (binOp2Expr instanceof IASTLiteralExpression) {
            handleLiteralParam(funDecl, binOp2Expr, nameHistory);
        }
    }

    private static void handleLiteralParam(final ICPPASTFunctionDeclarator funDecl, final IASTExpression binOp2Expr,
            final Map<String, Boolean> nameHistory) {
        final IASTLiteralExpression litexpr = (IASTLiteralExpression) binOp2Expr;
        final ICPPASTParameterDeclaration literalParam = ParamDeclCreator.createParameter(litexpr, nameHistory);
        funDecl.addParameterDeclaration(literalParam);
    }

    private void handleNamedParam(final ICPPASTFunctionDeclarator funDecl, final IASTExpression binOp2Expr, final Map<String, Boolean> nameHistory) {
        final IASTIdExpression opExpr = (IASTIdExpression) binOp2Expr;

        if (resolvesToTemplateParameter(opExpr)) {
            addReferenceParamToThisClass(funDecl, nameHistory);
        } else {
            final ICPPASTParameterDeclaration param = ParamDeclCreator.createParameter(opExpr, nameHistory);
            funDecl.addParameterDeclaration(param);
        }
    }

    private void addReferenceParamToThisClass(final ICPPASTFunctionDeclarator funDecl, final Map<String, Boolean> nameHistory) {
        final ICPPASTParameterDeclaration referenceToThis = ParamDeclCreator.createReferenceParamFrom(memberClassName, nameHistory);
        funDecl.addParameterDeclaration(referenceToThis);
    }

    private String getOperatorName() {
        return String.valueOf(operator.toCharArray());
    }

    private boolean resolvesToTemplateParameter(final IASTIdExpression opExpr) {
        final IBinding binding = opExpr.getName().resolveBinding();

        if (binding instanceof ICPPVariable) {
            final IType type = ((ICPPVariable) binding).getType();
            final IType resolvedType = CxxAstUtils.unwindTypedef(type);
            return ASTUtil.isSameType(resolvedType, injectedType);
        }

        return false;
    }

    private boolean isBinaryExpression() {
        return operatorExpr instanceof ICPPASTBinaryExpression;
    }

    private ICPPASTBinaryExpression getBinaryExpression() {
        return CPPVisitor.findAncestorWithType(operatorExpr, ICPPASTBinaryExpression.class).orElse(null);
    }

    private boolean isUnaryExpression() {
        return operatorExpr instanceof ICPPASTUnaryExpression;
    }

    private ICPPASTUnaryExpression getUnaryExpression() {
        return CPPVisitor.findAncestorWithType(operatorExpr, ICPPASTUnaryExpression.class).orElse(null);
    }

    private static void addEmptyIntParameter(final ICPPASTFunctionDeclarator decl) {
        final ICPPASTDeclarator emptyDecl = nodeFactory.newDeclarator(nodeFactory.newName());
        final ICPPASTParameterDeclaration emptyIntParam = nodeFactory.newParameterDeclaration(getDefaultType(), emptyDecl);
        decl.addParameterDeclaration(emptyIntParam);
    }

    private static ICPPASTSimpleDeclSpecifier getDefaultType() {
        final ICPPASTSimpleDeclSpecifier intSpec = nodeFactory.newSimpleDeclSpecifier();
        intSpec.setType(IASTSimpleDeclSpecifier.t_int);
        return intSpec;
    }

    private boolean isPostfixOperator() {
        final IASTUnaryExpression uExpr = (IASTUnaryExpression) operatorExpr;
        return uExpr != null && (uExpr.getOperator() == IASTUnaryExpression.op_postFixDecr || uExpr
                .getOperator() == IASTUnaryExpression.op_postFixIncr);
    }

    private boolean shouldYieldByPointer() {
        return isUnaryExpression() && getUnaryExpression().getOperator() == IASTUnaryExpression.op_amper;
    }

    private boolean shouldYieldByReference() {
        int opType;

        if (isBinaryExpression()) {
            opType = getBinaryExpression().getOperator();
        } else if (isUnaryExpression()) {
            opType = getUnaryExpression().getOperator();
        } else {
            return false;
        }

        if (isCompoundAssignOperator()) {
            return true;
        }

        switch (opType) {
        case IASTBinaryExpression.op_assign:
        case IASTUnaryExpression.op_prefixDecr:
        case IASTUnaryExpression.op_prefixIncr:
            return true;
        default:
            return false;
        }
    }

    private static ICPPASTDeclSpecifier boolType() {
        final ICPPASTSimpleDeclSpecifier bool = nodeFactory.newSimpleDeclSpecifier();
        bool.setType(IASTSimpleDeclSpecifier.t_bool);
        return bool;
    }

    private ICPPASTDeclSpecifier classType() {
        return nodeFactory.newTypedefNameSpecifier(nodeFactory.newName(memberClassName.toCharArray()));
    }

    private boolean isCompoundAssignOperator() {
        switch (operator) {
        case PLUSASSIGN:
        case MINUSASSIGN:
        case STARASSIGN:
        case DIVASSIGN:
        case MODASSIGN:
        case BITORASSIGN:
        case AMPERASSIGN:
        case XORASSIGN:
        case SHIFTLASSIGN:
        case SHIFTRASSIGN:
            return true;
        default:
            return false;
        }
    }

    private boolean isBitwiseOpterator() {
        switch (operator) {
        case BITCOMPLEMENT:
        case AMPER:
        case BITOR:
        case XOR:
        case SHIFTL:
        case SHIFTR:
            return true;
        default:
            return false;
        }
    }

    private boolean isArithmeticOperator() {
        switch (operator) {
        case INCR:
        case DECR:
        case PLUS:
        case PLUSASSIGN:
        case MINUS:
        case MINUSASSIGN:
        case DIV:
        case DIVASSIGN:
        case MOD:
        case MODASSIGN:
        case STAR:
        case STARASSIGN:
            return true;
        default:
            return false;
        }
    }

    private boolean isLogicalOperator() {
        switch (operator) {
        case AND:
        case OR:
        case NOT:
            return true;
        default:
            return false;
        }
    }

    private boolean isComparisonOperator() {
        switch (operator) {
        case LT:
        case LTEQUAL:
        case EQUAL:
        case NOTEQUAL:
        case GT:
        case GTEQUAL:
            return true;
        default:
            return false;
        }
    }

    @Override
    public Collection<IASTInitializerClause> createDefaultArguments(final CppStandard cppStd, final LinkedEditModeStrategy linkedEditStrategy) {
        if (isUnaryExpression() && isPostfixOperator()) {
            return new ArrayList<>();
        }
        return super.createDefaultArguments(cppStd, linkedEditStrategy);
    }

    @Override
    public boolean isCallEquivalent(final ICPPASTFunctionDefinition function, final ConstStrategy strategy) {
        if (!function.getDeclarator().getName().toString().equals(getOperatorName())) {
            return false;
        }

        final ICPPASTParameterDeclaration[] generatedParams = createFunDecl().getParameters();
        final ICPPASTFunctionDeclarator funDecl = (ICPPASTFunctionDeclarator) function.getDeclarator();
        final ICPPASTParameterDeclaration[] realParams = funDecl.getParameters();
        return generatedParams.length == realParams.length;
    }

    @Override
    public boolean isStatic() {
        return false;
    }
}
