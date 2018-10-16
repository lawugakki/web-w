package com.pinyougou.cart.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbOrderItem;
import com.pinyougou.pojogroup.Cart;

@Service
public class CartServicceImpl  implements CartService{
	
	//注入商品mapper
	@Autowired
	private TbItemMapper  itemMapper;

	@Override
	public List<Cart> addGoodsToCartList(List<Cart> cartList, Long itemId, Integer num) {
		//1,根据skuID商品明细SKU 的对象
		TbItem tbItem = itemMapper.selectByPrimaryKey(itemId);
	//	System.out.println(tbItem);
		if(tbItem==null){//商品不存在
			throw new RuntimeException("商品不存在");
		}
		if(!tbItem.getStatus().equals("1")){//商品未审核
			throw new RuntimeException("商品状态不合法");
		}
		//2,根据SKU对象得商家id
		String sellerId = tbItem.getSellerId();
	//	System.out.println(sellerId);
		
		//3,根据商家ID查询购物车对象
		Cart cartBySellerId = searchCartBySellerId(cartList,sellerId);
		if(cartBySellerId==null){
			//4,如果购物车列表中不存在该商家的购物车
			
			//4.1创建一个新的购物车对象
			cartBySellerId = new  Cart();
			cartBySellerId.setSellerId(sellerId);// 商家id
			cartBySellerId.setSellerName(tbItem.getSeller());//商家名称
			List<TbOrderItem> orderItemList = new ArrayList<TbOrderItem>();//购物车明细列表
			//创建新的购物车明细对象
			TbOrderItem orderItem = createNewCart(tbItem,num);
			
			orderItemList.add(orderItem);
			
			cartBySellerId.setOrderItemList(orderItemList );
			
		//	System.out.println(cartBySellerId.getSellerName());
			
			//4.2将新的购物车对象添加到购物车列表中
			
			cartList.add(cartBySellerId);
		}else{

			//5,购物车列表中存在该商家的购物车
			TbOrderItem orderItem = searchOrderItemByItemId(cartBySellerId.getOrderItemList(),itemId);
			if(orderItem==null){
				//5.2商品不存在添加新的商品明细列表
				orderItem=createNewCart(tbItem,num);
				//把购物车对象添加到购物车列表中
				cartBySellerId.getOrderItemList().add(orderItem);
			}else{
				//5.1商品存在在原有的数量上添加数量,并且更新金额
				orderItem.setNum(orderItem.getNum()+num);//更新数量
				//更新金额
				double iprice = orderItem.getPrice().doubleValue();
				double inum = orderItem.getNum().doubleValue();
			//	System.out.println(iprice+"========="+inum);
				orderItem.setTotalFee(new BigDecimal(iprice * inum));
				//当明细的商品数量小于0 从本购物车对象中移除
				if(orderItem.getNum()<0){
					cartBySellerId.getOrderItemList().remove(orderItem);
				}
				//如果购物车对象中一个商品明细都没有, 这个购物车就不要了  从购物车列表中移除
				if(cartBySellerId.getOrderItemList().size()==0){
					cartList.remove(cartBySellerId);
				}
			}
	
		}
				
		return cartList;
	}
	//根据商家ID查询购物车对象
	public Cart searchCartBySellerId(List<Cart> cartList ,String sellerId){
		for(Cart cart:cartList){
			if(cart.getSellerId().equals(sellerId)){//如果购物车中的商家id和传进来的商家di匹配
				return  cart;
				
			}
		}
		return  null;
	}
	//根据商品SKU id查询商品是否在购物车明细列表中
	public TbOrderItem searchOrderItemByItemId(List<TbOrderItem> orderItemlist,Long  itemId){
		for(TbOrderItem tbOrderItem:orderItemlist){
			if(tbOrderItem.getItemId().longValue()==itemId.longValue()){//因为这两个id都是long类型,是对象,需要先转换成数据类型再比较
				return tbOrderItem;
			}
		}
		
		
		return null;
		
	}
	
	
	//创建新的购物车对象
	private TbOrderItem createNewCart(TbItem tbItem,Integer num){
		//创建新的购物车明细对象,添加对应的属性 并放到购物车列表中
		TbOrderItem orderItem  =new  TbOrderItem();
		orderItem.setItemId(tbItem.getId());
		orderItem.setNum(num);
		orderItem.setPrice(tbItem.getPrice());
		//System.out.println("jiage:"+tbItem.getPrice());
		orderItem.setSellerId(tbItem.getSellerId());
		orderItem.setTitle(tbItem.getTitle());
		orderItem.setGoodsId(tbItem.getGoodsId());
		orderItem.setPicPath(tbItem.getImage());
		orderItem.setTotalFee(new BigDecimal(tbItem.getPrice().doubleValue()*num));
		return orderItem;
	}
	
	//注入redis
	@Autowired
	private  RedisTemplate<String, ?> redisTemplate;   
	
	
	@Override//从redis中读取购物车
	public List<Cart> findCartListFromRedis(String username) {
		System.out.println("从redis中提取购物车  "+username);
		List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("cartList").get(username);
		if(cartList==null){
			cartList= new ArrayList();
		}
		return cartList;
	}
	
	
	@Override//把购物车存储到redis中
	public void saveCartListToredis(String username, List<Cart> cartList) {
		System.out.println("向redis中保存购物车  "+username);
		redisTemplate.boundHashOps("cartList").put(username, cartList);
		
	}
	
	@Override //合并购物车
	public List<Cart> mergeCartList(List<Cart> cartList1, List<Cart> cartList2) {
		System.out.println("合并购物车");
		for(Cart cart:cartList2){
			for(TbOrderItem orderItem:cart.getOrderItemList()){
				cartList1 = addGoodsToCartList(cartList1, orderItem.getItemId(), orderItem.getNum());
			}
		}
		
		return cartList1;
	}
}
