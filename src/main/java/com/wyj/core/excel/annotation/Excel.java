package com.wyj.core.excel.annotation;

import java.lang.annotation.*;

/**
 * Created by wyj on 17-9-25.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Excel {

	String name();

	int order();
}
