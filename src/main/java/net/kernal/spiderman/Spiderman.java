package net.kernal.spiderman;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import net.kernal.spiderman.worker.WorkerManager;

/**
 * 蜘蛛侠，根据预言之子设定的目标引领蜘蛛大军开展网络世界采集行动。
 * @author 赖伟威 l.weiwei@163.com 2015-12-01
 * @author 赖伟威 l.weiwei@163.com 2015-12-30
 * @author 赖伟威 l.weiwei@163.com 2016-01-04
 */
public class Spiderman {

	public static final Logger logger = Logger.getLogger(Spiderman.class.getName());
	
	private ExecutorService threads;
	private List<WorkerManager> workerManagers;
	private Context context;
	
	public Spiderman(Context context) {
		this.context = context;
		
		// 6个包工头
		this.workerManagers = new ArrayList<WorkerManager>(6);
		this.threads = Executors.newFixedThreadPool(6);
		
		final WorkerManager.Builder builder = new WorkerManager.Builder(context);
		
		final int dpts1 = context.getConf().getProperties().getInt("downloader.primary.threadSize", 1);
		if (dpts1 > 0) {
			this.workerManagers.add(builder.buildPrimaryDownloadWorkerManager(dpts1));
		}
		
		final int dpts2 = context.getConf().getProperties().getInt("downloader.secondary.threadSize", 1);
		if (dpts2 > 0) {
			this.workerManagers.add(builder.buildSecondaryDownloadWorkerManager(dpts2));
		}
		
		final int ppts1 = context.getConf().getProperties().getInt("parser.primary.threadSize", 1);
		if (ppts1 > 0) {
			this.workerManagers.add(builder.buildPrimaryParseWorkerManager(ppts1));
		}
		
		final int ppts2 = context.getConf().getProperties().getInt("parser.secondary.threadSize", 1);
		if (ppts2 > 0) {
			this.workerManagers.add(builder.buildSecondaryParseWorkerManager(ppts2));
		}
		
		final int rts = context.getConf().getProperties().getInt("result.threadSize", 1);
		if (rts > 0) {
			this.workerManagers.add(builder.buildResultWorkerManager(rts));
		}
	}
	
	/**
	 * 开展行动
	 */
	public Spiderman go() {
		// 将种子添加到队列里
		this.context.getConf().getSeeds().all().forEach((seed) -> {
			this.context.getQueueManager().put(seed, 0);
		});
		// 状态报告: 行动开始了
		this.context.getConf().getReportings().reportStart();
		// 让包工头们开始工作了
		this.workerManagers.forEach(manager -> {
			this.threads.execute(manager);
		});
		// 坐下来喝杯茶，耐心等待工人们的结果
		this._holding();
		return this;
	}
	
	public Spiderman stop() {
		this.threads.shutdownNow();
		this.workerManagers.forEach(w -> {
			w.shutdown();
		});
		this.context.getConf().getReportings().reportStop(this.context.getCounter());
		
		return this;
	}
	
	private void _holding() {
		if (this.context.getCounter().getCountDown() != null) {
			try {
				this.context.getCounter().getCountDown().await();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			stop();
			System.exit(-1);
			return;
		} else {
			Long l = null;
			String duration = this.context.getConf().getProperties().getString("duration");
			if (K.isNotBlank(duration)) {
				try {
					long t = K.convertToSeconds(duration).longValue()*1000L;
					l = new Long(t);
				} catch (Throwable e){}
			}
			
			try {
				if (l == null) {
					Thread.currentThread().join();
				} else {
					Thread.currentThread().join(l);
					System.err.println("[Spiderman][由于配置了duration="+this.context.getConf().getProperties().get("duration")+",现在到时间了需要强制退出,若出现异常请以平常心对待]"+K.formatNow());
				}
				stop();
				System.exit(-1);
			} catch (InterruptedException e) {
				stop();
				System.exit(-1);
			}
		}
	}
	
}
