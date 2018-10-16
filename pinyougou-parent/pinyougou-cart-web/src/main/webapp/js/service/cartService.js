app.service('cartService',function($http){
	//购物车列表
	this.findCartList=function(){
		return $http.get('cart/findCartList.do');		
	}
	
	//添加商品到购物车
	this.addGoodsToCart=function(itemId,num){
		return $http.get('cart/addGoodsToCart.do?itemId='+itemId+'&num='+num);
	}
	
	
	this.sum=function(cartList){		
		var totalValue={totalNum:0, totalMoney:0.00 };//合计实体
		for(var i=0;i<cartList.length;i++){
			var cart=cartList[i];
			for(var j=0;j<cart.orderItemList.length;j++){
				var orderItem=cart.orderItemList[j];//购物车明细
				totalValue.totalNum+=orderItem.num;
				totalValue.totalMoney+= orderItem.totalFee;
			}				
		}
		return totalValue;
	}
	//获取当前登录账号的地址
	this.findAddressList=function(){
		
		return $http.get('address/findListByLoginUser.do');
	}
	
	//提交订单
	this.submitOrder=function(order){
		return $http.post('order/add.do',order);	  //穿对象过去 所以需要用post方法	
	}
	
	
	//
});