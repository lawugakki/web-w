package com.pinyougou.sellergoods.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbBrandMapper;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.pojo.TbBrandExample;
import com.pinyougou.pojo.TbBrandExample.Criteria;
import com.pinyougou.sellergoods.service.BrandService;

import entity.PageResult;

@Service
public class BrandServiceImpl implements BrandService{
		@Autowired
		private TbBrandMapper brandMapper;

		@Override
		public List<TbBrand> findAll() {
			// TODO Auto-generated method stub
			return brandMapper.selectByExample(null);
		}

		@Override
		public PageResult findPage(int pageNum, int pageSize) {
			//实现分页功能,需要使用分页插件
			PageHelper.startPage(pageNum, pageSize);//分页
			Page<TbBrand> page = (Page<TbBrand>) brandMapper.selectByExample(null);
			
			return new PageResult(page.getTotal(), page.getResult());
		}

		@Override
		public void add(TbBrand brand) {
			
			//实现品牌的增加功能
			brandMapper.insert(brand);
			
		}

		@Override
		public TbBrand findOne(Long id) {
			// 根据id查询 出来对应的商品
		return	brandMapper.selectByPrimaryKey(id);
		
		}

		@Override
		public void update(TbBrand brand) {
			// 保存
			brandMapper.updateByPrimaryKey(brand);
		}

		@Override
		public void delete(Long[] ids) {
			//复选删除,遍历删除每一个id
			for (Long id : ids) {
				brandMapper.deleteByPrimaryKey(id);
			}
			
		}

		@Override
		public PageResult findPage(TbBrand brand, int pageNum, int pageSize) {
			
			PageHelper.startPage(pageNum, pageSize);//分页
			TbBrandExample example = new TbBrandExample();//创建查询条件
			Criteria criteria = example.createCriteria();
			if(brand!=null){
				if(brand.getName()!=null&&brand.getName().length()>0){
					criteria.andNameLike("%"+brand.getName()+"%");//根据商品名字模糊查询
				}
				if(brand.getFirstChar()!=null&&brand.getFirstChar().length()>0){
					criteria.andFirstCharLike("%"+brand.getFirstChar()+"%");//根据字母模糊查询
				}
			}
			Page<TbBrand> page = (Page<TbBrand>) brandMapper.selectByExample(example);
			
			return new PageResult(page.getTotal(), page.getResult());
		}

		@Override
		public List<Map> selectOptionList() {
			// TODO Auto-generated method stub
			return brandMapper.selectOptionList();
		}

		
}
