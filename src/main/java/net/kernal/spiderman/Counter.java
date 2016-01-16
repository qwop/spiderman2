package net.kernal.spiderman;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class Counter {

	private CountDownLatch countDown;
	private AtomicLong count;
	private int limit;
	private long timeout;
	private long start;
	private long end;
	
	public Counter(int limit, long timeout) {
		this.limit = limit;
		this.countDown = new CountDownLatch(limit > 0 ? limit : 1);
		this.count = new AtomicLong();
		this.timeout = timeout;
		this.start = System.currentTimeMillis();
	}
	
	public long plus() {
		if (this.limit > 0) {
			this.countDown.countDown();
		}
		return this.count.addAndGet(1);
	}
	
	/**
	 * 供外界主动调用
	 */
	public void stop() {
		final int c = limit > 0 ? limit : 1;
		for (int i = 0; i < c; i++) {
			this.countDown.countDown();
		}
		this.end = System.currentTimeMillis();
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
		this.end = System.currentTimeMillis();
	}
	
	public int getLimit() {
		return this.limit;
	}
	
	public long get() {
		return this.count.get();
	}
	
	public long getCost() {
		final long cost = this.end - this.start;
		return cost;
	}
	
}
