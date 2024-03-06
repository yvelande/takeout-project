package com.example.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.reggie.Entity.Category;
import com.example.reggie.Entity.Dish;
import com.example.reggie.common.Result;
import com.example.reggie.dto.DishDto;
import com.example.reggie.service.CategoryService;
import com.example.reggie.service.DishFlavorService;
import com.example.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
    public Result<String> save(@RequestBody DishDto dishDto){
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
            Category category=categoryService.getById(item.getCategoryId());
            if(category!=null)
            { dishDto.setCategoryName(category.getName());}
            //结果返回一个dishDto对象
            return dishDto;
            //并将dishDto对象封装成一个集合，作为我们的最终结果
        }).collect(Collectors.toList());

        dishDtoPage.setRecords(list);
        return Result.success(dishDtoPage);
    }

    @GetMapping("/{id}")
    public Result<DishDto>getByIdWithFlavor(@PathVariable Long id){
        DishDto dishDto=dishService.getByIdWithFlavor(id);
        log.info("查询到的数据为：{}", dishDto);
        return Result.success(dishDto);
    }

    @PutMapping
    public Result<String>update(@RequestBody DishDto dishDto){
        log.info("接收到的数据为：{}", dishDto);
        dishService.updateWithFlavor(dishDto);
        return Result.success("更新菜品成功");
    }

    /**
     * 根据dish获得分类下所有dish信息
     * @param dish
     * @return
     */
    @GetMapping("/list")
    public Result<List<Dish>> list(Dish dish){
        //根据dish的分类找到分类所有的菜品
        log.info("获得的菜品分类信息如下{}",dish);
        LambdaQueryWrapper<Dish>lqw=new LambdaQueryWrapper<>();
        lqw.eq(dish.getCategoryId()!=null,Dish::getCategoryId,dish.getCategoryId());
        return Result.success(dishService.list(lqw));
    }
}
