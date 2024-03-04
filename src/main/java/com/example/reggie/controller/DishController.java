package com.example.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.reggie.Entity.Dish;
import com.example.reggie.common.Result;
import com.example.reggie.dto.DishDto;
import com.example.reggie.service.DishFlavorService;
import com.example.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/dish")
@Slf4j
public class DishController {

    @Autowired
    private DishService dishService;

    @Autowired
    private DishFlavorService dishFlavorService;

    @PostMapping
    public Result<String> save(@RequestBody DishDto dishDto){
        log.info("接收到的数据为：{}", dishDto);
        dishService.saveWithFlavors(dishDto);
        return Result.success("添加菜品成功");
    }

    @GetMapping("/page")
    public Result<Page>page(int page, int pageSize, String name){
        Page<Dish>pageInfo=new Page<>(page,pageSize);
        LambdaQueryWrapper<Dish>lqw=new LambdaQueryWrapper<>();
        lqw.like(!(name==null||"".equals(name)),Dish::getName,name);
        dishService.page(pageInfo,lqw);
        return Result.success(pageInfo);
    }
}
