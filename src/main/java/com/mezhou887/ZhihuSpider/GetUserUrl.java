package com.mezhou887.ZhihuSpider;

import java.io.IOException;
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

public class GetUserUrl implements Runnable {
	
	private static final Logger logger = Logger.getLogger(GetUserUrl.class);
	 private final CloseableHttpClient httpClient;
     private final HttpContext context;
     private final HttpPost httppost;
     
     public GetUserUrl(CloseableHttpClient httpClient, HttpPost httppost) {
         this.httpClient = httpClient;
         this.context = HttpClientContext.create();
         this.httppost = httppost;
     }	
     
     public  void insertUrl(String user) throws SQLException
  	 {
		String sql = "insert into userurl(url) values(?)";
		synchronized(Static.conn) {			
		  PreparedStatement ptmt = (PreparedStatement) Static.conn.prepareStatement(sql);
		  ptmt.setString(1, user);
		  ptmt.execute();
		}
  	 }     

	public void run() {
		Integer offset = 0;
		try {
			while (true) {
				httppost.setHeader("Cookie",
						"d_c0=\"AFBAQf6XlgqPTtYdnN-RzRPzpjaWuytWSLo=|1474684865\"; _za=fb1a1ef3-9484-4225-8849-9fab90e0feba; _zap=7cdbe920-a6de-40f5-b7b2-af7821420a3e; nweb_qa=heifetz; _xsrf=fa2a707345fb80b139e35af06eb4e9d0; _ga=GA1.2.1704872840.1490540507; capsion_ticket=\"2|1:0|10:1490871560|14:capsion_ticket|44:ODMyNWY4MzMwZjA1NDUzYWI0NGZiNjFlNzNiZjllNWY=|7e45182e5ee7a4d1f38ce1cc0d42545fd5216f4f3291865bc77cbea893abad68\"; aliyungf_tc=AQAAAPgLXlk4mQgABdv7ccQfshtJdRmP; q_c1=2878e530c4594d11b86b945817daeda5|1490947400000|1490947400000; r_cap_id=\"Y2E5Zjc1MDM4MjQzNDgyNGEzZjMwNjQ2MDc4OGZlMWY=|1490947400|fbcc43997eb568bb122dd768bb6ac62ab8b1c471\"; cap_id=\"OGIyZmQxZjQyZjNlNDllODhmMGE3ODAzOGMzODc0OTU=|1490947400|98209fe4abe8cadf0f672b5ff0c58223efce859e\"; l_n_c=1; __utma=51854390.1704872840.1490540507.1490942040.1490947224.2; __utmb=51854390.0.10.1490947224; __utmc=51854390; __utmz=51854390.1490942040.1.1.utmcsr=zhihu.com|utmccn=(referral)|utmcmd=referral|utmcct=/topic/19550517/followers; __utmv=51854390.100--|2=registration_date=20151203=1^3=entry_date=20151203=1;z_c0="
								+ Static.getCookie());
				httppost.setHeader("X-Xsrftoken", "fa2a707345fb80b139e35af06eb4e9d0");

				List<NameValuePair> formparams = new ArrayList<NameValuePair>();
				formparams.add(new BasicNameValuePair("offset", offset.toString()));
				UrlEncodedFormEntity entityPost = new UrlEncodedFormEntity(formparams, Consts.UTF_8);
				httppost.setEntity(entityPost);

				CloseableHttpResponse response = httpClient.execute(httppost, context);
				HttpEntity entity = response.getEntity();
				try {
					if (entity != null) {
						String body = EntityUtils.toString(entity, "UTF-8");

						if (body.length() < 100) {
							break;
						}

						if (offset > 2000) {
							break;
						}
						String regex = "people...[a-zA-z-]{0,200}\">";
						Pattern p = Pattern.compile(regex);
						Matcher m = p.matcher(body);

						while (m.find()) {
							String s = m.group();
							String user = s.substring(8, s.length() - 3);
							insertUrl(user);
						}
						logger.info("topicid: " + httppost.getURI() + " offset: " + offset);

						EntityUtils.consume(entity);
					}

				} catch (SQLException e) {
					continue;

				} finally {
					response.close();
					EntityUtils.consume(entity);
					offset = offset + 20;
				}
			}
			
		} catch (ClientProtocolException e) {
			logger.error(e);
		} catch (IOException e) {
			logger.error(e);
		}
     
 }

}
