package com.example.reggie.interceptor;

import com.alibaba.fastjson.JSONObject;
import com.example.reggie.common.BaseContext;
import com.example.reggie.common.R;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.plugin.Interceptor;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.pattern.PathPattern;

@Slf4j
@Component
public class LoginCheckInterceptor implements HandlerInterceptor {
    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
//        1. 获取本次请求的URL
        String requestURL = request.getRequestURI();
        log.info("Interceptor拦截到请求:{}", requestURL);
        String[] urls = new String[]{    //这些路径不需要处理
                "/employee/login",
                "/employee/logout",
                "/backend/**",
                "/front/**",
                "/common/**",
                "/user/sendMsg",
                "/user/login"
        };
//        2. 判断本次请求是否需要处理
        boolean check = check(urls, requestURL);
//        3. 如果不需要处理，则直接放行
        if (check) {
            log.info("Interceptor本次请求不需要处理");
            return true;
        }
//        4. 判断登录状态，如果已登录，则直接放行
        if (request.getSession().getAttribute("employee") != null) {
            log.info("Interceptor已登录，用户id为:{}", request.getSession().getAttribute("employee"));
            return true;
        }
        //4-2、判断登录状态，如果已登录，则直接放行
        if(request.getSession().getAttribute("user") != null){
            log.info("用户已登录，用户id为：{}",request.getSession().getAttribute("user"));

            return true;
        }

//        5. 如果未登录则返回未登录结果
        log.info("Interceptor未登录");
        R<String> result = R.error("NOTLOGIN");
        String json = JSONObject.toJSONString(result);
        response.getWriter().write(json);
        return false;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
    }

    public boolean check(String[] urls, String requestURL) {
        for (String url : urls) {
            boolean match = PATH_MATCHER.match(url, requestURL);
            if (match) return true;
        }
        return false;
    }
}
