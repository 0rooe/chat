package com.chatapp.frontend.filter;

import java.io.IOException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import javax.servlet.http.HttpSession;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@WebFilter(urlPatterns = "/*")
@Order(1)
@Slf4j
public class ResponseLoggingFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        log.info("【响应过滤器】初始化");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        // 记录请求信息
        String requestURI = httpRequest.getRequestURI();
        String method = httpRequest.getMethod();
        HttpSession session = httpRequest.getSession(false);
        String sessionId = session != null ? session.getId() : "无会话";
        
        // 记录请求头
        Map<String, String> headers = new HashMap<>();
        Enumeration<String> headerNames = httpRequest.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            headers.put(headerName, httpRequest.getHeader(headerName));
        }
        
        log.info("【请求开始】URI: {}, 方法: {}, 会话ID: {}, 头信息: {}", 
                requestURI, method, sessionId, headers);
        
        // 创建响应包装器
        ResponseWrapper responseWrapper = new ResponseWrapper(httpResponse);
        
        long startTime = System.currentTimeMillis();
        
        try {
            // 继续过滤链
            chain.doFilter(request, responseWrapper);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            
            // 记录响应信息
            int status = responseWrapper.getStatus();
            Collection<String> responseHeaderNames = responseWrapper.getHeaderNames();
            Map<String, String> responseHeaders = new HashMap<>();
            for (String headerName : responseHeaderNames) {
                responseHeaders.put(headerName, responseWrapper.getHeader(headerName));
            }
            
            log.info("【响应完成】URI: {}, 方法: {}, 状态: {}, 耗时: {}ms, 头信息: {}", 
                    requestURI, method, status, duration, responseHeaders);
        }
    }

    @Override
    public void destroy() {
        log.info("【响应过滤器】销毁");
    }
    
    // 响应包装器
    private static class ResponseWrapper extends HttpServletResponseWrapper {
        
        public ResponseWrapper(HttpServletResponse response) {
            super(response);
        }
    }
} 