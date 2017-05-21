package net.kernal.spiderman.worker.extract.schema.rule;

import net.kernal.spiderman.worker.download.Downloader.Request;
/**
 * 字符串相等规则
 * <Short overview of features> 
 * <Features detail> 
 * 
 * @author		qwop
 * @date 		May 21, 2017 
 * @version		[The version number, May 21, 2017] 
 * @see			[Related classes/methods] 
 * @since		[Products/Module version]
 */
public class EqualsRule extends UrlMatchRule {

	private String url;
	
	public EqualsRule(String url) {
		if (url == null) {
			throw new RuntimeException("url can not be null");
		}
		this.url = url.trim();
	}
	
	protected boolean doMatches(Request request) {
		return url.equals(request.getUrl().trim());
	}

}
