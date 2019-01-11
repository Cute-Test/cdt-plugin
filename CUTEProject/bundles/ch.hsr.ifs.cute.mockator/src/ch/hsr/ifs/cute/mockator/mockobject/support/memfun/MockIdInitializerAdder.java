package ch.hsr.ifs.cute.mockator.mockobject.support.memfun;

import java.util.function.Consumer;

import org.eclipse.cdt.core.dom.ast.ASTNodeFactoryFactory;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorChainInitializer;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNodeFactory;

import ch.hsr.ifs.cute.mockator.MockatorConstants;
import ch.hsr.ifs.cute.mockator.project.properties.CppStandard;


// Mock() : mock_id(reserveNextCallId(allCalls)) { }
public class MockIdInitializerAdder implements Consumer<ICPPASTFunctionDefinition> {

    private static final String          NEXT_CALL_ID_RESERVATION = "reserveNextCallId";
    private static final ICPPNodeFactory nodeFactory              = ASTNodeFactoryFactory.getDefaultCPPNodeFactory();
    private final String                 allCallsVectorName;
    private final CppStandard            cppStd;

    public MockIdInitializerAdder(final String allCallsVectorName, final CppStandard cppStd) {
        this.allCallsVectorName = allCallsVectorName;
        this.cppStd = cppStd;
    }

    @Override
    public void accept(final ICPPASTFunctionDefinition ctor) {
        if (alreadyHasMockIdInitializer(ctor)) {
            return;
        }

        final IASTInitializerClause nextCallIdReservation = createNextCallReservation();
        final ICPPASTConstructorChainInitializer ctorInitializer = getMockIdInitializer(nextCallIdReservation);
        ctor.addMemberInitializer(ctorInitializer);
    }

    private IASTInitializerClause createNextCallReservation() {
        final IASTName nextCallIdReservation = nodeFactory.newName(NEXT_CALL_ID_RESERVATION.toCharArray());
        final IASTInitializerClause[] params = new IASTInitializerClause[1];
        params[0] = nodeFactory.newIdExpression(nodeFactory.newName(allCallsVectorName.toCharArray()));
        return nodeFactory.newFunctionCallExpression(nodeFactory.newIdExpression(nextCallIdReservation), params);
    }

    private static boolean alreadyHasMockIdInitializer(final ICPPASTFunctionDefinition ctor) {
        for (final ICPPASTConstructorChainInitializer initializer : ctor.getMemberInitializers()) {
            if (initializer.getMemberInitializerId().toString().equals(MockatorConstants.MOCK_ID)) {
                return true;
            }
        }
        return false;
    }

    private ICPPASTConstructorChainInitializer getMockIdInitializer(final IASTInitializerClause nextCallIdReservation) {
        final IASTName mockId = nodeFactory.newName(MockatorConstants.MOCK_ID.toCharArray());
        final IASTInitializer init = cppStd.getInitializer(nextCallIdReservation);
        return nodeFactory.newConstructorChainInitializer(mockId, init);
    }
}
