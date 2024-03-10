package com.example.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.reggie.Entity.Category;
import com.example.reggie.Entity.Dish;
import com.example.reggie.Entity.Setmeal;
import com.example.reggie.Entity.SetmealDish;
import com.example.reggie.common.Result;
import com.example.reggie.dto.DishDto;
import com.example.reggie.dto.SetmealDto;
import com.example.reggie.service.CategoryService;
import com.example.reggie.service.DishService;
import com.example.reggie.service.SetmealDishService;
import com.example.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@Slf4j
@RequestMapping("/setmeal")
public class SetmealController {

    @Autowired
    private SetmealService setmealService;
    @Autowired
    private SetmealDishService setmealDishService;

    @Autowired
    private DishService dishService;

    @Autowired
    private CategoryService categoryService;

    @PostMapping
    public Result<String> save(@RequestBody SetmealDto setmealDto) {
        log.info("套餐信息：{}", setmealDto);
        setmealService.saveWithDish(setmealDto);
        return Result.success("套餐添加成功");
    }

    @GetMapping("/page")
    public Result<Page> page(int page, int pageSize, String name) {
        Page<Setmeal>pageInfo=new Page<>(page,pageSize);
        LambdaQueryWrapper<Setmeal>lqw=new LambdaQueryWrapper<>();
        lqw.like(!(name==null||"".equals(name)),Setmeal::getName,name);
        lqw.orderByDesc(Setmeal::getUpdateTime);
        setmealService.page(pageInfo,lqw);
        Page<SetmealDto>setmealDtoPage=new Page<>(page,pageSize);
        BeanUtils.copyProperties(pageInfo,setmealDtoPage,"records");
        List<Setmeal>setmeals=pageInfo.getRecords();
        List<SetmealDto>setmealDtos=setmeals.stream().map((item)->{
            SetmealDto setmealDto=new SetmealDto();
            BeanUtils.copyProperties(item,setmealDto);
            Category category=categoryService.getById(item.getCategoryId());
            if(category!=null)
                setmealDto.setCategoryName(category.getName());
            return setmealDto;
        }).collect(Collectors.toList());
        setmealDtoPage.setRecords(setmealDtos);
        return Result.success(setmealDtoPage);
    }
    @DeleteMapping
    public Result<String> deleteByIds(@RequestParam List<Long> ids) {
        log.info("要删除的套餐id为：{}",ids);
        setmealService.removeWithDish(ids);
        return Result.success("删除成功");
    }

    @GetMapping("/list")
    public Result<List<Setmeal>> list(Setmeal setmeal) {
        LambdaQueryWrapper<Setmeal>lqw=new LambdaQueryWrapper<>();
        lqw.eq(setmeal.getCategoryId()!=null,Setmeal::getCategoryId,setmeal.getCategoryId());
        lqw.eq(setmeal.getStatus()!=null,Setmeal::getStatus,1);
        lqw.orderByDesc(Setmeal::getUpdateTime);
        List<Setmeal>list=setmealService.list(lqw);
        return Result.success(list);
    }

    @GetMapping("/dish/{id}")
    public Result<List<DishDto>> showSetmealDish(@PathVariable Long id) {
        LambdaQueryWrapper<SetmealDish>lqw=new LambdaQueryWrapper<SetmealDish>();
        lqw.eq(SetmealDish::getSetmealId,id);
        List<SetmealDish>records=setmealDishService.list(lqw);
        //获得所有setmealDish
        List<DishDto>list=records.stream().map((item)->{
          Long dishId=item.getDishId();
          Dish dish=dishService.getById(dishId);
          DishDto dishDto=new DishDto();
          //copy套餐的口味
            BeanUtils.copyProperties(item,dishDto);
          BeanUtils.copyProperties(dish,dishDto);
          return dishDto;
        }).collect(Collectors.toList());
        return Result.success(list);
    }

    @PostMapping("/status/{status}")
    public Result<String> status(@PathVariable String status, @RequestParam List<Long> ids) {
        LambdaUpdateWrapper<Setmeal> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.in(Setmeal::getId, ids);
        //直接set
        updateWrapper.set(Setmeal::getStatus, status);
        setmealService.update(updateWrapper);
        return Result.success("批量操作成功");
    }

    @GetMapping("/{id}")
    public Result<SetmealDto> getById(@PathVariable Long id) {
        Setmeal setmeal=setmealService.getById(id);
        SetmealDto setmealDto=new SetmealDto();
        BeanUtils.copyProperties(setmeal,setmealDto);
        LambdaQueryWrapper<SetmealDish>lqw=new LambdaQueryWrapper<>();
        lqw.eq(SetmealDish::getSetmealId,id);
        List<SetmealDish>setmealDishes=setmealDishService.list(lqw);
        setmealDto.setSetmealDishes(setmealDishes);
        return Result.success(setmealDto);
    }

    @PutMapping
    public Result<Setmeal> updateWithDish(@RequestBody SetmealDto setmealDto) {
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        Long setmealId = setmealDto.getId();
        //先根据id把setmealDish表中对应套餐的数据删了
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId,setmealId);
        setmealDishService.remove(queryWrapper);
        //然后在重新添加
        setmealDishes = setmealDishes.stream().map((item) ->{
            item.setSetmealId(setmealId);
            return item;
        }).collect(Collectors.toList());
        //更新套餐数据
        setmealService.updateById(setmealDto);
        //更新套餐对应菜品数据
        setmealDishService.saveBatch(setmealDishes);
        return Result.success(setmealDto);
    }
}
