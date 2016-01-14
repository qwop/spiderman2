package net.kernal.spiderman.worker.download;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import net.kernal.spiderman.Properties;
import net.kernal.spiderman.worker.WorkerResult;

public interface Downloader {

	public Response download(Request request);
	
	/**
	 * HTTP请求
	 * @author 赖伟威 l.weiwei@163.com 2015-12-10
	 *
	 */
	public static class Request extends Properties implements Serializable, WorkerResult {
		
		private static final long serialVersionUID = -5271854259417049190L;

		public Request(String url) {
			this(url, "GET");
		}
		
		/**
		 * 实例化
		 * @param url 请求链接地址
		 * @param httpMethod 请求方法 GET,POST,PUT,DELETE,HEAD等等
		 */
		public Request(String url, String httpMethod) {
			this.url = url;
			this.method = httpMethod;
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
		 * 请求头参数
		 */
		private List<Header> headers;
		/**
		 * 请求Cookie参数
		 */
		private List<Cookie> cookies;
		
		public String getUrl() {
			return url;
		}
		public String getMethod() {
			return method;
		}
		
		public List<Header> getHeaders() {
			return headers;
		}
		public List<Cookie> getCookies() {
			return cookies;
		}

		@Override
		public String toString() {
			return "Request [url=" + url + ", method=" + method + ", headers=" + headers + ", cookies=" + cookies + "]";
		}
		
	}
	
	/**
	 * HTTP响应对象
	 * @author 赖伟威 l.weiwei@163.com 2015-12-01
	 *
	 */
	public static class Response implements Serializable, WorkerResult {
		
		private static final long serialVersionUID = -9068800067277456934L;

		public Response(Request request) {
			this.request = request;
		}
		
		public Response(Request request, int statusCode, String statusDesc) {
			this.request = request;
			this.statusCode = statusCode;
			this.statusDesc = statusDesc;
		}
		
		private Request request;
		private int statusCode;
		private String statusDesc;
		//----- 几个常用的header -----
		private String mimeType;
		private String charset;
		private String location;
		//------------------------
		private byte[] body;
		private String bodyStr;// 默认是null
		//------------------------
		private Throwable exception;
		
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

		@Override
		public String toString() {
			return "Response [request=" + request + ", statusCode=" + statusCode + ", statusDesc=" + statusDesc
					+ ", mimeType=" + mimeType + ", charset=" + charset + ", location=" + location + "]";
		}
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
	
	public static class Cookie extends Header {
		public Cookie(String name, String value) {
			super(name, value);
		}
		public Cookie(String name, String value, String path) {
			super(name, value);
			this.path = path;
		}
		
		public Cookie(String name, String value, String domain, String path, Date expiryDate, boolean secure) {
			super(name, value);
			this.domain = domain;
			this.path = path;
			this.expiryDate = expiryDate;
			this.secure = secure;
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
		private String domain;
		private String path;
		private Date expiryDate;
		private boolean secure;
	}
	
}
