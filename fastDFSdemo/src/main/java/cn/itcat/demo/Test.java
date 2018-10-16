package cn.itcat.demo;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.StorageServer;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;

public class Test {
	public static void main(String[] args) throws FileNotFoundException, IOException, Exception {
		//1.加载配置文件
		ClientGlobal.init("D:\\pinyougouWorkSpace\\fastDFSdemo\\src\\main\\resources\\fdfs_client.conf");
		//2.构建管理者客户端
		TrackerClient client =  new TrackerClient();
		//3.通过客户端得到服务端对象
		TrackerServer trackerServer = client.getConnection();
		//4.构建存储的服务端
		StorageServer server = null;
		//5.获取存储服务器的客户端对象
		StorageClient  storageClient = new StorageClient(trackerServer,server);
		//6.上传文件
		String[] strings = storageClient.upload_file("d:\\img\\c.jpg", "jpg", null);//文件位置 , 扩展名 , 文件详细信息
		//7.显示上传结果file_id
		for(String str:strings){
			System.out.println(str);
		
			//会输出组名和修改过的文件名
			
		}
	}
}
