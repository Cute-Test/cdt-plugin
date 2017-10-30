package ch.hsr.ifs.mockator.plugin.base.util;

import static ch.hsr.ifs.mockator.plugin.MockatorConstants.DOUBLE_QUOTE;
import static ch.hsr.ifs.mockator.plugin.base.functional.HigherOrder.fold;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import ch.hsr.ifs.mockator.plugin.base.dbc.Assert;
import ch.hsr.ifs.mockator.plugin.base.functional.Injector;


public abstract class StringUtil {

   public static String join(Collection<String> elements) {
      return join(elements, "");
   }

   public static String join(Collection<String> elements, final String delimiter) {
      Injector<String, String> injector = new Injector<String, String>() {

         private final StringBuilder result = new StringBuilder();

         @Override
         public void apply(String item) {
            if (result.length() > 0) {
               result.append(delimiter);
            }

            result.append(item);
         }

         @Override
         public String yield() {
            return result.toString();
         }
      };
      fold(elements, injector);
      return injector.yield();
   }

   public static String unquote(String s) {
      if (s != null && startsAndEndsWithQuotes(s)) {
         s = s.substring(1, s.length() - 1);
      }
      return s;
   }

   private static boolean startsAndEndsWithQuotes(String s) {
      return (s.startsWith("\"") && s.endsWith("\"")) || (s.startsWith("'") && s.endsWith("'"));
   }

   public static String quote(String s) {
      return DOUBLE_QUOTE + s + DOUBLE_QUOTE;
   }

   public static String getBase36Value(int n) {
      Assert.isTrue(n < 36, "Value not in range for base36 conversion");
      return Integer.toString(n, 36).toUpperCase();
   }

   public static String capitalize(String word) {
      char[] charArray = word.toCharArray();
      charArray[0] = Character.toUpperCase(charArray[0]);
      return String.valueOf(charArray);
   }

   public static String pythonFormat(String format, Map<String, ?> keyVals) {
      StringBuilder replaced = new StringBuilder(format);
      ArrayList<Object> repVals = new ArrayList<Object>();
      int pos = 1;

      for (String k : keyVals.keySet()) {
         String fKey = "%(" + k + ")";
         String fPos = "%" + Integer.toString(pos) + "$";
         int idx = -1;

         while ((idx = replaced.indexOf(fKey, idx)) != -1) {
            replaced.replace(idx, idx + fKey.length(), fPos);
            idx += fPos.length();
         }

         repVals.add(keyVals.get(k));
         ++pos;
      }
      return String.format(replaced.toString(), repVals.toArray());
   }
}
