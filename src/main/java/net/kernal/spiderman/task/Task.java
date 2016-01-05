package net.kernal.spiderman.task;

import java.io.Serializable;

import net.kernal.spiderman.downloader.Downloader;

/**
 * 任务包抽象
 * @author 赖伟威 l.weiwei@163.com 2015-12-30
 *
 */
public abstract class Task implements Serializable {

	private static final long serialVersionUID = 1858537902103806934L;

	protected Downloader.Request seed;
	protected Downloader.Request request;
	
	/**
	 * 优先级数字，值越小将会更优先被处理
	 */
	protected int priority;
	
	public Task(Downloader.Request seed, Downloader.Request request, int priority) {
		this.seed = seed;
		this.request = request;
		this.priority = priority;
	}
	
	public Downloader.Request getRequest() {
		return this.request;
	}
	
	public Downloader.Request getSeed() {
		return this.seed;
	}
	
	public int getPriority() {
		return priority;
	}
	
	public void setPriority(int priority) {
		this.priority = priority;
	}
	
	public boolean isPrimary() {
		return this.priority < 5;
	}
	
	public abstract String getType();
	
}
