package com.pinyougou.sellergoods.service;

import java.util.List;
import java.util.Map;

import com.pinyougou.pojo.TbBrand;

import entity.PageResult;

public interface BrandService {
	public List<TbBrand> findAll();
//	品牌分页
//
	public PageResult findPage(int pageNum,int pageSize);
//增加品牌
	public void add(TbBrand brand);
//修改品牌,需要先查出来 再存回去
	//根据id查询实体
	public TbBrand findOne(Long id);
	//把修改过的实体存回去
	public void update (TbBrand brand);
//删除品牌
	public void delete(Long[] ids);
//根据条件查询
	public PageResult findPage(TbBrand brand, int pageNum,int pageSize); 
	
//返回下拉列表数据
	public List<Map> selectOptionList();
}
