package net.kernal.spiderman.parser;

import java.io.File;
import java.util.List;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

import net.kernal.spiderman.K;

/**
 * 脚本化的转换解析器，使用脚本引擎来将输入的参数值转换成新值作为解析的结果
 * @author 赖伟威 l.weiwei@163.com 2015-12-29
 *
 */
public class ScriptTransformParser extends TransformParser {

	protected String script;
	public ScriptTransformParser(File script) {
		this.script = K.readFile(script);
	}
	public ScriptTransformParser(String script) {
		this.script = script;
	}
	public ScriptTransformParser(File file, String script) {
		String lib = K.readFile(file);
		this.script = lib+";"+script;
	}
	
	public ScriptTransformParser(List<File> files, String script) {
		StringBuilder sb = new StringBuilder();
		files.forEach(file -> {
			StringBuilder lines = new StringBuilder();
			K.readLine(file).forEach(line -> {
				if (lines.length() > 0) lines.append("\r\n");
				lines.append(line);
			});
			if (sb.length() > 0) {
				sb.append(";");
			}
			sb.append(lines.toString());
		});
		sb.append(";").append(script);
		this.script = sb.toString();
	}
	
	public Object transform(Object oldValue) {
		if (this.scriptEngine == null) {
			throw new RuntimeException("清给我设置一个脚本引擎对象，setScriptEngine");
		}
		try {
			Bindings bind = new SimpleBindings();
			bind.put("$this", oldValue);
			return scriptEngine.eval(this.script, bind);
		} catch (ScriptException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static void main(String[] args) {
		long start = System.currentTimeMillis();
		
		ScriptEngine scriptEngine = new ScriptEngineManager().getEngineByName("nashorn");
		
		ScriptTransformParser p = new ScriptTransformParser("var s = 'hello,'+$this; s;");
		p.setScriptEngine(scriptEngine);
		Object v = p.transform("vivi");
		System.out.println(v);
		
		ScriptTransformParser p2 = new ScriptTransformParser(new File("src/main/resources/parser.js"), "addPrefix('http://www.baidu.com')");
		p2.setScriptEngine(scriptEngine);
		Object v2 = p2.transform("/s?wd=baidu");
		System.out.println(v2);
		
		System.out.println(System.currentTimeMillis() - start);
	}
	
}
