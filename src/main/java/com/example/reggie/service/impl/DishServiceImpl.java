package com.example.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.reggie.Entity.Dish;
import com.example.reggie.Entity.DishFlavor;
import com.example.reggie.dto.DishDto;
import com.example.reggie.mapper.DishMapper;
import com.example.reggie.service.DishFlavorService;
import com.example.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@Transactional
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish>implements DishService {

    /**
     * 对每一dishDto里头的风味 在dishFlavor中设置其dishId并且保存在dishFlavor表格中
     * @param dishDto
     */
    @Autowired
    private DishFlavorService dishFlavorService;
    @Override
    public void saveWithFlavors(DishDto dishDto) {
       this.save(dishDto);
       Long id=dishDto.getId();
        List<DishFlavor>dishFlavors=dishDto.getFlavors();
        for(DishFlavor dishFlavor:dishFlavors){
            dishFlavor.setDishId(id);
        }
        dishFlavorService.saveBatch(dishFlavors);
    }
}
