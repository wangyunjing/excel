package com.wyj.excel.annotation;

import com.wyj.excel.convert.Converter;

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

    /**
     * 导入
     * 如果是空字符串"", 则该属性为null
     */
    boolean emptyToNull() default true;

    /**
     * 时间格式化(导入和导出共用)
     *
     * @return
     */
    String dateFormat() default "";

    /**
     * 导入转换器
     */
    Class<? extends Converter> importConverter() default Converter.class;

    /**
     * 导出转换器
     */
    Class<? extends Converter> exportConverter() default Converter.class;
}
