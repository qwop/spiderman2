package net.kernal.spiderman.queue;

import java.io.File;

import net.kernal.spiderman.K;
import net.kernal.spiderman.Properties;
import net.kernal.spiderman.Spiderman;
import net.kernal.spiderman.logger.Logger;
import net.kernal.spiderman.queue.Queue.AbstractElement;
import net.kernal.spiderman.queue.Queue.Element;
import net.kernal.spiderman.store.BDbStore;
import net.kernal.spiderman.store.KVStore;

public class RepeatableChecker implements CheckableQueue.Checker {

	private Logger logger;
	/** 去重需要用到存储 */
	private KVStore store;
	
	public RepeatableChecker(Properties params, Logger logger) {
		this.logger = logger;
		final String bdbFile = params.getString("queue.checker.bdb.file");
		if (K.isBlank(bdbFile)) {
			throw new Spiderman.Exception("缺少参数: queue.checker.bdb.file, 参考: conf.set(\"queue.checker.bdb.file\")");
		}
		final String dbName = params.getString("queue.checker.bdb.name", "spiderman_queue_repeat_check_db");
		final File file = new File(bdbFile);
		file.mkdirs();
		this.store = new BDbStore(file, dbName);
		logger.debug(RepeatableChecker.class.getName()+" 构建KVStore[name="+dbName+", file="+file.getAbsolutePath()+"]存储对象, 使用BDb实现");
	}
	
	public boolean check(Element e) {
		if (e instanceof AbstractElement) {
			// 检查重复
			final String key = ((AbstractElement)e).getKey();
			if (K.isBlank(key)) {
				return true;
			}
			
			if (store.contains(key)) {
				// key重复了
				logger.info("元素[key="+key+"]重复了");
				return false;
			}
			// 将key存储起来
			this.store.put(key, key.getBytes());
		}
		return true;
	}
	
	public void clear() {
		this.store.close();
	}

}
