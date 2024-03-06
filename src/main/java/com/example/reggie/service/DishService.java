package com.example.reggie.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.example.reggie.Entity.Dish;
import com.example.reggie.dto.DishDto;

public interface DishService extends IService<Dish> {

    void saveWithFlavors(DishDto dishDto);
    DishDto getByIdWithFlavor(Long id);

    void updateWithFlavor(DishDto dishDto);
}
