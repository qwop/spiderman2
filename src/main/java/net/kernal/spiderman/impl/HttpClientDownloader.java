/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.kernal.spiderman.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.GZIPInputStream;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.HttpEntityWrapper;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParamBean;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import net.kernal.spiderman.Downloader;
import net.kernal.spiderman.Properties;

/**
 * Web 页面内容获取器
 * @author weiwei l.weiwei@163.com
 * @date 2013-1-7 上午11:04:50
 */
public class HttpClientDownloader implements Downloader {

	private ThreadSafeClientConnManager connectionManager;
	private DefaultHttpClient httpClient;
	private final Object mutex = new Object();
	private long lastFetchTime = 0;
	private Map<String, String> headers = new Hashtable<String, String>();
	private Map<String, List<String>> cookies = new Hashtable<String, List<String>>();
	
	/**
	 * 处理GZIP解压缩
	 * @author weiwei l.weiwei@163.com
	 * @date 2013-1-7 上午11:26:24
	 */
	private static class GzipDecompressingEntity extends HttpEntityWrapper {
		public GzipDecompressingEntity(final HttpEntity entity) {
			super(entity);
		}
		public InputStream getContent() throws IOException, IllegalStateException {
			InputStream wrappedin = wrappedEntity.getContent();
			return new GZIPInputStream(wrappedin);
		}
		public long getContentLength() {
			return -1;
		}
	}
	
	public void addCookie(String name, String value, String domain, String path) {
		Cookie c = new Cookie(name, value);
		c.setDomain(domain);
		c.setPath(path);
		//设置Cookie
		List<String> vals = this.cookies.get(name);
		if (vals == null)
			vals = new ArrayList<String>();
		vals.add(value);
		this.cookies.put(name, vals);
		
		BasicClientCookie clientCookie = new BasicClientCookie(name, value);
		clientCookie.setPath(path);
		clientCookie.setDomain(domain);
		httpClient.getCookieStore().addCookie(clientCookie);
	}

	public void addHeader(String key, String val) {
		if (this.headers.containsKey(key))
			this.headers.put(key, this.headers.get(key) + "; " + val);
		else
			this.headers.put(key, val);
	}

	public HttpClientDownloader(Properties props) {
		//设置HTTP参数
		HttpParams params = new BasicHttpParams();
		params.setParameter(CoreProtocolPNames.USER_AGENT, props.getString("downloader.userAgent", ""));
//		params.setIntParameter(CoreConnectionPNames.SO_TIMEOUT, -1);
//		params.setIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, );
		
		HttpProtocolParamBean paramsBean = new HttpProtocolParamBean(params);
		paramsBean.setVersion(HttpVersion.HTTP_1_1);
		paramsBean.setContentCharset("UTF-8");
		paramsBean.setUseExpectContinue(false);
		
		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(new Scheme("http", 80, PlainSocketFactory.getSocketFactory()));

		schemeRegistry.register(new Scheme("https", 443, SSLSocketFactory.getSocketFactory()));

		connectionManager = new ThreadSafeClientConnManager(schemeRegistry);
//		connectionManager.setMaxTotal(config.getMaxTotalConnections());
//		connectionManager.setDefaultMaxPerRoute(config.getMaxConnectionsPerHost());
		httpClient = new DefaultHttpClient(connectionManager, params);
		
//		httpClient.getParams().setIntParameter("http.socket.timeout", -1);
		httpClient.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.BEST_MATCH);
		httpClient.getParams().setParameter(ClientPNames.HANDLE_REDIRECTS, false);
//				HttpClientParams.setCookiePolicy(httpClient.getParams(),CookiePolicy.BEST_MATCH);

		//设置响应拦截器
        httpClient.addResponseInterceptor(new HttpResponseInterceptor() {
            public void process(final HttpResponse response, final HttpContext context) throws HttpException, IOException {
                HttpEntity entity = response.getEntity();
                org.apache.http.Header contentEncoding = entity.getContentEncoding();
                if (contentEncoding != null) {
                    HeaderElement[] codecs = contentEncoding.getElements();
                    for (HeaderElement codec : codecs) {
                    	//处理GZIP解压缩
                        if (codec.getName().equalsIgnoreCase("gzip")) {
                            response.setEntity(new GzipDecompressingEntity(response.getEntity()));
                            return;
                        }
                    }
                }
            }
        });
	}
	
	public Response download(Request request) {
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		Response result = new Response(request);
		HttpGet get = null;
		HttpEntity entity = null;
		String toFetchURL = request.getUrl();
		try {
			get = new HttpGet(toFetchURL);
			//设置请求GZIP压缩，注意，前面必须设置GZIP解压缩处理
			get.addHeader("Accept-Encoding", "gzip");
			for (Iterator<Entry<String, String>> it = headers.entrySet().iterator(); it.hasNext();){
				Entry<String, String> entry = it.next();
				get.addHeader(entry.getKey(), entry.getValue());
			}
			
			//同步信号量,在真正对服务端进行访问之前进行访问间隔的控制
			// TODO 针对每个请求有一个delay的参数设置
//			synchronized (mutex) {
				//获取当前时间
//				long now = (new Date()).getTime();
				//对同一个Host抓取时间间隔进行控制，若在设置的时限内则进行休眠
//				if (now - lastFetchTime < config.getPolitenessDelay()) {
//					long t = config.getPolitenessDelay() - (now - lastFetchTime);
//					Thread.sleep(t);
//				}
				//不断更新最后的抓取时间，注意，是针对HOST的，不是针对某个URL的
//				lastFetchTime = (new Date()).getTime();
//			}
			
			//记录get请求信息
//			org.apache.http.Header[] headers = get.getAllHeaders();
			//执行get访问，获取服务端返回内容
			HttpResponse response = httpClient.execute(get);
//			headers = response.getAllHeaders();
			entity = response.getEntity();
			//服务端返回的状态码
			int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode != HttpStatus.SC_OK) {
				if (statusCode != HttpStatus.SC_NOT_FOUND) {
					org.apache.http.Header locationHeader = response.getFirstHeader("Location");
					//如果是301、302跳转，获取跳转URL即可返回
					if (locationHeader != null && (statusCode == HttpStatus.SC_MOVED_PERMANENTLY || statusCode == HttpStatus.SC_MOVED_TEMPORARILY)) 
						result.setLocation(locationHeader.getValue());
				}
				//只要不是OK的除了设置跳转URL外设置statusCode即可返回
				result.setStatusCode(statusCode);
				return result;
			}

			//处理服务端返回的实体内容
			if (entity != null) {
				result.setStatusCode(statusCode);
				// entity
				// content type and charset
				ContentType contentType = ContentType.getOrDefault(entity);
				Charset charset = contentType.getCharset();
				result.setCharset(charset == null ? null : charset.name());
				result.setMimeType(contentType.getMimeType());
				// body
				byte[] body = EntityUtils.toByteArray(entity);
				result.setBody(body);
				return result;
			}
		} catch (Throwable e) {
			e.printStackTrace();
			result.setStatusCode(500);
			return result;
		} finally {
			try {
				if (entity == null && get != null) 
					get.abort();
			} catch (Throwable e) {
				throw new RuntimeException(e);
			}
		}
		
		result.setStatusCode(0);
		return result;
	}
	
	public HttpClient getHttpClient() {
		return httpClient;
	}

    public void close() throws Exception {
        this.httpClient.close();
    }

	public Downloader keepHeader(Header header) {
		// TODO Auto-generated method stub
		return null;
	}

	public Downloader keepCookie(Cookie cookie) {
		// TODO Auto-generated method stub
		return null;
	}

}
