package ch.hsr.ifs.mockator.plugin.base.tuples;

enum Sentinel implements StringAppender {
   INSTANCE;

   @Override
   public void appendString(StringBuilder buffer, String separator) {
      // we don't need to do anything here because this is the end of the tuple
   }
}
