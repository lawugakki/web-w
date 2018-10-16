package com.pinyougou.sellergoods.service.impl;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbBrandMapper;
import com.pinyougou.mapper.TbGoodsDescMapper;
import com.pinyougou.mapper.TbGoodsMapper;
import com.pinyougou.mapper.TbItemCatMapper;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.mapper.TbSellerMapper;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojo.TbGoodsDesc;
import com.pinyougou.pojo.TbGoodsExample;
import com.pinyougou.pojo.TbGoodsExample.Criteria;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbItemCat;
import com.pinyougou.pojo.TbItemExample;
import com.pinyougou.pojo.TbSeller;
import com.pinyougou.pojogroup.Goods;
import com.pinyougou.sellergoods.service.GoodsService;

import entity.PageResult;

/**
 * 服务实现层
 * @author Administrator
 *
 */

@Service(timeout=40000)
@Transactional
public class GoodsServiceImpl implements GoodsService {

	@Autowired
	private TbGoodsMapper goodsMapper;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbGoods> findAll() {
		return goodsMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbGoods> page=   (Page<TbGoods>) goodsMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}
	
	
	
	@Autowired//注入商品详情mapper
	private TbGoodsDescMapper goodsDescMapper;
	@Autowired
	private TbItemMapper itemMapper;
	@Autowired
	private TbItemCatMapper itemCatMapper;
	@Autowired
	private TbBrandMapper brandMapper;
	@Autowired
	private TbSellerMapper sellerMapper;
	/**
	 * 增加
	 */
	@Override	
	public void add(Goods goods) {
		goods.getGoods().setAuditStatus("0");		
		goodsMapper.insert(goods.getGoods());	//插入商品表
		int x=1/0;
		goods.getGoodsDesc().setGoodsId(goods.getGoods().getId());
		goodsDescMapper.insert(goods.getGoodsDesc());//插入商品扩展数据
		saveItemList(goods);//插入SKU的商品数据
		
	}
	private void setItemValus(Goods goods,TbItem item) {
		item.setGoodsId(goods.getGoods().getId());//商品SPU编号
		item.setSellerId(goods.getGoods().getSellerId());//商家编号
		Long category3Id = goods.getGoods().getCategory3Id();
		System.err.println(category3Id);
		item.setCategoryid(category3Id);//商品分类编号（3级）
		item.setCreateTime(new Date());//创建日期
		item.setUpdateTime(new Date());//修改日期 
		
		//品牌名称
		TbBrand brand = brandMapper.selectByPrimaryKey(goods.getGoods().getBrandId());
		item.setBrand(brand.getName());
		//分类名称
		TbItemCat itemCat = itemCatMapper.selectByPrimaryKey(category3Id);
		item.setCategory(itemCat.getName());
		
		//商家名称
		TbSeller seller = sellerMapper.selectByPrimaryKey(goods.getGoods().getSellerId());
		item.setSeller(seller.getNickName());
		
		//图片地址（取spu的第一个图片）
		List<Map> imageList = JSON.parseArray(goods.getGoodsDesc().getItemImages(), Map.class) ;
		if(imageList.size()>0){
			item.setImage ( (String)imageList.get(0).get("url"));
		}		
	}
	
	/**
	 * 修改
	 */
	@Override
	public void update(Goods goods){
		//更新商品基本表数据
		goodsMapper.updateByPrimaryKey(goods.getGoods());
		//更新商品扩展表数据
		goodsDescMapper.updateByPrimaryKey(goods.getGoodsDesc());

		//先删除原有的SKU列表数据,
		TbItemExample example = new TbItemExample();
		com.pinyougou.pojo.TbItemExample.Criteria criteria = example.createCriteria();
		criteria.andGoodsIdEqualTo(goods.getGoods().getId());
		itemMapper.deleteByExample(example );
		
		//插入新的SKU列表
		saveItemList(goods);//插入SKU的商品数据
		//goodsMapper.updateByPrimaryKey(goods);
	}	
	//插入sku列表数据
	public void saveItemList(Goods goods){
		if("1".equals(goods.getGoods().getIsEnableSpec())){
			
			for(TbItem item :goods.getItemList()){
				System.out.println(item);
				//标题
				String title= goods.getGoods().getGoodsName();
				Map<String,Object> specMap = JSON.parseObject(item.getSpec());
				for(String key:specMap.keySet()){
					title+=" "+ specMap.get(key);
				}
				item.setTitle(title);		
				setItemValus(goods,item);		
				itemMapper.insert(item);
			}
			}else{//不点击规格
				
				TbItem item=new TbItem();
				item.setTitle(goods.getGoods().getGoodsName());//商品KPU+规格描述串作为SKU名称
				item.setPrice( goods.getGoods().getPrice() );//价格			
				item.setStatus("1");//状态
				item.setIsDefault("1");//是否默认			
				item.setNum(99999);//库存数量
				item.setSpec("{}");			
				setItemValus(goods,item);					
				itemMapper.insert(item);
			}		
		
	}
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public Goods findOne(Long id){
		Goods goods = new Goods();
		//查询商品基本表
		TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);
		goods.setGoods(tbGoods);
		//查询商品扩展表
		TbGoodsDesc goodsDesc = goodsDescMapper.selectByPrimaryKey(id);
		goods.setGoodsDesc(goodsDesc);
	
		//读取SKU的列表
		
		TbItemExample example = new TbItemExample();//创建条件查询对象
		com.pinyougou.pojo.TbItemExample.Criteria criteria = example.createCriteria();
		criteria.andGoodsIdEqualTo(id);//根据SKU id 查询对应的SPU
		List<TbItem> itemList = itemMapper.selectByExample(example );
		goods.setItemList(itemList);
		return goods;
				//goodsMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			TbGoods goods = goodsMapper.selectByPrimaryKey(id);//查询出goods对象
			goods.setIsDelete("1");//表示逻辑删除
			goodsMapper.updateByPrimaryKey(goods);//更新商品
			
		}		
	}
	
	
		@Override
	public PageResult findPage(TbGoods goods, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbGoodsExample example=new TbGoodsExample();
		Criteria criteria = example.createCriteria();
		criteria.andIsDeleteIsNull();//idDelete为null,既没有被逻辑删除的物品才能被查询出来
		if(goods!=null){			
						if(goods.getSellerId()!=null && goods.getSellerId().length()>0){
							//
				//criteria.andSellerIdLike("%"+goods.getSellerId()+"%");
							//此处商家id是模糊查询,为防止意外,需要改成精确查询
						criteria.andSellerIdEqualTo(goods.getSellerId());
							
			}
			if(goods.getGoodsName()!=null && goods.getGoodsName().length()>0){
				criteria.andGoodsNameLike("%"+goods.getGoodsName()+"%");
			}
			if(goods.getAuditStatus()!=null && goods.getAuditStatus().length()>0){
				criteria.andAuditStatusLike("%"+goods.getAuditStatus()+"%");
			}
			if(goods.getIsMarketable()!=null && goods.getIsMarketable().length()>0){
				criteria.andIsMarketableLike("%"+goods.getIsMarketable()+"%");
			}
			if(goods.getCaption()!=null && goods.getCaption().length()>0){
				criteria.andCaptionLike("%"+goods.getCaption()+"%");
			}
			if(goods.getSmallPic()!=null && goods.getSmallPic().length()>0){
				criteria.andSmallPicLike("%"+goods.getSmallPic()+"%");
			}
			if(goods.getIsEnableSpec()!=null && goods.getIsEnableSpec().length()>0){
				criteria.andIsEnableSpecLike("%"+goods.getIsEnableSpec()+"%");
			}
			if(goods.getIsDelete()!=null && goods.getIsDelete().length()>0){
				criteria.andIsDeleteLike("%"+goods.getIsDelete()+"%");
			}
	
		}
		
		Page<TbGoods> page= (Page<TbGoods>)goodsMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}

		@Override
		public void updateStatus(Long[] ids, String status) {
			for (Long id : ids) {
				//查出goods对象
				TbGoods goods = goodsMapper.selectByPrimaryKey(id);
				//设置状态
				goods.setAuditStatus(status);
				//更新商品状态
				goodsMapper.updateByPrimaryKey(goods);
				
			}
			
		}
	//根据SPU的ID集合 查询SKU的列表
		public List<TbItem> findItemListByGoodsIListAndStatus(Long[] goodsIds,String  status){
			
			TbItemExample example = new TbItemExample();
			com.pinyougou.pojo.TbItemExample.Criteria criteria = example.createCriteria();
			criteria.andStatusEqualTo(status);//状态
			criteria.andGoodsIdIn(Arrays.asList(goodsIds));//将穿进来的数组 转换为集合 SPU的集合
			 return  itemMapper.selectByExample(example );
			
		}
}
