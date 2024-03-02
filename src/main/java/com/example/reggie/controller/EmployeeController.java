package com.example.reggie.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.reggie.Entity.Employee;
import com.example.reggie.common.Result;
import com.example.reggie.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/employee")
public class EmployeeController {
    @Autowired
    private EmployeeService employeeService;
    @PostMapping("/login")
    public Result<Employee> login(HttpServletRequest httpServletRequest, @RequestBody Employee employee){

        //对用户密码用md5加密
        String password=employee.getPassword();
        password=DigestUtils.md5DigestAsHex(password.getBytes());
        //根据用户姓名查询用户
        LambdaQueryWrapper<Employee>lqw=new LambdaQueryWrapper<>();
        lqw.eq(Employee::getUsername,employee.getUsername());
        Employee emp=employeeService.getOne(lqw);
        //如果查询结果为空登录失败
        if(emp==null)
            return Result.error("登录失败");
        //密码不对登录失败
       if(!emp.getPassword().equals(password))
           return Result.error("登录失败");
            //用户状态为0禁止使用
        if(emp.getStatus()==0)
            return Result.error("该用户已被禁用");
            //session中保存用户id
        httpServletRequest.getSession().setAttribute("employee",emp.getId());
            //最终返回正确用户结果
        return Result.success(emp);
    }

    @PostMapping("/logout")
    public Result<String> logout(HttpServletRequest httpServletRequest){
        httpServletRequest.getSession().removeAttribute("employee");
        return Result.success("退出成功");
    }
}
