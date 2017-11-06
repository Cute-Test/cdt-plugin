package ch.hsr.ifs.mockator.tests.base.misc;

import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.unorderedMap;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import ch.hsr.ifs.mockator.plugin.base.misc.CastHelper;


public class CastHelperTest {

   @Test
   public void instanceIsOfSameClass() {
      assertTrue(CastHelper.isInstanceOf(42, Integer.class));
   }

   @Test
   public void instanceIsOfDifferentClass() {
      assertFalse(CastHelper.isInstanceOf(42, String.class));
   }

   @Test
   public void unsecureCastYieldsCastedValue() {
      final Map<String, String> m = unorderedMap();
      final HashMap<String, String> hm = CastHelper.unsecureCast(m);
      assertTrue(hm != null);
   }
}
