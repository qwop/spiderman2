package spiderman;

import com.alibaba.fastjson.JSON;

import net.kernal.spiderman.Context;
import net.kernal.spiderman.Spiderman;
import net.kernal.spiderman.conf.Conf;
import net.kernal.spiderman.conf.XMLConfBuilder;
import net.kernal.spiderman.worker.extract.ExtractResult;

/** 网易国内新闻采集 */
public class TestGeneral {
	public static void main(String[] args) {
		final String xml = "general-example.xml";
		final Conf conf = new XMLConfBuilder(xml).build();// 通过XMLBuilder构建CONF对象
		final Context ctx = new Context(conf, (task, c) -> {
			final ExtractResult er = task.getResult();
			final String json =  JSON.toJSONString(er.getFields(), true);
			final String fmt = "获取第%s个[page=%s, model=%s, url=%s]结果：\r\n%s";
			System.err.println(String.format(fmt, c, er.getPageName(), er.getModelName(), task.getRequest().getUrl(), json));
		});
		new Spiderman(ctx).go();//启动，别忘记看控制台信息哦，结束之后会有统计信息的
	}
}
