package com.example.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.reggie.Entity.User;
import com.example.reggie.common.Result;
import com.example.reggie.service.UserService;
import com.example.reggie.utils.MailUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.mail.MessagingException;
import javax.servlet.http.HttpSession;
import java.security.GeneralSecurityException;
import java.util.Map;

@RestController
@Slf4j
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;
    @PostMapping("/sendMsg")
    public Result<String> sendMsg(@RequestBody User user, HttpSession session) throws MessagingException, GeneralSecurityException {
        String phone=user.getPhone();
        if(!phone.isEmpty()){
            String code= MailUtils.achieveCode();
            log.info("生成的验证码是",code);
            MailUtils.sendTestMail(phone,code);
            //把code存入session方便验证
            session.setAttribute(phone,code);
            return Result.success("验证码发送成功");
        }
        return Result.error("验证码发送失败");
    }

    @PostMapping("/login")
    public Result<User> login(@RequestBody Map map, HttpSession session){
        log.info(map.toString());
        //获取邮箱
        String phone = map.get("phone").toString();
//获取验证码
        String code = map.get("code").toString();
        String codeInSession= session.getAttribute(phone).toString();
        if(code!=null&&code.equals(codeInSession)){
            LambdaQueryWrapper<User>lqw=new LambdaQueryWrapper<>();
            lqw.eq(User::getPhone,phone);
            User user=userService.getOne(lqw);
            if(user==null){
                user=new User();
                user.setPhone(phone);
                user.setName("用户" + codeInSession);
                userService.save(user);
            }
            session.setAttribute("user",user.getId());
            return Result.success(user);
        }
        return Result.error("登录失败");
    }
}