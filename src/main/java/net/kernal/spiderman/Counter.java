package net.kernal.spiderman;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class Counter {

	private CountDownLatch countDown;
	private AtomicLong count;
	private int limit;
	private long timeout;
	private boolean isWorking;
	
	public Counter(int limit, long timeout) {
		this.limit = limit;
		this.countDown = new CountDownLatch(limit > 0 ? limit : 1);
		this.count = new AtomicLong();
		this.timeout = timeout;
		this.isWorking = true;
	}
	
	public long plus() {
		if (this.limit > 0) {
			this.countDown.countDown();
		}
		return this.count.addAndGet(1);
	}
	
	public boolean isWorking() {
		return this.isWorking;
	}
	
	public void await() {
		try {
			if (timeout > 0) {
				this.countDown.await(timeout, TimeUnit.MILLISECONDS);
			} else {
				this.countDown.await();
			}
		} catch (InterruptedException e) {
		}
		this.isWorking = false;
	}
	
}
