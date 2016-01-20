package spiderman;

import net.kernal.spiderman.K;
import net.kernal.spiderman.worker.extract.conf.Field.ValueFilter;

public class ResetPageUrlFilter implements ValueFilter {

	public String filter(Context ctx) {
		final String v = ctx.getValue();
		final String pn = K.findOneByRegex(v, "&pn\\=\\d+");
		return K.isBlank(pn) ? v : ctx.getSeed().getUrl()+pn;
	}

}
