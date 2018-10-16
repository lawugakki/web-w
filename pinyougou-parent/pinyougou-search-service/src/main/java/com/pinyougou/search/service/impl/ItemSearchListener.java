package com.pinyougou.search.service.impl;

import java.util.List;
import java.util.Map;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;

@Component
public class ItemSearchListener implements MessageListener{
	
	@Autowired
	private  ItemSearchService itemSearchServie;

	
	@Override
	public void onMessage(Message message) {
	
		// 强转消息
		TextMessage  textMesage  = (TextMessage)message;
		try {
			String text = textMesage.getText();//json字符串
			System.out.println("监听到消息:"+text);
			 List<TbItem> list = JSON.parseArray(text,TbItem.class);  //将json字符串转换成list集合
			 itemSearchServie.importList(list);
			 System.out.println("导入到json索引库");
			
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	

	
}
