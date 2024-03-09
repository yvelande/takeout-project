package com.example.reggie.dto;

import com.example.reggie.Entity.OrderDetail;
import com.example.reggie.Entity.Orders;
import lombok.Data;

import java.util.List;

@Data
public class OrdersDto extends Orders {

    private String userName;

    private String phone;

    private String address;

    private String consignee;

    private List<OrderDetail> orderDetails;

}