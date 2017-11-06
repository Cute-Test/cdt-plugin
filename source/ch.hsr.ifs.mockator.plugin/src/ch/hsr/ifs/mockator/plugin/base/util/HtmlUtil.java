package ch.hsr.ifs.mockator.plugin.base.util;

import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.unorderedMap;

import java.util.Map;

import ch.hsr.ifs.mockator.plugin.base.misc.Default;


public abstract class HtmlUtil {

   private static final Map<String, String> LOOKUP_TABLE = unorderedMap();

   static {
      LOOKUP_TABLE.put(">", "&gt;");
      LOOKUP_TABLE.put("<", "&lt;");
      LOOKUP_TABLE.put("&", "&amp;");
      LOOKUP_TABLE.put("\"", "&quot;");
      LOOKUP_TABLE.put("'", "&#039;");
      LOOKUP_TABLE.put("\\", "&#092;");
   }

   public final static String escapeHtml(final String html) {
      final StringBuilder escapedHtml = new StringBuilder(html.length() * 2);

      for (int i = 0; i < html.length(); ++i) {
         final char c = html.charAt(i);
         final String escaped = escapeSingleChar(c);
         escapedHtml.append(Default.whenNull(escaped, c));
      }

      return escapedHtml.toString();
   }

   private static String escapeSingleChar(final char c) {
      return LOOKUP_TABLE.get(String.valueOf(c));
   }
}
