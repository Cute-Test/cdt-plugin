package ch.hsr.ifs.mockator.plugin.mockobject.registrations;

import static ch.hsr.ifs.mockator.plugin.MockatorConstants.CALL;
import static ch.hsr.ifs.mockator.plugin.MockatorConstants.MOCKATOR_NS;
import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.array;

import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;

import ch.hsr.ifs.mockator.plugin.project.properties.CppStandard;
import ch.hsr.ifs.mockator.plugin.refsupport.utils.AstUtil;

public class FreeFunCallRegistrationAdder extends AbstractFunCallRegistrationAdder {
  private final String callsVectorName;

  public FreeFunCallRegistrationAdder(ICPPASTFunctionDeclarator fun, CppStandard cppStd, String name) {
    super(fun, cppStd);
    this.callsVectorName = name;
  }

  @Override
  protected String getNameForCallsVector() {
    return callsVectorName;
  }

  @Override
  protected IASTExpression getPushBackOwner() {
    return createCallSequence();
  }

  @Override
  protected String getNameForCall() {
    return AstUtil.getQfName(array(MOCKATOR_NS, CALL));
  }
}
