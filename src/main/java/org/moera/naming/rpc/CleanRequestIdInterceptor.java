package org.moera.naming.rpc;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
