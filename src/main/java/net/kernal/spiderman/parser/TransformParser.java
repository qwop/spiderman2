package net.kernal.spiderman.parser;

import java.util.ArrayList;
import java.util.List;

import net.kernal.spiderman.K;

/**
 * 转换器，将上一个解析好的结果转换为一个新的结果
 * @author 赖伟威 l.weiwei@163.com 2015-12-29
 *
 */
public abstract class TransformParser extends FieldParser {

	protected ParsedResult parsedResult;
	
	public TransformParser() {}

	public ParsedResult parse() {
		if (this.prevParsedResult != null && K.isNotEmpty(this.prevParsedResult.all())) {
			List<Object> newValues = new ArrayList<Object>();
			this.prevParsedResult.all().forEach((oldValue) -> {
				Object newValue = this.transform(oldValue);
				newValues.add(newValue);
			});
			this.parsedResult = ParsedResult.fromList(newValues);
		}
		return getParsedResult();
	}
	
	public abstract Object transform(Object oldValue);
	
	public ParsedResult getParsedResult() {
		return this.parsedResult;
	}
	
	
}
