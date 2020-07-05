package com.atguigu.gmall.order.controller;

import com.atguigu.gmall.cart.client.CartFeignClient;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.cart.CartInfo;
import com.atguigu.gmall.model.enums.OrderStatus;
import com.atguigu.gmall.model.enums.PaymentWay;
import com.atguigu.gmall.model.enums.ProcessStatus;
import com.atguigu.gmall.model.order.OrderDetail;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.model.user.UserAddress;
import com.atguigu.gmall.order.service.OrderService;
import com.atguigu.gmall.user.client.UserFeignClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * OrderController
 *
 * @Author: 郭思钊
 * @CreateTime: 2020-06-27
 * @Description:
 */
@RestController
@RequestMapping("api/order/")
public class OrderApiController {
    @Autowired
    OrderService orderService;

    @Autowired
    CartFeignClient cartFeignClient;

    @Autowired
    UserFeignClient userFeignClient;

    /**
     * 提交秒杀订单
     * @param orderInfo
     * @return
     */
    @PostMapping("/api/order/inner/seckill/submitOrder")
    Long submitOrder(@RequestBody OrderInfo orderInfo){
        OrderInfo orderInfo1 = orderService.saveOrderInfo(orderInfo);
        Long id = orderInfo1.getId();
        return id;
    }

    @PostMapping("/inner/getOrderInfo/{orderId}")
    OrderInfo getOrderInfo(@PathVariable("orderId") String orderId){
        OrderInfo orderInfo = orderService.getOrderInfo(orderId);
        return orderInfo;
    }

    //提交订单（生成订单）
    @RequestMapping("auth/submitOrder")
    public Result submitOrder(String tradeNo, HttpServletRequest request) {

        String userId = request.getHeader("userId");
        // 对比tradeNo
        boolean b = orderService.checkTradeNo(userId, tradeNo);
        if (b) {
            // 生成需要提交
            OrderInfo orderInfo = new OrderInfo();
            // 查询订单详情(购物车)
            // detailArrayList//被选中的购物车集合
            List<OrderDetail> orderDetails = new ArrayList<>();
            List<CartInfo> cartInfos = cartFeignClient.getCartCheckedList(userId);
            StringBuffer tradeBody = new StringBuffer();

            for (CartInfo cartInfo : cartInfos) {
                OrderDetail orderDetail = new OrderDetail();
                // 将购物车数据封装给订单详情
                orderDetail.setImgUrl(cartInfo.getImgUrl());
                orderDetail.setOrderPrice(cartInfo.getCartPrice());
                orderDetail.setSkuId(cartInfo.getSkuId());
                orderDetail.setSkuName(cartInfo.getSkuName());
                orderDetail.setSkuNum(cartInfo.getSkuNum());

                // 校验当前库存数量是否>购买数量
                // 调用库存系统接口，获得库存查询结果http://localhost:9001/hasStock
                // 通过httpClient，远程调用库存系统的webservice接口
//                String stockHttp = "http://localhost:9001/hasStock?skuId=" + cartInfo.getSkuId() + "&num=" + cartInfo.getSkuNum();
//                String stock = HttpClientUtil.doGet(stockHttp);
//
//                if (stock.equals("0")) {
//                    return Result.fail(ResultCodeEnum.SECKILL_FINISH);
//                }
//
//                // 校验当前商品的价格是否=购物车价格
//                if (false) {
//                    return Result.fail("价格不合法");
//                }

                orderDetails.add(orderDetail);//
            }

            orderInfo.setOrderDetailList(orderDetails);
            if (tradeBody.toString().length() > 100) {
                orderInfo.setTradeBody(tradeBody.toString().substring(0, 100));
            } else {
                orderInfo.setTradeBody(tradeBody.toString());
            }
            // 页面商品总数量和结算总金额
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
            String format = sdf.format(new Date());
            String outTradeNo = "ATGUIGU" + System.currentTimeMillis() + "" + new Random().nextInt(1000);//外部订单号
            orderInfo.setOutTradeNo(outTradeNo);
            orderInfo.setOrderStatus(OrderStatus.UNPAID.getComment());
            orderInfo.setProcessStatus(ProcessStatus.UNPAID.getComment());
            orderInfo.setPaymentWay(PaymentWay.ONLINE.getComment());
            orderInfo.setTradeBody("谷粒订单");
            orderInfo.setTrackingNo("物流单号");
            orderInfo.setUserId(Long.parseLong(userId));
            orderInfo.setImgUrl(cartInfos.get(0).getImgUrl());
            orderInfo.sumTotalAmount();// 核算价格
            orderInfo.setCreateTime(new Date());
            // 封装收货信息
            List<UserAddress> userAddressListByUserId = userFeignClient.findUserAddressListByUserId(userId);
            UserAddress userAddress = userAddressListByUserId.get(0);
            orderInfo.setDeliveryAddress(userAddress.getUserAddress());
            orderInfo.setConsignee(userAddress.getConsignee());
            orderInfo.setConsigneeTel(userAddress.getPhoneNum());

            // 过期时间的计算
            Calendar instance = Calendar.getInstance();
            instance.add(Calendar.DATE, 1);
            orderInfo.setExpireTime(instance.getTime());

            // 提交订单业务调用
            orderInfo = orderService.saveOrderInfo(orderInfo);
            return Result.ok(orderInfo.getId());
        } else {
            return Result.fail("订单已经提交过");
        }
    }

    @RequestMapping("inner/getTradeNo/{userId}")
    public String getTradeNo(@PathVariable("userId") String userId , HttpServletRequest request){
        if(StringUtils.isEmpty(userId)){
            userId = request.getHeader("userId");
        }
        return orderService.getTradeNo(userId);
    }
}