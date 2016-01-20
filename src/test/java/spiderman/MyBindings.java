package spiderman;

import java.util.Map;

import net.kernal.spiderman.conf.Conf;

public class MyBindings implements Conf.Bindings {

	public void config(Map<String, Object> bindings, Conf conf) {
		bindings.put("$seeds", conf.getSeeds());
	}
	
}
