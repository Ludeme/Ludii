package annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Alias to use for keyword in grammar instead of class.
 * @author cambolbro
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Alias
{
	public String alias() default "";
}
