package net.kernal.spiderman.queue;

import net.kernal.spiderman.worker.Task;

public interface TaskQueue {

	public Task take();
	
	public void append(Task task);
	
	public void clear();
	
}
