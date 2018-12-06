package com.rate_controller.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RateControl {

    /**
     * 可以自行选择个你自己拼接的加一个后缀随意
     */
    String keySuffix() default "";

    /**
     * 限流的窗口大小
     * 设置是秒为单位
     * @return
     */
    int time();

    /**
     * 在特定的观察时间窗口内访问次数
     * @return
     */
    int count();
}

