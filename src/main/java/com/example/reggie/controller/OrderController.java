package com.example.reggie.controller;

import ch.qos.logback.core.hook.ShutdownHook;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.reggie.Entity.OrderDetail;
import com.example.reggie.Entity.Orders;
import com.example.reggie.Entity.ShoppingCart;
import com.example.reggie.common.BaseContext;
import com.example.reggie.common.Result;
import com.example.reggie.dto.OrdersDto;
import com.example.reggie.service.OrderDetailService;
import com.example.reggie.service.OrderService;
import com.example.reggie.service.ShoppingCartService;
import com.sun.org.apache.xpath.internal.operations.Or;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@Slf4j
@RequestMapping("/order")
public class OrderController {
    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderDetailService orderDetailService;

    @Autowired
    private ShoppingCartService shoppingCartService;
    @PostMapping("/submit")
    public Result<String> submit(@RequestBody Orders orders){
        log.info("收到的订单数据为{}",orders);
        orderService.submit(orders);
        return Result.success("订单提交成功");
    }

    @GetMapping("/userPage")
    public Result<Page> page(int page, int pageSize) {
        Page<Orders>pageInfo=new Page<>(page,pageSize);
        Page<OrdersDto>ordersDtoPage=new Page<>(page,pageSize);
        BeanUtils.copyProperties(pageInfo,ordersDtoPage,"records");
        LambdaQueryWrapper<Orders>lqw=new LambdaQueryWrapper<>();
        Long userId= BaseContext.getCurrentId();
        lqw.eq(userId!=null,Orders::getUserId,userId);
        lqw.orderByDesc(Orders::getOrderTime);
        orderService.page(pageInfo,lqw);
        List<Orders>list=pageInfo.getRecords();
        List<OrdersDto>orderDetails=list.stream().map((item)->{
            OrdersDto ordersDto=new OrdersDto();
            Long orderId=item.getId();
            BeanUtils.copyProperties(item,ordersDto);
            LambdaQueryWrapper<OrderDetail>lambdaQueryWrapper=new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(orderId!=null,OrderDetail::getOrderId,orderId);
            ordersDto.setOrderDetails(orderDetailService.list(lambdaQueryWrapper));
            return ordersDto;
        }).collect(Collectors.toList());
        ordersDtoPage.setRecords(orderDetails);
        log.info("list:{}", orderDetails);
        return Result.success(ordersDtoPage);
    }

    @PostMapping("/again")
    public Result<String>again(@RequestBody Map<String,String> map){
        Long id= Long.valueOf(map.get("id"));
        LambdaQueryWrapper<OrderDetail>lqw=new LambdaQueryWrapper<>();
        lqw.eq(id!=null,OrderDetail::getOrderId,id);
        List<OrderDetail> orderDetailList=orderDetailService.list(lqw);
        List<ShoppingCart>shoppingCartList=orderDetailList.stream().map((item)->{
            ShoppingCart shoppingCart=new ShoppingCart();
            BeanUtils.copyProperties(item,shoppingCart);
            shoppingCart.setUserId(BaseContext.getCurrentId());
            shoppingCart.setCreateTime(LocalDateTime.now());
            return shoppingCart;
        }).collect(Collectors.toList());
        shoppingCartService.saveBatch(shoppingCartList);
        return Result.success("喜欢吃就再来一单吖~");
    }

    @GetMapping("/page")
    public Result<Page> page(int page, int pageSize, Long number, String beginTime, String endTime){
        Page<Orders>pageInfo=new Page<>(page,pageSize);
        LambdaQueryWrapper<Orders>lqw=new LambdaQueryWrapper<>();
        lqw.eq(number!=null,Orders::getId,number);
        lqw.orderByDesc(Orders::getOrderTime);
        lqw.gt(!StringUtils.isEmpty(beginTime),Orders::getOrderTime,beginTime).lt(
                !StringUtils.isEmpty(endTime),Orders::getOrderTime,endTime
        );
        orderService.page(pageInfo,lqw);
        Page<OrdersDto>ordersDtoPage=new Page<>(page,pageSize);
        BeanUtils.copyProperties(pageInfo,ordersDtoPage,"records");
        List<Orders>list=pageInfo.getRecords();
        List<OrdersDto>ordersDtoList=list.stream().map((item)->{
            OrdersDto ordersDto=new OrdersDto();
            BeanUtils.copyProperties(item,ordersDto);
            LambdaQueryWrapper<OrderDetail>lambdaQueryWrapper=new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(OrderDetail::getOrderId,item.getId());
            List<OrderDetail>orderDetails=orderDetailService.list(lambdaQueryWrapper);
            ordersDto.setOrderDetails(orderDetails);
            return ordersDto;
        }).collect(Collectors.toList());
        ordersDtoPage.setRecords(ordersDtoList);
        return Result.success(ordersDtoPage);
    }

    @PutMapping
    public Result<String> changeStatus(@RequestBody Map<String, String> map) {
        int status= Integer.parseInt(map.get("status"));
        Long orderId= Long.valueOf(map.get("id"));
        LambdaUpdateWrapper<Orders>luw=new LambdaUpdateWrapper<>();
        luw.eq(orderId!=null,Orders::getId,orderId);
        luw.set(Orders::getStatus,status);
        orderService.update(luw);
        return Result.success("订单状态修改成功");
    }
}
