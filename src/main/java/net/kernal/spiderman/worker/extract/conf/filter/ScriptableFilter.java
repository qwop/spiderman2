package net.kernal.spiderman.worker.extract.conf.filter;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

import net.kernal.spiderman.Spiderman;
import net.kernal.spiderman.worker.extract.Extractor;
import net.kernal.spiderman.worker.extract.conf.Field;

public class ScriptableFilter implements Field.ValueFilter {

	private ScriptEngine scriptEngine;
	private String script;
	public ScriptableFilter(String script) {
		this.script = script;
	}
	public void setScriptEngine(ScriptEngine scriptEngine) {
		this.scriptEngine = scriptEngine;
	}
	
	public String filter(Extractor extractor, String value) {
		if (this.scriptEngine == null) {
			throw new Spiderman.Exception("缺少脚本引擎,请给我设置一个吧 setScriptEngine");
		}
		try {
			Bindings bind = new SimpleBindings();
			bind.put("$this", value);
			
			return (String)scriptEngine.eval(this.script, bind);
		} catch (ScriptException e) {
			e.printStackTrace();
		}
		
		return value;
	}

}
