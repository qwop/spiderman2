package net.kernal.spiderman.impl;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.util.EntityUtils;

import net.kernal.spiderman.Downloader;
import net.kernal.spiderman.K;
import net.kernal.spiderman.Properties;

public class DefaultDownloader implements Downloader {

	private RequestConfig defaultRequestConfig;
	private HttpClient httpClient;
	private CookieStore cookieStore;  
	private Map<String, String> headers;
	
	public DefaultDownloader(Properties props) {
		RequestConfig.Builder builder = RequestConfig.custom()
	            .setCookieSpec(CookieSpecs.NETSCAPE)
	            .setExpectContinueEnabled(false)
	            .setRedirectsEnabled(props.getBoolean("downloader.redirectsEnabled", false))
	            .setCircularRedirectsAllowed(props.getBoolean("downloader.circularRedirectsAllowed", false))
	            // 设置从连接池获取连接的超时时间
	            .setConnectionRequestTimeout(props.getInt("downloader.connectionRequestTimeout", 1000))
	            // 设置连接远端服务器的超时时间
	            .setConnectTimeout(props.getInt("downloader.connectTimeout", 1000))
	            // 设置从远端服务器上传输数据回来的超时时间
	            .setSocketTimeout(props.getInt("downloader.socketTimeout", 10000))
	            .setTargetPreferredAuthSchemes(Arrays.asList(AuthSchemes.NTLM, AuthSchemes.DIGEST))
	            .setProxyPreferredAuthSchemes(Arrays.asList(AuthSchemes.BASIC));
		
		String proxy = props.getString("downloader.proxy");
		if (K.isNotBlank(proxy)) {
			builder.setProxy(HttpHost.create(proxy));
		}
		this.cookieStore = new BasicCookieStore();
		this.headers = new HashMap<String, String>();
	    this.defaultRequestConfig = builder.build();
		this.httpClient = HttpClients.custom()
				.setUserAgent(props.getString("downloader.userAgent", "Spiderman[http://git.oschina.net/l-weiwei/Spiderman2]"))
				.setDefaultCookieStore(cookieStore)
				.build();
	}
	
	public Downloader keepHeader(Downloader.Header header) {
		String key = header.getName();
		String val = header.getValue();
		if (this.headers.containsKey(key))
			this.headers.put(key, this.headers.get(key) + "; " + val);
		else
			this.headers.put(key, val);
		return this;
	}

	public Downloader keepCookie(Downloader.Cookie c) {
		BasicClientCookie cookie = new BasicClientCookie(c.getName(), c.getValue());
		cookie.setDomain(c.getDomain());
		cookie.setExpiryDate(c.getExpiryDate());
		cookie.setPath(c.getPath());
		cookie.setSecure(c.isSecure());
		this.cookieStore.addCookie(cookie);
		return this;
	}

	public Response download(Request request) {
		String method = request.getMethod();
		String url = request.getUrl();
		final HttpRequestBase req;
		if (K.HTTP_POST.equals(method)) {
			req = new HttpPost(url);
		} else {
			req = new HttpGet(url);
		}
		
		RequestConfig reqCfg = buildRequestConfig(request);
		req.setConfig(reqCfg);
		for (Iterator<Entry<String, String>> it = this.headers.entrySet().iterator(); it.hasNext(); ) {
			Entry<String, String> e = it.next();
			req.addHeader(e.getKey(), e.getValue());
		}
		K.foreach(request.getHeaders(), new K.ForeachCallback<Downloader.Header>() {
			public void each(int i, Downloader.Header item) {
				req.addHeader(item.getName(), item.getValue());
			}
		});
		K.foreach(request.getCookies(), new K.ForeachCallback<Downloader.Cookie>() {
			public void each(int i, Downloader.Cookie item) {
				keepCookie(item);
			}
		});
		final Response response = new Response(request);
		try {
			HttpClientContext ctx = HttpClientContext.create();
			HttpResponse resp = this.httpClient.execute(req, ctx);
			// get status
			StatusLine statusLine = resp.getStatusLine();
			int statusCode = statusLine.getStatusCode();
			String statusDesc = statusLine.getReasonPhrase();
			response.setStatusCode(statusCode);
			response.setStatusDesc(statusDesc);
			// cookies
			CookieStore cs = ctx.getCookieStore();
			for (org.apache.http.cookie.Cookie c : cs.getCookies()) {
				Cookie nc = new Cookie(c.getName(), c.getValue(), c.getDomain(), c.getPath(), c.getExpiryDate(), c.isSecure());
				this.keepCookie(nc);
			}
			
			// get redirect location
			org.apache.http.Header locationHeader = resp.getFirstHeader("Location");
			if (locationHeader != null && (statusCode == HttpStatus.SC_MOVED_PERMANENTLY || statusCode == HttpStatus.SC_MOVED_TEMPORARILY)) 
				response.setLocation(locationHeader.getValue());
			
		    // entity
			HttpEntity entity = resp.getEntity();
			// content type and charset
			ContentType contentType = ContentType.getOrDefault(entity);
			Charset charset = contentType.getCharset();
			response.setCharset(charset == null ? null : charset.name());
			response.setMimeType(contentType.getMimeType());
			// body
			byte[] body = EntityUtils.toByteArray(entity);
			response.setBody(body);
		} catch (Throwable e) {
//			e.printStackTrace();
		} finally {
		}
		
		return response;
	}

	private RequestConfig buildRequestConfig(Request request) {
		RequestConfig.Builder builder = RequestConfig.copy(defaultRequestConfig);
		Properties reqProps = request.getProperties();
		if (reqProps.containsKey("socketTimeout")) {
			builder.setSocketTimeout(reqProps.getInt("socketTimeout"));
		}
		if (reqProps.containsKey("connectTimeout")) {
			builder.setConnectTimeout(reqProps.getInt("connectTimeout"));
		}
		if (reqProps.containsKey("connectionRequestTimeout")) {
			builder.setConnectionRequestTimeout(reqProps.getInt("connectionRequestTimeout"));
		}
		if (reqProps.containsKey("redirectsEnabled")) {
			builder.setRedirectsEnabled(reqProps.getBoolean("redirectsEnabled"));
		}
		if (reqProps.containsKey("circularRedirectsAllowed")) {
			builder.setCircularRedirectsAllowed(reqProps.getBoolean("circularRedirectsAllowed"));
		}
		RequestConfig reqCfg = builder.build();
		return reqCfg;
	}

}
