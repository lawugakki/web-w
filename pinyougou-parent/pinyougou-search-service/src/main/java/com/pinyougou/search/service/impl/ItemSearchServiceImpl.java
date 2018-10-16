package com.pinyougou.search.service.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Criteria;
import org.springframework.data.solr.core.query.FilterQuery;
import org.springframework.data.solr.core.query.GroupOptions;
import org.springframework.data.solr.core.query.HighlightOptions;
import org.springframework.data.solr.core.query.HighlightQuery;
import org.springframework.data.solr.core.query.Query;
import org.springframework.data.solr.core.query.SimpleFilterQuery;
import org.springframework.data.solr.core.query.SimpleHighlightQuery;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.data.solr.core.query.SolrDataQuery;
import org.springframework.data.solr.core.query.result.GroupEntry;
import org.springframework.data.solr.core.query.result.GroupPage;
import org.springframework.data.solr.core.query.result.GroupResult;
import org.springframework.data.solr.core.query.result.HighlightEntry;
import org.springframework.data.solr.core.query.result.HighlightEntry.Highlight;
import org.springframework.data.solr.core.query.result.HighlightPage;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;

@Service(timeout=5000)
public class ItemSearchServiceImpl implements ItemSearchService {
	public static void main(String[] args) {
		String replace = "as df as".replace(" ", "");
		System.out.println(replace);
	}
	@Autowired
	private SolrTemplate solrTemplate;
	@Override
	public Map search(Map searchMap) {
		
		
		Map map = new HashMap();
		
		
		//空格处理
		String keywords = (String) searchMap.get("keywords");
		searchMap.put("keywords", keywords.replace(" ", ""));//关键字去空格
		//1查询高亮列表方法
		map.putAll(searchList(searchMap)); //把方法返回的map集合 直接追加到新的map中
		//2查询分组列表方法
		List<String> categoryList = searchCategoryList(searchMap);
		map.put("categoryList", categoryList );
		
		//3查询品牌和规格列表
		String category  = (String) searchMap.get("category");
		if(!category.equals("")){
			map.putAll(searchBrandAndSpecList(category)); //category 要根据点击规格的变化而
		}else{

			if(categoryList.size()>0){
				
				map.putAll(searchBrandAndSpecList(categoryList.get(0)));
			}
		}
				
		
		return map;
	}
	
	//查询列表方法
	private Map searchList(Map searchMap){
		Map map = new HashMap();
		//高亮显示初始化
		HighlightQuery query = new SimpleHighlightQuery();//高亮query
		
		//构建高亮选项对象
		HighlightOptions highlightOptions = new HighlightOptions().addField("item_title");//表示在这个域上进行高亮	
		highlightOptions.setSimplePrefix("<em style='color:red'>");//设置前缀
		highlightOptions.setSimplePostfix("</em>");//设置后缀
		//为查询对象设置高亮选项
		query.setHighlightOptions(highlightOptions );
		
		//1关键字查询
		Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
		query.addCriteria(criteria );
		
		//2按照商品分类过滤
		if(!"".equals(searchMap.get("category"))){//当用户点击了分类,才会进行根据分类进行过滤
		FilterQuery filterQuery = new SimpleFilterQuery();
		Criteria filterCriteria = new Criteria("item_category").is(searchMap.get("category"));//根据item_category这个域传进来的categroy进行过滤,
		filterQuery.addCriteria(filterCriteria );
		query.addFilterQuery(filterQuery );
		}
	
		//3按照商品品牌过滤
		if(!"".equals(searchMap.get("brand"))){
			FilterQuery filterQuery  =  new SimpleFilterQuery();
			Criteria filtercriteria = new  Criteria("item_brand").is(searchMap.get("brand"));
			filterQuery.addCriteria(filtercriteria );
			query.addFilterQuery(filterQuery );
		}
		//4按照规格进行过滤
		if(searchMap.get("spec")!=null){
			Map<String,String > specMap  = (Map<String, String>) searchMap.get("spec");//把规格转换成map类型
			for(String key:specMap.keySet()){//循环map集合 循环的是他的key  
				FilterQuery filterQuery = new  SimpleFilterQuery();
				Criteria criteria2=   new Criteria("item_spec_"+key).is(specMap.get(key));
				filterQuery.addCriteria(criteria2 );
				query.addFilterQuery(filterQuery);	
			}
		}
		//5按照价格过滤
		if(!"".equals(searchMap.get("price"))){
			//先获取价格 格式 500-1000
			String price  = (String ) searchMap.get("price");
			String[] prices = price.split("-");//然后切割,得到价格
			//对价格下限处理
			if(!prices[0].equals("0")){//在最低价格不为0的情况下
				
				FilterQuery filterQuery = new SimpleFilterQuery();
				Criteria filterCriteria = new Criteria("item_price").greaterThanEqual(prices[0]);//过滤条件,大于等于价格
				filterQuery.addCriteria(filterCriteria );
				query.addFilterQuery(filterQuery );
				
			}
			
			//对价格上限处理
			if(!prices[1].equals("*")){//在最高价格不为*的情况下
				
				FilterQuery filterQuery = new SimpleFilterQuery();
				Criteria filterCriteria = new Criteria("item_price").lessThanEqual(prices[1]);//过滤条件,大于等于价格
				filterQuery.addCriteria(filterCriteria );
				query.addFilterQuery(filterQuery );
				
			}
			
			
		}
		
		
		//6搜索分页
		Integer pageNo = (Integer) searchMap.get("pageNo");//获取页码
		if(pageNo==null){
			pageNo=1;
		}
		Integer pageSize = (Integer) searchMap.get("pageSize");//获取页大小
		if(pageSize==null){
			pageSize=20;
		}
		System.out.println(pageNo+"-------"+pageSize);
		query.setOffset((pageNo-1)*pageSize);//起始索引 计算公式 页码减一乘以页数量
		query.setRows(pageSize);//设置每页记录数	
		
		//7排序
		
		String sortValue = (String) searchMap.get("sort");//从前段接受参数  ,升序 ASC 降序 DESC 
		String sortFiled = (String) searchMap.get("sortField"); //排序字段
		
		if(sortValue !=null && !sortValue.equals("")){
			if(sortValue.equals("ASC")){
				Sort sort = new Sort(Sort.Direction.ASC, "item_"+sortFiled);//query的排序方法,ASC升序 ,后面是排序字段
				query.addSort(sort );
			}
			if(sortValue.equals("DESC")){
				Sort sort = new Sort(Sort.Direction.DESC, "item_"+sortFiled);//query的排序方法,DESC升序 ,后面是排序字段
				query.addSort(sort );
			}
			
		}
		//返回高亮页对象
		HighlightPage<TbItem> page = solrTemplate.queryForHighlightPage(query, TbItem.class);
		
		//
		
		//高亮页里的高亮入口集合(每条记录的高亮)
		List<HighlightEntry<TbItem>> entryList = page.getHighlighted();
		for (HighlightEntry<TbItem> entry: entryList) {
			//这个才是真的高亮列表,因为高亮域可以有多个,所以高亮列表也是一个集合
			List<Highlight> highlightList = entry.getHighlights();
		
			
			if(highlightList.size()>0 && highlightList.get(0).getSnipplets().size()>0){
				TbItem tbItem = entry.getEntity();
				tbItem.setTitle(highlightList.get(0).getSnipplets().get(0));//把高亮过的标题放到实体类
			}
			
		}
		map.put("rows", page.getContent());
	/*System.out.println("=============================="+page.getTotalPages());
		System.out.println("=============================="+page.getSize());
		System.out.println("=============================="+page.getTotalElements());
		
		
*/		map.put("pageNo",pageNo);
		map.put("totalPages", page.getTotalPages());//总页数
		map.put("total", page.getTotalElements());//总记录数
		return map;
	}
		//使用springdatasolr 相关技术 实现分组查询
	public List<String> searchCategoryList(Map searchMap){
		//定义list,用于添加分组数据
		
		List<String> list = new ArrayList();
		
		//2创建查询对象
		Query query = new SimpleQuery("*:*");
		
		//3,创建查询条件,根据关键字查询
		Criteria criteria= new Criteria("item_keywords").is(searchMap.get("keywords"));
		query.addCriteria(criteria);
		
		//4,根据域名称进行分组 ,可以指定多个域 获取的时候只能一个一个获取
		GroupOptions groupOptions= new GroupOptions().addGroupByField("item_category");//
		query.setGroupOptions(groupOptions);
		
		//1,solr的分组查询  ,最终返回分组页,根据域名多少,可以有多个分组结果
		GroupPage<TbItem> page = solrTemplate.queryForGroupPage(query, TbItem.class);
		
		//5,获得分组结果,里面要指定域名称 
		GroupResult<TbItem> groupResult = page.getGroupResult("item_category");
		
		//6获得分组入口页,
		Page<GroupEntry<TbItem>> groupEntries = groupResult.getGroupEntries();
		
		//7获取分组入口集合
		List<GroupEntry<TbItem>> content = groupEntries.getContent();
		
		//8遍历得到每一个分组数据
		for (GroupEntry<TbItem> groupEntry : content) {
			//将分组结果添加到返回值中
			String groupValue = groupEntry.getGroupValue();
			list.add(groupValue);
		}
		return list;
		
	}
	@Autowired
	private  RedisTemplate redisTemplate;
	//根据商品分类名称 ,查询品牌和规格列表对象 方法 ,数据来自redis
	private Map searchBrandAndSpecList(String category){
		Map  map  = new HashMap();
		
		//1,根据商品分类名称得到模板id
		Long templateId = (Long) redisTemplate.boundHashOps("itemCat").get(category);
	
		if(templateId!=null){
		//2,根据模板id取出品牌列表
			List brandList = (List) redisTemplate.boundHashOps("brandList").get(templateId);
			map.put("brandList", brandList);//返回值添加品牌列表
	
		//根据模板id取出商品规格列表
		List specList = (List) redisTemplate.boundHashOps("specList").get(templateId);
		map.put("specList", specList);
	
		}
		return map;
	}

	@Override
	public void importList(List list) {
		solrTemplate.saveBeans(list);
		solrTemplate.commit();
		
	}

	@Override
	public void deleteByIds(List goodsIds) {
		
		Query query = new SimpleQuery();
		Criteria criteria = new Criteria("item_goodsid").in(goodsIds);
		query.addCriteria(criteria );
		solrTemplate.delete(query );
		solrTemplate.commit();
		
	}
}
