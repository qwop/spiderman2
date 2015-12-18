package net.kernal.spiderman;

import java.util.ArrayList;
import java.util.List;

/**
 * 网页内容解析器
 * @author 赖伟威 l.weiwei@163.com 2015-12-01
 *
 */
public interface Parser {

	/**
	 * 根据目标规则去解析下载下来的网页内容，并将解析结果返回
	 * @param response 下载下来的网页内容
	 * @param target 包含解析规则的目标对象
	 * @return 返回解析后的结果
	 */
	public void parse(ParserContext context);
	
	/**
	 * 设置上一个解析器
	 * @param parsedResult
	 */
	public void setPrevParser(Parser prevParser);
	
	/**
	 * 设置下一个解析器
	 * @param nextParser
	 */
	public void setNextParser(Parser nextParser);
	
	/**
	 * 解析规则模型
	 */
	public static class Model extends Properties {
		private static final long serialVersionUID = 1L;
	}
	
	/**
	 * 解析后的结果
	 */
	public static class ParsedResult {
		
		private Downloader.Response response;
		private Target target;
		private List<Model> models;
		public ParsedResult(Downloader.Response response, Target target) {
			this(response, target, null);
		}
		public ParsedResult(Downloader.Response response, Target target, List<Model> models) {
			this.response = response;
			this.target = target;
			if (K.isEmpty(models)) {
				this.models = new ArrayList<Model>();
			} else {
				this.models = models;
			}
		}
		public ParsedResult addModel(Model model) {
			this.models.add(model);
			return this;
		}
		public Target getTarget() {
			return this.target;
		}
		public List<Model> getModels() {
			return this.models;
		}
		public Downloader.Response getResponse() {
			return this.response;
		}
		public Model first() {
			return this.models == null ? null : this.models.isEmpty() ? null : this.models.get(0);
		}
	}
	
	public static class ParserContext extends Properties {
		private static final long serialVersionUID = 1L;
		
		public ParserContext(Target target, Downloader.Response response) {
			this.target = target;
			this.response = response;
			this.parsedModels = new ArrayList<Model>();
		}
		private Downloader.Response response;
		private Target target;
		private Object modelParsed;
		private Object fieldParsed;
		private List<Model> parsedModels;
		public Object getModelParsed() {
			return modelParsed;
		}
		public void setModelParsed(Object modelParsed) {
			this.modelParsed = modelParsed;
		}
		public Object getFieldParsed() {
			return fieldParsed;
		}
		public void setFieldParsed(Object fieldParsed) {
			this.fieldParsed = fieldParsed;
		}
		public Target getTarget() {
			return this.target;
		}
		public Downloader.Response getResponse() {
			return this.response;
		}
		public List<Model> getParsedModels(){
			return this.parsedModels;
		}
	}
	
}
