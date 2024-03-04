package com.example.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.reggie.Entity.Category;
import com.example.reggie.Entity.Dish;
import com.example.reggie.Entity.Setmeal;
import com.example.reggie.common.CustomException;
import com.example.reggie.mapper.CategoryMapper;
import com.example.reggie.service.CategoryService;
import com.example.reggie.service.DishService;
import com.example.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService{
    @Autowired
    public DishService dishService;

    @Autowired
    public SetmealService setmealService;
    @Override
    public void remove(Long id) {
        LambdaQueryWrapper<Dish>dishLambdaQueryWrapper=new LambdaQueryWrapper<>();
        dishLambdaQueryWrapper.eq(Dish::getCategoryId,id);
        int count1=dishService.count(dishLambdaQueryWrapper);
        log.info("dish查询条件，查询到的条目数为：{}",count1);
        if(count1>0){
            throw new CustomException("当前分类下关联了菜品，不能删除");
        }
        LambdaQueryWrapper<Setmeal>setmealLambdaQueryWrapper=new LambdaQueryWrapper<>();
        setmealLambdaQueryWrapper.eq(Setmeal::getCategoryId,id);
        int count2=setmealService.count(setmealLambdaQueryWrapper);
        log.info("setmeal查询条件，查询到的条目数为：{}",count2);
        if(count2>0){
            throw new CustomException("当前分类下关联了菜品，不能删除");
        }
        super.removeById(id);
    }
}
