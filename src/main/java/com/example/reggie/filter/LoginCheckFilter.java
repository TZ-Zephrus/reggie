package com.example.reggie.filter;

import com.alibaba.fastjson.JSONObject;
import com.example.reggie.common.BaseContext;
import com.example.reggie.common.R;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import java.io.IOException;

@Slf4j
@WebFilter(filterName = "loginCheckFilter", urlPatterns = "/*")
public class LoginCheckFilter implements Filter {
    //spring提供的路径匹配器
    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        System.out.println("过滤器初始方法执行");
        Filter.super.init(filterConfig);
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        System.out.println("过滤器拦截请求");
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;
        log.info("过滤到请求:{}", httpServletRequest.getRequestURI());

//        1. 获取本次请求的URI
        String requestURL = httpServletRequest.getRequestURI();
        log.info("过滤到请求:{}", requestURL);
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
            log.info("本次请求不需要处理");
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }
//        4. 判断登录状态，如果已登录，则直接放行
        if ((httpServletRequest.getSession().getAttribute("employee") != null)) {
            log.info("用户已登录，id为:{}", httpServletRequest.getSession().getAttribute("employee"));

            //尝试
            long userId = (long) httpServletRequest.getSession().getAttribute("employee");
//            ThreadLocal<Long> threadLocal = new ThreadLocal<>();
//            threadLocal.set(userId);
            log.info("filter中id:{}",userId);
            BaseContext.setCurrentId(userId);

            //doFilter一定要最后写 别问我怎么知道的
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        //4-2、判断登录状态，如果已登录，则直接放行
        if(httpServletRequest.getSession().getAttribute("user") != null){
            log.info("用户已登录，用户id为：{}",httpServletRequest.getSession().getAttribute("user"));

            Long userId = (Long) httpServletRequest.getSession().getAttribute("user");
            BaseContext.setCurrentId(userId);

            filterChain.doFilter(httpServletRequest,httpServletResponse);
            return;
        }
//        5. 如果未登录则返回未登录结果,通过输出流来向客户端响应数据
        log.info("未登录");
        R<String> result = R.error("NOTLOGIN");
        String json = JSONObject.toJSONString(result);
        httpServletResponse.getWriter().write(json);
    }

    @Override
    public void destroy() {
        System.out.println("过滤器销毁方法执行");
        Filter.super.destroy();
    }


    public boolean check(String[] urls, String requestURL) {
        for (String url : urls) {
            boolean match = PATH_MATCHER.match(url, requestURL);
            if (match) return true;
        }
        return false;
    }
}
