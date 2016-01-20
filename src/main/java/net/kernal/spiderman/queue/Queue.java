package net.kernal.spiderman.queue;

import java.io.Serializable;

public interface Queue {

	public Element take();
	
	public void append(Element element);
	
	public void clear();
	
	public static interface Element extends Serializable {
	}
	
	public static abstract class AbstractElement implements Element {
		
		private static final long serialVersionUID = 5693140072005182715L;
		
		private String key;
		
		public AbstractElement(String key) {
			this.key = key;
		}
		
		public String getKey() {
			return this.key;
		}
		
	}
	
}
