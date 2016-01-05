package net.kernal.spiderman.task;

import net.kernal.spiderman.downloader.Downloader;

/**
 * 用于解析的任务包
 * @author 赖伟威 l.weiwei@163.com 2015-12-30
 *
 */
public class ParseTask extends Task {
	
	private static final long serialVersionUID = -6399150276163991806L;
	
	private Downloader.Response response;
	public ParseTask(Downloader.Request seed, Downloader.Response response, int priority) {
		super(seed, response.getRequest(), priority);
		this.response = response;
	}
	
	public Downloader.Response getResponse() {
		return this.response;
	}
	
	public String getType() {
		return "parse";
	}
}
