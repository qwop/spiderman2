package net.kernal.spiderman.worker.extract.schema.rule;

import net.kernal.spiderman.worker.download.Downloader;
/**
 * url匹配规则抽象类
 * <Short overview of features> 
 * <Features detail> 
 * 
 * @author		qwop
 * @date 		May 21, 2017 
 * @version		[The version number, May 21, 2017] 
 * @see			[Related classes/methods] 
 * @since		[Products/Module version]
 */
public abstract class UrlMatchRule {
	
	protected boolean isNegativeEnabled;
	public UrlMatchRule setNegativeEnabled(boolean isNegativeEnabled) {
		this.isNegativeEnabled = isNegativeEnabled;
		return this;
	}
	
	protected abstract boolean doMatches(Downloader.Request request);
	
	public boolean matches(Downloader.Request request) {
		boolean r = this.doMatches(request);
		return this.isNegativeEnabled ? !r : r;
	}

	@Override
	public String toString() {
		return "UrlMatchRule [isNegativeEnabled=" + isNegativeEnabled + "]";
	}
	
}