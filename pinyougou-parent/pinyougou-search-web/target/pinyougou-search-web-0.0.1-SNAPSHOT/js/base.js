var app=angular.module('pinyougou',[]);//不带分页的js
//定义一个文本转换html的过滤器 ,需要用到服务$sce
/*app.filter('trustHtml',['$sce',function($sce){//$sce是一个服务而已
	return function(data){ //方法参数,要被过滤的内容
		//返回过滤后的内容
		return $sce.trustAsHtml(data);
		
	}
	
}]);
*/

//定义过滤器
app.filter('trustHtml',['$sce',function($sce){
		return function(data){
			return $sce.trustAsHtml(data);
		}
	
}]);


/*
//定义过滤器
app.filter('trustHtml',['$sce' ,function($sce){ //'$sce' 调用这个服务
	
	return function(data){//传入被过滤的内容
		//返回过滤后的内容
		return $sce.trustAsHtml(data)//此方法为信任html转换
		
	}
	
	
} ]);*/