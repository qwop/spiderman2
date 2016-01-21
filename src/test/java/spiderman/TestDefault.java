package spiderman;

import com.alibaba.fastjson.JSON;

import net.kernal.spiderman.Context;
import net.kernal.spiderman.K;
import net.kernal.spiderman.Properties;
import net.kernal.spiderman.Seed;
import net.kernal.spiderman.Spiderman;
import net.kernal.spiderman.conf.Conf;
import net.kernal.spiderman.conf.Conf.Pages;
import net.kernal.spiderman.conf.Conf.Seeds;
import net.kernal.spiderman.conf.DefaultConfBuilder;
import net.kernal.spiderman.logger.Logger;
import net.kernal.spiderman.worker.extract.HtmlCleanerExtractor;
import net.kernal.spiderman.worker.extract.TextExtractor;
import net.kernal.spiderman.worker.extract.conf.Model;
import net.kernal.spiderman.worker.extract.conf.Page;

/**
 * 这个测试代码完全使用Java代码的方式来配置抽取规则，可以看到配置躲起来之后代码不太好看，至少是比较繁杂的。
 * 另外一个TestXML例子就使用大部分配置通过XML文件加载，小部分用Java代码处理，看起来会好很多。
 */
public class TestDefault {
	
	public static void main(String[] args) {
		final Conf conf = new DefaultConfBuilder() {
			public void configPages(Pages pages) {
				pages.add(new Page("网页内容") {
					public void config(UrlMatchRules rules, Models models) { 
						this.setTaskDuplicateCheckEnabled(true);
						this.setExtractorBuilder(TextExtractor.builder());
						rules.addNegativeContainsRule("baidu"); 
					}
				});
				pages.add(new Page("百度网页搜索") { 
					public void config(UrlMatchRules rules, Models models) {
						this.setExtractorBuilder(HtmlCleanerExtractor.builder());
						rules.addRegexRule("(?=http://www\\.baidu\\.com/s\\?wd\\=).[^&]*(&pn\\=\\d+)?");
						Model model = models.addModel("demo");
						model.addField("详情URL")
							.set("xpath", "//div[@id='content_left']//div[@class='result c-container ']//h3//a[@href]")
							.set("attribute", "href")
							.set("isArray", true)
							.set("isForNewTask", true);
						model.addField("分页URL")
							.set("xpath", "//div[@id='page']//a[@href]")
							.set("attr", "href")
							.set("isArray", true)
							.set("isDistinct", true)
							.set("isForNewTask", true)
							.addFilter(ctx -> {
								final String v = ctx.getValue();
								final String pn = K.findOneByRegex(v, "&pn\\=\\d+");
								return K.isBlank(pn) ? v : ctx.getSeed().getUrl()+pn;
							});
					}
				});
			}
			public void configSeeds(Seeds seeds) {
				seeds.add(new Seed("http://www.baidu.com/s?wd="+K.urlEncode("\"蜘蛛侠\"")));
			}
			public void configParams(Properties params) {
				params.put("logger.level", Logger.LEVEL_INFO);
				params.put("duration", "60s");// 运行时间
				params.put("worker.download.size", 10);// 下载线程数
				params.put("worker.extract.size", 10);// 解析线程数
				params.put("worker.result.size", 10);// 结果处理线程数
				params.put("queue.element.repeatable", false);// 队列元素是否允许重复,默认允许。若不允许，需用到重复检查器
				params.put("queue.checker.bdb.file", "store/checker");// 检查器需用到BDb来存储
			}
		}.build();
		
		final Context ctx = new Context(conf,  (result, c) -> {
			System.err.println(String.format("获得第%s个结果: %s", c, JSON.toJSONString(result, true)));
		}); 
		new Spiderman(ctx).go();//别忘记看控制台信息哦，结束之后会有统计信息的
	}
	
}
