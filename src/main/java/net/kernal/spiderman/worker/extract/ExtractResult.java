package net.kernal.spiderman.worker.extract;

import java.io.Serializable;

import net.kernal.spiderman.Properties;
import net.kernal.spiderman.worker.download.Downloader;

public class ExtractResult implements Serializable {
	
	private static final long serialVersionUID = 2390695820923166121L;
	
	private Downloader.Request request;
	
	/**
	 * 所属页面名称
	 */
	private String pageName;
	/**
	 * 结果所属模型名称
	 */
	private String modelName;
	/**
	 * 字段值
	 */
	private Properties values;
	
	public ExtractResult(String pageName, String modelName, Properties values, Downloader.Request request) {
		this.pageName = pageName;
		this.modelName = modelName;
		this.values = values;
		this.request = request;
	}

	public String getPageName() {
		return this.pageName;
	}
	
	public String getModelName() {
		return this.modelName;
	}
	
	public Properties getValues() {
		return this.values;
	}
	
	public Downloader.Request getRequest() {
		return this.request;
	}

	@Override
	public String toString() {
		return "ExtractResult [page=" + pageName + ", model=" + modelName + ", fields=" + values + "]";
	}
	
}
