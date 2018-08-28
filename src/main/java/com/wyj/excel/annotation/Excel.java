package com.wyj.excel.annotation;

import java.lang.annotation.*;

/**
 * Created by wyj on 17-9-25.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Excel {

	/**
	 * 在导入Excel，建议一定要定义name属性，和Excel中的标题一样（Excel的标题名可以重复，重复的标题会根据order属性来进行匹配）
	 * 因为需要根据name来判断是否匹配当前列
	 * 如果name=""， 那么默认匹配任何列
	 */
	String name() default ""; // 标题

	int order() default Integer.MIN_VALUE; // 排序

	// 如果是空字符串"", 则该属性为null
	boolean emptyToNull() default true;
}
