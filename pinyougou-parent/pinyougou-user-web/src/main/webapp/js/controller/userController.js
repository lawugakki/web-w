 //控制层 
app.controller('userController' ,function($scope,$controller   ,userService){	
	
	
	//注册
$scope.reg=function(){
	//判断两次密码是否相同
	if($scope.entity.password!=$scope.password){
		alert("两次密码不同,请重新输入");
		$scope.entity.password="";
		$scope.password="";
		
		return;
	}
	
	//新增方法
	userService.add($scope.entity,$scope.smscode).success(
			function(response){
				alert(response.message);
			}
	);
	
}
	//发送短信
$scope.sendCode=function(){
	
	if($scope.entity.phone==null||$scope.entity.phone==""){
		alert("请输入手机号");
		return ;
	}
	userService.sendCode($scope.entity.phone).success(
			function(response){
				alert(response.message);
			}
			
	);
}
	
    
});	
