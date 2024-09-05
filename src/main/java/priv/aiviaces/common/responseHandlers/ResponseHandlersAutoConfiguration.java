package priv.aiviaces.common.responseHandlers;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import priv.aiviaces.common.responseHandlers.aops.ResponseTimeAspect;
import priv.aiviaces.common.responseHandlers.handler.ResponseResultHandler;

@Configuration
@Slf4j
@EnableAspectJAutoProxy
@AutoConfiguration
public class ResponseHandlersAutoConfiguration {

    @Value("${response.handlers.time-record:false}")
    private boolean enableTimeRecord;

    @Value("${response.handlers.result-handler:true}")
    private boolean enableResultHandler;

    @Bean
    @ConditionalOnProperty(name = "response.handlers.result-handler", havingValue = "true")
    public ResponseResultHandler responseResultHandler() {
        return new ResponseResultHandler();
    }

    @Bean
    @ConditionalOnProperty(name = "response.handlers.time-record", havingValue = "true")
    public ResponseTimeAspect responseTimeAspect() {
        return new ResponseTimeAspect();
    }

    @PostConstruct
    public void init() {
        log.debug("ResponseHandlerAutoConfiguration(统一响应处理器自动配置) loaded success.");
    }
}
