package com.akademi.finsight.auth.ratelimiter.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

public class CachedBodyFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request,
                         ServletResponse response,
                         FilterChain chain
    ) throws IOException, ServletException {
        chain.doFilter(
                wrapRequest(request),
                response
        );


    }

    private ServletRequest wrapRequest(ServletRequest request) throws IOException {
        if (request instanceof HttpServletRequest httpRequest){
            return new CachedBodyHttpServletRequest(httpRequest);
        }
        return request;
    }
}