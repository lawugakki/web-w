package com.pinyougou.cart.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.pojogroup.Cart;

import entity.Result;

@RestController
@RequestMapping("/cart")
public class CartController {
	
	
	//注入serveletrequest
	@Autowired
	private  HttpServletRequest request;
	//注入servletResponse
	@Autowired
	private  HttpServletResponse response;
	//注入购物车服务对象
	@Reference(timeout=40000)
	private CartService cartService;
	
	//从cookie中提取购物车
	@RequestMapping("/findCartList")
	public List<Cart> findCartList(){
		//获取当点登陆人姓名
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		System.out.println("当前登陆人:"+username);
		String cartListString = util.CookieUtil.getCookieValue(request, "cartList", "UTF-8");
		
		if(cartListString==null||cartListString.equals("")){
			cartListString="[]";
		}

		List<Cart> cartList_cookie = JSON.parseArray(cartListString,Cart.class);
		
		if(username.equals("anonymousUser")){//未登录,就从cookie中获取购物车
			System.out.println("从cookie中提取购物车");
			
			
			return cartList_cookie;	
			
		}else{//登陆
		//获取redis购物车
			List<Cart> cartListFromRedis = cartService.findCartListFromRedis(username);
			
			if(cartList_cookie.size()>0){//当cookie中有数据 才进行合并操作
				//得到合并后的购物车
				cartListFromRedis = cartService.mergeCartList(cartList_cookie, cartListFromRedis);
				
				//清空cookie中的购物车
			 	util.CookieUtil.deleteCookie(request, response, "cartList");
				
				//把合并后的购物车存到redis
				
				cartService.saveCartListToredis(username, cartListFromRedis);
			
				System.out.println("合并");
			
			}
			
		
			return  cartListFromRedis;
		}
		
		
	}
	@RequestMapping("/addGoodsToCart")
	@CrossOrigin(origins="http://localhost:9105")//注解的方式 进行js跨域 默认允许使用cookie ,allowCredentials="true"
	public Result addGoodsToCart(Long itemId,Integer num){
		
		//添加头信息,让域可以访问
	/*	response.setHeader("Access-Control-Allow-Origin", "http://localhost:9105");//(当方法不需要操作cookie)
		response.setHeader("Access-Control-Allow-Credentials", "true");//允许使用cookie,而且上面的域必须明确
*/		String name = SecurityContextHolder.getContext().getAuthentication().getName();
		System.out.println("当前登陆人:"+name);
	
		try {
			//从cookie中提取购物车
			List<Cart> cartList = findCartList();
			
			//调用服务方法操作购物车
			 cartList = cartService.addGoodsToCartList(cartList, itemId, num);
			 
			if(name.equals("anonymousUser")){//如果未登陆  存入到cookie
				
				String cartListString = JSON.toJSONString(cartList);
				util.CookieUtil.setCookie(request, response, "cartList", cartListString,3600*24, "UTF-8");
				System.out.println("保存购物车到cookie");
			}else{// 登陆存到redis
				cartService.saveCartListToredis(name, cartList);
				
			}
			
			return new Result(true, "厉害厉害");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return new Result(true, "垃圾垃圾");
		}
	
	}
}
