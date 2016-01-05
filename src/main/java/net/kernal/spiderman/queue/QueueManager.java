package net.kernal.spiderman.queue;

import java.io.File;
import java.math.BigDecimal;

import org.zbus.broker.Broker;
import org.zbus.broker.BrokerConfig;
import org.zbus.broker.SingleBroker;
import org.zbus.mq.MqConfig;

import net.kernal.spiderman.Counter;
import net.kernal.spiderman.K;
import net.kernal.spiderman.conf.Conf;
import net.kernal.spiderman.conf.Seed;
import net.kernal.spiderman.conf.Targets;
import net.kernal.spiderman.reporting.Reportings;
import net.kernal.spiderman.store.KVDb;
import net.kernal.spiderman.store.MapDb;
import net.kernal.spiderman.task.DownloadTask;
import net.kernal.spiderman.task.ParseTask;
import net.kernal.spiderman.task.ResultTask;
import net.kernal.spiderman.task.Task;

public class QueueManager {

	private final static String REGION = "urls";
	
	private Targets targets;
	private Reportings reportings;
	private Counter counter;
	private KVDb db;
	private Broker queueBroker;
	
	// 下载(主)队列
	private TaskQueue primaryDownloadTaskQueue;
	// 下载(次)队列
	private TaskQueue secondaryDownloadTaskQueue;
	// 解析(主)队列
	private TaskQueue primaryParseTaskQueue;
	// 解析(次)队列
	private TaskQueue secondaryParseTaskQueue;
	// 结果队列
	private TaskQueue resultTaskQueue;
	
	public QueueManager(Conf conf, Counter counter) {
		this.targets = conf.getTargets();
		this.reportings = conf.getReportings();
		this.counter = counter;
		
		final String file = conf.getProperties().getString("mapdb.file");
		if (K.isNotBlank(file)) {
			this.db = new MapDb(new File(file), conf);
		}
		
		// build queue
		boolean zbusEnabled = conf.getProperties().getBoolean("zbus.enabled", false);
		if (zbusEnabled) {
			//开启分布式支持
			final String zbusServerAddress = conf.getProperties().getString("zbus.serverAddress");
			if (K.isBlank(zbusServerAddress)) {
				throw new RuntimeException("缺少参数zbus.serverAddress");
			}
			final String dcn = conf.getProperties().getString("zbus.duplicateCheckQueueName", "spiderman_duplicate_check_task");
			if (K.isBlank(dcn)) {
				throw new RuntimeException("缺少参数zbus.duplicateCheckQueueName");
			}
			final String dqn = conf.getProperties().getString("zbus.downloadTaskQueueName", "spiderman_download_task");
			if (K.isBlank(dqn)) {
				throw new RuntimeException("缺少参数zbus.downloadTaskQueueName");
			}
			final String pqn = conf.getProperties().getString("zbus.parseTaskQueueName", "spiderman_parse_task");
			if (K.isBlank(pqn)) {
				throw new RuntimeException("缺少参数zbus.parseTaskQueueName");
			}
			final String rqn = conf.getProperties().getString("zbus.resultQueueName", "spiderman_result_task");
			if (K.isBlank(rqn)) {
				throw new RuntimeException("缺少参数zbus.resultQueueName");
			}
		    BrokerConfig brokerConfig = new BrokerConfig();
		    brokerConfig.setServerAddress(zbusServerAddress);
		    try {
		    	this.queueBroker = new SingleBroker(brokerConfig);
		    } catch (Throwable e) {
		    	throw new RuntimeException(e);
		    }
		    String timeout = conf.getProperties().getString("zbus.timeout", "10s");
			this.setPrimaryDownloadTaskQueue(buildQueue(dqn+"_primary", timeout));
			this.setSecondaryDownloadTaskQueue(buildQueue(dqn+"_secondary", timeout));
			this.setPrimaryParseTaskQueue(buildQueue(pqn+"_primary", timeout));
			this.setSecondaryParseTaskQueue(buildQueue(pqn+"_secondary", timeout));
			this.setResultTaskQueue(buildQueue(rqn, timeout));
		} else {
			this.setPrimaryDownloadTaskQueue(new DefaultTaskQueue());
			this.setSecondaryDownloadTaskQueue(new DefaultTaskQueue());
			this.setPrimaryParseTaskQueue(new DefaultTaskQueue());
			this.setSecondaryParseTaskQueue(new DefaultTaskQueue());
			this.setResultTaskQueue(new DefaultTaskQueue());
		}
	}
	
	private TaskQueue buildQueue(String mq, String timeout) {
		MqConfig cfg = new MqConfig(); 
	    cfg.setBroker(queueBroker);
	    cfg.setMq(mq);
	    BigDecimal b = new BigDecimal(K.convertToSeconds(timeout).doubleValue()*1000L);
	    return new ZBusTaskQueue(cfg, b.intValue());
	}
	
	public void put(final Seed seed, int priority) {
		this.put(new DownloadTask(seed, seed.getRequest(), priority));
	}
	
	public void put(final Task task) {
		if (task instanceof DownloadTask) {
			if (db != null) {
				// 检查重复
				final String key = K.md5(task.getRequest().getUrl()+"#"+task.getRequest().getMethod());
				boolean duplicate = db.contains(REGION, key);
				if (duplicate) {
					reportings.reportDuplicate(key, task.getRequest());
					return;
				}
				db.put(REGION, key, (byte)0);// TODO 将内容也保存起来
			}
			
			setPriority(task);
			if (task.isPrimary()) {
				this.primaryDownloadTaskQueue.put(task);
				// 队列计数+1
				counter.primaryDownloadQueuePlus();
			} else {
				this.secondaryDownloadTaskQueue.put(task);
				// 队列计数+1
				counter.secondaryDownloadQueuePlus();
			}
			// 状态报告: 创建新任务
			reportings.reportNewTask(task);
			return;
		}
		
		setPriority(task);
		if (task instanceof ParseTask) {
			// 解析任务
			if (task.isPrimary()) {
				primaryParseTaskQueue.put(task);
				// 队列计数+1
				counter.primaryParseQueuePlus();
			} else {
				secondaryParseTaskQueue.put(task);
				// 队列计数+1
				counter.secondaryParseQueuePlus();
			}
		} else if (task instanceof ResultTask) {
			// 将解析结果放入队列
			resultTaskQueue.put(task);
			// 解析结果计数＋1
			if (task.isPrimary()) {
				this.counter.primaryParsedPlus();
			} else {
				this.counter.secondaryParsedPlus();
			}
		}
		
		// 状态报告: 创建新任务
		reportings.reportNewTask(task);
	}
	
	private void setPriority(Task task) {
		task.setPriority(500);
		targets.all().forEach(tgt -> {
			if (tgt.matches(task.getRequest())) {
				Integer p = null;
				int _p = tgt.getRules().getPriority();
				p = p == null ? _p : (_p < p ? _p : p);
				task.setPriority(p == null ? 1 : p);
			}
		});
	}
	
	public void setPrimaryDownloadTaskQueue(TaskQueue primaryDownloadTaskQueue) {
		this.primaryDownloadTaskQueue = primaryDownloadTaskQueue;
	}
	public void setSecondaryDownloadTaskQueue(TaskQueue secondaryDownloadTaskQueue) {
		this.secondaryDownloadTaskQueue = secondaryDownloadTaskQueue;
	}
	public void setPrimaryParseTaskQueue(TaskQueue primaryParseTaskQueue) {
		this.primaryParseTaskQueue = primaryParseTaskQueue;
	}
	public void setSecondaryParseTaskQueue(TaskQueue secondaryParseTaskQueue) {
		this.secondaryParseTaskQueue = secondaryParseTaskQueue;
	}
	public void setResultTaskQueue(TaskQueue resultTaskQueue) {
		this.resultTaskQueue = resultTaskQueue;
	}
	public void setDb(KVDb db) {
		this.db = db;
	}

	public TaskQueue getPrimaryDownloadTaskQueue() {
		return primaryDownloadTaskQueue;
	}

	public TaskQueue getSecondaryDownloadTaskQueue() {
		return secondaryDownloadTaskQueue;
	}

	public TaskQueue getPrimaryParseTaskQueue() {
		return primaryParseTaskQueue;
	}

	public TaskQueue getSecondaryParseTaskQueue() {
		return secondaryParseTaskQueue;
	}

	public TaskQueue getResultTaskQueue() {
		return resultTaskQueue;
	}
	public KVDb getDb() {
		return this.db;
	}
}
