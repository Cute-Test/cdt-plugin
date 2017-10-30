package ch.hsr.ifs.mockator.plugin.refsupport.functions.params;

import org.eclipse.cdt.core.dom.ast.IArrayType;
import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.IQualifierType;
import org.eclipse.cdt.core.dom.ast.IType;


class ConstArrayVerifier {

   private final IType type;

   public ConstArrayVerifier(IType type) {
      this.type = type;
   }

   public boolean isConstCharArray() {
      if (!(type instanceof IArrayType)) return false;

      IType arrayType = ((IArrayType) type).getType();

      if (!(arrayType instanceof IQualifierType)) return false;

      IQualifierType qualifierType = (IQualifierType) arrayType;
      IType qualifiedType = qualifierType.getType();

      if (qualifierType.isConst() && qualifiedType instanceof IBasicType) {
         IBasicType basicType = (IBasicType) qualifiedType;
         return basicType.getKind() == IBasicType.Kind.eChar;
      }

      return false;
   }
}
