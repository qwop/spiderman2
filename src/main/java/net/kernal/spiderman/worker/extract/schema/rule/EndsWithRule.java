package net.kernal.spiderman.worker.extract.schema.rule;

import net.kernal.spiderman.worker.download.Downloader;
/**
 * 以某些字符结束规则
 * <Short overview of features> 
 * <Features detail> 
 * 
 * @author		qwop
 * @date 		May 21, 2017 
 * @version		[The version number, May 21, 2017] 
 * @see			[Related classes/methods] 
 * @since		[Products/Module version]
 */
public class EndsWithRule extends UrlMatchRule {
	
	private String suffix;

	public EndsWithRule(String suffix) {
		if (suffix == null) {
			throw new RuntimeException("suffix can not be null");
		}
		this.suffix = suffix;
	}

	protected boolean doMatches(Downloader.Request request) {
		return request.getUrl().trim().endsWith(suffix);
	}
	
}