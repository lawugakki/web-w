package com.pinyougou.pay.service;

import java.util.Map;

public interface WeixinPayService {
	//生成二维码
	public Map createNative(String out_trade_no,String total_fee);//创建本地支付账单,参数 商户订单号  钱
	
	// 查询订单状态
	
	public Map queryPayStatus(String out_trade_no);
	
	//关闭微信订单
	public Map closePay(String out_trade_no);
}
