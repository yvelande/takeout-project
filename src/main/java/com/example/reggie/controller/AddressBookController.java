package com.example.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.example.reggie.Entity.AddressBook;
import com.example.reggie.common.BaseContext;
import com.example.reggie.common.CustomException;
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

    @GetMapping("/default")
    public Result<AddressBook> defaultAddress() {
        Long userId=BaseContext.getCurrentId();
        LambdaQueryWrapper<AddressBook>lqw=new LambdaQueryWrapper<>();
        lqw.eq(userId!=null,AddressBook::getUserId,userId);
        lqw.eq(AddressBook::getIsDefault,1);
        AddressBook addressBook=addressBookService.getOne(lqw);
        log.info("获得的默认地址是{}",addressBook);
        return Result.success(addressBook);
    }

    @GetMapping("/{id}")
    public Result<AddressBook>getById(@PathVariable Long id){
        log.info("获得的ids",id);
        AddressBook addressBook=addressBookService.getById(id);
        if (addressBook == null){
            throw new CustomException("地址信息不存在");
        }
        log.info("获得的地址信息是{}",addressBook);
        return Result.success(addressBook);
    }

    @PutMapping
    public Result<String> updateAdd(@RequestBody AddressBook addressBook){
        if(addressBook==null)
            throw new CustomException("地址信息不存在，请刷新重试");
        addressBookService.updateById(addressBook);
        return Result.success("地址修改成功");
    }

    @DeleteMapping()
    public Result<String> deleteAdd(@RequestParam("ids") Long id) {
        if (id == null) {
            throw new CustomException("地址信息不存在，请刷新重试");
        }
        AddressBook addressBook=addressBookService.getById(id);
        if (addressBook == null) {
            throw new CustomException("地址信息不存在，请刷新重试");
        }
        addressBookService.removeById(id);
        return Result.success("地址删除成功");
    }
}