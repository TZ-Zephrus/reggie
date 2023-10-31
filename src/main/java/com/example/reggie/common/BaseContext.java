package com.example.reggie.common;

//基于ThreadLocal封装工具类，用户保存和获取当前登录id
public class BaseContext {
    private static ThreadLocal<Long> threadLocal = new ThreadLocal<>();
    public static void setCurrentId(Long id) {
        threadLocal.set(id);
    }
    public static long getCurrentId() {
        return threadLocal.get();
    }
}
