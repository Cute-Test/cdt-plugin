package ch.hsr.ifs.mockator.tests.base.util;

import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.list;
import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.unorderedMap;
import static org.junit.Assert.assertEquals;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.Test;

import ch.hsr.ifs.mockator.plugin.base.util.StringUtil;


public class StringUtilTest {

   @Test
   public void joinListOfStrings() {
      List<String> famousPainters = list();
      final Collection<String> elements = famousPainters;
      assertEquals("", elements.stream().collect(Collectors.joining(", ")));
      famousPainters = list("Andy Warhol", "Claude Monet", "Pablo Picasso", "Paul Gaguin", "Wassily Kandinsky");
      final Collection<String> elements1 = famousPainters;
      final String joinedPainters = elements1.stream().collect(Collectors.joining(", "));
      assertEquals("Andy Warhol, Claude Monet, Pablo Picasso, Paul Gaguin, Wassily Kandinsky", joinedPainters);
   }

   @Test
   public void quoteString() {
      final String toQuote = "Mockator";
      assertEquals("\"Mockator\"", StringUtil.quote(toQuote));
   }

   @Test
   public void unQuoteStringWithDoubleQuotes() {
      final String toUnquote = "\"Mockator\"";
      assertEquals("Mockator", StringUtil.unquote(toUnquote));
   }

   @Test
   public void unQuoteStringWithSingleQuotes() {
      final String toUnquote = "'Mockator'";
      assertEquals("Mockator", StringUtil.unquote(toUnquote));
   }

   @Test
   public void unQuoteStringWithNoQuotes() {
      final String toUnquote = "Mockator";
      assertEquals("Mockator", StringUtil.unquote(toUnquote));
   }

   @Test
   public void base36ValueRangeWithNumbersAndUppercaseLetters() {
      final String expected = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
      final StringBuilder actual = new StringBuilder();
      for (int i = 0; i < 36; i++) {
         actual.append(StringUtil.getBase36Value(i));
      }
      assertEquals(expected, actual.toString());
   }

   @Test
   public void capitalizeString() {
      final String toCapitalize = "mockator";
      assertEquals("Mockator", StringUtil.capitalize(toCapitalize));
   }

   @Test
   public void pythonFormatWithKeyValue() {
      final Map<String, Object> m = unorderedMap();
      m.put("key", "mockator");
      m.put("value", 123);
      assertEquals("The key 'mockator' has the value 123", StringUtil.pythonFormat("The key '%(key)s' has the value %(value)d", m));
   }
}
