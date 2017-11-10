package ch.hsr.ifs.mockator.tests.base.dbc;

import org.junit.Test;

import ch.hsr.ifs.iltis.core.exception.ILTISException;
import ch.hsr.ifs.mockator.plugin.base.dbc.Assert;


public class AssertTest {

   @Test(expected = ILTISException.class)
   public void throwsIfNull() {
      Assert.notNull(null, "");
   }

   @Test
   public void noThrowWhenNotNull() {
      Assert.notNull(Integer.valueOf(42), "");
   }

   @Test(expected = ILTISException.class)
   public void throwsIfNotTrue() {
      Assert.isTrue(false, "");
   }

   @Test
   public void noThrowWhenTrue() {
      Assert.isTrue(true, "");
   }

   @Test(expected = ILTISException.class)
   public void throwsIfNotFalse() {
      Assert.isFalse(true, "");
   }

   @Test
   public void noThrowWhenFalse() {
      Assert.isFalse(false, "");
   }

   @Test(expected = ILTISException.class)
   public void throwsIfObjOfClassType() {
      final Integer i = 42;
      Assert.notInstanceOf(i, Number.class, "");
   }

   @Test
   public void noThrowWhenNotOfClassType() {
      final String s = "Mockator";
      Assert.notInstanceOf(s, Number.class, "");
   }

   @Test(expected = ILTISException.class)
   public void throwsIfObjNotOfClassType() {
      final String s = "Mockator";
      Assert.instanceOf(s, Number.class, "");
   }

   @Test
   public void noThrowWhenOfClassType() {
      final Integer i = 42;
      Assert.instanceOf(i, Number.class, "");
   }
}
