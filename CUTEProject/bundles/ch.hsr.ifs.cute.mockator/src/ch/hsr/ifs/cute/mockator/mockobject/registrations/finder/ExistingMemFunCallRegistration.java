package ch.hsr.ifs.cute.mockator.mockobject.registrations.finder;

import org.eclipse.cdt.core.dom.ast.IASTStatement;

import ch.hsr.ifs.iltis.cpp.core.resources.info.IStringifyable;

import ch.hsr.ifs.cute.mockator.testdouble.entities.ExistingTestDoubleMemFun;
import ch.hsr.ifs.cute.mockator.testdouble.support.MemFunSignature;


public class ExistingMemFunCallRegistration extends MemFunSignature implements IStringifyable<ExistingMemFunCallRegistration> {

   private final ExistingTestDoubleMemFun memFun;
   private final IASTStatement            registrationStmt;

   /**
    * Default constructor for IStringifyable
    */
   public ExistingMemFunCallRegistration() {
      registrationStmt = null;
      memFun = null;
   }
   
   public ExistingMemFunCallRegistration(final String funSignature) {
      this(null,null,funSignature);
   }

   public ExistingMemFunCallRegistration(final ExistingTestDoubleMemFun memFun) {
      this(memFun, null, memFun.getFunctionSignature());
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

   @Override
   public ExistingMemFunCallRegistration unstringify(String string) {
      setFunSignature(string);
      return this;
   }

   @Override
   public String stringify() {
      return toString();
   }

}
