app.controller('seckillGoodsController',function($scope,$location,seckillGoodsService,$interval){
	
		 $scope.findList=function(){
			 seckillGoodsService.findList().success(
					 function(response){
						 $scope.list=response;
					 }
			 );
			 
		 }
	
		 //从缓存中读取数据
		 $scope.findOne=function(id){
			 //接受参数id
			 var  id = $location.search()['id'];
			 seckillGoodsService.findOne(id).success(
					 function(response){
						 $scope.entity=response;
							
						 
					 }
			 );
		 }
		 
		 
	/*
	$scope.second = 10; 
			time= $interval(function(){ 
			  if($scope.second>0){ 
				$scope.second =$scope.second-1;  			
			  }else{
				  $interval.cancel(time); 		  
				  alert("秒杀服务已结束");
			  }
			},1000);*/
		 
		 
		 $scope.submitOrder=function(){
			 seckillGoodsService.submitOrder($scope.entity.id).success(
					 function(response){
						 if(response.success){//下单成功 ,跳转到支付页面
							 alert("先涨价再降价,你就买吧");
							 location.href="pay.html";
							 
						 }else{
							 alert(response.message);
						 }
						
						 
					 }
			 );
			 
		 }
		 
		 
});