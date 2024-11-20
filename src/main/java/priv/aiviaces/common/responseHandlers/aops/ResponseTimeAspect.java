package priv.aiviaces.common.responseHandlers.aops;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import priv.aiviaces.common.responseHandlers.annotations.ResponseTimeRecord;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

@Slf4j(topic = "response-handler-timer")
@Aspect
@Component
public class ResponseTimeAspect {

    @PostConstruct
    public void init() {
        log.debug("ResponseTimeAspect(统一接口计时器) enabled.");
    }

    @Around("(@within(priv.aiviaces.common.responseHandlers.annotations.ResponseTimeRecord) || @annotation(priv.aiviaces.common.responseHandlers.annotations.ResponseTimeRecord))")
    public Object calculateResponseTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();

        // 获取 ResponseTimeRecord 注解的实例
        ResponseTimeRecord annotation = getResponseTimeRecordAnnotation(joinPoint);

        // 如果注解不存在，则使用默认值
        String msgPrefix = annotation != null ? annotation.msgPrefix() : "Response time for";
        String msgSource = annotation != null ? annotation.msgSource() : joinPoint.getSignature().getName();
        String msgLevel = annotation != null ? annotation.msgLevel().toUpperCase() : "INFO";

        Object result = joinPoint.proceed();

        long endTime = System.currentTimeMillis();
        long responseTime = endTime - startTime;

        switch (msgLevel) {
            case "INFO" -> log.info("{} {} >>>> {} ms <<<<", msgPrefix, msgSource, responseTime);
            case "DEBUG" -> log.debug("{} {} >>>> {} ms <<<<", msgPrefix, msgSource, responseTime);
            case "WARN" -> log.warn("{} {} >>>> {} ms <<<<", msgPrefix, msgSource, responseTime);
            case "ERROR" -> log.error("{} {} >>>> {} ms <<<<", msgPrefix, msgSource, responseTime);
        }

        return result;
    }

    private ResponseTimeRecord getResponseTimeRecordAnnotation(ProceedingJoinPoint joinPoint) {
        // 尝试从方法获取注解
        ResponseTimeRecord methodAnnotation = getAnnotationFromMethod(joinPoint);
        if (methodAnnotation != null) {
            return methodAnnotation;
        }
        // 如果方法没有注解，则尝试从类获取注解
        return getAnnotationFromClass(joinPoint);
    }

    private ResponseTimeRecord getAnnotationFromMethod(ProceedingJoinPoint joinPoint) {
        try {
            Method[] methods = joinPoint.getSignature().getDeclaringType().getMethods();
            for (Method method : methods) {
                if (method.getName().equals(joinPoint.getSignature().getName()) && method.getParameterCount() == joinPoint.getArgs().length) {
                    return method.getAnnotation(ResponseTimeRecord.class);
                }
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private ResponseTimeRecord getAnnotationFromClass(ProceedingJoinPoint joinPoint) {
        @SuppressWarnings(value = "unchecked")
        Annotation annotation = joinPoint.getSignature().getDeclaringType().getAnnotation(ResponseTimeRecord.class);
        return annotation instanceof ResponseTimeRecord ? (ResponseTimeRecord) annotation : null;
    }
}
