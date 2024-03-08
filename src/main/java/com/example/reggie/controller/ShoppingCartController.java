package com.example.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.reggie.Entity.ShoppingCart;
import com.example.reggie.common.BaseContext;
import com.example.reggie.common.Result;
import com.example.reggie.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("/shoppingCart")
public class ShoppingCartController {
    @Autowired
    private ShoppingCartService shoppingCartService;

    @PostMapping("/add")
    public Result<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart){
        log.info("购物车添加信息：{}",shoppingCart);
        //设置UserId
        shoppingCart.setUserId(BaseContext.getCurrentId());
        //判断是dish还是setmeal
        Long id=shoppingCart.getDishId();
        LambdaQueryWrapper<ShoppingCart>lqw=new LambdaQueryWrapper<>();
        if(id==null){
            lqw.eq(ShoppingCart::getDishId,id);
        }else{
            lqw.eq(ShoppingCart::getSetmealId,shoppingCart.getSetmealId());
        }
        //一个菜就一个条目
        ShoppingCart currentShopping=shoppingCartService.getOne(lqw);
        if(currentShopping!=null){
            Integer number=currentShopping.getNumber();
            currentShopping.setNumber(number+1);
            shoppingCartService.updateById(currentShopping);
        }else{
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartService.save(shoppingCart);
            currentShopping=shoppingCart;
        }
        return Result.success(currentShopping);
    }

    @GetMapping("/list")
    public Result<List<ShoppingCart>> list() {
        LambdaQueryWrapper<ShoppingCart>lqw=new LambdaQueryWrapper<>();
        Long id=BaseContext.getCurrentId();
        lqw.eq(ShoppingCart::getUserId,id);
        return Result.success(shoppingCartService.list(lqw));

    }

    @DeleteMapping("/clean")
    public Result<String> clean() {
        //条件构造器
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        //获取当前用户id
        Long userId = BaseContext.getCurrentId();
        queryWrapper.eq(userId != null, ShoppingCart::getUserId, userId);
        //删除当前用户id的所有购物车数据
        shoppingCartService.remove(queryWrapper);
        return Result.success("成功清空购物车");
    }

}
