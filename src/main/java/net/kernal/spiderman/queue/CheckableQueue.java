package net.kernal.spiderman.queue;

import net.kernal.spiderman.Spiderman;

/**
 * 可检查队列，元素入队列的时候会受到检查器检查，只有检查通过的元素方可入队列
 * @author 赖伟威 l.weiwei@163.com 2016-01-19
 *
 */
public abstract class CheckableQueue implements Queue {

	private Checker checker;
	
	public CheckableQueue(Checker checker) {
		this.checker = checker;
	}
	
	public abstract void appendChecked(Element e);
	
	public void append(Element e) {
		if (checker == null) {
			throw new Spiderman.Exception("可检查队列必须有检查器对象");
		}
		if (checker.check(e)) {
			this.appendChecked(e);
		}
	}
	
	public Checker getChecker() {
		return this.checker;
	}
	
	public static interface Checker {
		public boolean check(Element e);
	}
	
}
