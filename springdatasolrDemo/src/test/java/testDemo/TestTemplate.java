package testDemo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.apache.solr.client.solrj.SolrServer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Criteria;
import org.springframework.data.solr.core.query.Crotch;
import org.springframework.data.solr.core.query.Query;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.data.solr.core.query.SolrDataQuery;
import org.springframework.data.solr.core.query.result.ScoredPage;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import cn.laowang.pojo.TbItem;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations="classpath:applicationContext-solr.xml")
public class TestTemplate {
	@Autowired
	private SolrTemplate solrTemplate;
	@Test
	public  void  testAdd(){
		TbItem item = new TbItem();
		item.setId(1L);
		item.setTitle("iphoneX");
		item.setCategory("手机");
		item.setBrand("iphone");
		item.setSeller("旗舰店分店");
		item.setGoodsId(10L);
		//BigDecimal就是一个类型 在需要用的时候 要new出来
		item.setPrice(new BigDecimal(6999.11));
		
		solrTemplate.saveBean(item);
		//封装好数据之后需要提交
		solrTemplate.commit();
	}
	@Test   //根据主键查询
	public void findById(){
		//查询出来的是一个pojo对象,想要获取里面的值 就用它的get 方法
		TbItem item = solrTemplate.getById(1L, TbItem.class);
		item.setBrand("三星");
		//这个item对象是从实体类中获取来的,还是从索引库中获取的
		solrTemplate.saveBean(item);
		solrTemplate.commit();
	//	System.out.println(item.getBrand());
	}
	@Test //根据主键删除
	public void deleById(){
		
		solrTemplate.deleteById("1");//删除的是id为1 的这个对象
		
	}
	@Test //批量插入数据
	public void pageInsert(){
		
		//创建一个item集合
		List<TbItem> list = new ArrayList();
		for(int i=0;i<100;i++){
			TbItem item = new TbItem();
			item.setId(i+1L);//主键是唯一的,所以在循环的时候不能一样,要变
			item.setTitle("iphoneX"+i);
			item.setCategory("手机"+i);
			item.setBrand("iphone"+i);
			item.setSeller("旗舰店分店");
			item.setGoodsId(10L);
			//BigDecimal就是一个类型 在需要用的时候 要new出来
			item.setPrice(new BigDecimal(6999.11+i));
			list.add(item);
		}
		
		solrTemplate.saveBeans(list);
		//封装好数据之后需要提交
		solrTemplate.commit();
		
	}
	/*@Test //根据分页查询
	public void pageQuery1(){
		SolrSer ss
		
	}*/
	@Test //根据分页查询
	public void pageQuery(){
		
		//是一个查询接口.不能直接new 需要使用它的子类SimpleQuery实现
		//里面可以传入参数 为查询表达式	
		Query query  = new SimpleQuery("*:*");
		
		//solr中的条件查询
	/*	Criteria criteria = new Criteria("item_brand").contains("iphone");
		criteria = criteria.and("item_brand").contains("2");*/
	//	query.addCriteria(criteria);
		//传入分页相关参数
		query.setOffset(1);//起始索引
		query.setRows(40);//每页记录数
		//在solr 中,所有的查询方法都是通过query开头的
		ScoredPage<TbItem> page = solrTemplate.queryForPage(query, TbItem.class);
		//返回的page对象
		List<TbItem> content = page.getContent();//获取当前页所有的记录,就是实体类的集合
		for (TbItem tbItem : content) {
			System.out.println(tbItem.getId()+" "+tbItem.getBrand()+"的价格为"+tbItem.getPrice());
		}
		System.out.println(page.getTotalElements());//数据的总记录数
		System.out.println(page.getTotalPages());//在不分页的情况下就是一页,分页的情况下,页数等于总数除以每页的数量;		
	}
		
	
		@Test // 删除全部数据 记得要commit
		public void deleAll(){
			
		Query query = new  SimpleQuery("*:*");
	/*	Criteria criteria = new Criteria("item_category").contains("手机");
		query.addCriteria(criteria );*/
			solrTemplate.delete(query);
			solrTemplate.commit();
			/*query.setOffset(91);//起始索引
				query.setRows(15);//每页记录数
				//在solr 中,所有的查询方法都是通过query开头的
				ScoredPage<TbItem> page = solrTemplate.queryForPage(query, TbItem.class);
				List<TbItem> list = page.getContent();
				for (TbItem tbItem : list) {
					System.out.println(tbItem);
				}
				System.out.println(page.getTotalElements());*/
			
			
		}
	
}
