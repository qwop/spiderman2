package net.kernal.spiderman.worker;

import net.kernal.spiderman.queue.Queue;
import net.kernal.spiderman.worker.download.Downloader;

public abstract class Task extends Queue.AbstractElement {

	private static final long serialVersionUID = 2506296221733528670L;

	/** 种子 */
	private Downloader.Request seed;
	/** 请求 */
	private Downloader.Request request;
	
	protected Task(String key, Downloader.Request seed) {
		this(key, seed, seed);
	}
	
	protected Task(String key, Downloader.Request seed, Downloader.Request request) {
		super(key);
		this.seed = seed;
		this.request = request;
	}
	
	public Downloader.Request getSeed() {
		return this.seed;
	}
	
	public Downloader.Request getRequest() {
		return this.request;
	}
	
}
