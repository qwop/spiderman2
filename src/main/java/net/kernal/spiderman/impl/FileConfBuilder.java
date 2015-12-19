package net.kernal.spiderman.impl;

import net.kernal.spiderman.Spiderman;
import net.kernal.spiderman.Spiderman.Conf;

public class FileConfBuilder implements Spiderman.Conf.Builder {

	private String path;
	public FileConfBuilder(String path) {
		this.path = path;
	}
	
	public Conf build() {
		return null;
	}

}
