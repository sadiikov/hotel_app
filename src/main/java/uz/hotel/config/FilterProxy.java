package uz.hotel.config;

import org.springframework.security.web.context.AbstractSecurityWebApplicationInitializer;

public class FilterProxy extends AbstractSecurityWebApplicationInitializer {
    @Override
    protected boolean enableHttpSessionEventPublisher() {
        return true;
    }
}
