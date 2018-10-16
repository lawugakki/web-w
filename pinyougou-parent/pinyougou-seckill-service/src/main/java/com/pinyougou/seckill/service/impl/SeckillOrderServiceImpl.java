package com.pinyougou.seckill.service.impl;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbSeckillGoodsMapper;
import com.pinyougou.mapper.TbSeckillOrderMapper;
import com.pinyougou.pojo.TbSeckillGoods;
import com.pinyougou.pojo.TbSeckillOrder;
import com.pinyougou.pojo.TbSeckillOrderExample;
import com.pinyougou.pojo.TbSeckillOrderExample.Criteria;
import com.pinyougou.seckill.service.SeckillOrderService;

import entity.PageResult;
import util.IdWorker;

/**
 * 服务实现层
 * 
 * @author Administrator
 *
 */
@Service
public class SeckillOrderServiceImpl implements SeckillOrderService {

	@Autowired
	private TbSeckillOrderMapper seckillOrderMapper;

	/**
	 * 查询全部
	 */
	@Override
	public List<TbSeckillOrder> findAll() {
		return seckillOrderMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		Page<TbSeckillOrder> page = (Page<TbSeckillOrder>) seckillOrderMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbSeckillOrder seckillOrder) {
		seckillOrderMapper.insert(seckillOrder);
	}

	/**
	 * 修改
	 */
	@Override
	public void update(TbSeckillOrder seckillOrder) {
		seckillOrderMapper.updateByPrimaryKey(seckillOrder);
	}

	/**
	 * 根据ID获取实体
	 * 
	 * @param id
	 * @return
	 */
	@Override
	public TbSeckillOrder findOne(Long id) {
		return seckillOrderMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for (Long id : ids) {
			seckillOrderMapper.deleteByPrimaryKey(id);
		}
	}

	@Override
	public PageResult findPage(TbSeckillOrder seckillOrder, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);

		TbSeckillOrderExample example = new TbSeckillOrderExample();
		Criteria criteria = example.createCriteria();

		if (seckillOrder != null) {
			if (seckillOrder.getUserId() != null && seckillOrder.getUserId().length() > 0) {
				criteria.andUserIdLike("%" + seckillOrder.getUserId() + "%");
			}
			if (seckillOrder.getSellerId() != null && seckillOrder.getSellerId().length() > 0) {
				criteria.andSellerIdLike("%" + seckillOrder.getSellerId() + "%");
			}
			if (seckillOrder.getStatus() != null && seckillOrder.getStatus().length() > 0) {
				criteria.andStatusLike("%" + seckillOrder.getStatus() + "%");
			}
			if (seckillOrder.getReceiverAddress() != null && seckillOrder.getReceiverAddress().length() > 0) {
				criteria.andReceiverAddressLike("%" + seckillOrder.getReceiverAddress() + "%");
			}
			if (seckillOrder.getReceiverMobile() != null && seckillOrder.getReceiverMobile().length() > 0) {
				criteria.andReceiverMobileLike("%" + seckillOrder.getReceiverMobile() + "%");
			}
			if (seckillOrder.getReceiver() != null && seckillOrder.getReceiver().length() > 0) {
				criteria.andReceiverLike("%" + seckillOrder.getReceiver() + "%");
			}
			if (seckillOrder.getTransactionId() != null && seckillOrder.getTransactionId().length() > 0) {
				criteria.andTransactionIdLike("%" + seckillOrder.getTransactionId() + "%");
			}

		}

		Page<TbSeckillOrder> page = (Page<TbSeckillOrder>) seckillOrderMapper.selectByExample(example);
		return new PageResult(page.getTotal(), page.getResult());
	}

	// 注入缓存
	@Autowired
	private RedisTemplate redisTemplate;
	
	//
	@Autowired
	private TbSeckillGoodsMapper seckillGoodsMapper; 
	
	@Autowired //用于产生订单id
	private IdWorker idWorker;

	@Override
	public void submitOrder(Long seckillId, String userId) {
		// 查询缓存中的商品
		TbSeckillGoods tbSeckillGoods = (TbSeckillGoods) redisTemplate.boundHashOps("seckillGoods").get(seckillId);
		if (tbSeckillGoods == null) {
			throw new RuntimeException("商品没啦");
		}
		if (tbSeckillGoods.getStockCount() <= 0) {
			throw new RuntimeException("商品被抢光了");
		}
		//减少库存
		tbSeckillGoods.setStockCount(tbSeckillGoods.getStockCount()-1);
		redisTemplate.boundHashOps("seckillGoods").put(seckillId, tbSeckillGoods);//把商品重新装入缓存
		if(tbSeckillGoods.getStockCount()==0){//当缓存中的商品为0  ,把商品放回数据库
			seckillGoodsMapper.updateByPrimaryKey(tbSeckillGoods);//更新数据库
			redisTemplate.boundHashOps("seckillGoods").delete(seckillId);//清空缓存
			
			System.out.println("将商品同步到数据库");
		}
		//存储秒杀订单(不向数据库存,只向缓存中存)
		TbSeckillOrder order = new  TbSeckillOrder();//创建订单对象
		order.setId(idWorker.nextId());//订单id
		order.setSeckillId(seckillId);//秒杀id
		order.setMoney(tbSeckillGoods.getCostPrice());//秒杀价格
		order.setUserId(userId);//用户id
		order.setSellerId(tbSeckillGoods.getSellerId());//商家id
		order.setCreateTime(new  Date());//下单时间
		order.setStatus("0");//状态
		
		redisTemplate.boundHashOps("secKillOrder").put(userId, order);//根据用户id' 把订单存入缓存
		System.out.println("保存订单到redis");
	}

	@Override
	public TbSeckillOrder searchOrderFromRedisByUserId(String userId) {
		return 	(TbSeckillOrder) redisTemplate.boundHashOps("secKillOrder").get(userId);
		
	}

	@Override//保存订单到数据库
	public void saveOrderFromRedisToDb(String userId, Long orderId, String transactionId) {
		
		//从缓存中提取订单数据
		TbSeckillOrder seckilOrder = searchOrderFromRedisByUserId(userId);
		if(seckilOrder==null){
			throw new RuntimeException("没有对应的订单");
			 
		}
		if(seckilOrder.getId().longValue()!=orderId.longValue()){
			throw new RuntimeException("订单号不对");
			 
		}
		//修改订单实体的属性
		seckilOrder.setPayTime(new Date());//支付时间
		seckilOrder.setStatus("1");//订单状态
		seckilOrder.setTransactionId(transactionId);//交易流水号  来自微信
		//将订单存入数据库
		seckillOrderMapper.insert(seckilOrder);
		
		//清除缓存中的订单
		redisTemplate.boundHashOps("secKillOrder").delete(userId);
	}

	@Override
	public void deleteOrderFromRedis(String userId, Long orderId) {
		// 1,查询出缓存中的订单
		TbSeckillOrder seckillOrder = searchOrderFromRedisByUserId(userId);
		if(seckillOrder!=null){
			//删除缓存中的订单
			redisTemplate.boundHashOps("secKillOrder").delete(userId);
			
			// 2,库存回退
				//查询出缓存中的商品
		TbSeckillGoods seckillGoods =	(TbSeckillGoods) redisTemplate.boundHashOps("secKillOrder").get(seckillOrder.getSeckillId());
		if(seckillGoods!=null){
			seckillGoods.setStockCount(seckillGoods.getStockCount()+1);//库存加1
			redisTemplate.boundHashOps("secKillOrder").put(seckillOrder.getSeckillId(), seckillGoods);//存入懂啊缓存
		}else{
			seckillGoods =  new TbSeckillGoods();
			seckillGoods.setId(seckillOrder.getSeckillId());
			//属性要设置
			seckillGoods.setStockCount(1);
			redisTemplate.boundHashOps("secKillOrder").put(seckillOrder.getSeckillId(), seckillGoods);//存入懂啊缓存
			
		}
		System.out.println("订单取消:"+orderId);
		}
		
		
		// 3,
		
	}

}
