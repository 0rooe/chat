package com.chatapp.frontend.exception;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ModelAndView handleException(Exception ex, HttpServletRequest request, HttpServletResponse response) {
        log.error("【全局异常】未处理的异常: {}", ex.getMessage(), ex);
        log.error("【全局异常】请求URL: {}", request.getRequestURL());
        log.error("【全局异常】请求方法: {}", request.getMethod());
        log.error("【全局异常】请求参数: {}", request.getParameterMap());
        
        ModelAndView mav = new ModelAndView();
        mav.addObject("errorMessage", "系统发生错误，请稍后再试");
        mav.addObject("errorDetails", ex.getMessage());
        mav.addObject("timestamp", System.currentTimeMillis());
        mav.addObject("path", request.getRequestURI());
        mav.setViewName("error");
        
        return mav;
    }
    
    @ExceptionHandler(FeignException.class)
    public ModelAndView handleFeignException(FeignException ex, HttpServletRequest request) {
        log.error("【Feign异常】调用微服务异常: {}", ex.getMessage(), ex);
        log.error("【Feign异常】请求URL: {}", request.getRequestURL());
        log.error("【Feign异常】请求方法: {}", request.getMethod());
        log.error("【Feign异常】请求参数: {}", request.getParameterMap());
        
        if (ex.responseBody().isPresent()) {
            String responseBody = new String(ex.responseBody().get().array());
            log.error("【Feign异常】服务响应: {}", responseBody);
        }
        
        ModelAndView mav = new ModelAndView();
        mav.addObject("errorMessage", "调用服务出错，请稍后再试");
        mav.addObject("errorDetails", ex.getMessage());
        mav.addObject("timestamp", System.currentTimeMillis());
        mav.addObject("path", request.getRequestURI());
        mav.addObject("status", ex.status());
        mav.setViewName("error");
        
        return mav;
    }
} 