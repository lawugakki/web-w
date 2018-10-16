package springjms_producer;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import cn.itcast.demo.QueueProducer;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations="classpath:applicationContext-jms-producer.xml")
public class JmsTest {
	
	@Autowired
	private QueueProducer producer;
	
	@Test
	public void testSend(){
		producer.seneTextMessage("求求你别错了 大哥");
	}
	
}
