package springjms_producer;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import cn.itcast.demo.QueueProducer;
import cn.itcast.demo.TopicProducer;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations="classpath:applicationContext-jms-producer.xml")
public class TopicTest {
	
	@Autowired
	private TopicProducer producer;
	
	@Test
	public void testSend(){
		producer.seneTextMessage("套你的猴子");
	}
	
}
