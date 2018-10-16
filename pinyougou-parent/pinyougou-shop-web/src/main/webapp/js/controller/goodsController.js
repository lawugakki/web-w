 //控制层 
app.controller('goodsController' ,function($scope,$controller   ,goodsService,$location,uploadService,itemCatService,typeTemplateService){	
	
	$controller('baseController',{$scope:$scope});//继承
	
    //读取列表数据绑定到表单中  
	$scope.findAll=function(){
		goodsService.findAll().success(
			function(response){
				$scope.list=response;
			}			
		);
	}    
	
	//分页
	$scope.findPage=function(page,rows){			
		goodsService.findPage(page,rows).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
	
	//查询实体 
	$scope.findOne=function(){	
	var id =	$location.search()['id'];   //以数组的形式获取页面上 的所有参数\

		if(id==null){
			return;
		}
		goodsService.findOne(id).success(
			function(response){
				$scope.entity= response;	
				editor.html($scope.entity.goodsDesc.introduction);//商品介绍,显示富文本内容
				//商品图片
				$scope.entity.goodsDesc.itemImages=JSON.parse(	$scope.entity.goodsDesc.itemImages);
				//扩展属性
				$scope.entity.goodsDesc.customAttributeItems=JSON.parse($scope.entity.goodsDesc.customAttributeItems);
				//规格选择
				
				$scope.entity.goodsDesc.specificationItems=JSON.parse($scope.entity.goodsDesc.specificationItems);
				//转换SKU列表中的规格对象
				for(var i=0;i<$scope.entity.itemList.length;i++){
				//itemList中每一行都是下面这样的
					//[{"attributeValue":["移动4G"],"attributeName":"网络"},{"attributeValue":["32G","128G"],"attributeName":"机身内存"}]
				//需要把每一行都转换成json格式,才能保存进去
					
					
					$scope.entity.itemList[i].spec =	 JSON.parse($scope.entity.itemList[i].spec);
				}
				
			}
		);			
	}
	//新增保存方法
	$scope.save=function(){
		$scope.entity.goodsDesc.introduction=editor.html();//获取文本域信息
		var serviceObject;//服务层对象
		
		//判断是执行修改还是增加
		if($scope.entity.goods.id!=null){
			//有id  说明是修改
			serviceObject=goodsService.update($scope.entity);//
		}else{//增加
			serviceObject=goodsService.add($scope.entity);//
		}
		serviceObject.success(
				function(response){
					if(response.success){
			alert("新增成功");
			location.href="goods.html"; //修改成功后返回到商品页
		/*	$scope.entity={};
			editor.html("");//清空富文本编辑器
*/					}else{
						alert(response.message);
					}
			}
				);
	}
	
	//增加商品
	/*$scope.add=function(){				
	
		//把文本域中的值取出来给从组合实体类$scope.entity中取出里面的商品信息
	//	$scope.entity.goodsDesc.introduction=editor.html();
		$scope.entity.goodsDesc.introduction=editor.html();
					
		goodsService.add( $scope.entity  ).success(
			function(response){
				if(response.success){
					alert("新增成功");
					//增加成功后把表格置空,方便继续添加
					$scope.entity={};
					editor.html('');//清空富文本编辑器
				}else{
					alert(response.message);
				}
			}		
		);				
	}*/
	
	 
	//批量删除 
	$scope.dele=function(){			
		//获取选中的复选框			
		goodsService.dele( $scope.selectIds ).success(
			function(response){
				if(response.success){
					$scope.reloadList();//刷新列表
					$scope.selectIds=[];
				}						
			}		
		);				
	}
	
	$scope.searchEntity={};//定义搜索对象 
	
	//搜索
	$scope.search=function(page,rows){			
		goodsService.search(page,rows,$scope.searchEntity).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
    //文件上传

	 
		$scope.uploadFile=function(){	  
			uploadService.uploadFile().success(function(response) { 
				
	        	if(response.success){//如果上传成功，取出url
	        		//alert($scope.image_entity.url);
	        		$scope.image_entity.url=response.message;//设置文件地址
	        	}else{
	        		alert(response.message);
	        	}
	        }).error(function() {           
	        	     alert("上传发生错误");
	        });        
	    };    
	$scope.entity={goods:{},goodsDesc:{itemImages:[],specificationItems:[]}}//定义页面实体结构
	//添加图片列表
	$scope.add_image_entity=function(){
		$scope.entity.goodsDesc.itemImages.push($scope.image_entity);
	}
	
	
	
	//删除图片方法
	 
	$scope.remove_image_entity=function(index){
		 $scope.entity.goodsDesc.itemImages.splice(index,1);
	 }
	
	//查询一级商品分类列表
	$scope.selectItemCat1List=function(){//一级列表id为0
		itemCatService.findByParentId(0).success(
				function(response){//返回结果response就是当前的列表
					$scope.itemCat1List=response;
			
				
			
		});
		
		
	}//新的方法 ,当数据发生变化,就会执行此方法
	//用于查询二级商品分类
$scope.$watch('entity.goods.category1Id',function(newValue,oldValue){
	if(newValue==oldValue || newValue==null || newValue=="undefined"){
		return;
	}
	itemCatService.findByParentId(newValue).success(
			function(response){//返回结果response就是当前的列表
				$scope.itemCat2List=response;
		
			
		
	});
	
	
});
//查询三级商品分类
$scope.$watch('entity.goods.category2Id',function(newValue,oldValue){
	if(newValue==oldValue || newValue==null || newValue=="undefined"){
		return;
	}
	itemCatService.findByParentId(newValue).success(
			function(response){//返回结果response就是当前的列表
				$scope.itemCat3List=response;
		
			
		
	});
	
	
});
//读取模板id
$scope.$watch('entity.goods.category3Id',function(newValue,oldValue){
	if(newValue==oldValue || newValue==null || newValue=="undefined"){
		return;
	}
	
	//通过监视列表3 获取到列表3的id,然后根据列表3的id 读取
	itemCatService.findOne(newValue).success(
			function(response){
				$scope.entity.goods.typeTemplateId=response.typeId;
				
			}
			
	);
			
		
	
	
});
//读取模板id后读取品牌列表
$scope.$watch('entity.goods.typeTemplateId',function(newValue,oldValue){
	if(newValue==oldValue || newValue==null || newValue=="undefined"){
		return;
	}

	typeTemplateService.findOne(newValue).success(
			
			function(response){
				//response是商品的全部属性
				$scope.typeTemplate=response;
				
				//获取商品的id,由于商品id是String类型,还需要转换为json类型
				$scope.typeTemplate.brandIds=  JSON.parse($scope.typeTemplate.brandIds);
				//扩展属性
				if($location.search()['id']==null){
					//商品的扩展属性,在修改和增加的时候都需要转换,
					//如果id为null,说明是在增加,执行此方法.
					//在不为null的情况下,是在进行修改.,这句话就不能被执行,要不就修改不了
				$scope.entity.goodsDesc.customAttributeItems=JSON.parse($scope.typeTemplate.customAttributeItems);	
				}
	});	
	typeTemplateService.findSpecList(newValue).success(
			function(response){
				$scope.specList=response;
			}
		);
});	

//保存规格方法,需要保存到数据库的为规格,以及勾选的属性
	$scope.updateSpecAttribute=function($event,name,value){
		//先用base.js中的方法判断是否集合中是否有东西,就是数据库中的规格列表,它本身就是一个集合
		//方法会返回集合
		
		/*[{"attributeName":"网络","attributeValue":["移动3G","移动4G"]},{"attributeName":"颜色","attributeValue":["红色","黑色"]}]
		"attributeName"
		"机身内存"*/
		var object=$scope.searchObjectByKey($scope.entity.goodsDesc.specificationItems,'attributeName',name);
		//如果返回的集合不为null,说明最起码网络是存在的,只需要往value里面追加属性就行了
		if(object!=null){
			//判断选择状态,从而决定是要勾选还是要取消勾选
			if($event.target.checked){//选择
				object.attributeValue.push(value);
			}else{//取消勾选			
				object.attributeValue.splice(object.attributeValue.indexOf(value),1);//移除选项
				//如果勾选的全部取消掉,前面的规格也不用要了
				//就从集合中移除这个属性就行了
				if(object.attributeValue.length==0){
					$scope.entity.goodsDesc.specificationItems.splice($scope.entity.goodsDesc.specificationItems.indexOf(object),1);
				}
			}
			
		
			
			}else{
				//如果返回的集合为空,说明这项规格就一直没有被点击过,这时候点击需要把前面的网络还有后面的勾选属性都push进去
				$scope.entity.goodsDesc.specificationItems.push({"attributeName":name,"attributeValue":[value]});
			}
		
	}
	
	//创建SKU列表
	$scope.createItemList=function(){
		$scope.entity.itemList=[{spec:{},price:0,num:99999,status:0,isDefault:'0' }];//列表初始化
		
		var items= $scope.entity.goodsDesc.specificationItems;
		
		for(var i=0;i<items.length;i++){
			$scope.entity.itemList=	addColumn($scope.entity.itemList,items[i].attributeName,items[i].attributeValue);
		}
	}
		addColumn=function(list,columnName,columnValues){
			//私有方法不用加$scope,在调用的时候也不用加
			var newList=[];
			for(var i=0;i<list.length;i++){
				var oldRow=list[i];
				
				for(var j=0;j<columnValues.length;j++){
					//对旧行进行深克隆
					var newRow = JSON.parse(JSON.stringify(oldRow));//两个不同的对象,内容是一样的
					newRow.spec[columnName]=columnValues[j];//产生新的行
					newList.push(newRow);
				}
			}
			
			return newList;
		}
		$scope.status=['未审核','已审核','审核未通过','已关闭'];//设置审核状态,用于网页显示
		//定义数组,用于封装返回的视频列表
		$scope.itemCatList=[];
		//查询商品分类列表
		$scope.findItemCatList=function(){
			//调用方法,查询所有的商品分类
			itemCatService.findAll().success(
			function(response){
				for(var i=0;i<response.length;i++){
					$scope.itemCatList[response[i].id]=response[i].name;
				}
			}		
			);
		}
		
		
		//
		//[{"attributeValue":["移动4G"],"attributeName":"网络"},{"attributeValue":["32G","128G"],"attributeName":"机身内存"}]
		//判断规格与规格选项是否被勾选
		$scope.checkAttributeValue=function(specName,optionName){ //此方法作用于checkbox的选中状态,如果为true则为选中,false为不选中
			var items = $scope.entity.goodsDesc.specificationItems;//转换后的规格集合
			
			
			//判断集合是否存在的方法,要传入三个参数
			//会返回一个object对象
			var object =$scope.searchObjectByKey(items,'attributeName',specName);
			//判断对象是否为null
			if(object!=null){
				//如果查到的集合不为null
				if(object.attributeValue.indexOf(optionName)>=0){//如果能查询到规格选项
					return true
					
				}else{//没有规格选项
					return false;
				}
			}else{
				return false;//说明集合中什么也没有,页面中什么都不需要显示
			}
				
			
			return true;
		}
});	
