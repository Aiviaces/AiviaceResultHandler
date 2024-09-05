package priv.aiviaces.common.responseHandlers.handler;

import jakarta.annotation.PostConstruct;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.MethodParameter;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
import priv.aiviaces.common.responseHandlers.annotations.ResponseResult;
import priv.aiviaces.common.responseHandlers.entitys.Result;
import priv.aiviaces.common.responseHandlers.errors.ResultReturnError;
import priv.aiviaces.common.responseHandlers.errors.ResultReturnWarn;

@Slf4j
@Component
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ResponseResultHandler implements ResponseBodyAdvice<Object> {

    @Value("${response.handlers.base-packages:}")
    private String basePackages;

    private String successMessage;
    private String warnMessage;
    private String errorMessage;


    @PostConstruct
    public void init() {
        this.basePackages = this.basePackages.trim();
        log.debug("ResponseResultHandler(统一结果处理器) init, basePackages: {}", basePackages);
    }

    private boolean isInBasePackages(Class<?> targetClass) {
        if (basePackages == null || basePackages.isEmpty()) {
            return true; // 如果没有指定basePackages，包含所有
        }
        String classPackage = targetClass.getPackageName();
        for (String basePackage : basePackages.split(",")) {
            if (classPackage.startsWith(basePackage)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean supports(
            @NonNull MethodParameter returnType,
            @NonNull Class<? extends HttpMessageConverter<?>> converterType
    ) {
        // 检查该控制器类是否在启用范围内
        if (!isInBasePackages(returnType.getContainingClass())) return false;


        // 如果方法没有注解，则检查控制器类上的注解
        Class<?> controllerClass = returnType.getContainingClass();
        if (controllerClass.isAnnotationPresent(ResponseResult.class)) {
            ResponseResult classAnnotation = controllerClass.getAnnotation(ResponseResult.class);
            this.successMessage = classAnnotation.successMessage();
            this.warnMessage = classAnnotation.warnMessage();
            this.errorMessage = classAnnotation.errorMessage();
            return classAnnotation.enabled();
        }

        // 检查方法上的注解
        if (returnType.getMethod() != null && returnType.getMethod().isAnnotationPresent(ResponseResult.class)) {
            ResponseResult methodAnnotation = returnType.getMethod().getAnnotation(ResponseResult.class);
            this.successMessage = methodAnnotation.successMessage();
            this.warnMessage = methodAnnotation.warnMessage();
            this.errorMessage = methodAnnotation.errorMessage();
            return methodAnnotation.enabled();
        }

        // 如果都没有注解，或者注解设置为不启用，则不支持
        return false;
    }

    @Override
    public Object beforeBodyWrite(Object body,
                                  @NonNull MethodParameter returnType,
                                  @NonNull MediaType selectedContentType,
                                  @NonNull Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  @NonNull ServerHttpRequest request,
                                  @NonNull ServerHttpResponse response
    ) {

        if (body instanceof Result) {
            // 如果已经是Result类型，则直接返回
            return body;
        }

        Object result = null;

        if (body instanceof String) {
            // 如果已经是字符串类型类型，由于自带的String消息转化器会再做处理，导致类型转换报错，直接转json返回
            try {
                String resultToJson = convertStringResultToJson(Result.success(this.successMessage, body));
                response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
                log.debug("===> 封装Result对象完成 (调用toString结果)：" + resultToJson);
                return resultToJson;
            } catch (Exception e) {
                log.error("转化字符串返回值时产生异常", e);
                return convertStringResultToJson(Result.error(500, "系统内部错误"));
            }
        }
        result = Result.success(this.successMessage, body);
        log.debug("===> 封装Result对象完成 (调用toString结果)：" + result);
        return result;
    }

    private String convertStringResultToJson(Result<Object> result) {
        return "{\n" +
                "  \"code\": " + result.getCode() + ",\n" +
                "  \"message\": \"" + result.getMessage() + "\",\n" +
                "  \"data\": " +
                "\"" + result.getData().toString() + "\"" +
                "\n}";
    }

    @ExceptionHandler(ResultReturnWarn.class)
    public Result<Object> handleResultReturnWarn(ResultReturnWarn ex) {
        String message = ex.getMessage();
        log.warn("封装Result对象时主动抛出警告: {}", ex.getMessage());
        return Result.error(ex.getCode(), message != null ? message : this.warnMessage);
    }

    @ExceptionHandler(ResultReturnError.class)
    public Result<Object> handleResultReturnError(ResultReturnError ex) {
        String message = ex.getMessage();
        log.error("封装Result对象时主动抛出异常: {}", ex.getMessage(), ex);
        return Result.error(ex.getCode(), message != null ? message : this.warnMessage);
    }

    @ExceptionHandler(Exception.class)
    public Result<Object> handleOtherExceptions(Exception ex) {
        log.error("封装Result对象时发生错误: {}", ex.getMessage(), ex);
        return Result.error(500, "系统内部错误");
    }
}
