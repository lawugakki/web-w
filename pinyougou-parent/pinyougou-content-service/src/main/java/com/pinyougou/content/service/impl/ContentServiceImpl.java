package com.pinyougou.content.service.impl;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbContentMapper;
import com.pinyougou.pojo.TbContent;
import com.pinyougou.pojo.TbContentExample;
import com.pinyougou.pojo.TbContentExample.Criteria;
import com.pinyougou.content.service.ContentService;

import entity.PageResult;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class ContentServiceImpl implements ContentService {

	@Autowired
	private TbContentMapper contentMapper;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbContent> findAll() {
		return contentMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbContent> page=   (Page<TbContent>) contentMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbContent content) {
		contentMapper.insert(content);	
		//清除缓存
		
		redisTemplate.boundHashOps("content").delete(content.getCategoryId());
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbContent content){
		
		//查询出原来的分组id
		Long categoryId = contentMapper.selectByPrimaryKey(content.getId()).getCategoryId();
		//清除原来的缓存
		redisTemplate.boundHashOps("content").delete(categoryId);
		contentMapper.updateByPrimaryKey(content);
		//清除现分组的缓存
		if(categoryId.longValue()!=content.getCategoryId().longValue()){
			redisTemplate.boundHashOps("content").delete(content.getCategoryId());
		}
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbContent findOne(Long id){
		return contentMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			//在删除之前清掉缓存
			//先通过id查出广告列表,再通过列表Id查出是那个广告,然后从缓存中删掉
			Long categoryId = contentMapper.selectByPrimaryKey(id).getCategoryId();
			redisTemplate.boundHashOps("content").delete(categoryId);
			contentMapper.deleteByPrimaryKey(id);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbContent content, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbContentExample example=new TbContentExample();
		Criteria criteria = example.createCriteria();
		
		if(content!=null){			
						if(content.getTitle()!=null && content.getTitle().length()>0){
				criteria.andTitleLike("%"+content.getTitle()+"%");
			}
			if(content.getUrl()!=null && content.getUrl().length()>0){
				criteria.andUrlLike("%"+content.getUrl()+"%");
			}
			if(content.getPic()!=null && content.getPic().length()>0){
				criteria.andPicLike("%"+content.getPic()+"%");
			}
			if(content.getStatus()!=null && content.getStatus().length()>0){
				criteria.andStatusLike("%"+content.getStatus()+"%");
			}
	
		}
		
		Page<TbContent> page= (Page<TbContent>)contentMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}
		//引入RedisTemplate
		
		@Autowired
		private RedisTemplate redisTemplate; 
		
		
		//根据广告分类id查询 广告分类下的列表
		@Override
		public List<TbContent> findByCategoryId(Long categoryId) {
			//把广告存入到redis中 Hash类型
			List<TbContent> list= (List<TbContent>) redisTemplate.boundHashOps("content").get(categoryId);
			if(list==null){
				System.out.println("从数据库查找出来并放入缓存");
				TbContentExample example= new TbContentExample();
				Criteria criteria2 = example.createCriteria();
				criteria2.andCategoryIdEqualTo(categoryId);//指定条件,分类id
				criteria2.andStatusEqualTo("1");//指定条件有效的广告
				example.setOrderByClause("sort_order");//根据广告排序进行分类
				 list = contentMapper.selectByExample(example);
				
				 redisTemplate.boundHashOps("content").put(categoryId, list);//把广告放入缓存
			
				
			}else{
			
				System.out.println("缓存中已经有了");
			}
			
	
			return list;
		}
	
}
