package com.example.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.reggie.Entity.Category;
import com.example.reggie.common.Result;
import com.example.reggie.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/category")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    @PostMapping
    public Result<String> save(@RequestBody Category category) {
        categoryService.save(category);
        log.info("category:{}", category);
        return Result.success("新增分类成功");
    }

    @GetMapping("/page")
    public Result<Page> save(int page, int pageSize) {
        Page<Category>pageInfo=new Page<>(page,pageSize);
        LambdaQueryWrapper<Category>lqw=new LambdaQueryWrapper<>();
        lqw.orderByDesc(Category::getSort);
        categoryService.page(pageInfo,lqw);
        return Result.success(pageInfo);
    }

    @DeleteMapping
    public Result<String>delete(Long id){
        log.info("将被删除的id：{}", id);
        categoryService.remove(id);
        return Result.success("分类信息删除成功");
    }

    @PutMapping
    public Result<String> update(@RequestBody Category category) {
        log.info("修改分类信息为：{}", category);
        categoryService.updateById(category);
        return Result.success("修改分类信息成功");
    }

    @GetMapping("/list")
    public Result<List<Category>>list(Category category){
        LambdaQueryWrapper<Category>lqw=new LambdaQueryWrapper<>();
        lqw.eq(category.getType()!=null,Category::getType,category.getType());
        lqw.orderByAsc(Category::getSort).orderByDesc(Category::getUpdateTime);
        List<Category>list=categoryService.list(lqw);
        return Result.success(list);
    }

}
