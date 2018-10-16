package cn.itcast.demo;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Component;

@Component
public class TopicProducer {
	//引入jms模板
	@Autowired
	private  JmsTemplate jmsTemplate;
	
	//引入队列
	@Autowired
	private  Destination topicTextDestination;
	
	//发送文本消息
	public void  seneTextMessage(final String  text){
		jmsTemplate.send(topicTextDestination, new MessageCreator() {
			
			public Message createMessage(Session session) throws JMSException {
				//强转消息类型
			//	TextMessage textMessage = (TextMessage)
				return session.createTextMessage(text);
			}
		});
		
	}
	
}
