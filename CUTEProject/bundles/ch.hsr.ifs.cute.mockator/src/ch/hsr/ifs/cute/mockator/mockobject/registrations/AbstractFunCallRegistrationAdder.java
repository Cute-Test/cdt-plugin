package ch.hsr.ifs.cute.mockator.mockobject.registrations;

import static ch.hsr.ifs.cute.mockator.MockatorConstants.PUSH_BACK;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.ASTNodeFactoryFactory;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpressionStatement;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFieldReference;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNodeFactory;

import ch.hsr.ifs.iltis.core.resources.StringUtil;

import ch.hsr.ifs.cute.mockator.project.properties.CppStandard;
import ch.hsr.ifs.cute.mockator.refsupport.functions.FunctionSignatureFormatter;


// Adds the following statement to a function body for registering function calls:
//
// void foo1(int const& i) const {
// C++03:
// allCalls[mock_id].push_back(call("foo1(int const&) const", i));
// C++11:
// allCalls[mock_id].push_back(call{"foo1(int const&) const", i});
// }
//
// Note: If it is a static function, index 0 is used in the subscript.
abstract class AbstractFunCallRegistrationAdder {

    protected static final ICPPNodeFactory      nodeFactory = ASTNodeFactoryFactory.getDefaultCPPNodeFactory();
    private final String                        signature;
    private final ICPPASTParameterDeclaration[] funParameters;
    private final CppStandard                   cppStd;

    public AbstractFunCallRegistrationAdder(final ICPPASTFunctionDeclarator function, final CppStandard cppStd) {
        this.cppStd = cppStd;
        funParameters = function.getParameters();
        signature = new FunctionSignatureFormatter(function).getFunctionSignature();
    }

    public void addRegistrationTo(final IASTCompoundStatement functionBody) {
        functionBody.addStatement(createFunCallRegistration());
    }

    protected IASTExpressionStatement createFunCallRegistration() {
        final IASTName pushBack = nodeFactory.newName(PUSH_BACK.toCharArray());
        final ICPPASTFieldReference fieldRef = nodeFactory.newFieldReference(pushBack, getPushBackOwner());
        final Collection<IASTInitializerClause> args = createRegistrationArgs();
        final IASTInitializerClause[] callParam = new IASTInitializerClause[] { cppStd.getCtorCall(getNameForCall(), args) };
        final IASTFunctionCallExpression funExpr = nodeFactory.newFunctionCallExpression(fieldRef, callParam);
        return nodeFactory.newExpressionStatement(funExpr);
    }

    protected abstract String getNameForCall();

    protected abstract String getNameForCallsVector();

    protected abstract IASTExpression getPushBackOwner();

    protected IASTIdExpression createCallSequence() {
        return nodeFactory.newIdExpression(nodeFactory.newName(getNameForCallsVector().toCharArray()));
    }

    protected Collection<IASTInitializerClause> createRegistrationArgs() {
        final List<IASTInitializerClause> params = new ArrayList<>();
        params.add(createArgument(StringUtil.quote(signature)));

        for (final ICPPASTParameterDeclaration param : funParameters) {
            final IASTName funParamName = param.getDeclarator().getName();
            // e.g. operator ++(int)
            if (funParamName.toString().isEmpty()) {
                continue;
            }
            params.add(createArgument(funParamName.toString()));
        }
        return params;
    }

    private static IASTIdExpression createArgument(final String arg) {
        return nodeFactory.newIdExpression(nodeFactory.newName(arg.toCharArray()));
    }
}
