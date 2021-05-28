package annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Anonymous parameter in the grammar, i.e. parameter name is not shown or
 * bracketed.
 * @author cambolbro
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Anon 
{
	// ...
}
