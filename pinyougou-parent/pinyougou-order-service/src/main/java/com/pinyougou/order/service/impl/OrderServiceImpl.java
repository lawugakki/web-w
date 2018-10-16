package com.pinyougou.order.service.impl;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.mapper.TbOrderItemMapper;
import com.pinyougou.mapper.TbOrderMapper;
import com.pinyougou.mapper.TbPayLogMapper;
import com.pinyougou.order.service.OrderService;
import com.pinyougou.pojo.TbOrder;
import com.pinyougou.pojo.TbOrderExample;
import com.pinyougou.pojo.TbOrderExample.Criteria;
import com.pinyougou.pojo.TbOrderItem;
import com.pinyougou.pojo.TbPayLog;
import com.pinyougou.pojogroup.Cart;

import entity.PageResult;
import util.IdWorker;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
@Transactional
public class OrderServiceImpl implements OrderService {

	@Autowired
	private TbOrderMapper orderMapper;
	
	@Autowired
	private TbPayLogMapper payLogMapper;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbOrder> findAll() {
		return orderMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbOrder> page=   (Page<TbOrder>) orderMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	
	//注入redis
	@Autowired
	private  RedisTemplate  redisTemplate;
	
	//注入雪花啤酒
	@Autowired
	private  IdWorker idWorker;
	
	//注入明细mapper
	@Autowired
	private  TbOrderItemMapper  tbOrderItemMapper; 
	/**
	 * 增加
	 */
	@Override
	public void add(TbOrder order) {
		//1.从redis中提取购物车列表
	  List<Cart> cartList=	(List<Cart>) redisTemplate.boundHashOps("cartList").get(order.getUserId());
	  
	  List<String> orderIdList =  new ArrayList();//订单id集合
	  double total_money= 0;//总金额
			    
		//2.循环购物车列表 添加订单
		for (Cart cart : cartList) {
			//创建新的订单对象
			TbOrder tbOrder = new  TbOrder();
			//使用雪花算法生成orderid
			long orderId = idWorker.nextId();
			//插入id
			tbOrder.setOrderId(orderId);
			tbOrder.setUserId(order.getUserId());//用户名
			tbOrder.setPaymentType(order.getPaymentType());//支付类型
			tbOrder.setStatus("1");//状态：未付款
			tbOrder.setCreateTime(new Date());//订单创建日期
			tbOrder.setUpdateTime(new Date());//订单更新日期
			tbOrder.setReceiverAreaName(order.getReceiverAreaName());//地址
			tbOrder.setReceiverMobile(order.getReceiverMobile());//手机号
			tbOrder.setReceiver(order.getReceiver());//收货人
			tbOrder.setSourceType(order.getSourceType());//订单来源
			tbOrder.setSellerId(cart.getSellerId());//商家ID		
			
			
			double money=0;//总金额
			//遍历购物车明细列表
			for(TbOrderItem orderItem: cart.getOrderItemList()){
				//为商品明细 添加id
				orderItem.setId(idWorker.nextId());
				orderItem.setOrderId(orderId);//订单编号
				orderItem.setSellerId(cart.getSellerId());//商家id
				tbOrderItemMapper.insert(orderItem);
				money+=orderItem.getTotalFee().doubleValue();//把每个商品的总金额累加到 订单的总金额
			}
			tbOrder.setPayment(new  BigDecimal(money));
			orderMapper.insert(tbOrder);
			orderIdList.add(orderId+"");
			total_money+=money;
		}
		
		//添加支付日志
		if("1".equals(order.getPaymentType())){//微信支付的情况下
			TbPayLog log = new  TbPayLog();
			log.setOutTradeNo(idWorker.nextId()+"");//设置支付订单号  用雪花完成
			log.setCreateTime(new Date());
			log.setUserId(order.getUserId());//用户id
			
			String ids=orderIdList.toString().replace("[", "").replace("]", "").replace(" ", "");
			log.setOrderList(ids);//订单列表
			log.setTotalFee((long)(total_money*100));//总金额
			log.setTradeState("0");//交易状态
			
			payLogMapper.insert(log);
			
			
			redisTemplate.boundHashOps("log").put(order.getUserId(), log);//把支付日志放入缓存
			
			
		}
		
		//3.清除redis购物车
		redisTemplate.boundHashOps("cartList").delete(order.getUserId());
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbOrder order){
		orderMapper.updateByPrimaryKey(order);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbOrder findOne(Long id){
		return orderMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			orderMapper.deleteByPrimaryKey(id);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbOrder order, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbOrderExample example=new TbOrderExample();
		Criteria criteria = example.createCriteria();
		
		if(order!=null){			
						if(order.getPaymentType()!=null && order.getPaymentType().length()>0){
				criteria.andPaymentTypeLike("%"+order.getPaymentType()+"%");
			}
			if(order.getPostFee()!=null && order.getPostFee().length()>0){
				criteria.andPostFeeLike("%"+order.getPostFee()+"%");
			}
			if(order.getStatus()!=null && order.getStatus().length()>0){
				criteria.andStatusLike("%"+order.getStatus()+"%");
			}
			if(order.getShippingName()!=null && order.getShippingName().length()>0){
				criteria.andShippingNameLike("%"+order.getShippingName()+"%");
			}
			if(order.getShippingCode()!=null && order.getShippingCode().length()>0){
				criteria.andShippingCodeLike("%"+order.getShippingCode()+"%");
			}
			if(order.getUserId()!=null && order.getUserId().length()>0){
				criteria.andUserIdLike("%"+order.getUserId()+"%");
			}
			if(order.getBuyerMessage()!=null && order.getBuyerMessage().length()>0){
				criteria.andBuyerMessageLike("%"+order.getBuyerMessage()+"%");
			}
			if(order.getBuyerNick()!=null && order.getBuyerNick().length()>0){
				criteria.andBuyerNickLike("%"+order.getBuyerNick()+"%");
			}
			if(order.getBuyerRate()!=null && order.getBuyerRate().length()>0){
				criteria.andBuyerRateLike("%"+order.getBuyerRate()+"%");
			}
			if(order.getReceiverAreaName()!=null && order.getReceiverAreaName().length()>0){
				criteria.andReceiverAreaNameLike("%"+order.getReceiverAreaName()+"%");
			}
			if(order.getReceiverMobile()!=null && order.getReceiverMobile().length()>0){
				criteria.andReceiverMobileLike("%"+order.getReceiverMobile()+"%");
			}
			if(order.getReceiverZipCode()!=null && order.getReceiverZipCode().length()>0){
				criteria.andReceiverZipCodeLike("%"+order.getReceiverZipCode()+"%");
			}
			if(order.getReceiver()!=null && order.getReceiver().length()>0){
				criteria.andReceiverLike("%"+order.getReceiver()+"%");
			}
			if(order.getInvoiceType()!=null && order.getInvoiceType().length()>0){
				criteria.andInvoiceTypeLike("%"+order.getInvoiceType()+"%");
			}
			if(order.getSourceType()!=null && order.getSourceType().length()>0){
				criteria.andSourceTypeLike("%"+order.getSourceType()+"%");
			}
			if(order.getSellerId()!=null && order.getSellerId().length()>0){
				criteria.andSellerIdLike("%"+order.getSellerId()+"%");
			}
	
		}
		
		Page<TbOrder> page= (Page<TbOrder>)orderMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}

		@Override
		public TbPayLog searchPayLogFromRedis(String userId) {

			return (TbPayLog) redisTemplate.boundHashOps("log").get(userId);
		}

		@Override
		public void updateOrderStatus(String out_trade_no, String transaction_id) {
			// 修改支付日志的状态及相关字段
			
			//查出来支付日志
			TbPayLog payLog = payLogMapper.selectByPrimaryKey(out_trade_no);
			//更改支付状态
			payLog.setCreateTime(new Date());//支付时间
			payLog.setTradeState("1");//支付状态 1为成功
			payLog.setTransactionId(transaction_id);//微信的交易流水号
			payLogMapper.updateByPrimaryKey(payLog);//更新
			
			// 修改订单表的状态
			
			String orderList = payLog.getOrderList();//订单 ID 串 
			String[] orderIds = orderList.split(",");
			for (String orderId : orderIds) {
				TbOrder order = orderMapper.selectByPrimaryKey(Long.valueOf(orderId));
				order.setStatus("2");//已付款状态
				orderMapper.updateByPrimaryKey(order);
			}
			// 清除缓存中的payLog
			
			redisTemplate.boundHashOps("log").delete(payLog.getUserId());
			
		}
	
}
