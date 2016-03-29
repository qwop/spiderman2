package net.kernal.spiderman;

import java.io.File;
import java.io.IOException;

import net.kernal.spiderman.Context.ScriptBindings;
import net.kernal.spiderman.conf.Conf;
import net.kernal.spiderman.conf.XMLConfBuilder;
import net.kernal.spiderman.worker.extract.ExtractResult;
import net.kernal.spiderman.worker.result.ResultTask;

import com.alibaba.fastjson.JSON;

/**
 * 启动类
 * @author 赖伟威 l.weiwei@163.com 2016-01-20
 *
 */
public class Bootstrap {

	/** 以XML文件方式来构建配置对象，这样的好处是可以将那些不需要代码编写的配置规则放到XML去，减少代码处理。*/
	public static void main(String[] args) {
		final Properties params = Properties.from(args);// 将参数里的 -k1 v1 -k2 v2 转成 map
		final String xml = params.getString("-conf", "spiderman-bootstrap.xml");// 获得XML配置文件路径
		final Conf conf = new XMLConfBuilder(xml).build();// 通过XMLBuilder构建CONF对象
		new Spiderman(new Context(conf)).go();//启动，别忘记看控制台信息哦，结束之后会有统计信息的
	}
	
	public static class ConsoleResultHandler implements net.kernal.spiderman.worker.extract.ExtractManager.ResultHandler {
		public void handle(ResultTask task, Counter c) {
			final ExtractResult er = task.getResult();
			final String url = task.getRequest().getUrl();
			final String json =  JSON.toJSONString(er.getFields(), true);
			final String fmt = "\r\n获取第%s个[page=%s, model=%s, url=%s]结果：\r\n%s\r\n";
			System.err.println(String.format(fmt, c, er.getPageName(), er.getModelName(), url, json));
		}
	}
	
	public static class FileBodyResultHandler implements net.kernal.spiderman.worker.extract.ExtractManager.ResultHandler {
		public void handle(ResultTask task, Counter c) {
			final ExtractResult er = task.getResult();
			final String url = task.getRequest().getUrl();
			final String content = er.getResponseBody();
			final Context ctx = context.get();
			final String validName = url.replace("\\", "_").replace("/", "_").replace(":", "_").replace("*", "_").replace("?", "_").replace("\"", "_").replace("<", "_").replace(">", "_").replace("|", "_");
			final String fileName = String.format("/body_result_%s_%s_%s_%s.txt", c.get(), er.getPageName(), er.getModelName(), validName);
			final String path = ctx.getParams().getString("worker.result.store", "store/result") + fileName;
			File file = new File(path);
			try {
				K.writeFile(file, content);
				final String fmt = "\r\n保存第%s个[page=%s, model=%s, url=%s]结果[%s]";
				System.err.println(String.format(fmt, c, er.getPageName(), er.getModelName(), url, file.getAbsolutePath()));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static class FileJsonResultHandler implements net.kernal.spiderman.worker.extract.ExtractManager.ResultHandler {
		public void handle(ResultTask task, Counter c) {
			final ExtractResult er = task.getResult();
			final String url = task.getRequest().getUrl();
			final String json =  JSON.toJSONString(er.getFields(), true);
			final Context ctx = context.get();
			final String validName = url.replace("\\", "_").replace("/", "_").replace(":", "_").replace("*", "_").replace("?", "_").replace("\"", "_").replace("<", "_").replace(">", "_").replace("|", "_");
			final String fileName = String.format("/json_result_%s_%s_%s_%s.json", c.get(), er.getPageName(), er.getModelName(), validName);
			final String path = ctx.getParams().getString("worker.result.store", "store/result") + fileName;
			File file = new File(path);
			try {
				K.writeFile(file, json);
				final String fmt = "\r\n保存第%s个[page=%s, model=%s, url=%s]结果[%s]";
				System.err.println(String.format(fmt, c, er.getPageName(), er.getModelName(), url, file.getAbsolutePath()));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static class Bindings implements ScriptBindings {
		public void config(javax.script.Bindings bindings, Context ctx) {
			bindings.put("$ctx", ctx);
			bindings.put("$conf", ctx.getConf());
			bindings.put("$seeds", ctx.getConf().getSeeds());
		}
	}
	
}
