 //控制层 
app.controller('itemCatController' ,function($scope,$controller   ,itemCatService){	
	
	$controller('baseController',{$scope:$scope});//继承
	
    //读取列表数据绑定到表单中  
	$scope.findAll=function(){
		itemCatService.findAll().success(
			function(response){
				$scope.list=response;
			}			
		);
	}    
	
	//分页
	$scope.findPage=function(page,rows){			
		itemCatService.findPage(page,rows).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
	
	//查询实体 
	$scope.findOne=function(id){				
		itemCatService.findOne(id).success(
			function(response){
				$scope.entity= response;					
			}
		);				
	}
	
	//保存 
	$scope.save=function(){				
		var serviceObject;//服务层对象  				
		if($scope.entity.id!=null){//如果有ID
			serviceObject=itemCatService.update( $scope.entity ); //修改  
		}else{
			//把点击的id赋值给当前商品的上级id
			$scope.entity.parentId=$scope.parentId;
			
			
			serviceObject=itemCatService.add( $scope.entity  );//增加 
		}				
		serviceObject.success(
			function(response){
				if(response.success){
					//重新查询 
					
	       // 	$scope.reloadList();//重新加载
	        	
	        	$scope.findByParentId($scope.parentId);
				}else{
					alert(response.message);
				}
			}		
		);				
	}
	
	 
	//批量删除 
	$scope.dele=function(){			
		//获取选中的复选框			
		itemCatService.dele( $scope.selectIds ).success(
			function(response){
				if(response.success){
					
				 // 	$scope.findByParentId($scope.parentId);
					//$scope.reloadList();//刷新列表
					window.location.href=window.location.href;
					$scope.selectIds=[];
				}						
			}		
		);				
	}
	
	$scope.searchEntity={};//定义搜索对象 
	
	//搜索
	$scope.search=function(page,rows){			
		itemCatService.search(page,rows,$scope.searchEntity).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
   //根据上级分类查询商品列表
	
	//自定义上级id
	$scope.parentId=0;
	$scope.findByParentId=function(parentId){
		
		$scope.parentId=parentId;//记住上级id
		
		itemCatService.findByParentId(parentId).success(
				function(response){
					
					$scope.list=response; //返回的就是要的
				}
		
		);
		
	}
	$scope.grade=1;//当前级别,进图级别肯定是一
	//设置级别
	$scope.setGrade=function(value){
		$scope.grade=value;
	}
	
	//创建方法用于判断级别,根据id的不同结果也不同
	$scope.selectList=function(p_entity){
		
		//if判断
		if($scope.grade==1){
			//如果是第一等级,导航栏只显示第一级别的名称,下面两个级别为null;
			$scope.entity_1 = null;
			$scope.entity_2 = null;
		}
		if($scope.grade==2){
			//第二级别,显示当前第二级别的名字,不显示第三级别
			$scope.entity_1= p_entity;
			$scope.entity_2= null;
		}
		if($scope.grade==3){
			//第三级别,都显示(2不用进行变更)
			$scope.entity_2=p_entity;
		}
		//展示出对应的商品
		$scope.findByParentId(p_entity.id);
	}
	
	

	
});	
