package test;

import net.kernal.spiderman.kit.K;
import net.kernal.spiderman.worker.extract.schema.Field.ValueFilter;

public class MyFilter implements ValueFilter {

	public String filter(Context ctx) {
		final String v = ctx.getValue();
		final String pn = K.findOneByRegex(v, "&pn\\=\\d+");
		if ("&pn=0".equals(pn) || K.isBlank(pn))
			return null;
		return ctx.getSeed().getUrl()+pn;
	}

}
