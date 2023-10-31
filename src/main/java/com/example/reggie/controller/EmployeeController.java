package com.example.reggie.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.reggie.common.R;
import com.example.reggie.entity.Employee;
import com.example.reggie.service.EmployeeService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {
    @Autowired
    private EmployeeService employeeService;

    //登录功能     这里也引入了HttpServletRequest，目的是拿到请求之后，可以获取请求体中的Employee信息，从而获得其id，便于后续处理
    @PostMapping("/login")
    public R<Employee> login(HttpServletRequest httpServletRequest, @RequestBody Employee employee) {
        //1.将页面提交的密码password进行md5加密处理
        String password = employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        //2.根据页面提交的用户名username查询数据库
        //这里使用了mp的wrapper
        LambdaQueryWrapper<Employee> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(Employee::getUsername, employee.getUsername());
        Employee emp = employeeService.getOne(lambdaQueryWrapper);
        //3.如果没有查询到就返回登陆失败结果
        if (emp == null) {
            log.info("用户不存在，登陆失败");
            return R.error("用户不存在，登陆失败");
        }
        //4.密码比对，如果不一致则返回登陆失败状态
        if (!emp.getPassword().equals(password)) {
            log.info("密码错误，登陆失败");
            return R.error("密码错误");
        }
        //5.查看员工状态，如果为已禁用则返回员工已禁用
        if (emp.getStatus() != 1) {
            log.info("用户已禁用，登陆失败");
            return R.error("用户已禁用");
        }
        //6.登陆成功，将员工id存入Session并返回登陆成功结果
        httpServletRequest.getSession().setAttribute("employee", emp.getId());
        log.info("登陆成功！");
        return R.success(emp);
    }


    //退出
    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest httpServletRequest) {
        //1.退出session保存的id
        httpServletRequest.getSession().removeAttribute("employee");
        return R.success("退出成功");
    }

    //添加员工
    @PostMapping
    public R<String> save(HttpServletRequest httpServletRequest, @RequestBody Employee employee) {
        //设置初始密码并转换为md5保存
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));

//        employee.setCreateTime(LocalDateTime.now());
//        employee.setUpdateTime(LocalDateTime.now());
        //获取当前操作用户的id
        long id = (long) httpServletRequest.getSession().getAttribute("employee");
//        employee.setCreateUser(id);
//        employee.setUpdateUser(id);
        employeeService.save(employee);
        return R.success("用户添加成功");
    }

    //员工信息分页查询
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name) {
        log.info("page = {}, pageSize = {}, name = {}", page, pageSize, name);

        //构造分页构造器
        Page pageInfo = new Page<>(page, pageSize);
        //构造条件构造器
        LambdaQueryWrapper<Employee> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper
                .like(StringUtils.hasText(name), Employee::getName, name)    //过滤条件
                .orderByDesc(Employee::getUpdateTime);          //排序条件
        //执行查询
        //这里查完之后会自动把数据封装到pageInfo中
        employeeService.page(pageInfo, lambdaQueryWrapper);

        return R.success(pageInfo);
    }


    //根据id修改员工信息（包括修改状态）
    @PutMapping
    public R<String> update(HttpServletRequest httpServletRequest, @RequestBody Employee employee) {
        log.info(employee.toString());

        long empId = (long) httpServletRequest.getSession().getAttribute("employee");
//        employee.setUpdateTime(LocalDateTime.now());
//        employee.setUpdateUser(empId);
        employeeService.updateById(employee);
        return R.success("修改成功");
    }

    //根据id查询员工信息
    @GetMapping("/{id}")
    public R<Employee> getById(@PathVariable long id) {
        log.info("根据id查询员工信息");
        Employee byId = employeeService.getById(id);
        if (byId != null) {
            return R.success(byId);
        }
        return R.error("没有查询到对应员工信息");
    }
}
