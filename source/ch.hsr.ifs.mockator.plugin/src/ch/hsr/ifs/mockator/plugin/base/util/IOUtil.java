package ch.hsr.ifs.mockator.plugin.base.util;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;


public abstract class IOUtil {

   public static void safeClose(final Closeable toClose) {
      if (toClose != null) {
         try {
            toClose.close();
         }
         catch (final IOException e) {
            // Do nothing
         }
      }
   }

   public static InputStream stringToStream(final String text) {
      try {
         return new ByteArrayInputStream(text.getBytes("UTF-8"));
      }
      catch (final UnsupportedEncodingException e) {
         return new ByteArrayInputStream(text.getBytes());
      }
   }
}
