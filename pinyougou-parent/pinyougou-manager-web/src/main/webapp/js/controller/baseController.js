app.controller('baseController',function($scope){
	//把共性的控制层代码抽取出来

		//刷新列表
	$scope.reloadList=function(){
		$scope.search( $scope.paginationConf.currentPage ,  $scope.paginationConf.itemsPerPage);
	}
	
	//分页控件配置currentPage:当前页   totalItems :总记录数  itemsPerPage:每页记录数  perPageOptions :分页选项  onChange:当页码变更后自动触发的方法 
	$scope.paginationConf = {
		currentPage: 1,
		totalItems: 10,
		itemsPerPage: 10,
		perPageOptions: [10, 20, 30, 40, 50],
		onChange: function(){
			$scope.reloadList();
		}
	};
	

	 $scope.selectIds=[];//用户勾选到的id集合
		$scope.updateSelection=function($event,id){ //此方法作用于复选框,用于统计id
			if($event.target.checked){
				$scope.selectIds.push(id);//push方法向集合中添加元素
			}else{
			var index =	$scope.selectIds.indexOf(id);//查找值的位置
			$scope.selectIds.splice(index,1);//index 移除的位置.1,移除一个
			}
		
		 
		} 
		
		$scope.jsonToString=function(jsonString,key){
		var json=	JSON.parse(jsonString);
		var value= "";
		
		
		for(var i =0;i<json.length;i++){
			if(i>0){
				value +=",";
			}
		value +=	json[i][key];
		}
		
		return value;
		}
});