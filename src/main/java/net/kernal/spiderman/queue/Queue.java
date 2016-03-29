package net.kernal.spiderman.queue;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import net.kernal.spiderman.Properties;
import net.kernal.spiderman.logger.Logger;

public interface Queue<E> {

	public E take() throws InterruptedException;
	
	public void append(E element);
	
	public void append(byte[] data);
	
	public void clear();
	
	public void removeKeys(String group);
	
	public static class Element implements Serializable {
		private static final long serialVersionUID = -4673461723487153923L;
		protected byte[] body;
		public void setBody(byte[] body) {
			this.body = body;
		}
		public byte[] getBody() {
			return this.body;
		}
	}
	
	/**
	 * 提供key和group的抽象元素
	 *
	 */
	public static abstract class AbstractElement extends Element {
		
		private static final long serialVersionUID = 5693140072005182715L;
		
		private Map<String, Object> headers;
		
		public AbstractElement(String group) {
			this.headers = new HashMap<String, Object>();
			this.headers.put("group", group);
		}
		public abstract String getKey();
		public String getGroup() {
			return (String)this.headers.get("group");
		}
		public void addHeader(String name, Object value) {
			this.headers.put(name, value);
		}
		public Object getHeader(String name) {
			return this.headers.get(name);
		}
		
	}
	
	public static interface Builder {
		public Queue<? extends Element> build(String queueName, Properties params, Logger logger);
	}
	
}
