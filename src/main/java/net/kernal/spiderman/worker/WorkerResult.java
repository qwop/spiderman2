package net.kernal.spiderman.worker;

import java.io.Serializable;

import net.kernal.spiderman.worker.extract.conf.Page;

public class WorkerResult implements Serializable {
	
	private static final long serialVersionUID = -7049032302488600282L;
	
	private Page page;
	private AbstractTask task;
	private Object result;
	
	public WorkerResult(Page page, AbstractTask task, Object result) {
		this.page = page;
		this.task = task;
		this.result = result;
	}
	
	public Page getPage() {
		return this.page;
	}
	
	public AbstractTask getTask() {
		return this.task;
	}
	
	public Object getResult() {
		return this.result;
	}
	
}
