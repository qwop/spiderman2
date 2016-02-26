package net.kernal.spiderman.queue;

import java.io.Serializable;

import net.kernal.spiderman.Properties;
import net.kernal.spiderman.logger.Logger;

public interface Queue<E> {

	public E take() throws InterruptedException;
	
	public void append(E element);
	
	public void clear();
	
	public void removeKeys(String group);
	
	public static interface Element extends Serializable {
	}
	
	/**
	 * 提供key和group的抽象元素
	 *
	 */
	public static abstract class AbstractElement implements Element {
		
		private static final long serialVersionUID = 5693140072005182715L;
		
		private String group;
		
		public AbstractElement(String group) {
			this.group = group;
		}
		
		public abstract String getKey();
		
		public String getGroup() {
			return this.group;
		}
		
	}
	
	public static interface Builder {
		public Queue<? extends Element> build(String queueName, Properties params, Logger logger);
	}
	
}
