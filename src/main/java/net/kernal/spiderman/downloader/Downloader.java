package net.kernal.spiderman.downloader;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.kernal.spiderman.K;
import net.kernal.spiderman.Properties;

/**
 * 网页下载器
 * @author 赖伟威 l.weiwei@163.com 2015-12-10
 */
public interface Downloader {

	/**
	 * 所有请求都带上给定的头参数
	 * @param header
	 * @return
	 */
	public Downloader keepHeader(Header header);
	
	/**
	 * 所有请求都带上给定的Cookie头参数
	 * @param cookie
	 * @return
	 */
	public Downloader keepCookie(Cookie cookie);
	
	/**
	 * 异步下载网页内容
	 * @param request http请求
	 * @param callback 完成下载后的回调函数
	 * @return 返回http响应
	 */
	public void download(Request request, Callback callback);
	
	/**
	 * 同步下载网络内容
	 * @param request
	 * @return
	 */
	public Response download(Request request);
	
	public static interface Callback {
		public void completed(Response response);
	}
	
	public static class Header {
		private String name;
		private String value;
		public Header(String name, String value) {
			this.name = name;
			this.value = value;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String getValue() {
			return value;
		}
		public void setValue(String value) {
			this.value = value;
		}
	}
	
	public static class Cookie {
		public Cookie(String name, String value) {
			super();
			this.name = name;
			this.value = value;
		}
		public Cookie(String name, String value, String path) {
			super();
			this.name = name;
			this.value = value;
			this.path = path;
		}
		
		public Cookie(String name, String value, String domain, String path, Date expiryDate, boolean secure) {
			super();
			this.name = name;
			this.value = value;
			this.domain = domain;
			this.path = path;
			this.expiryDate = expiryDate;
			this.secure = secure;
		}
		
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String getValue() {
			return value;
		}
		public void setValue(String value) {
			this.value = value;
		}
		public String getPath() {
			return path;
		}
		public void setPath(String path) {
			this.path = path;
		}
		public Date getExpiryDate() {
			return expiryDate;
		}
		public void setExpiryDate(Date expiryDate) {
			this.expiryDate = expiryDate;
		}
		public boolean isSecure() {
			return secure;
		}
		public void setSecure(boolean secure) {
			this.secure = secure;
		}
		public void setDomain(String domain) {
			this.domain = domain;
		}
		public String getDomain() {
			return this.domain;
		}
		private String name;
		private String value;
		private String domain;
		private String path;
		private Date expiryDate;
		private boolean secure;
	}
	
	/**
	 * HTTP请求
	 * @author 赖伟威 l.weiwei@163.com 2015-12-10
	 *
	 */
	public static class Request implements Serializable {
		
		private static final long serialVersionUID = -5271854259417049190L;

		public Request(String url) {
			this(url, K.HTTP_GET);
		}
		
		/**
		 * 实例化
		 * @param url 请求链接地址
		 * @param httpMethod 请求方法 GET,POST,PUT,DELETE,HEAD等等
		 */
		public Request(String url, String httpMethod) {
			this.url = url;
			this.method = httpMethod;
			this.params = new HashMap<String, List<Object>>();
			this.headers = new ArrayList<Header>();
			this.cookies = new ArrayList<Cookie>();
			this.properties = new Properties();
		}
		
		@Override
		public String toString() {
			return "Request [url=" + url + ", method=" + method + ", params=" + params + ", headers=" + headers
					+ ", cookies=" + cookies + "]";
		}

		/**
		 * 添加请求参数
		 * @param name 参数名
		 * @param value 参数值
		 * @return 返回当前对象
		 */
		public Request addParam(String name, Object value) {
			List<Object> values = this.params.get(name);
			if (values == null) {
				values = new ArrayList<Object>();
				this.params.put(name, values);
			}
			values.add(value);
			return this;
		}
		
		/**
		 * 添加请求Cookie参数
		 * @param name Cookie参数名
		 * @param value Cookie参数值
		 * @return 返回当前对象
		 */
		public Request addCookie(Cookie cookie) {
			this.cookies.add(cookie);
			return this;
		}
		
		/**
		 * 添加请求头
		 * @param name 头参数名
		 * @param value 头参数值
		 * @return 返回当前对象
		 */
		public Request addHeader(Header header) {
			this.headers.add(header);
			return this;
		}
		
		public Request addProperty(String name, Object value) {
			this.properties.put(name, value);
			return this;
		}
		
		/**
		 * 请求链接地址
		 */
		private String url;
		/**
		 * 请求方法GET,POST,PUT,DELETE,HEAD等等
		 */
		private String method;
		/**
		 * 请求参数
		 */
		private Map<String, List<Object>> params;
		/**
		 * 请求头参数
		 */
		private List<Header> headers;
		/**
		 * 请求Cookie参数
		 */
		private List<Cookie> cookies;
		
		private Properties properties;
		
		public String getUrl() {
			return url;
		}
		public String getMethod() {
			return method;
		}
		public Map<String, List<Object>> getParams() {
			return params;
		}
		public List<Header> getHeaders() {
			return headers;
		}
		public List<Cookie> getCookies() {
			return cookies;
		}
		public Properties getProperties() {
			return this.properties;
		}
	}
	
	/**
	 * HTTP响应对象
	 * @author 赖伟威 l.weiwei@163.com 2015-12-01
	 *
	 */
	public static class Response implements Serializable {
		
		private static final long serialVersionUID = -9068800067277456934L;

		public Response(Request request) {
			this.request = request;
			this.headers = new HashMap<String, List<String>>();
		}
		
		public Response(Request request, int statusCode, String statusDesc) {
			this.request = request;
			this.statusCode = statusCode;
			this.statusDesc = statusDesc;
			this.headers = new HashMap<String, List<String>>();
		}
		
		private Request request;
		private int statusCode;
		private String statusDesc;
		private Map<String, List<String>> headers;
		//----- 几个常用的header -----
		private String mimeType;
		private String charset;
		private String location;
		//------------------------
		private byte[] body;
		private String bodyStr;// 默认是null
		//------------------------
		private Throwable exception;
		
		public Response addHeader(String name, String value) {
			List<String> values = this.headers.get(name);
			if (values == null) {
				values = new ArrayList<String>();
				this.headers.put(name, values);
			}
			values.add(value);
			return this;
		}
		
		public Request getRequest() {
			return request;
		}
		public void setRequest(Request request) {
			this.request = request;
		}
		public int getStatusCode() {
			return statusCode;
		}
		public void setStatusCode(int statusCode) {
			this.statusCode = statusCode;
		}
		public String getStatusDesc() {
			return statusDesc;
		}
		public void setStatusDesc(String statusDesc) {
			this.statusDesc = statusDesc;
		}
		public Map<String, List<String>> getHeaders() {
			return headers;
		}
		public String getMimeType() {
			return mimeType;
		}
		public void setMimeType(String mimeType) {
			this.mimeType = mimeType;
		}
		public String getCharset() {
			return charset;
		}
		public void setCharset(String charset) {
			this.charset = charset;
		}
		public byte[] getBody() {
			return body;
		}
		public void setBody(byte[] body) {
			this.body = body;
		}
		public String getLocation() {
			return location;
		}
		public void setLocation(String location) {
			this.location = location;
		}

		public String getBodyStr() {
			if (bodyStr == null) {
				bodyStr = new String(this.body);
			}
			return bodyStr;
		}

		public void setBodyStr(String bodyStr) {
			this.bodyStr = bodyStr;
		}

		public Throwable getException() {
			return exception;
		}

		public void setException(Throwable exception) {
			this.exception = exception;
		}
		
	}
	
}
