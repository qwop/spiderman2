package net.kernal.spiderman.conf;

import net.kernal.spiderman.Properties;
import net.kernal.spiderman.reporting.ConsoleReporting;

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
	 * 留给客户端去注册自定义函数，可以直接在script脚本中调用
	 * @param functionName
	 * @param function
	 * @return
	 */
	public abstract void registerFunction(Functions functions);
	
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
		conf.addReporting(new ConsoleReporting(conf.getProperties().getBoolean("debug", true)));
		
		this.addSeed(conf.getSeeds());
		this.addTarget(conf.getTargets());
		for (Target target : conf.getTargets().all()) {
			target.configModel(target.getModel());
			target.configRules(target.getRules());
		}
		
		return conf;
	}

}
