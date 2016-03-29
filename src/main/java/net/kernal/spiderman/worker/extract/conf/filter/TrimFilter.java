package net.kernal.spiderman.worker.extract.conf.filter;

import net.kernal.spiderman.worker.extract.conf.Field.ValueFilter;

public class TrimFilter implements ValueFilter {

	public String filter(Context ctx) {
		return ctx.getValue().trim();
	}

}
