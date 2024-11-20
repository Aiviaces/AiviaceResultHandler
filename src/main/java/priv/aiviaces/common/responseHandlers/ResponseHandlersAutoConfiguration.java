package priv.aiviaces.common.responseHandlers;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import priv.aiviaces.common.responseHandlers.aops.ResponseTimeAspect;
import priv.aiviaces.common.responseHandlers.handler.ResponseResultHandler;

@Configuration
@Slf4j(topic = "response-handlers-autoconfig")
@EnableAspectJAutoProxy
@AutoConfiguration
@Import({
        ResponseResultHandler.class,
        ResponseTimeAspect.class
})
public class ResponseHandlersAutoConfiguration {
    @PostConstruct
    public void init() {
        log.debug("ResponseHandlerAutoConfiguration(统一响应处理器自动配置)");
        log.debug("ResponseHandlers loaded success.");
    }
}
