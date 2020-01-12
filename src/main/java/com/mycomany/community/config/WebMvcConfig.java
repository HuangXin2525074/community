package com.mycomany.community.config;


import com.mycomany.community.controller.interceptor.LoginRequiredInterceptor;
import com.mycomany.community.controller.interceptor.LoginTicketInterceptor;
import com.mycomany.community.controller.interceptor.MessageInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {


@Autowired
private LoginTicketInterceptor loginTicketInterceptor;

//@Autowired
//private LoginRequiredInterceptor loginRequiredInterceptor;

@Autowired
private MessageInterceptor messageInterceptor;


    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        registry.addInterceptor(loginTicketInterceptor).
                excludePathPatterns("/**/*.cs","/**/*.js","/**/*.png","/**/*.jpg","/**/*.jpeg");

//        registry.addInterceptor(loginRequiredInterceptor).
//                excludePathPatterns("/**/*.cs","/**/*.js","/**/*.png","/**/*.jpg","/**/*.jpeg");

        registry.addInterceptor(messageInterceptor).
                excludePathPatterns("/**/*.cs","/**/*.js","/**/*.png","/**/*.jpg","/**/*.jpeg");
    }
}
