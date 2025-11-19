package io.hohichh.marketplace.order.config;

import feign.RequestInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.fileupload.RequestContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
public class FeignConfig {

    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

            if(attr != null) {
                HttpServletRequest request = attr.getRequest();
                String header = request.getHeader("Authorization");
                if(header != null) {
                    requestTemplate.header("Authorization", header);
                }
            }
        };
    }
}
