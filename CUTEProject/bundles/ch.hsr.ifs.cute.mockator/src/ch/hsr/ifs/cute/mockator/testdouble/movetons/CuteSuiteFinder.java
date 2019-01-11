package ch.hsr.ifs.cute.mockator.testdouble.movetons;

import static ch.hsr.ifs.cute.mockator.MockatorConstants.CUTE_NS;
import static ch.hsr.ifs.cute.mockator.MockatorConstants.CUTE_SUITE;
import static ch.hsr.ifs.iltis.core.core.collections.CollectionUtil.array;

import java.util.Optional;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamedTypeSpecifier;

import ch.hsr.ifs.iltis.cpp.core.ast.ASTUtil;
import ch.hsr.ifs.iltis.cpp.core.wrappers.CPPVisitor;

import ch.hsr.ifs.cute.mockator.MockatorConstants;
import ch.hsr.ifs.cute.mockator.refsupport.utils.NodeContainer;


// Parts taken from CUTE
class CuteSuiteFinder extends ASTVisitor {

    private static final String             FQ_CUTE_SUITE = ASTUtil.getQfName(array(CUTE_NS, CUTE_SUITE));
    private final NodeContainer<IASTName>   relatedSuiteName;
    private final ICPPASTFunctionDefinition testFunction;

    {
        shouldVisitNames = true;
    }

    public CuteSuiteFinder(final ICPPASTFunctionDefinition testFunction) {
        this.testFunction = testFunction;
        relatedSuiteName = new NodeContainer<>();
    }

    public Optional<String> getCuteSuiteName() {
        return relatedSuiteName.getNode().map(IASTName::toString);
    }

    @Override
    public int visit(final IASTName name) {
        if (!isCuiteSuiteName(name)) {
            return PROCESS_CONTINUE;
        }

        final IBinding suiteBinding = name.resolveBinding();

        for (final IASTName refName : name.getTranslationUnit().getReferences(suiteBinding)) {
            if (ASTUtil.isPushBack(refName) && matchesTestFunction(refName)) {
                relatedSuiteName.setNode(name);
                return PROCESS_ABORT;
            }
        }

        return PROCESS_CONTINUE;
    }

    private static boolean isCuiteSuiteName(final IASTName name) {
        final IASTSimpleDeclaration simpleDecl = CPPVisitor.findAncestorWithType(name, IASTSimpleDeclaration.class).orElse(null);

        if (simpleDecl == null) {
            return false;
        }

        final IASTDeclSpecifier declSpecifier = simpleDecl.getDeclSpecifier();

        if (!(declSpecifier instanceof ICPPASTNamedTypeSpecifier)) {
            return false;
        }

        final ICPPASTNamedTypeSpecifier namedSpec = (ICPPASTNamedTypeSpecifier) declSpecifier;
        final IASTName typeName = namedSpec.getName();

        if (typeName.toString().equals(FQ_CUTE_SUITE)) {
            return true;
        }

        final IBinding binding = typeName.resolveBinding();

        if (!(binding instanceof ITypedef)) {
            return false;
        }

        final ITypedef typeDef = (ITypedef) binding;
        return typeDef.getName().equals(MockatorConstants.CUTE_SUITE) && typeDef.getOwner().getName().equals(MockatorConstants.CUTE_NS);
    }

    private boolean matchesTestFunction(final IASTName referencingName) {
        return getRegisteredFunctionName(referencingName).map((registeredFunName) -> registeredFunName.equals(testFunction.getDeclarator().getName()
                .toString())).orElse(false);
    }

    private static Optional<String> getRegisteredFunctionName(final IASTName name) {
        final IASTFunctionCallExpression funcCall = CPPVisitor.findAncestorWithType(name, IASTFunctionCallExpression.class).orElse(null);
        final IASTInitializerClause[] arguments = funcCall.getArguments();

        if (isFunctionPushBack(arguments)) {
            return getFunctionName(arguments);
        }
        if (isSimpleMemberFunctionPushBack(arguments)) {
            return getSimpleMemFunName(arguments);
        }
        if (isFunctorPushBack(arguments)) {
            return getFunctorName(arguments);
        }

        return Optional.empty();
    }

    private static Optional<String> getFunctorName(final IASTInitializerClause[] arguments) {
        if (isFunctorPushBack(arguments)) {
            final ICPPASTFunctionCallExpression funcCall = (ICPPASTFunctionCallExpression) arguments[0];
            final IASTIdExpression idExp = (IASTIdExpression) funcCall.getFunctionNameExpression();
            return Optional.of(idExp.getName().toString());
        }
        return Optional.empty();
    }

    private static boolean isFunctorPushBack(final IASTInitializerClause[] arguments) {
        if (!(arguments.length == 1 && arguments[0] instanceof ICPPASTFunctionCallExpression)) {
            return false;
        }

        final ICPPASTFunctionCallExpression funcCall = (ICPPASTFunctionCallExpression) arguments[0];
        return funcCall.getArguments().length == 0;
    }

    private static Optional<String> getSimpleMemFunName(final IASTInitializerClause[] arguments) {
        if (!isSimpleMemberFunctionPushBack(arguments)) {
            return Optional.empty();
        }

        final ICPPASTFunctionCallExpression funCall = (ICPPASTFunctionCallExpression) arguments[0];

        if (hasFunCallTwoArgsWithUnaryExpr(funCall)) {
            final IASTUnaryExpression unExp = (IASTUnaryExpression) funCall.getArguments()[0];

            if (unExp.getOperand() instanceof IASTIdExpression) {
                final IASTIdExpression idExp = (IASTIdExpression) unExp.getOperand();
                return Optional.of(idExp.getName().toString());
            }
        }

        return Optional.empty();
    }

    private static boolean hasFunCallTwoArgsWithUnaryExpr(final ICPPASTFunctionCallExpression funCall) {
        return funCall.getArguments().length == 2 && funCall.getArguments()[0] instanceof IASTUnaryExpression;
    }

    private static Optional<String> getFunctionName(final IASTInitializerClause[] arguments) {
        if (!isFunctionPushBack(arguments)) {
            return Optional.empty();
        }

        final ICPPASTFunctionCallExpression funCall = (ICPPASTFunctionCallExpression) arguments[0];

        if (hasFunCallTwoArgsWithUnaryExpr(funCall)) {
            final IASTUnaryExpression unExp = (IASTUnaryExpression) funCall.getArguments()[0];

            if (unExp.getOperand() instanceof IASTUnaryExpression && ((IASTUnaryExpression) unExp.getOperand())
                    .getOperand() instanceof IASTIdExpression) {
                final IASTIdExpression idExp = (IASTIdExpression) ((IASTUnaryExpression) unExp.getOperand()).getOperand();
                return Optional.of(idExp.getName().toString());
            }
        }
        return Optional.empty();
    }

    private static boolean isFunctionPushBack(final IASTInitializerClause[] arguments) {
        return isFunctionPushBackWithName(arguments, ASTUtil.getQfName(array(MockatorConstants.CUTE_NS, "test")));
    }

    private static boolean isSimpleMemberFunctionPushBack(final IASTInitializerClause[] arguments) {
        return isFunctionPushBackWithName(arguments, ASTUtil.getQfName(array(MockatorConstants.CUTE_NS, "makeSimpleMemberFunctionTest")));
    }

    private static boolean isFunctionPushBackWithName(final IASTInitializerClause[] arguments, final String funName) {
        if (arguments.length == 1 && arguments[0] instanceof ICPPASTFunctionCallExpression) {
            final ICPPASTFunctionCallExpression funcCall = (ICPPASTFunctionCallExpression) arguments[0];
            return isSameFunctionName(funcCall, funName);
        }

        return false;
    }

    private static boolean isSameFunctionName(final ICPPASTFunctionCallExpression funCall, final String funName) {
        final IASTExpression funNameExpr = funCall.getFunctionNameExpression();
        if (!(funNameExpr instanceof IASTIdExpression)) {
            return false;
        }
        return ((IASTIdExpression) funNameExpr).getName().toString().startsWith(funName);
    }
}
