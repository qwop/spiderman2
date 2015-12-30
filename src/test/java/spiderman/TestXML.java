package spiderman;

import java.io.File;

import net.kernal.spiderman.K;
import net.kernal.spiderman.Spiderman;
import net.kernal.spiderman.conf.Conf;
import net.kernal.spiderman.conf.Target;
import net.kernal.spiderman.conf.XMLConfBuilder;
import net.kernal.spiderman.parser.TextParser;

public class TestXML {

	/**
	 * 测试XML文件方式来构建配置对象，这样的好处是可以将那些不需要代码编写的目标配置规则放到XML文件去，减少代码复杂性和冗杂性。
	 * 还可以在代码里添加种子，添加目标，添加属性等等来跟XML文件的配置进行合并。
	 */
	public static void main(String[] args) {
		Conf conf = 
			new XMLConfBuilder(new File("src/main/resources/baidu-search.xml"))//XML配置构建器
			.addSeed("http://www.baidu.com/s?wd="+K.urlEncode("\"蜘蛛侠\""))//种子
			.addTarget(new Target("网页内容"){//目标
				public void configRules(Rules rules) {
					rules.setPriority(1).addNotContainsRule("baidu");//目标URL规则
				}
				public void configModel(Model model) {
					model.addParser(new TextParser());// 目标解析规则，这里直接用通用的正文抽取器解析
				}
			})
			.set("duration", "20s")//持续时间
			.set("downloader.threadSize", 20)//下载线程数量
			.set("parser.threadSize", 10)//解析线程数量
//			.set("parsedLimit", 10)//解析网页数量上限，达到后将会自动结束行动
			.build();
		
		new Spiderman(conf).go();//别忘记看控制台信息哦，结束之后会有统计信息的,查看关键词"[结束]"(去掉双引号来查找)
	}
	
}
