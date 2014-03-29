package ch.hsr.ifs.mockator.plugin.mockobject.support.memfun;

import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorChainInitializer;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPNodeFactory;

import ch.hsr.ifs.mockator.plugin.MockatorConstants;
import ch.hsr.ifs.mockator.plugin.base.functional.F1V;
import ch.hsr.ifs.mockator.plugin.project.properties.CppStandard;

// Mock() : mock_id(reserveNextCallId(allCalls)) { }
@SuppressWarnings("restriction")
public class MockIdInitializerAdder implements F1V<ICPPASTFunctionDefinition> {
  private static final String NEXT_CALL_ID_RESERVATION = "reserveNextCallId";
  private static final CPPNodeFactory nodeFactory = CPPNodeFactory.getDefault();
  private final String allCallsVectorName;
  private final CppStandard cppStd;

  public MockIdInitializerAdder(String allCallsVectorName, CppStandard cppStd) {
    this.allCallsVectorName = allCallsVectorName;
    this.cppStd = cppStd;
  }

  @Override
  public void apply(ICPPASTFunctionDefinition ctor) {
    if (alreadyHasMockIdInitializer(ctor))
      return;

    IASTInitializerClause nextCallIdReservation = createNextCallReservation();
    ICPPASTConstructorChainInitializer ctorInitializer =
        getMockIdInitializer(nextCallIdReservation);
    ctor.addMemberInitializer(ctorInitializer);
  }

  private IASTInitializerClause createNextCallReservation() {
    IASTName nextCallIdReservation = nodeFactory.newName(NEXT_CALL_ID_RESERVATION.toCharArray());
    IASTInitializerClause[] params = new IASTInitializerClause[1];
    params[0] = nodeFactory.newIdExpression(nodeFactory.newName(allCallsVectorName.toCharArray()));
    return nodeFactory.newFunctionCallExpression(
        nodeFactory.newIdExpression(nextCallIdReservation), params);
  }

  private static boolean alreadyHasMockIdInitializer(ICPPASTFunctionDefinition ctor) {
    for (ICPPASTConstructorChainInitializer initializer : ctor.getMemberInitializers()) {
      if (initializer.getMemberInitializerId().toString().equals(MockatorConstants.MOCK_ID))
        return true;
    }
    return false;
  }

  private ICPPASTConstructorChainInitializer getMockIdInitializer(
      IASTInitializerClause nextCallIdReservation) {
    IASTName mockId = nodeFactory.newName(MockatorConstants.MOCK_ID.toCharArray());
    IASTInitializer init = cppStd.getInitializer(nextCallIdReservation);
    return nodeFactory.newConstructorChainInitializer(mockId, init);
  }
}
