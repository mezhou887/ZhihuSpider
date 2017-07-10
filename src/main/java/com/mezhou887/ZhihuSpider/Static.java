package com.mezhou887.ZhihuSpider;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.apache.log4j.Logger;

import com.mysql.jdbc.PreparedStatement;

public class Static {

	private static final Logger logger = Logger.getLogger(Static.class);
	public static BlockingQueue<String> topicIDs = new  ArrayBlockingQueue<String>(500); 
	
	public static Connection conn=getConn();
    
	public static Connection getConn() {

		String driver = "com.mysql.jdbc.Driver";
		String url = "jdbc:mysql://192.168.31.83:3306/zhihuspider?useUnicode=true&characterEncoding=utf8";
		String username = "root";
		String password = "bitnami";
		Connection conn = null;
		try {
			Class.forName(driver);
			conn = (Connection) DriverManager.getConnection(url, username, password);
		} catch (Exception e) {
			logger.error(e);
		}
		return conn;
	}
	
	public static List<String> getSubTopicIDs() throws SQLException {
		List<String> subTopicIDs = new ArrayList<String>();
		String sql ="select distinct(url) from subtopicids";
		PreparedStatement pstmt = (PreparedStatement)conn.prepareStatement(sql);
		ResultSet rs = pstmt.executeQuery();
		while(rs.next()) {
			subTopicIDs.add(rs.getString("url"));
		}
		return subTopicIDs;
	}
	
	
    public static String[] cookies=new String[22];    
    static{
    	//关于cookie部分 需要在淘宝买几个号（也可以用自己的号）抓个包 找到zc_0=".." 中间的部分加到这个数组当中， 如下就是一个zc_0的值（知乎根据这个判断用户，设置的越多越不容易封号）
    	cookies[0]="Mi4wQUZEQ243R3JrUXNBQUlKU1lBQ1hDeGNBQUFCaEFsVk5lOGNWV1FEUkxIMkZrdG5mLWp4eTBTbEhaejdldU9rVm5B|1492007551|10c21c3b1bab28a836483200300f73d8e38f6be2";
    }
    
    public static String getCookie()
    {        
    	Random random = new Random();
        int a=random.nextInt(21);
//        return cookies[a];
        return cookies[0];
    }
    
	
}
