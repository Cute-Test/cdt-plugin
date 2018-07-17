package ch.hsr.ifs.cute.core.headers;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD) 
public @interface CuteVersionNumber {

   int major() default 0;

   int minor() default 0;

   int patch() default 0;
}
