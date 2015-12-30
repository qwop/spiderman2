package net.kernal.spiderman;

import net.kernal.spiderman.downloader.Downloader;

/**
 * 任务包
 * @author 赖伟威 l.weiwei@163.com 2015-12-10
 *
 */
public class Task {

	/**
	 * 构造器
	 * @param request 请求对象
	 * @param priority 优先级数字，值越小将会更优先被处理
	 */
	public Task(Downloader.Request request, int priority) {
		this.request = request;
		this.priority = priority;
	}
	
	public boolean isMatchedTarget() {
		return true;
	}
	
	/**
	 * 请求对象
	 */
	private Downloader.Request request;
	/**
	 * 所属父任务(任务来源,比如来自种子任务，来自某些列表页面任务)
	 */
	private Task parent;
	/**
	 * 任务深度(跟parent层次有关，后面可做深度控制)
	 */
	private int depth;
	/**
	 * 优先级数字，值越小将会更优先被处理
	 */
	private int priority;
	
	public Downloader.Request getRequest() {
		return request;
	}
	public void setRequest(Downloader.Request request) {
		this.request = request;
	}
	public Task getParent() {
		return parent;
	}
	public void setParent(Task parent) {
		this.parent = parent;
	}
	public int getDepth() {
		return depth;
	}
	public void setDepth(int depth) {
		this.depth = depth;
	}

	public int getPriority() {
		return priority;
	}

	@Override
	public String toString() {
		return "Task [request=" + request + ", parent=" + parent + ", depth=" + depth +", priority=" + priority + "]";
	}
	
}
