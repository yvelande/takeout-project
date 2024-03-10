package com.example.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.reggie.Entity.Category;
import com.example.reggie.Entity.Dish;
import com.example.reggie.Entity.DishFlavor;
import com.example.reggie.common.CustomException;
import com.example.reggie.common.Result;
import com.example.reggie.dto.DishDto;
import com.example.reggie.dto.SetmealDto;
import com.example.reggie.service.CategoryService;
import com.example.reggie.service.DishFlavorService;
import com.example.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/dish")
@Slf4j
public class DishController {
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private DishService dishService;

    @Autowired
    private DishFlavorService dishFlavorService;

    @PostMapping
    public Result<String> save(@RequestBody DishDto dishDto) {
        log.info("接收到的数据为：{}", dishDto);
        dishService.saveWithFlavors(dishDto);
        return Result.success("添加菜品成功");
    }

    @GetMapping("/page")
    public Result<Page> page(int page, int pageSize, String name) {
        //构造分页构造器对象
        Page<Dish> pageInfo = new Page<>(page, pageSize);
        //这个就是我们到时候返回的结果
        Page<DishDto> dishDtoPage = new Page<>(page, pageSize);
        //条件构造器
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        //添加条件
        queryWrapper.like(name != null, Dish::getName, name);
        queryWrapper.orderByDesc(Dish::getUpdateTime);
        //执行分页查询
        dishService.page(pageInfo, queryWrapper);

        //对象拷贝，这里只需要拷贝一下查询到的条目数
        //忽略掉records属性
        BeanUtils.copyProperties(pageInfo, dishDtoPage, "records");

        //获取原records数据
        List<Dish> records = pageInfo.getRecords();

        //遍历每一条records数据
        List<DishDto> list = records.stream().map((item) -> {
            DishDto dishDto = new DishDto();
            //将数据赋给dishDto对象
            BeanUtils.copyProperties(item, dishDto);
            //然后获取一下dish对象的category_id属性
            Long categoryId = item.getCategoryId();  //分类id
            //根据这个属性，获取到Category对象（这里需要用@Autowired注入一个CategoryService对象）
            Category category = categoryService.getById(item.getCategoryId());
            if (category != null) {
                dishDto.setCategoryName(category.getName());
            }
            //结果返回一个dishDto对象
            return dishDto;
            //并将dishDto对象封装成一个集合，作为我们的最终结果
        }).collect(Collectors.toList());

        dishDtoPage.setRecords(list);
        return Result.success(dishDtoPage);
    }

    @GetMapping("/{id}")
    public Result<DishDto> getByIdWithFlavor(@PathVariable Long id) {
        DishDto dishDto = dishService.getByIdWithFlavor(id);
        log.info("查询到的数据为：{}", dishDto);
        return Result.success(dishDto);
    }

    @PutMapping
    public Result<String> update(@RequestBody DishDto dishDto) {
        log.info("接收到的数据为：{}", dishDto);
        dishService.updateWithFlavor(dishDto);
        return Result.success("更新菜品成功");
    }

    /**
     * 根据dish获得分类下所有dish信息
     *
     * @param dish
     * @return
     */
    @GetMapping("/list")
    public Result<List<DishDto>> list(Dish dish) {
        //根据dish的分类找到分类所有的菜品
        log.info("获得的菜品分类信息如下{}", dish);
        LambdaQueryWrapper<Dish> lqw = new LambdaQueryWrapper<>();
        lqw.eq(dish.getCategoryId() != null, Dish::getCategoryId, dish.getCategoryId());
        lqw.eq(Dish::getStatus, 1);
        lqw.orderByDesc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
        List<Dish> list = dishService.list(lqw);
        log.info("查询到的菜品信息list:{}", list);
        //设置categoryName
        List<DishDto> dishDtoList = list.stream().map((item) -> {
            DishDto dishDto = new DishDto();
            Category category = categoryService.getById(item.getCategoryId());
            if (category != null)
                dishDto.setCategoryName(category.getName());
            //获取flavors进行设置
            LambdaQueryWrapper<DishFlavor> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(DishFlavor::getDishId, dish.getId());
            List<DishFlavor> dishFlavorList = dishFlavorService.list(lambdaQueryWrapper);
            dishDto.setFlavors(dishFlavorList);
            return dishDto;
        }).collect(Collectors.toList());
        return Result.success(dishDtoList);

    }

//    @PostMapping("/status/{status}")
//    public Result<String> status(@PathVariable Integer status, Long ids) {
//        log.info("status:{},ids:{}", status, ids);
//        Dish dish = dishService.getById(ids);
//        if (dish != null) {
//            dish.setStatus(status);
//            dishService.updateById(dish);
//            return Result.success("售卖状态修改成功");
//        }
//        return Result.error("系统繁忙，请稍后再试");
//    }


    @PostMapping("/status/{status}")
    public Result<String> status(@PathVariable Integer status, @RequestParam List<Long> ids){
        log.info("status:{},ids:{}", status, ids);
        LambdaUpdateWrapper<Dish> lqw=new  LambdaUpdateWrapper<>();
        lqw.in(ids!=null,Dish::getId,ids);
        lqw.set(Dish::getStatus,status);
        dishService.update(lqw);
        return Result.success("批量操作成功");
}

    @DeleteMapping
    public Result<String> delete(@RequestParam List<Long> ids) {
        LambdaQueryWrapper<Dish>lqw=new LambdaQueryWrapper<>();
        lqw.in(Dish::getId,ids);
        lqw.eq(Dish::getStatus,1);
        int count=dishService.count(lqw);
        if(count>0)
            throw new CustomException("删除列表中存在启售状态商品，无法删除");
        dishService.removeByIds(ids);
        return Result.success("删除成功");
    }
}
