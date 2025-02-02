package org.moera.naming.rpc;

import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class CleanRequestIdInterceptor implements HandlerInterceptor {

    @Inject
    private ExceptionsControllerAdvice exceptionsControllerAdvice;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        exceptionsControllerAdvice.setRequestId(null);

        return true;
    }

}
