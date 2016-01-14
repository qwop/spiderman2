package net.kernal.spiderman.queue;

import java.io.File;

import org.zbus.broker.Broker;
import org.zbus.broker.BrokerConfig;
import org.zbus.broker.SingleBroker;

import net.kernal.spiderman.K;
import net.kernal.spiderman.Properties;
import net.kernal.spiderman.Spiderman;
import net.kernal.spiderman.logger.Logger;
import net.kernal.spiderman.store.BDbStore;
import net.kernal.spiderman.store.KVStore;
import net.kernal.spiderman.worker.Task;
import net.kernal.spiderman.worker.download.DownloadTask;
import net.kernal.spiderman.worker.extract.ExtractTask;

public class QueueManager {
	
	private Logger logger;
	
	private TaskQueue downloadQueue;
	private TaskQueue extractQueue;
	private KVStore store;
	
	public QueueManager(Properties params, Logger logger) {
		this.logger = logger;
		// 构建存储
		final boolean bdbEnabled = params.getBoolean("store.bdb.enabled", false);
		if (bdbEnabled) {
			final String bdbFile = params.getString("store.bdb.file");
			if (K.isBlank(bdbFile)) {
				throw new Spiderman.Exception("缺少参数: store.bdb.file, 参考: conf.set(\"store.bdb.file\")");
			}
			final String dbName = params.getString("store.bdb.name", "spiderman_store");
			this.store = new BDbStore(new File(bdbFile), dbName);
		}
		
		// 构建队列
		final boolean zbusEnabled = params.getBoolean("queue.zbus.enabled", false);
		if (zbusEnabled) {
			// 构建ZBus队列
			final String server = params.getString("queue.zbus.server");
			if (K.isBlank(server)) {
				throw new Spiderman.Exception("缺少参数: queue.zbus.server, 参考: conf.set(\"queue.zbus.enabled\")");
			}
			final BrokerConfig brokerConfig = new BrokerConfig();
		    brokerConfig.setServerAddress(server);
		    final Broker broker;
		    try {
		    	broker = new SingleBroker(brokerConfig);
		    } catch (Throwable e) {
		    	throw new Spiderman.Exception("连接ZBus服务失败", e);
		    }
		    final String downloadQueueName = params.getString("queue.download.name", "SPIDERMAN_DOWNLOAD_TASK");
			final String extractQueueName = params.getString("queue.download.name", "SPIDERMAN_DOWNLOAD_TASK");
			downloadQueue = new ZBusTaskQueue(broker, downloadQueueName);
			logger.debug("创建ZBus下载队列");
			extractQueue = new ZBusTaskQueue(broker, extractQueueName);
			logger.debug("创建ZBus解析队列");
		} else {
			// 构建默认队列
			downloadQueue = new DefaultTaskQueue();
			logger.debug("创建默认下载队列");
			extractQueue = new DefaultTaskQueue();
			logger.debug("创建默认下载队列");
		}
	}
	
	public void append(Task task) {
		// 检查重复
		final String key = task.getUniqueKey();
		if (this.store != null && K.isNotBlank(key)) {
			if (this.store.contains(key)) {
				// key重复了
				logger.info("任务[key="+key+"]重复了");
				return;
			}
			// 将key存储起来
			this.store.put(key, key.getBytes());
		}
		if (task instanceof DownloadTask) {
			this.downloadQueue.append(task);
			logger.info("添加下载任务: "+((DownloadTask)task).getRequest().getUrl()+", 所属种子->"+task.getSeed().getUrl());
		} else if (task instanceof ExtractTask) {
			this.extractQueue.append(task);
			logger.info("添加解析任务: "+((ExtractTask)task).getResponse().getRequest().getUrl()+", 所属种子->"+task.getSeed().getUrl());
		}
	}
	
	public TaskQueue getDownloadQueue() {
		return this.downloadQueue;
	}
	
	public TaskQueue getExtractQueue() {
		return this.extractQueue;
	}
	
	public void shutdown() {
		if (this.downloadQueue != null) {
			this.downloadQueue.clear();
		}
		if (this.extractQueue != null) {
			this.extractQueue.clear();
		}
		if (this.store != null) {
			this.store.close();
		}
		
		logger.debug("退出...");
	}
	
}
