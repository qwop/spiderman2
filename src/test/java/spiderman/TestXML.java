package spiderman;

import net.kernal.spiderman.client.Bootstrap;

/** 以XML文件方式来构建配置对象，这样的好处是可以将那些不需要代码编写的配置规则放到XML去，减少代码处理。*/
public class TestXML {

	public static void main(String[] args) {
		Bootstrap.main(new String[]{"-conf", "advanced-example.xml"});
	}
	
}
