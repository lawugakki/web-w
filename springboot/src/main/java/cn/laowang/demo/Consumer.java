package cn.laowang.demo;

import java.util.Map;

import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

//消费者
@Component
public class Consumer {
	
	@JmsListener(destination="itcast")
	public void rece(String  text){
		System.out.println("接收到消息:"+text);
	}
	
	@JmsListener(destination="itmap")
	
	public void recemap(Map map){
		System.out.println("map:"+map);
	}
}
