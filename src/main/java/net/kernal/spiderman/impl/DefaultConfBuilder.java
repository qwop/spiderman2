package net.kernal.spiderman.impl;

import net.kernal.spiderman.Properties;
import net.kernal.spiderman.Spiderman;
import net.kernal.spiderman.Spiderman.Seeds;
import net.kernal.spiderman.Spiderman.Targets;
import net.kernal.spiderman.TaskManager;

/**
 * 默认的配置构建器
 * @author 赖伟威 l.weiwei@163.com 2015-12-01
 *
 */
public abstract class DefaultConfBuilder implements Spiderman.Conf.Builder {

	private Spiderman.Conf conf;
	public DefaultConfBuilder() {
		super();
		conf = new Spiderman.Conf();
		conf.setTaskQueue(new TaskManager(new DefaultTaskQueue(), new DefaultTaskQueue()))
			.setDownloader(new DefaultDownloader(conf.getProperties()))
//			.setParser(new HtmlCleanerParser())
			.addReporting(new ConsoleReporting());
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
	public Spiderman.Conf build() {
		this.addProperty(conf.getProperties());
		this.addSeed(conf.getSeeds());
		this.addTarget(conf.getTargets());
		return conf;
	}

}
