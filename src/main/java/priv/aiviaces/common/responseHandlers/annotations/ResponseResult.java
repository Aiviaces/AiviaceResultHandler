package priv.aiviaces.common.responseHandlers.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义注解，用于标记需要统一响应处理的类或方法。
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ResponseResult {
    // 默认成功消息
    String successMessage() default "Operation succeeded.";

    // 默认警告消息
    String warnMessage() default "Operation warning.";

    // 默认错误消息
    String errorMessage() default "Operation failed.";

    // 是否启用统一响应处理
    boolean enabled() default true;


}
