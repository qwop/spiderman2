package net.kernal.spiderman.parser;

import java.io.File;
import java.util.List;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

import net.kernal.spiderman.K;
import net.kernal.spiderman.conf.Functions;

/**
 * 脚本化的转换解析器，使用脚本引擎来将输入的参数值转换成新值作为解析的结果
 * @author 赖伟威 l.weiwei@163.com 2015-12-29
 *
 */
public class ScriptTransformParser extends TransformParser {

	protected final String script;
	
	public ScriptTransformParser(String script) {
		this.script = script;
	}
	
	public ScriptTransformParser(File script) {
		this(K.readFile(script));
	}
	public ScriptTransformParser(File file, String script) {
		this(K.readFile(file)+";\r\n"+script);
	}
	
	public ScriptTransformParser(List<File> files, String script) {
		this(loadFiles(files)+";\r\n"+script);
	}
	
	private static String loadFiles(List<File> files) {
		StringBuilder sb = new StringBuilder();
		files.forEach(file -> {
			StringBuilder lines = new StringBuilder();
			K.readLine(file).forEach(line -> {
				if (lines.length() > 0) lines.append("\r\n");
				lines.append(line);
			});
			if (sb.length() > 0) {
				sb.append(";\r\n");
			}
			sb.append(lines.toString());
		});
		return sb.toString();
	}
	
	public Object transform(Object oldValue) {
		if (this.scriptEngine == null) {
			throw new RuntimeException("清给我设置一个脚本引擎对象，setScriptEngine");
		}
		try {
			Bindings bind = new SimpleBindings();
			bind.put("$this", oldValue);
			this.javaInvoker.setParser(this);
			bind.put("$Java", this.javaInvoker);
			
			return scriptEngine.eval(this.script, bind);
		} catch (ScriptException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static class My {
		public Object invoke(String name, Object arg) {
			return "hello, " + name + "  " + arg;
		}
	}
	
	public static void main(String[] args) throws Exception {
		long start = System.currentTimeMillis();
		Functions funcs = new Functions();
		funcs.register("cleanPageUrl", new TransformParser() {
			public Object transform(Object oldValue) {
				return "hello, " + oldValue;
			}
		});
		JavaInvoker java = new JavaInvoker(funcs);
		Bindings bind = new SimpleBindings();
		bind.put("$this", "http://www.baidu.com");
		bind.put("$Java", java);
		ScriptEngine scriptEngine = new ScriptEngineManager().getEngineByName("nashorn");
		Object val = scriptEngine.eval("$Java.invoke('cleanPageUrl', $this)", bind);
		System.out.println(val);
		
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
