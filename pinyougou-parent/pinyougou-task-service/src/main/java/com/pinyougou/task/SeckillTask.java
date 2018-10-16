package com.pinyougou.task;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.pinyougou.mapper.TbSeckillGoodsMapper;
import com.pinyougou.pojo.TbSeckillGoods;
import com.pinyougou.pojo.TbSeckillGoodsExample;
import com.pinyougou.pojo.TbSeckillGoodsExample.Criteria;

@Component
public class SeckillTask {
	//注入缓存
	@Autowired
	private  RedisTemplate redisTemplate;
	
	@Autowired
	private TbSeckillGoodsMapper seckillGoodsMapper;
	
	//刷新秒杀商品
	@Scheduled(cron="0 5 * * * ?")
	public void refreshSeckillGoods(){
		System.out.println("执行了秒杀商品增量更新任务调度"+new Date());		
		//查询缓存中的秒杀商品的ID集合
		 Set keys =  redisTemplate.boundHashOps("seckillGoods").keys();//存的时候是以所有商品的id 作为键存进去 返回值是Set集合
		 System.out.println(keys.size());
		 List<TbSeckillGoods> seckillGoodsList = 	redisTemplate.boundHashOps("seckillGoods").values();
		TbSeckillGoodsExample example = new TbSeckillGoodsExample();
		Criteria criteria = example.createCriteria();
		criteria.andStatusEqualTo("1");//审核过的商品
		criteria.andStockCountGreaterThan(0);//库存大于0
		criteria.andStartTimeLessThanOrEqualTo(new Date());//秒杀开始时间要提前于当前时间
		criteria.andEndTimeGreaterThanOrEqualTo(new Date());//秒杀结束时间要大于等于当前日期
		
		if(keys.size()>0){
			criteria.andIdNotIn(new ArrayList(keys));//增加查询条件  排除缓存中已经存在的商品id 集合
		}
		
		
		 seckillGoodsList = seckillGoodsMapper.selectByExample(example );
		 
		 System.out.println(seckillGoodsList.size());
		 for (TbSeckillGoods seckillGoods : seckillGoodsList) {
			 redisTemplate.boundHashOps("seckillGoods").put(seckillGoods.getId(), seckillGoods);
			 System.out.println("增量更新秒杀商品id:"+seckillGoods.getId());
		}
		
	}	
	
	//移除过期的秒杀商品
	@Scheduled(cron="1/20 * * * * ?")
	public void  removeSeckillGoods(){
		System.out.println("开始进行过期秒杀商品移除");
		//1 ,先从缓存中查找出所有的秒杀商品
		
	List<TbSeckillGoods> secList = 	redisTemplate.boundHashOps("seckillGoods").values();
	     //2 , 进行条件筛选  进行个p  商品自带条件  
		 	//遍历商品
		for(TbSeckillGoods goods: secList){
			//如果结束日期 小于当前日期 就代表商品已经过期
			
			if(goods.getEndTime().getTime()<new Date().getTime()){
				//向数据库中保存记录
				seckillGoodsMapper.updateByPrimaryKey(goods);
				redisTemplate.boundHashOps("seckillGoods").delete(goods.getId());
				System.out.println("删除了过期商品的id是:"+ goods.getId());
			}
		}
		System.out.println("商品移除完毕");
	
	
	}
}
