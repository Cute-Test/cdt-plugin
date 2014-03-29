package ch.hsr.ifs.mockator.plugin.mockobject.registrations.finder;

import org.eclipse.cdt.core.dom.ast.IASTStatement;

import ch.hsr.ifs.mockator.plugin.testdouble.entities.ExistingTestDoubleMemFun;
import ch.hsr.ifs.mockator.plugin.testdouble.support.MemFunSignature;

public class ExistingMemFunCallRegistration extends MemFunSignature {
  private final ExistingTestDoubleMemFun memFun;
  private final IASTStatement registrationStmt;

  public ExistingMemFunCallRegistration(ExistingTestDoubleMemFun memFun) {
    this(memFun, null, memFun.getFunctionSignature());
  }

  public ExistingMemFunCallRegistration(String registredMemFunSig) {
    this(null, null, registredMemFunSig);
  }

  protected ExistingMemFunCallRegistration(ExistingTestDoubleMemFun memFun,
      IASTStatement registrationStmt, String funSignature) {
    super(funSignature);
    this.memFun = memFun;
    this.registrationStmt = registrationStmt;
  }

  public ExistingTestDoubleMemFun getExistingMemFun() {
    return memFun;
  }

  public IASTStatement getRegistrationStmt() {
    return registrationStmt;
  }
}
