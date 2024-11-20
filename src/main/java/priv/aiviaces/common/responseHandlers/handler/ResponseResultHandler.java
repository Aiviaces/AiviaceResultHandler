package priv.aiviaces.common.responseHandlers.handler;

import jakarta.annotation.PostConstruct;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.core.MethodParameter;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
import priv.aiviaces.common.responseHandlers.annotations.ForceEnable;
import priv.aiviaces.common.responseHandlers.annotations.ResponseResult;
import priv.aiviaces.common.responseHandlers.entitys.Result;
import priv.aiviaces.common.responseHandlers.errors.ResultReturnError;
import priv.aiviaces.common.responseHandlers.errors.ResultReturnWarn;

@Slf4j(topic = "response-handler-result")
@Component
@RestControllerAdvice
@ConditionalOnProperty(name = "response.handlers.result-handler", havingValue = "true")
@Primary
public class ResponseResultHandler implements ResponseBodyAdvice<Object> {

    @Value("${response.handlers.base-packages:}")
    private String basePackages;

    private String successMessage;
    private String warnMessage;
    private String errorMessage;

    @PostConstruct
    public void init() {
        if (this.basePackages != null) this.basePackages = this.basePackages.trim();
        else this.basePackages = "";
        if (basePackages.isEmpty()) {
            log.warn("ResponseResultHandler(统一结果处理器) inited, basePackages is empty, will handle all classes");
        } else log.debug("ResponseResultHandler(统一结果处理器) inited, basePackages: {}", basePackages);
    }

    private boolean isInBasePackages(Class<?> targetClass) {
        if (basePackages == null || basePackages.isEmpty()) {
            return true; // 如果没有指定basePackages，包含所有
        }
        String classPackage = targetClass.getPackageName();

        for (String basePackage : basePackages.split(",")) {
            if (classPackage.startsWith(basePackage)) {
                log.debug("[✓] package: {} satisfy the base: {}", classPackage, basePackage);
                return true;
            }
        }
        log.debug("[✗] package: {} not satisfy any base.", classPackage);
        return false;
    }

    @Override
    public boolean supports(
            @NonNull MethodParameter returnType,
            @NonNull Class<? extends HttpMessageConverter<?>> converterType
    ) {
        // 检查该控制器类是否在启用范围内
        if (!isInBasePackages(returnType.getContainingClass())) return false;

        // 获取控制器类
        Class<?> controllerClass = returnType.getContainingClass();

        // 检查控制器类上的注解
        if (controllerClass.isAnnotationPresent(ForceEnable.class)) {
            return true;
        }

        if (controllerClass.isAnnotationPresent(ResponseResult.class)
            && controllerClass.isAnnotationPresent(RestController.class)) {
            ResponseResult classAnnotation = controllerClass.getAnnotation(ResponseResult.class);
            this.successMessage = classAnnotation.successMessage();
            this.warnMessage = classAnnotation.warnMessage();
            this.errorMessage = classAnnotation.errorMessage();
            return classAnnotation.enabled();
        }

        // 检查方法上的注解
        if (returnType.getMethod() != null
            && returnType.getMethod().isAnnotationPresent(ResponseResult.class)) {
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
        if (body != null) {
            log.debug("返回结果类型： {}", body.getClass().getName());
        } else {
            log.warn("返回结果类型，读取到空值！");
        }

        if (body instanceof Result) {
            // 如果已经是Result类型，则直接返回
            return body;
        }

        Result<Object> result;

        // 设置返回值类型为Json
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        if (body instanceof String) {
            // 如果已经是字符串类型类型，由于自带的String消息转化器会再做处理，导致类型转换报错，直接转json返回
            try {
                String resultToJson = convertStringResultToJson(Result.success(this.successMessage, body));
                log.debug("===> 预处理纯字符串 ...");
                log.debug("===> 封装Result对象完成: " + resultToJson);
                return resultToJson;
            } catch (Exception e) {
                log.error("转化字符串返回值时产生异常", e);
                return convertStringResultToJson(Result.error(500, "系统内部错误"));
            }
        }
        result = Result.success(this.successMessage, body);
        log.debug("===> 封装Result对象完成: " + result);
        return result;
    }

    private String convertStringResultToJson(Result<Object> result) {
        return "{\n" +
               "  \"code\": " + result.getCode() + "," +
               "  \"message\": \"" + result.getMessage() + "\"," +
               "  \"data\": " +
               "\"" + result.getData().toString() + "\"" +
               "}";
    }

    @ExceptionHandler(ResultReturnWarn.class)
    @Order(Ordered.HIGHEST_PRECEDENCE)
    @ResponseBody
    public Result<Object> handleResultReturnWarn(ResultReturnWarn ex) {
        String message = ex.getMessage();
        log.warn("封装Result对象时主动抛出警告: {}", ex.getMessage());
        return Result.error(ex.getCode(), message != null ? message : this.warnMessage);
    }

    @ExceptionHandler(ResultReturnError.class)
    @Order(Ordered.HIGHEST_PRECEDENCE)
    @ResponseBody
    public Result<Object> handleResultReturnError(ResultReturnError ex) {
        String message = ex.getMessage();
        log.error("封装Result对象时主动抛出异常: {}", ex.getMessage(), ex);
        return Result.error(ex.getCode(), message != null ? message : this.errorMessage);
    }

    @ExceptionHandler(Exception.class)
    @Order(Ordered.HIGHEST_PRECEDENCE)
    @ResponseBody
    public Result<Object> handleOtherExceptions(Exception ex) {
        log.error("封装Result对象时发生错误: {}", ex.getMessage(), ex);
        return Result.error(500, "系统内部错误");
    }
}
