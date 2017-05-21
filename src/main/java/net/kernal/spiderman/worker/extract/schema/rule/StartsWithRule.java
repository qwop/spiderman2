package net.kernal.spiderman.worker.extract.schema.rule;

import net.kernal.spiderman.worker.download.Downloader;
/**
 * 以某些字符串开始规则
 * <Short overview of features> 
 * <Features detail> 
 * 
 * @author		qwop
 * @date 		May 21, 2017 
 * @version		[The version number, May 21, 2017] 
 * @see			[Related classes/methods] 
 * @since		[Products/Module version]
 */
public class StartsWithRule extends UrlMatchRule {
	
	private String prefix;

	public StartsWithRule(String prefix) {
		if (prefix == null) {
			throw new RuntimeException("regex can not be null");
		}
		this.prefix = prefix;
	}

	protected boolean doMatches(Downloader.Request request) {
		return request.getUrl().trim().startsWith(prefix);
	}
	
}