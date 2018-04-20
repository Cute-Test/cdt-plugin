package ch.hsr.ifs.mockator.plugin.testdouble;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;

import ch.hsr.ifs.iltis.core.core.exception.ILTISException;


public enum PolymorphismKind {
   SubTypePoly, StaticPoly;

   private static final Map<String, PolymorphismKind> STRING_TO_ENUM = new HashMap<>();

   static {
      for (final PolymorphismKind standard : values()) {
         STRING_TO_ENUM.put(standard.toString(), standard);
      }
   }

   public static PolymorphismKind from(final String name) {
      final PolymorphismKind kind = STRING_TO_ENUM.get(name);
      ILTISException.Unless.notNull(String.format("Unknown polymorphism name '%s'", name), kind);
      return kind;
   }

   public static PolymorphismKind from(final ICPPASTCompositeTypeSpecifier testDouble) {
      return hasAtLeastOneBaseClass(testDouble) ? SubTypePoly : StaticPoly;
   }

   private static boolean hasAtLeastOneBaseClass(final ICPPASTCompositeTypeSpecifier testDouble) {
      return testDouble.getBaseSpecifiers().length > 0;
   }
}
