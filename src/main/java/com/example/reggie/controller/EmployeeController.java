package com.example.reggie.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.reggie.Entity.Employee;
import com.example.reggie.common.Result;
import com.example.reggie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

@Slf4j
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

    /**
     *根据输入的员工信息添加员工 post
     * @param request
     * @param employee
     * @return
     */

    @PostMapping
    public Result<String>save(HttpServletRequest httpServletRequest,@RequestBody Employee employee) {
        //log记录新增的员工信息：{} 是实体tostring
        log.info("新增的员工信息：{}",employee.toString());
        //设置默认密码为123456，并采用MD5加密
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));
        //设置createTime和updateTime
        employee.setCreateTime(LocalDateTime.now());
        employee.setUpdateTime(LocalDateTime.now());
        //根据session来获取创建人的id
        Long empid= (Long) httpServletRequest.getSession().getAttribute("employee");
        log.info("操作人是员工：{}",empid);
        employee.setCreateUser(empid);
        employee.setUpdateUser(empid);
        //并设置
        //存入数据库
        employeeService.save(employee);
        //返回成功添加
        return Result.success("添加成功");
    }


    @GetMapping("/page")
    public Result<Page> page(int page, int pageSize, String name){
        //构造分页构造器 page有类型
        Page<Employee>pageInfo=new Page<>();
        //构造条件构造器 lqw也有类型
        LambdaQueryWrapper<Employee>lqw=new LambdaQueryWrapper<>();
        //添加过滤条件（当我们没有输入name时，就相当于查询所有了）
        lqw.like(!(name==null||"".equals(name)), Employee::getName,name);
        //并对查询的结果进行降序排序，根据更新时间
        lqw.orderByDesc(Employee::getUpdateTime);
        //执行查询
        employeeService.page(pageInfo,lqw);
        return Result.success(pageInfo);
    }

    @PutMapping
    public Result<String> update(HttpServletRequest httpServletRequest,@RequestBody Employee employee){
        employee.setUpdateUser((long)httpServletRequest.getSession().getAttribute("employee"));
        employee.setUpdateTime(LocalDateTime.now());
        Long id=Thread.currentThread().getId();
        log.info("update的线程id为：{}", id);
        employeeService.updateById(employee);
        return Result.success("修改成功");
    }

    @GetMapping("/{id}")
    public Result<Employee>getById(@PathVariable long id){
        log.info("根据id查询员工信息..");
        Employee emp=employeeService.getById(id);
        if(emp!=null)
            return Result.success(emp);
        return Result.error("未查询到信息");

    }
}
