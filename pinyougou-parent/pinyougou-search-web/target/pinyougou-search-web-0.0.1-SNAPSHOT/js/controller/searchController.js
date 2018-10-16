app.controller(
				'searchController',
				function($scope, searchService,$location) {

					// 定义搜索对象的结构
					$scope.searchMap = {
						'keywords' : '',
						'category' : '',
						'brand' : '',
						'spec' : {},
						'price' : '',
						'pageNo' : 1,// 起始页
						'pageSize' : 40,// 每页的尺寸
						'sort' : '',// 分类升序或者降序
						'sortField' : ''// 排序字段
					};

					$scope.search = function() {
						// 把pageNO转换为Integer类型
						$scope.searchMap.pageNo = parseInt($scope.searchMap.pageNo);
						searchService.search($scope.searchMap).success(
								function(response) {
									$scope.resultMap = response;

									bulidPageLabel(); // 构建分页栏
									// 查询后显示第一页

								});
					}
					// 分页
					bulidPageLabel = function() {
						// 构建分页栏
						$scope.pageLabel = [];
						var firstPage = 1;// 设置页面显示开始页
						var lastPage = $scope.resultMap.totalPages;// 页面显示的截至页码
						$scope.firstDot = true;// 前面有点
						$scope.lastDot = true;// 后面有点

						if ($scope.resultMap.totalPages > 5) {// 如果总页码大于5
							if ($scope.resultMap.pageNo <= 3) {// 当前显示的页码小于3
								lastPage = 5;// 固定显示的总页数为5
								// 如果是在前五页,就让前面没有点
								$scope.firstDot = false;

							} else if ($scope.resultMap.pageNo >= $scope.resultMap.totalPages - 2) {// 如果当前显示的页码大于总页码-2
								// lastPage=$scope.getTotalPages;
								firstPage = $scope.resultMap.totalPages - 4;
								// 如果实在后五页,就让后面没点
								$scope.lastDot = false;

							} else {// 正常情况下 ,起始页就是当前页-2 结束页就是当前页+2
								// 即显示以当前页为中心的五页
								firstPage = $scope.resultMap.pageNo - 2;
								lastPage = $scope.resultMap.pageNo + 2;

							}
						} else {// 在页数小于五的情况下 ,前面后面都没点
							$scope.firstDot = false;// 前面有点
							$scope.lastDot = false;// 后面有点
						}
						for (i = firstPage; i <= lastPage; i++) {
							$scope.pageLabel.push(i);
						}
					}

					// 添加搜索项,改变searchMap的值
					$scope.addSearchItem = function(key, value) {
						if (key == 'category' || key == 'brand'
								|| key == 'price') {// 如果点击的是分类或者 是品牌
							$scope.searchMap[key] = value;
						} else {// 剩下的就是规格
							$scope.searchMap.spec[key] = value;
						}
						$scope.search();
					}

					$scope.removeSearchItem = function(key) {
						if (key == 'category' || key == 'brand'
								|| key == 'price') {// 如果点击的是分类或者 是品牌
							$scope.searchMap[key] = "";
						} else {// 剩下的就是规格
							delete $scope.searchMap.spec[key];
						}
						$scope.search();
					}

					// 分页查询,根据pageNo来进行查询
					$scope.queryByPage = function(pageNo) {
						// 如果是第一页或者是最后一页 就不要查询了
						if (pageNo < 1 || pageNo > $scope.searchMap.totalPages) {
							return;
						}

						// 让传进来的pageNo 等于searchMap中的pageNo ,然后进行查询
						$scope.searchMap.pageNo = pageNo;
						$scope.search();
					}

					// 判断当前页是不是首页
					$scope.isTopPage = function() {
						if ($scope.searchMap.pageNo == 1) {
							return true;
						} else {
							return false;
						}
					}
					// 在网页上引用为:三元运算符判断是不是首页 然后对上一页进行置灰 但是我不用 :)
					// <li class="prev {{isTopPage()}}?'disable:'''" >

					// 判断当前页是不是尾页
					$scope.isEndPage = function() {
						if ($scope.searchMap.pageNo == $scope.resultMap.totalPages) {
							return true;
						} else {
							return false;
						}

					}
					// 排序查询
					$scope.sortSearch = function(sortField, sort) {
						$scope.searchMap.sortField = sortField;
						$scope.searchMap.sort = sort;
						$scope.search();// 查询
					}
					// 判断关键字 是否是品牌
					$scope.keywordsIsBrand = function() {
						// 遍历品牌礼拜
						for (var i = 0; i < $scope.resultMap.brandList.length; i++) {
							// indexOf 判断当前对象是不是 ()内的子对象
							if ($scope.searchMap.keywords
									.indexOf($scope.resultMap.brandList[i].text) >= 0) {

								// 如果搜索的关键字中有品牌表里面的东西 ,返回true
								return true;
							}

						}
						return false;
					}
					
					//接受首页传来的参数
					$scope.loadkeywords=function(){
					$scope.searchMap.keywords =	$location.search()['keywords'];
					$scope.search();
						
					}
				});
