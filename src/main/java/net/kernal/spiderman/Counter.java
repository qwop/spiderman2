package net.kernal.spiderman;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicLong;

public class Counter {
	
	private CountDownLatch countDown;
	
	private AtomicLong countPrimaryDownload;
	private AtomicLong countSecondaryDownload;
	
	private AtomicLong countPrimaryDownloadQueue;
	private AtomicLong countSecondaryDownloadQueue;
	
	private AtomicLong countPrimaryParseQueue;
	private AtomicLong countSecondaryParseQueue;
	
	private AtomicLong countPrimaryParsed;
	private AtomicLong countSecondaryParsed;
	
	private Threads primaryDownloadPool;
	private Threads secondaryDownloadPool;
	private Threads primaryParsePool;
	private Threads secondaryParsePool;
	
	public Counter() {
		this.countPrimaryDownload = new AtomicLong(0);
		this.countSecondaryDownload = new AtomicLong(0);
		
		this.countPrimaryDownloadQueue = new AtomicLong(0);
		this.countSecondaryDownloadQueue = new AtomicLong(0);
		
		this.countPrimaryParseQueue = new AtomicLong(0);
		this.countSecondaryParseQueue = new AtomicLong(0);
		
		this.countPrimaryParsed = new AtomicLong(0);
		this.countSecondaryParsed = new AtomicLong(0);
		
		this.primaryDownloadPool = new Threads(null);
		this.secondaryDownloadPool = new Threads(null);
		this.primaryParsePool = new Threads(null);
		this.secondaryParsePool = new Threads(null);
	}
	
	public Long primaryDownloadPlus() {
		return this.countPrimaryDownload.addAndGet(1);
	}
	public Long secondaryDownloadPlus() {
		return this.countSecondaryDownload.addAndGet(1);
	}
	
	public Long primaryDownloadQueuePlus() {
		return this.countPrimaryDownloadQueue.addAndGet(1);
	}
	public Long secondaryDownloadQueuePlus() {
		return this.countSecondaryDownloadQueue.addAndGet(1);
	}
	
	public Long primaryParseQueuePlus() {
		return this.countPrimaryParseQueue.addAndGet(1);
	}
	public Long secondaryParseQueuePlus() {
		return this.countSecondaryParseQueue.addAndGet(1);
	}
	
	public Long primaryParsedPlus() {
		if (this.countDown != null) {
			this.countDown.countDown();
		}
		return this.countPrimaryParsed.addAndGet(1);
	}
	public Long secondaryParsedPlus() {
		return this.countSecondaryParsed.addAndGet(1);
	}
	
	public CountDownLatch getCountDown() {
		return countDown;
	}
	public void setCountDown(CountDownLatch countDown) {
		this.countDown = countDown;
	}
	
	public AtomicLong getCountPrimaryDownload() {
		return countPrimaryDownload;
	}
	public AtomicLong getCountSecondaryDownload() {
		return countSecondaryDownload;
	}
	public AtomicLong getCountPrimaryDownloadQueue() {
		return countPrimaryDownloadQueue;
	}
	public AtomicLong getCountSecondaryDownloadQueue() {
		return countSecondaryDownloadQueue;
	}
	public AtomicLong getCountPrimaryParseQueue() {
		return countPrimaryParseQueue;
	}
	public AtomicLong getCountSecondaryParseQueue() {
		return countSecondaryParseQueue;
	}
	public AtomicLong getCountPrimaryParsed() {
		return this.countPrimaryParsed;
	}
	public AtomicLong getCountSecondaryParsed() {
		return this.countSecondaryParsed;
	}
	public Threads getPrimaryDownloadPool() {
		return primaryDownloadPool;
	}
	public void setPrimaryDownloadPool(Threads primaryDownloadPool) {
		this.primaryDownloadPool = primaryDownloadPool;
	}
	public Threads getSecondaryDownloadPool() {
		return secondaryDownloadPool;
	}
	public void setSecondaryDownloadPool(Threads secondaryDownloadPool) {
		this.secondaryDownloadPool = secondaryDownloadPool;
	}
	public Threads getPrimaryParsePool() {
		return primaryParsePool;
	}
	public void setPrimaryParsePool(Threads primaryParsePool) {
		this.primaryParsePool = primaryParsePool;
	}
	public Threads getSecondaryParsePool() {
		return secondaryParsePool;
	}
	public void setSecondaryParsePool(Threads secondaryParsePool) {
		this.secondaryParsePool = secondaryParsePool;
	}

	public static class Threads {
		private ThreadPoolExecutor pool;
		public Threads(ThreadPoolExecutor pool) {
			this.pool = pool;
		}
		public int getPoolSize() {
			return this.pool != null ? this.pool.getCorePoolSize() : 0;
		}
		public int getActiveCount() {
			return this.pool != null ? this.pool.getActiveCount() : 0;
		}
		public long getCompletedTaskCount() {
			return this.pool != null ? this.pool.getCompletedTaskCount() : 0;
		}
	}
}