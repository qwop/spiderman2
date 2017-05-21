package net.kernal.spiderman.worker.extract.schema.rule;

import net.kernal.spiderman.worker.download.Downloader;
/**
 * 字符包含规则
 * <Short overview of features> 
 * <Features detail> 
 * 
 * @author		qwop
 * @date 		May 21, 2017 
 * @version		[The version number, May 21, 2017] 
 * @see			[Related classes/methods] 
 * @since		[Products/Module version]
 */
public class ContainsRule extends UrlMatchRule {
	
	private String chars;

	public ContainsRule(String chars) {
		if (chars == null) {
			throw new RuntimeException("chars can not be null");
		}
		this.chars = chars.trim();
	}

	public boolean doMatches(Downloader.Request request) {
		return request.getUrl().trim().contains(chars);
	}
	
}