package spiderman;

import java.io.File;

import net.kernal.spiderman.Downloader;
import net.kernal.spiderman.K;
import net.kernal.spiderman.Properties;
import net.kernal.spiderman.Spiderman;
import net.kernal.spiderman.Spiderman.Seeds;
import net.kernal.spiderman.Spiderman.Targets;
import net.kernal.spiderman.Target;
import net.kernal.spiderman.impl.DefaultConfBuilder;
import net.kernal.spiderman.impl.HtmlCleanerParser;
import net.kernal.spiderman.impl.TextParser;
import net.kernal.spiderman.impl.XPathParser;

public class Test {

	public static void main(String[] args) throws InterruptedException {
		// 构建配置
		Spiderman.Conf conf = new DefaultConfBuilder(){
			// 添加种子链接
			public void addSeed(final Seeds seeds) {
				K.foreach(K.readLine(new File("src/main/resources/company.csv")), new K.ForeachCallback<String>(){
					public void each(int i, String line) {
						if (i > 500) this.breakoff();
						
						String company = line.trim().split(",")[0].replace("'", "");
						seeds.add("http://www.baidu.com/s?wd="+K.urlEncode("\""+company+"\""));
						System.out.println("company->"+company);
					}
				});
			}
			// 添加抽取目标
			public void addTarget(Targets targets) {
				targets.add(
					new Target("baidu-list"){
						public void configRules(Rules rules) {
							rules.addRegexRule("(?=http://www\\.baidu\\.com/s\\?wd\\=).[^&;]*");// URL规则
						}
						public void configModel(Model model) {
							model.addParser(new HtmlCleanerParser());
							model.addField("page_url")
								 .addParser(new XPathParser("//div[@id='page']//a[@href]", "href"))
								 .asNewTask();
						}
					}, 
					new Target("baidu-detail"){
						public void configRules(Rules rules) {
							rules.addStartsWithRule("http://www.baidu.com/s?wd=");// URL规则
						}
						public void configModel(Model model) {
							model.addParser(new HtmlCleanerParser());
							model.addField("target_urls")
								 .addParser(new XPathParser("//div[@id='content_left']//div[@class='result c-container ']//h3//a[@href]", "href"))
								 .asNewTask();
						}
					}
//					, 
//					new Target("baidu-page"){
//						public void configRules(Rules rules) {
//							rules.setPriority(1).add(new Target.Rule(){
//								public boolean matches(Downloader.Request request) {
//									return !request.getUrl().contains("baidu");
//								}
//							});
//						}
//						public void configModel(Model model) {
//							model.addParser(new TextParser());
//						}
//					}
				);
			}
			// 添加配置属性
			public void addProperty(Properties p) {
//				p.put("duration", "10s");
				p.put("threadSize", 100);
				p.put("downloader.limit", 500);
				p.put("downloader.connectionRequestTimeout", 100);
				p.put("downloader.connectTimeout", 500);
				p.put("downloader.socketTimeout", 10000);
				p.put("downloader.maxConnTotal", 500);
				p.put("downloader.maxConnPerRoute", 500);
				/*
				 *   总共花费时间:31087ms 
					  线程池: 总数(150) 运行中(124) 已完成(1884) 
					  计数器: 已下载(1001) 目标(1503) 当前队列(0)
				 */
				// 线程：1    网页：50   时间：116739ms
				// 线程：50   网页：50   时间：4411ms
				// 线程：200  网页：50   时间：3542ms   有效 线程池完成406个任务
				// 线程：100  网页：100  时间：6378ms   有效 线程池完成358个任务
				// 线程：500  网页：100  时间：4962ms   有效 线程池完成823个任务
				// 线程：200  网页：200  时间：8562ms   有效
				// 线程：500  网页：200  时间：9817ms   参数搞错，无效，需重新测试
				// 线程：500  网页：400  时间：9396ms   参数搞错，无效，需重新测试
				// 线程：500  网页：500  时间：9294ms   参数搞错，无效，需重新测试
				// 线程：500  网页：800  时间：9627ms   参数搞错，无效，需重新测试
				// 线程：1000 网页：800  时间：9176ms   参数搞错，无效，需重新测试 线程池完成1007个任务，使用线程数量最大191
				// 线程：200  网页：800  时间：9320ms   参数搞错，无效，需重新测试 线程池完成1115个任务，使用线程数量最大189
				// 线程：500  网页：
			}
		}.build();
		
		new Spiderman(conf).go();
	}
	
}
