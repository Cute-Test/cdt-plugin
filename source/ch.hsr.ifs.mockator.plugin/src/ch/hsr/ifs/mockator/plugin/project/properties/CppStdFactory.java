package ch.hsr.ifs.mockator.plugin.project.properties;

import ch.hsr.ifs.mockator.plugin.base.MockatorException;


public class CppStdFactory<T> {

   private final Class<? extends T> cpp03Class;
   private final Class<? extends T> cpp11Class;

   private CppStdFactory(final Class<? extends T> cpp03Class, final Class<? extends T> cpp11Class) {
      this.cpp03Class = cpp03Class;
      this.cpp11Class = cpp11Class;
   }

   public static <T> CppStdFactory<T> from(final Class<? extends T> cpp03Class, final Class<? extends T> cpp11Class) {
      return new CppStdFactory<>(cpp03Class, cpp11Class);
   }

   public T getHandler(final CppStandard cppStd) {
      try {
         switch (cppStd) {
         case Cpp03Std:
            return cpp03Class.newInstance();
         case Cpp11Std:
            return cpp11Class.newInstance();
         default:
            throw new MockatorException("Unsupported C++ Standard");
         }
      }
      catch (final InstantiationException e) {
         throw new MockatorException(e);
      }
      catch (final IllegalAccessException e) {
         throw new MockatorException(e);
      }
   }
}
