package com.pinyougou.solrutil;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbItemExample;
import com.pinyougou.pojo.TbItemExample.Criteria;
 // 查询SKU列表 而不是 Goods  是因为SKU数据更详细,可用来查询的东西更多
 

@Component
public class SolrUtil {
	
	//注入TbItem
	@Autowired
	private TbItemMapper tbItemMapper;
	@Autowired
	private SolrTemplate solrTemplate;
  public void importData(){
	  TbItemExample example = new TbItemExample();
	  Criteria criteria = example.createCriteria();
	  criteria.andStatusEqualTo("1");//审核通过的才导入
	List<TbItem> list = tbItemMapper.selectByExample(example );
	for (TbItem tbItem : list) {
		Map map = JSON.parseObject(tbItem.getSpec(), Map.class);//从数据库中提取规格,为json字符串形式,转换为map
		tbItem.setSpecMap(map);//自动把规格转换为动态域的值
	}
	  
	solrTemplate.saveBeans(list);
	solrTemplate.commit();
  }
  public static void main(String[] args) {
	ApplicationContext applicationContext = new ClassPathXmlApplicationContext("classpath*:spring/applicationContext*.xml");
	SolrUtil solrutil = (SolrUtil) applicationContext.getBean("solrUtil");
	solrutil.importData();
	
	
	
}
}
