package spiderman;

import net.kernal.spiderman.Context;
import net.kernal.spiderman.Spiderman;
import net.kernal.spiderman.conf.Conf;
import net.kernal.spiderman.conf.XMLConfBuilder;

public class Tianyancha {

	public static void main(String[] args) throws ClassNotFoundException {
		final String xml = "tianyancha.xml";
		final Conf conf = new XMLConfBuilder(xml).build();// 通过XMLBuilder构建CONF对象
		final Context ctx = new Context(conf);
		new Spiderman(ctx).go();//启动，别忘记看控制台信息哦，结束之后会有统计信息的
	}
	
}
