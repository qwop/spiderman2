package net.kernal.spiderman.worker;

public abstract class Worker implements Runnable {

	private WorkerManager manager;
	private Task task;
	private WorkerResult result;
	
	public Worker(WorkerManager manager, Task task) {
		this.manager = manager;
		this.task = task;
	}
	
	protected Task getTask() {
		return this.task;
	}
	
	protected WorkerManager getManager() {
		return this.manager;
	}
	
	protected void setResult(WorkerResult result) {
		this.result = result;
	}
	
	public WorkerResult getResult() {
		return this.result;
	}
	
}
