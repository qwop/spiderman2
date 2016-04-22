package net.kernal.spiderman.worker.extract;

import java.io.Serializable;

import net.kernal.spiderman.kit.Properties;

public class ExtractResult implements Serializable {
	
	private static final long serialVersionUID = 2390695820923166121L;
	
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
	private Properties fields;
	/**
	 * 响应内容体
	 */
	private String responseBody;
	
	public ExtractResult(String pageName, String responseBody, String modelName, Properties fields) {
		this.pageName = pageName;
		this.responseBody = responseBody;
		this.modelName = modelName;
		this.fields = fields;
	}

	public String getPageName() {
		return this.pageName;
	}
	
	public String getResponseBody() {
		return this.responseBody;
	}
	
	public String getModelName() {
		return this.modelName;
	}
	
	public Properties getFields() {
		return this.fields;
	}
	
	@Override
	public String toString() {
		return "ExtractResult [page=" + pageName + ", model=" + modelName + ", fields=" + fields + "]";
	}
	
}
