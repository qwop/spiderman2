package net.kernal.spiderman.task;

public class DuplicateCheckTask extends Task {

	private static final long serialVersionUID = -2475467271138773766L;

	private DownloadTask task;
	
	public DuplicateCheckTask(DownloadTask task) {
		super(task.getRequest(), task.getPriority());
		this.task = task;
	}

	public String getType() {
		return "duplicate-check";
	}
	
	public DownloadTask getDownloadTask() {
		return this.task;
	}

}
