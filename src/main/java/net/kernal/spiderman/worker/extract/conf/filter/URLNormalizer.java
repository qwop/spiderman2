package net.kernal.spiderman.worker.extract.conf.filter;

import net.kernal.spiderman.worker.extract.URLKit;
import net.kernal.spiderman.worker.extract.conf.Field.ValueFilter;

public class URLNormalizer implements ValueFilter {

	public String filter(Context ctx) {
		final String url = ctx.getValue();
		return URLKit.normalize(ctx.getRequest().getBaseUrl(), url);
	}

}
