package ch.hsr.ifs.mockator.plugin.testdouble;

import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.unorderedMap;

import java.util.Map;

import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;

import ch.hsr.ifs.mockator.plugin.base.dbc.Assert;


public enum PolymorphismKind {
   SubTypePoly, StaticPoly;

   private static final Map<String, PolymorphismKind> STRING_TO_ENUM = unorderedMap();

   static {
      for (PolymorphismKind standard : values()) {
         STRING_TO_ENUM.put(standard.toString(), standard);
      }
   }

   public static PolymorphismKind from(String name) {
      PolymorphismKind kind = STRING_TO_ENUM.get(name);
      Assert.notNull(kind, String.format("Unknown polymorphism name '%s'", name));
      return kind;
   }

   public static PolymorphismKind from(ICPPASTCompositeTypeSpecifier testDouble) {
      return hasAtLeastOneBaseClass(testDouble) ? SubTypePoly : StaticPoly;
   }

   private static boolean hasAtLeastOneBaseClass(ICPPASTCompositeTypeSpecifier testDouble) {
      return testDouble.getBaseSpecifiers().length > 0;
   }
}
