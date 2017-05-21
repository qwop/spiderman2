package net.kernal.spiderman.worker.extract.schema.filter;

import java.util.Map;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

import net.kernal.spiderman.Spiderman;
import net.kernal.spiderman.worker.extract.schema.Field;
/**
 * 可执行脚本过滤器
 * <Short overview of features> 
 * <Features detail> 
 * 
 * @author		qwop
 * @date 		May 21, 2017 
 * @version		[The version number, May 21, 2017] 
 * @see			[Related classes/methods] 
 * @since		[Products/Module version]
 */
public class ScriptableFilter implements Field.ValueFilter {

	private ScriptEngine scriptEngine;
	private Map<String, Object> bindings;
	private String script;
	public ScriptableFilter(String script) {
		this.script = script;
	}
	
	public void setBindings(Map<String, Object> bindings) {
		this.bindings = bindings;
	}
	
	public void setScriptEngine(ScriptEngine scriptEngine) {
		this.scriptEngine = scriptEngine;
	}
	
	public String filter(Context ctx) {
		if (this.scriptEngine == null) {
			throw new Spiderman.Exception("缺少脚本引擎,请给我设置一个吧 setScriptEngine");
		}
		
		final String value = ctx.getString("value");
		try {
			Bindings bind = new SimpleBindings();
			if (bindings != null) {
				bind.putAll(bindings);
			}
			bind.put("$ctx", ctx);
			bind.put("$this", value);
			
			return (String)scriptEngine.eval(this.script, bind);
		} catch (ScriptException e) {
			e.printStackTrace();
		}
		
		return value;
	}

}
