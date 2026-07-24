package tribefire.extension.process.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Assigns a ProcessItem type to the process definition registered under {@link #value()}. */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface ManageProcessWith {
	String globalId() default "";
	String value();
}
