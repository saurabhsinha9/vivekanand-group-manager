
package com.vivekanand.manager.audit;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Auditable {
    String action();

    String entity() default "";
}
