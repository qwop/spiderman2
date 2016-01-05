package net.kernal.spiderman.parser;

import javax.script.ScriptEngine;

import net.kernal.spiderman.task.ParseTask;

public abstract class ModelParser implements Parser {

	protected ParseTask task;
	protected ScriptEngine scriptEngine;
	protected Parser.ParsedResult prevParsedResult;
	protected ModelParser prevParser;
	public ModelParser(ParseTask task) {
		this.task = task;
	}
	public ModelParser(ParseTask task, ModelParser prevParser) {
		this.task = task;
		this.prevParser = prevParser;
	}
	
	public ModelParser setTask(ParseTask task) {
		this.task = task;
		return this.afterSetTask();
	}
	
	public abstract ModelParser afterSetTask();
	
	public ModelParser setPrevParser(ModelParser prevParser) {
		this.prevParser = prevParser;
		return this;
	}
	public ModelParser setPrevParsedResult(Parser.ParsedResult prevParsedResult) {
		this.prevParsedResult = prevParsedResult;
		return this;
	}
	public ParseTask getTask() {
		return this.task;
	}
	public void setScriptEngine(ScriptEngine scriptEngine) {
		this.scriptEngine = scriptEngine;
	}
}
