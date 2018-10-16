app.controller('brandController',function($scope,$http,$controller,brandService){
		$controller('baseController',{$scope:$scope});//伪继承
			
			//查询品牌列表
			$scope.findAll=function(){
				brandService.findAll().success(
					function(response){
						$scope.list=response;
					}		
				);				
			}
			
			
			
			//分页 
			$scope.findPage=function(page,size){
				brandService.findPage(page,size).success(
					function(response){
						$scope.list=response.size;//显示当前页数据 	
						$scope.paginationConf.totalItems=response.total;//更新总记录数 
					}		
				);				
			}
			//实现新增功能
			$scope.save=function(){				//entity是要在页面上绑定的变量 
	//将方法的名字定义为一个变量,既能实现保存,又能实现修改
				var object=null;//保存方法
				//if进行判断,如果点击的id不为null,说明是已存在的商品,就是执行修改操作
				//也可以通过在后端进行id 判断,根据判断结果,执行不同的sql方法
				if($scope.entity.id!=null){
					object =brandService.update($scope.entity);
				}else{
					object =brandService.add($scope.entity);
				}
				object.success(
				function(response){
					//判断添加的状态
					if(response.success){
						//添加成功,静态刷新页面
						$scope.reloadList();
					}else{
						//失败给出添加失败提示
						alert(response.message);
					}
				}		
				
				);
			}
			//查询实体
			$scope.findOne=function(id){
				brandService.findOne(id).success(
					function(response){
						//双向绑定,在这里修改 下面的entity也会变化
						$scope.entity=response;
						
					}		
				
				);
				
			}
		
			 //删除
		$scope.dele=function(){
			brandService.dele($scope.selectIds).success(
				function(response){
					if(response.success){
						$scope.reloadList();
					}else{
						alert(response.message);
					}
				}		
			
			);
		}
		//条件查询
		$scope.searchEntity={};
		$scope.search=function(page,size){
			brandService.search(page,size,$scope.searchEntity).success(
					function(response){
						$scope.list=response.rows;//显示当前页数据 	
						$scope.paginationConf.totalItems=response.total;//更新总记录数 
					}		
				);		
		}
 	});