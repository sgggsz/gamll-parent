package com.atguigu.gmall.common.cache;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * GmallCache
 *
 * @Author: 郭思钊
 * @CreateTime: 2020-06-10
 * @Description:
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface GmallCache {

    /**
     * 缓存key的前缀
     * @return
     */
    String prefix() default "cache";
}
