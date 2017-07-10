package com.mezhou887.ZhihuSpider;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

public class Main {
	
	private static final Logger logger = Logger.getLogger(Main.class);
	
	/**
	 * 获取主话题
	 * @throws Exception
	 */
	public static void getTopicIDs() throws Exception {
		String topics = "https://www.zhihu.com/topics";
		HttpClient httpClient = HttpClients.createDefault();
		HttpGet httpGet = new HttpGet(topics);
		CloseableHttpResponse response = (CloseableHttpResponse) httpClient.execute(httpGet);

		HttpEntity enity = response.getEntity();
		String body = EntityUtils.toString(enity, "UTF-8");

		// 字符串处理 以及保存到topicIDs中去
		String regex = "data-id=\"[0-9]{0,6}\"";
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(body);

		while (m.find()){
			String s = m.group();
			Static.topicIDs.add(m.group().substring(9, s.length() - 1));
		}

		response.close();
		EntityUtils.consume(enity);
		logger.debug("firstTopicIDs: "+ Static.topicIDs.size());
	}
	
	/**
	 * 获取分话题
	 * @throws Exception
	 */
	public static void getAllSubTopicIDs() throws Exception {
		ExecutorService executorService = Executors.newFixedThreadPool(5);
		PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
		cm.setMaxTotal(200);
		cm.setDefaultMaxPerRoute(200);
		CloseableHttpClient client = HttpClients.custom().setRetryHandler(new DefaultHttpRequestRetryHandler()).setConnectionManager(cm).build();

		int len = Static.topicIDs.size();
		String url = "https://www.zhihu.com/node/TopicsPlazzaListV2";

		for(int i=0; i<len; i++){
			HttpPost post = new HttpPost(url);
			String str = Static.topicIDs.poll();
			executorService.execute(new HandleTopic(client, post, str));
			post.releaseConnection();
		}
		executorService.shutdown();
		while(true){
			if (executorService.isTerminated()){
				client.close();
				logger.info("getAllSubTopicIDs已经执行完成！");
				break;
			}
			Thread.sleep(2000);
		}
	}
	
	// 获取全部用户的url
	public static void getAllUserUrl() throws Exception
	{
		ExecutorService executorService = Executors.newFixedThreadPool(5);
		PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
		cm.setMaxTotal(200);
		cm.setDefaultMaxPerRoute(200);
		CloseableHttpClient httpClient = HttpClients.custom().setRetryHandler(new DefaultHttpRequestRetryHandler()).setConnectionManager(cm).build();

		List<String> subTopicIDs = Static.getSubTopicIDs();
		for(String topicIDs: subTopicIDs) {
			HttpPost httppost = new HttpPost("https://www.zhihu.com/topic/" + topicIDs + "/followers");
			logger.debug(httppost.getURI());
			executorService.execute(new GetUserUrl(httpClient, httppost));
			httppost.releaseConnection();
		}
		executorService.shutdown();
		while(true){
			if (executorService.isTerminated()) {
				httpClient.close();
				logger.info("getAllUserUrl已经执行完成！");
				break;
			}
			Thread.sleep(1000);
		}
	}	
	
	public static void main(String[] args) throws Exception {

		long startTime = System.currentTimeMillis();
//		getTopicIDs(); // 获取全部一级话题
//		getAllSubTopicIDs(); // 获取全部二级话题
		getAllUserUrl(); // 获取全部用户的url
		long endTime = System.currentTimeMillis();
		
		logger.debug("共花费" + (endTime - startTime)/1000/60 + "分钟");
		
	}

}
