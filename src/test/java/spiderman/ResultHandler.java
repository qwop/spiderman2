package spiderman;

import java.util.concurrent.atomic.AtomicLong;

import com.alibaba.fastjson.JSON;

import net.kernal.spiderman.Counter;
import net.kernal.spiderman.worker.extract.ExtractResult;

public class ResultHandler implements net.kernal.spiderman.worker.extract.ExtractManager.ResultHandler {

	final AtomicLong count = new AtomicLong(0);
	
	public void handle(ExtractResult result, Counter c) {
		if ("网页内容".equals(result.getPageName())) {
			System.err.println("获取到第"+count.incrementAndGet()+"个结果[page="+result.getPageName()+"]："+JSON.toJSONString(result.getValues(), true));
			final byte[] json = JSON.toJSONBytes(result.getValues());
			//context对象利用了接口默认实现方法初始化获得
			context.get().getQueueManager().getQueue("SPIDERMAN_JSON_RESULT").append(json);
		}
	} 

}
