app.controller('cartController', function($scope, cartService) {
	// 查询购物车列表
	$scope.findCartList = function() {
		cartService.findCartList().success(function(response) {
			$scope.cartList = response;
			$scope.totalValue = cartService.sum($scope.cartList);

			// sum();//求合计数
		});
	}

	// 添加商品到购物车
	$scope.addGoodsToCart = function(itemId, num) {
		// alert(2334);
		cartService.addGoodsToCart(itemId, num).success(function(response) {
			if (response.success) {
				$scope.findCartList();// 刷新列表
			} else {
				alert(response.message);
			}
		}

		);
	}

	// 获取当前用户的地址列表
	$scope.findAddressList = function() {
		cartService.findAddressList().success(function(response) {
			$scope.addressList = response;

			for (var i = 0; i < $scope.addressList.length; i++) {
				if ($scope.addressList[i].isDefault == '1') {
					$scope.address = $scope.addressList[i];
					break;
				}
			}

		}

		);

	}
	$scope.selectAddress = function(address) {
		$scope.address = address;
	}

	// 判断是否是当前选中的地址
	$scope.isSelectedAddress = function(address) {
		if (address == $scope.address) {
			return true;
		} else {
			return false;
		}
	}
	//定义订单对象
	$scope.order={paymentType:'1'};
	//选择支付类型
	$scope.selectPayType=function(type){
		$scope.order.paymentType=type;
	}
	
	
	
	//保存订单
	$scope.submitOrder=function(){
		
		$scope.order.receiverAreaName=$scope.address.address;//地址
		$scope.order.receiverMobile=$scope.address.mobile;//手机
		$scope.order.receiver=$scope.address.contact;//联系人
		
		cartService.submitOrder($scope.order).success(
				function(response){
					if(response){
					//	alert(response.message);
						if(response.success){
							//跳转到支付页
							if($scope.order.paymentType=='1'){//微信支付 跳转到支付页面
								location.href="pay.html";
							}else{//其他付款方式 		
								location.href="paysuccess.html";
							}
						}else{
							alert(response.message);
						}
					}
				}
		);
			
		
			
		
	}
});