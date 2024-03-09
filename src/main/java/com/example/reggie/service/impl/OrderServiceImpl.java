package com.example.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.reggie.Entity.*;
import com.example.reggie.common.BaseContext;
import com.example.reggie.common.CustomException;
import com.example.reggie.mapper.OrderMapper;
import com.example.reggie.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Transactional
@Slf4j
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Orders> implements OrderService {

    @Autowired
    private ShoppingCartService shoppingCartService;

    @Autowired
    private AddressBookService addressBookService;

    @Autowired
    private OrderDetailService orderDetailService;

    @Autowired
    private UserService userService;
    @Override
    public void submit(Orders orders) {
        Long userId= BaseContext.getCurrentId();
        //获得用户购物车数据
        LambdaQueryWrapper<ShoppingCart>lqw=new LambdaQueryWrapper<>();
        lqw.eq(userId!=null,ShoppingCart::getUserId,userId);
        List<ShoppingCart> shoppingCartList=shoppingCartService.list(lqw);
        if(shoppingCartList==null)
            throw new CustomException("购物车数据为空，不能下单");

        //判断地址是否错误
        AddressBook addressBook=addressBookService.getById(orders.getAddressBookId());
        if(addressBook==null)
            throw new CustomException("地址信息有误，不能下单");

        //直接获得随机产生的id
        Long orderId= IdWorker.getId();
        //设置全体总金额
        AtomicInteger amount=new AtomicInteger(0);
        //设置orderDetail
        List<OrderDetail>list=shoppingCartList.stream().map((item)->{
            OrderDetail orderDetail=new OrderDetail();
            orderDetail.setName(item.getName());
            orderDetail.setOrderId(orderId);
            orderDetail.setDishId(item.getDishId());
            orderDetail.setSetmealId(item.getSetmealId());
            orderDetail.setDishFlavor(item.getDishFlavor());
            orderDetail.setNumber(item.getNumber());
            orderDetail.setAmount(item.getAmount());
            orderDetail.setImage(item.getImage());
            amount.addAndGet(item.getAmount().multiply(new BigDecimal(item.getNumber())).intValue());
            return orderDetail;
        }).collect(Collectors.toList());
        orderDetailService.saveBatch(list);


        User user=userService.getById(userId);
//        log.info(user.toString());
        orders.setId(orderId);
        orders.setNumber(String.valueOf(orderId));
        orders.setStatus(2);
        orders.setUserId(userId);
        orders.setAddressBookId(addressBook.getId());
        orders.setOrderTime(LocalDateTime.now());
        orders.setCheckoutTime(LocalDateTime.now());
        orders.setAmount(new BigDecimal(amount.get()));
        orders.setPhone(addressBook.getPhone());
        orders.setUserName("李先生");
        orders.setConsignee(addressBook.getConsignee());
        orders.setAddress(((addressBook.getProvinceName()==null)?"":addressBook.getProvinceName())
                +((addressBook.getCityName()==null)?"":addressBook.getCityName())+
                (addressBook.getDistrictName()==null?"":addressBook.getDistrictName())+
                (addressBook.getDetail()==null?"":addressBook.getDetail())
        );
        this.save(orders);
        //清空购物车
        shoppingCartService.remove(lqw);
    }
}
