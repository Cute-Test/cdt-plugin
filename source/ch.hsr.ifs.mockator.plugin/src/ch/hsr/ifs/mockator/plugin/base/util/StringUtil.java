package ch.hsr.ifs.mockator.plugin.base.util;

import static ch.hsr.ifs.mockator.plugin.MockatorConstants.DOUBLE_QUOTE;

import java.util.ArrayList;
import java.util.Map;

import ch.hsr.ifs.iltis.core.exception.ILTISException;


public abstract class StringUtil {

   public static String unquote(String s) {
      if (s != null && startsAndEndsWithQuotes(s)) {
         s = s.substring(1, s.length() - 1);
      }
      return s;
   }

   private static boolean startsAndEndsWithQuotes(final String s) {
      return s.startsWith("\"") && s.endsWith("\"") || s.startsWith("'") && s.endsWith("'");
   }

   public static String quote(final String s) {
      return DOUBLE_QUOTE + s + DOUBLE_QUOTE;
   }

   public static String getBase36Value(final int n) {
      ILTISException.Unless.isTrue(n < 36, "Value not in range for base36 conversion");
      return Integer.toString(n, 36).toUpperCase();
   }

   public static String capitalize(final String word) {
      final char[] charArray = word.toCharArray();
      charArray[0] = Character.toUpperCase(charArray[0]);
      return String.valueOf(charArray);
   }

   public static String pythonFormat(final String format, final Map<String, ?> keyVals) {
      final StringBuilder replaced = new StringBuilder(format);
      final ArrayList<Object> repVals = new ArrayList<>();
      int pos = 1;

      for (final String k : keyVals.keySet()) {
         final String fKey = "%(" + k + ")";
         final String fPos = "%" + Integer.toString(pos) + "$";
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
