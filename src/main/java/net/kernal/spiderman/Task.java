package net.kernal.spiderman;

public class Task {

	public Task(Downloader.Request request, int priority) {
		this.request = request;
		this.priority = priority;
	}
	
	public boolean isMatchedTarget() {
		return true;
	}
	
	private Downloader.Request request;
	private Task parent;
	private int depth;
	private int priority;
	private Downloader.Response response;
	
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
	public Downloader.Response getResponse() {
		return response;
	}
	public void setResponse(Downloader.Response response) {
		this.response = response;
	}

	public int getPriority() {
		return priority;
	}

	@Override
	public String toString() {
		return "Task [request=" + request + ", parent=" + parent + ", depth=" + depth +", priority=" + priority + ", response=" + response + "]";
	}
	
}
