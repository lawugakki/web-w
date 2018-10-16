package cn.laowang.demo;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsMessagingTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


//消息的生产者
@RestController
public class QueueController {
	
	@Autowired
	private  JmsMessagingTemplate  jmsMessagingTemplate ;
	@RequestMapping("/send")
	public void  send(String  text){
		jmsMessagingTemplate.convertAndSend("itcast",text);
		
	}
	
	@RequestMapping("/sendmap")
	public void  sendmap(){
		Map  map  = new  HashMap();
		map.put("mobile", "15738363516");
		map.put("template_code", "SMS_138079910");
		map.put("sign_name", "王丙财");
		map.put("param", "{\"name\":\"老王\"}");
		jmsMessagingTemplate.convertAndSend("sms",map);
		
	}
}
