package net.kernal.spiderman.queue;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

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
import net.kernal.spiderman.worker.extract.ExtractResult;
import net.kernal.spiderman.worker.extract.ExtractTask;
import net.kernal.spiderman.worker.result.ResultTask;

public class QueueManager {
	
	private Logger logger;
	
	private Map<String, Queue<Object>> queues;
	private Queue<Task> downloadQueue;
	private Queue<Task> extractQueue;
	private Queue<Task> resultQueue;
	
	private KVStore store;
	
	public QueueManager(Properties params, Logger logger) {
		this.logger = logger;
		this.queues = new HashMap<String, Queue<Object>>();
		// 构建存储
		final boolean bdbEnabled = params.getBoolean("store.bdb.enabled", false);
		if (bdbEnabled) {
			final String bdbFile = params.getString("store.bdb.file");
			if (K.isBlank(bdbFile)) {
				throw new Spiderman.Exception("缺少参数: store.bdb.file, 参考: conf.set(\"store.bdb.file\")");
			}
			final String dbName = params.getString("store.bdb.name", "spiderman_store");
			final File file = new File(bdbFile);
			file.mkdirs();
			this.store = new BDbStore(file, dbName);
			logger.debug("构建KVStore[name="+dbName+", file="+file.getAbsolutePath()+"]存储对象, 使用BDb实现");
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
			downloadQueue = new ZBusQueue<Task>(broker, downloadQueueName, true);
			logger.debug("创建下载队列(ZBus)");
			final String extractQueueName = params.getString("queue.download.name", "SPIDERMAN_EXTRACT_TASK");
			extractQueue = new ZBusQueue<Task>(broker, extractQueueName, true);
			logger.debug("创建解析队列(ZBus)");
			final String resultQueueName = params.getString("queue.download.name", "SPIDERMAN_RESULT_TASK");
			resultQueue = new ZBusQueue<Task>(broker, resultQueueName, true);
			logger.debug("创建结果队列(ZBus)");
			// 创建其他队列
			final List<String> queueNames = params.getListString("queue.other.names", "", ",");
			new HashSet<String>(queueNames).parallelStream().filter(n -> K.isNotBlank(n)).forEach(n -> {
				Queue<Object> queue = new ZBusQueue<Object>(broker, n, false);
				queues.put(n, queue);
				logger.debug("创建其他[name="+n+"]队列(ZBus)");
			});
		} else {
			// 构建默认队列
			final int capacity = params.getInt("queue.capacity");
			downloadQueue = new DefaultQueue<Task>(capacity, logger);
			logger.debug("创建下载队列(默认)");
			extractQueue = new DefaultQueue<Task>(capacity, logger);
			logger.debug("创建下载队列(默认)");
			resultQueue = new DefaultQueue<Task>(capacity, logger);
			logger.debug("创建结果队列(默认)");
			// 创建其他队列
			final List<String> queueNames = params.getListString("queue.other.names", "", ",");
			new HashSet<String>(queueNames).parallelStream().filter(n -> K.isNotBlank(n)).forEach(n -> {
				Queue<Object> queue = new DefaultQueue<Object>(capacity, logger);
				queues.put(n, queue);
				logger.debug("创建其他[name="+n+"]队列(默认)");
			});
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
		} else if (task instanceof ResultTask) {
			this.resultQueue.append(task);
			final ExtractResult r = ((ResultTask)task).getResult();
			logger.info("添加结果任务: [page="+r.getPageName()+"].[model="+r.getModelName()+"], 所属种子->"+task.getSeed().getUrl());
		}
	}
	
	public Queue<Task> getDownloadQueue() {
		return this.downloadQueue;
	}
	
	public Queue<Task> getExtractQueue() {
		return this.extractQueue;
	}
	
	public Queue<Task> getResultQueue() {
		return this.resultQueue;
	}
	
	public Queue<Object> getQueue(String name) {
		return this.queues.get(name);
	}
	
	public void register(String name, Queue<Object> queue) {
		if (this.queues.containsKey(name)) {
			throw new Spiderman.Exception("duplicate name " + name);
		}
		
		this.queues.put(name, queue);
	}
	
	public void shutdown() {
		if (this.downloadQueue != null) {
			this.downloadQueue.clear();
		}
		if (this.extractQueue != null) {
			this.extractQueue.clear();
		}
		if (this.resultQueue != null) {
			this.resultQueue.clear();
		}
		if (this.queues != null && !this.queues.isEmpty()) {
			this.queues.forEach((k, q) -> q.clear());
		}
		if (this.store != null) {
			this.store.close();
		}
		
		logger.debug("退出...");
	}
	
}
