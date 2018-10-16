package com.pinyougou.cart.service;

import java.util.List;

import com.pinyougou.pojogroup.Cart;

//购物车服务接口

public interface CartService {
	
	//添加商品到购物车 , 返回添加后的结果
	public List<Cart> addGoodsToCartList(List<Cart> cartList, Long itemId, Integer num);
	
	
	//从redis中提取购物车
	
	public List<Cart> findCartListFromRedis(String username);
	
	//把购物车存储到redis中
	
	public void  saveCartListToredis(String username , List<Cart> cartList);
	
	
	//合并购物车
	public List<Cart> mergeCartList(List<Cart> cartList1,List<Cart> cartList2);
}
