package net.kernal.spiderman.conf;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.zbus.broker.Broker;
import org.zbus.broker.BrokerConfig;
import org.zbus.broker.SingleBroker;
import org.zbus.mq.MqConfig;

import net.kernal.spiderman.K;
import net.kernal.spiderman.Properties;
import net.kernal.spiderman.downloader.DefaultDownloader;
import net.kernal.spiderman.queue.DefaultTaskQueue;
import net.kernal.spiderman.queue.TaskQueue;
import net.kernal.spiderman.queue.ZBusTaskQueue;
import net.kernal.spiderman.reporting.ConsoleReporting;
import net.kernal.spiderman.task.TaskManager;

/**
 * 默认的配置构建器
 * @author 赖伟威 l.weiwei@163.com 2015-12-01
 *
 */
public abstract class DefaultConfBuilder implements Conf.Builder {

	protected Conf conf;
	public DefaultConfBuilder() {
		super();
		conf = new Conf();
	}
	/**
	 * 留给客户端程序去添加属性
	 * @param properties
	 */
	public abstract void addProperty(Properties properties);
	/**
	 * 留给客户端程序去添加种子
	 * @param seeds
	 */
	public abstract void addSeed(Seeds seeds);
	/**
	 * 留给客户端程序去添加目标
	 * @param targets
	 */
	public abstract void addTarget(Targets targets);
	
	/**
	 * 构建Spiderman.Conf对象
	 */
	public Conf build() {
		this.addProperty(conf.getProperties());
		boolean zbusEnabled = conf.getProperties().getBoolean("zbus.enabled", false);
		if (zbusEnabled) {
			//开启分布式支持
			final String zbusServerAddress = conf.getProperties().getString("zbus.serverAddress");
			if (K.isBlank(zbusServerAddress)) {
				throw new RuntimeException("缺少参数zbus.serverAddress");
			}
			final String dqn = conf.getProperties().getString("zbus.downloadTaskQueueName", "spiderman_download_task");
			if (K.isBlank(dqn)) {
				throw new RuntimeException("缺少参数zbus.downloadTaskQueueName");
			}
			final String pqn = conf.getProperties().getString("zbus.parseTaskQueueName", "spiderman_parse_task");
			if (K.isBlank(pqn)) {
				throw new RuntimeException("缺少参数zbus.parseTaskQueueName");
			}
		    BrokerConfig brokerConfig = new BrokerConfig();
		    brokerConfig.setServerAddress(zbusServerAddress);
		    Broker broker = null;
		    try {
		    	broker = new SingleBroker(brokerConfig);
		    	conf.setZbusBroker(broker);
		    } catch (Throwable e) {
		    	throw new RuntimeException(e);
		    }
		    
			conf.setDownloadTaskQueue(new TaskManager(buildQueue(broker, dqn+"_primary"), buildQueue(broker, dqn+"_secondary")));
		    conf.setParseTaskQueue(new TaskManager(buildQueue(broker, pqn+"_primary"), buildQueue(broker, pqn+"_secondary")));
		} else {
			conf.setDownloadTaskQueue(new TaskManager(new DefaultTaskQueue(), new DefaultTaskQueue()));
		    conf.setParseTaskQueue(new TaskManager(new DefaultTaskQueue(), new DefaultTaskQueue()));
		}
		
		conf.setDownloader(new DefaultDownloader(conf.getProperties()))
		    .addReporting(new ConsoleReporting());
		
		final String engineName = conf.getProperties().getString("scriptEngine", "nashorn");
		final ScriptEngine scriptEngine = new ScriptEngineManager().getEngineByName(engineName);
		this.conf.setScriptEngine(scriptEngine);
		
		this.addSeed(conf.getSeeds());
		this.addTarget(conf.getTargets());
		for (Target target : conf.getTargets().all()) {
			target.configModel(target.getModel());
			target.configRules(target.getRules());
		}
		return conf;
	}

	private TaskQueue buildQueue(Broker broker, String mq) {
		MqConfig cfg = new MqConfig(); 
	    cfg.setBroker(broker);
	    cfg.setMq(mq);
	    String timeout = conf.getProperties().getString("zbus.timeout", "5s");
	    return new ZBusTaskQueue(cfg, K.convertToSeconds(timeout).intValue()*1000);
	}
	
}
