package net.kernal.spiderman.worker.result.handler;

import java.io.File;
import java.util.concurrent.atomic.AtomicReference;

import net.kernal.spiderman.kit.Context;
import net.kernal.spiderman.kit.Counter;
import net.kernal.spiderman.worker.result.ResultTask;
/**
 * 结果处理接口
 * JDK8新特性默认方法， init ，实现类将会默认继承该方法,也可以覆盖这个方法。
 * <结果处理> 
 * <通过原子引用 Context 来保证 context 上下文是线程安全的> 
 * 
 * @author		qwop
 * @date 		May 16, 2017 
 * @version		[The version number, May 16, 2017] 
 * @see			[Related classes/methods] 
 * @since		[Products/Module version]
 */
public interface ResultHandler {
	
	final AtomicReference<Context> context = new AtomicReference<Context>();
	
	/**
	 * 默认实现加载上下文、获取配置参数 worker.result.store 来保存数据
	 * @param ctx 
	 * @author	qwop
	 * @date	May 16, 2017
	 * @see [Class、Class#Method、Class#Field]
	 */
	public default void init(Context ctx) {
		context.set(ctx);
		final String savePath = ctx.getParams().getString("worker.result.store", "store/result");
		final File dir = new File(savePath);
		if (!dir.exists()) {
			dir.mkdirs();
		}
	}

	public void handle(ResultTask task, Counter c);
	
}