package com.example.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.example.reggie.Entity.AddressBook;
import com.example.reggie.common.BaseContext;
import com.example.reggie.common.Result;
import com.example.reggie.service.AddressBookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/addressBook")
public class AddressBookController {

    @Autowired
    private AddressBookService addressBookService;

    @GetMapping("/list")
    public Result<List<AddressBook>> list(AddressBook addressBook) {
        //设置userId
        addressBook.setUserId(BaseContext.getCurrentId());
        log.info("addressBook={}", addressBook);
        LambdaQueryWrapper<AddressBook>lqw=new LambdaQueryWrapper<>();
        lqw.eq(addressBook.getUserId()!=null,AddressBook::getUserId,addressBook.getUserId());
        lqw.orderByDesc(AddressBook::getUpdateTime);
        return Result.success(addressBookService.list(lqw));
//        List<AddressBook>list=
    }

    @PostMapping
    public Result<AddressBook> addAddress(@RequestBody AddressBook addressBook) {
      addressBook.setUserId(BaseContext.getCurrentId());
        log.info("addressBook:{}", addressBook);
        addressBookService.save(addressBook);
        return Result.success(addressBook);
    }

    @PutMapping("/default")

    public Result<AddressBook> setDefaultAddress(@RequestBody AddressBook addressBook) {
        addressBook.setUserId(BaseContext.getCurrentId());
        LambdaUpdateWrapper<AddressBook>luw=new LambdaUpdateWrapper<>();
        luw.eq(addressBook.getUserId()!=null,AddressBook::getUserId,addressBook.getUserId());
        luw.set(AddressBook::getIsDefault,0);
        addressBookService.update(luw);
        addressBook.setIsDefault(1);
        addressBookService.updateById(addressBook);
        return Result.success(addressBook);
    }
}