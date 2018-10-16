package com.pinyougou.manager.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.sellergoods.service.BrandService;

import entity.PageResult;
import entity.Result;

@RestController
@RequestMapping("/brand")
public class BrandController {
			
	
	@Reference
	private BrandService brandService;
	/**
	 * 返回全部列表
	 * @return
	 */
	@RequestMapping("/findAll")
	public List<TbBrand> findAll(){			
		return brandService.findAll();
	}
	@RequestMapping("/findPage")
	public PageResult findPage(int page,int size){
		
		return brandService.findPage(page, size);
	}
	
	@RequestMapping("/add")
	//数据是从前段通过post穿过来的 要使用@RequestBody
	public Result add (@RequestBody TbBrand brand){
		//使用mapper中的添加方法
		try {
			brandService.add(brand);
			return new Result(true, "增加成功");
		} catch (Exception e) {
		
			e.printStackTrace();
			return new Result(false, "增加失败");
		}
	}
	@RequestMapping("/findOne")
	public TbBrand findOne(Long id){
		return brandService.findOne(id);
	}
	@RequestMapping("/update")
	public Result update(@RequestBody TbBrand brand){
		try {
			brandService.update(brand);
			return new Result(true, "修改成功");
		} catch (Exception e) {
		
			e.printStackTrace();
			return new Result(false, "修改失败");
		}
		
	}
	@RequestMapping("/delete")
	public Result delete(Long[] ids){
		try {
			brandService.delete(ids);
			return new Result(true, "删除成功");
		} catch (Exception e) {
		
			e.printStackTrace();
			return new Result(false, "删除失败");
		}
		
	}
	@RequestMapping("/serach") 
	//传递进来查询条件
	public PageResult serach(@RequestBody TbBrand brand,int page, int size){
		return  brandService.findPage(brand,page, size);
	}
	@RequestMapping("/selectOptionList") 
	public List<Map> selectOptionList(){
		
		return brandService.selectOptionList();
	}
}
