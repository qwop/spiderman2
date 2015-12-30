package net.kernal.spiderman.queue;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import net.kernal.spiderman.Task;

public class DefaultTaskQueue implements TaskQueue {
	
	private BlockingQueue<Task> queue = new LinkedBlockingQueue<Task>(5000);
	
	public Task poll() {
		return this.queue.poll();
	}
	
	public void put(Task task) {
		try {
			this.queue.put(task);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public long size() {
		return this.queue.size();
	}
	
}
