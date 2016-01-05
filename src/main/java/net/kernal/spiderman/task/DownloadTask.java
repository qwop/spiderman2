package net.kernal.spiderman.task;

import net.kernal.spiderman.conf.Seed;
import net.kernal.spiderman.downloader.Downloader;

/**
 * 用于下载的任务包
 * @author 赖伟威 l.weiwei@163.com 2015-12-10
 *
 */
public class DownloadTask extends Task {
	
	private static final long serialVersionUID = 945890606939291092L;
	private Object uniqueKey;
	
	/**
	 * 构造器
	 * @param request 请求对象
	 * @param uniqueKey 唯一键
	 * @param priority 优先级数字，值越小将会更优先被处理
	 */
	public DownloadTask(Seed seed, Downloader.Request request, Object uniqueKey, int priority) {
		super(seed, request, priority);
		this.request = request;
		this.uniqueKey = uniqueKey;
	}
	
	public DownloadTask(Seed seed, Downloader.Request request, int priority) {
		this(seed, request, null, priority);
	}
	
	public String getType() {
		return "download";
	}
	
	/**
	 * 请求对象
	 */
	private Downloader.Request request;
	/**
	 * 所属父任务(任务来源,比如来自种子任务，来自某些列表页面任务)
	 */
	private DownloadTask parent;
	/**
	 * 任务深度(跟parent层次有关，后面可做深度控制)
	 */
	private int depth;
	
	public Downloader.Request getRequest() {
		return request;
	}
	public void setRequest(Downloader.Request request) {
		this.request = request;
	}
	public DownloadTask getParent() {
		return parent;
	}
	public void setParent(DownloadTask parent) {
		this.parent = parent;
	}
	public int getDepth() {
		return depth;
	}
	public void setDepth(int depth) {
		this.depth = depth;
	}
	public Object getUniqueKey() {
		return this.uniqueKey;
	}

	@Override
	public String toString() {
		return "Task [request=" + request + ", parent=" + parent + ", depth=" + depth +", priority=" + priority + "]";
	}
	
}
