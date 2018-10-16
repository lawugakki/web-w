app.service('uploadService',function($http){
	
		//上传文件的方法
	this.uploadFile=function(){
		var formData=new FormData();
		formData.append('file',file.files[0]);//file 文件上传框的name
		
		return $http({
		
			method:'post',
			url:'../upload.do',
			data:formData,
			headers:{'Content-Type':undefined},//上传文件必须指定为undefined,否则默认是json类型
			 transformRequest: angular.identity//对表单进行二进制序列化
			
		}
				
		
		);
		
		
	}
	
	
});