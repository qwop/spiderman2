package spiderman;

import java.io.File;

import com.alibaba.fastjson.JSON;

import net.kernal.spiderman.Context;
import net.kernal.spiderman.Spiderman;
import net.kernal.spiderman.conf.Conf;
import net.kernal.spiderman.conf.XMLConfBuilder;

public class TestXML {

	/**
	 * 测试XML文件方式来构建配置对象，这样的好处是可以将那些不需要代码编写的目标配置规则放到XML文件去，减少代码复杂性和冗杂性。
	 * 还可以在代码里添加种子，添加目标，添加属性等等来跟XML文件的配置进行合并。
	 */
	public static void main(String[] args) {
		final Conf conf = new XMLConfBuilder(new File("src/main/resources/spiderman.conf.xml")).build();
		final Context ctx = new Context(conf, (result, c) -> {
			System.err.println("获得第"+c+"个结果：\r\n"+JSON.toJSONString(result, true));
		});
		new Spiderman(ctx).go();//别忘记看控制台信息哦，结束之后会有统计信息的
	}
	
}
