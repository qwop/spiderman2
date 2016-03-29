package net.kernal.spiderman.worker.extract;

import java.io.File;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import net.kernal.spiderman.Context;
import net.kernal.spiderman.Counter;
import net.kernal.spiderman.K;
import net.kernal.spiderman.Spiderman;
import net.kernal.spiderman.logger.Logger;
import net.kernal.spiderman.queue.QueueManager;
import net.kernal.spiderman.worker.Task;
import net.kernal.spiderman.worker.Worker;
import net.kernal.spiderman.worker.WorkerManager;
import net.kernal.spiderman.worker.WorkerResult;
import net.kernal.spiderman.worker.download.DownloadTask;
import net.kernal.spiderman.worker.download.Downloader;
import net.kernal.spiderman.worker.extract.conf.Page;
import net.kernal.spiderman.worker.result.ResultTask;

public class ExtractManager extends WorkerManager {
	
	private List<Page> pages;
	private Downloader downloader;
	
	public ExtractManager(int nWorkers, QueueManager queueManager, Counter counter, Logger logger, List<Page> pages, Downloader downloader) {
		super(nWorkers, queueManager, counter, logger);
		this.pages = pages;
		if (K.isEmpty(pages)) {
			throw new Spiderman.Exception("缺少页面抽取配置");
		}
		this.downloader = downloader;
	}
	
	public List<Page> getPages() {
		return this.pages;
	}
	
	protected void handleResult(WorkerResult wr) {
		final Task task = wr.getTask();
		final Object result = wr.getResult();
		Page page = wr.getPage();
		String group = page.getName();
		if (result instanceof ExtractResult) {
			// 计数器加1
			final long count = getCounter().plus();
			getLogger().info("解析了第"+count+"个模型");
			// 将成果放入结果处理队列
			final ExtractResult extractResult = (ExtractResult)result;
			getQueueManager().append(new ResultTask((ExtractTask)task, extractResult));
		} else if (result instanceof Downloader.Request) {
			final Downloader.Request request = (Downloader.Request)result;
			// group应该是重新匹配Conf里的Pages来获得
			page = this.pages.parallelStream()//多线程来做
			.filter(pg -> pg.matches(request))//过滤，只要能匹配request的page
			.findFirst().orElse(page);
			group = page.getName();
			getQueueManager().append(new DownloadTask((ExtractTask)task, group, request));
		}
	}

	protected Task takeTask() throws InterruptedException {
		return (Task)getQueueManager().getExtractQueue().take();
	}

	protected Worker buildWorker() {
		return new ExtractWorker(pages, this, downloader);
	}
	
	public static interface ResultHandler { 
		AtomicReference<Context> context = new AtomicReference<Context>();
		public default void init(Context ctx){
			context.set(ctx);
			final File dir = new File(ctx.getParams().getString("worker.result.store", "store/result"));
			if (!dir.exists()) {
				dir.mkdirs();
			}
		}
		public void handle(ResultTask task, Counter c);
	}

	protected void clear() {
	}

}
