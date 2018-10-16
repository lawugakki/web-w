package com.pinyougou.cart.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.order.service.OrderService;
import com.pinyougou.pay.service.WeixinPayService;
import com.pinyougou.pojo.TbPayLog;

import entity.Result;
import util.IdWorker;

@RestController
@RequestMapping("/pay")
public class PayController {
	@Reference
	private WeixinPayService payService;
	
	@Reference
	private OrderService   orderService; 
	@RequestMapping("/createNative")
	public Map createNative(){
		
		//获取当前登录用户
		String userId = SecurityContextHolder.getContext().getAuthentication().getName();
		//提取支付日志
		TbPayLog payLog = orderService.searchPayLogFromRedis(userId);
	//	System.out.println(payLog.getUserId());
		//调用微信支付接口
		if(payLog!=null){
			return payService.createNative(payLog.getOutTradeNo(),payLog.getTotalFee()+"");		
		}else{
			return  new HashMap();
		}
		
		
	}
	
	@RequestMapping("/queryPayStatus")
	public Result queryPayStatus(String out_trade_no){
		Result result  = null;
		int x = 0;
		while(true){
			Map<String,String > map = payService.queryPayStatus(out_trade_no);//调用查询
			System.out.println(map.get("trade_state"));
			if(map==null){
				result = new   Result(false, "支付发生错误");
				break;
			}
			if(map.get("trade_state").equals("SUCCESS")){
				result  =new  Result(true, "支付成功");
				orderService.updateOrderStatus(out_trade_no, map.get("transaction_id"));
				break;
			}
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			x++;
			
			if(x>=100){
				result  =new  Result(false, "你走吧");
				break;
			}
			
		}
		return result;
	}
	
}
