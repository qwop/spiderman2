package spiderman;

import net.kernal.spiderman.K;
import net.kernal.spiderman.worker.extract.Extractor;
import net.kernal.spiderman.worker.extract.conf.Field.ValueFilter;

public class ResetPageUrlFilter implements ValueFilter {

	public String filter(Extractor e, String v) {
		final String pn = K.findOneByRegex(v, "&pn\\=\\d+");
		return K.isBlank(pn) ? v : e.getTask().getSeed().getUrl()+pn;
	}

}
