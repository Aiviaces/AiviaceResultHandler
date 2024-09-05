package priv.aiviaces.common.responseHandlers.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义注解，用于标记需要统一记录接口响应时间的注解
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ResponseTimeRecord {
    // 消息前缀
    String msgPrefix() default "Response time for ";
    // 消息源，如果为空则自动指定为方法名
    String msgSource() default "";
    // 消息级别，默认为INFO
    String msgLevel() default "INFO";
}
