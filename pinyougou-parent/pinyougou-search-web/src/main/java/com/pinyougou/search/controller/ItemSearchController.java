package com.pinyougou.search.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.search.service.ItemSearchService;

@RestController
@RequestMapping("/itemsearch")
public class ItemSearchController {
	//注入服务层 
	@Reference  //防止超时  ,也可以在服务层的 @Service 加这个属性
	private ItemSearchService itemSearchService;
	@RequestMapping("/search")
	public Map search(@RequestBody Map searchMap){
	
		return itemSearchService.search(searchMap);
	}
}
