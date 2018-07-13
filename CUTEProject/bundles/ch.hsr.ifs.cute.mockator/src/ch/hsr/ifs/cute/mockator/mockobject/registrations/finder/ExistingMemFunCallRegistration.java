package ch.hsr.ifs.cute.mockator.mockobject.registrations.finder;

import org.eclipse.cdt.core.dom.ast.IASTStatement;

import ch.hsr.ifs.cute.mockator.testdouble.entities.ExistingTestDoubleMemFun;
import ch.hsr.ifs.cute.mockator.testdouble.support.MemFunSignature;


public class ExistingMemFunCallRegistration extends MemFunSignature {

   private final ExistingTestDoubleMemFun memFun;
   private final IASTStatement            registrationStmt;

   public ExistingMemFunCallRegistration(final ExistingTestDoubleMemFun memFun) {
      this(memFun, null, memFun.getFunctionSignature());
   }

   public ExistingMemFunCallRegistration(final String registredMemFunSig) {
      this(null, null, registredMemFunSig);
   }

   protected ExistingMemFunCallRegistration(final ExistingTestDoubleMemFun memFun, final IASTStatement registrationStmt, final String funSignature) {
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
