package com.mezhou887.ZhihuSpider;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

public class HandleTopic implements Runnable {
	
	private final CloseableHttpClient httpClient;
	private final HttpContext context;
	private final HttpPost httppost;
	private final String topicid;
	private int count;
	private static final Logger logger = Logger.getLogger(HandleTopic.class);

	public HandleTopic(CloseableHttpClient httpClient, HttpPost httpost, String topicid) {
		this.httpClient = httpClient;
		this.context = HttpClientContext.create();
		this.httppost = httpost;
		this.topicid = topicid;
		this.count = 0;
	}	
	
    public static void writeToDB(String url) throws SQLException {
    	if(url != null) {
    		String sql ="insert into subtopicids(url) values(?)";
    		synchronized (Static.conn) {
    			PreparedStatement ptmt = (PreparedStatement) Static.conn.prepareStatement(sql);
    			ptmt.setString(1, url);
    			ptmt.executeUpdate();
    		}
    	}
    }	
	

	public void run() {
		Integer offset = 0;
		while (true) {
			try {
				List<NameValuePair> formparams = new ArrayList<NameValuePair>();
				formparams.add(new BasicNameValuePair("method", "next"));
				String p2 = "{\"topic_id\":" + topicid + "," + "\"offset\":" + offset + ",\"hash_id\":\"" + HashID.getUUID() + "\"}";
				logger.debug(p2);
				formparams.add(new BasicNameValuePair("params", p2));
				UrlEncodedFormEntity entityPost = new UrlEncodedFormEntity(formparams, Consts.UTF_8);
				httppost.setEntity(entityPost);
				CloseableHttpResponse response = httpClient.execute(httppost, context);
				HttpEntity entity = response.getEntity();

				try {
					if (entity != null) {
						String body = EntityUtils.toString(entity, "UTF-8");
						if (body.length() < 100) {
							logger.debug("Thread:" + topicid + " 结束.");
							break;
						}
						// 正则匹配
						String regex = "topic..[0-9]{1,10}";
						Pattern p = Pattern.compile(regex);
						Matcher m = p.matcher(body);

						while (m.find()) {
							String s = m.group();
							count++;
							writeToDB(s.substring(7));
						}
						logger.debug("topicid: " + topicid + ", offset: " + offset);
						offset = offset + 20;
					}
				} catch (SQLException e) {
					logger.error(e);
				} finally {
					EntityUtils.consume(entity);
					response.close();
				}

				logger.info("Thread:" + topicid + " --爬取数量:" + count);
			} catch (ClientProtocolException e) {
				logger.error(e);
			} catch (IOException e) {
				logger.error(e);
			}
		}
	}
}
