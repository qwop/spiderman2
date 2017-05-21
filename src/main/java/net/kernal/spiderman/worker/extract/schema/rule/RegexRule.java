package net.kernal.spiderman.worker.extract.schema.rule;

import net.kernal.spiderman.worker.download.Downloader;
/**
 * 字符串正则表达式匹配规则
 * <Short overview of features> 
 * <Features detail> 
 * 
 * @author		qwop
 * @date 		May 21, 2017 
 * @version		[The version number, May 21, 2017] 
 * @see			[Related classes/methods] 
 * @since		[Products/Module version]
 */
public class RegexRule extends UrlMatchRule {
	
	private String regex;

	public RegexRule(String regex) {
		if (regex == null) {
			throw new RuntimeException("regex can not be null");
		}
		this.regex = regex.trim();
	}

	protected boolean doMatches(Downloader.Request request) {
		return request.getUrl().trim().matches(this.regex);
	}
	
}