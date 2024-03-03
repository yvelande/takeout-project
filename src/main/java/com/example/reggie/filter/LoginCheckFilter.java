package com.example.reggie.filter;

//注解

import com.alibaba.fastjson.JSON;
import com.example.reggie.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.LogRecord;

@Slf4j
@WebFilter(filterName = "loginCheckFilter",urlPatterns = "/*")
public class LoginCheckFilter implements Filter {

    //路径转换定义
    public static final AntPathMatcher PATH_MATCHER=new AntPathMatcher();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

        //强转
        HttpServletRequest httpServletRequest=(HttpServletRequest) servletRequest;
        HttpServletResponse httpServletResponse=(HttpServletResponse) servletResponse;

        //1.获取本次请求的URI
        String requestURI=httpServletRequest.getRequestURI();
        log.info("拦截到请求：{}",requestURI);

        //定义不需要处理的请求
        String[]urls=new String[]{
                "/employee/login",
                "/employee/logout",
                "/backend/**",
                "/front/**"
        };

        //2.判断本次请求是否需要处理
        boolean check=check(urls,requestURI);

        //3.如果不需要处理，则直接放行
        if(check) {
            log.info("本次请求：{}，不需要处理",requestURI);
            filterChain.doFilter(httpServletRequest, httpServletResponse);
        }
        //4.判断登录状态，如果已登录，则直接放行
        if(httpServletRequest.getSession().getAttribute("employee")!=null)
        {
            log.info("用户已登录，id为：{}",httpServletRequest.getSession().getAttribute("employee"));
            filterChain.doFilter(httpServletRequest,httpServletResponse);
        }
        //5.如果未登录则返回未登录结果,通过输出流方式向客户端页面响应数据
        log.info("用户未登录");
        log.info("用户id{}",httpServletRequest.getSession().getAttribute("employee"));
        httpServletResponse.getWriter().write(JSON.toJSONString(Result.error("NOTLOGIN")));

    }
    //定义check的函数
    public boolean check(String[]urls,String requestURI){
        for(String url:urls){
            if(PATH_MATCHER.match(url,requestURI))
                return true;
        }
        return false;
    }
    }





