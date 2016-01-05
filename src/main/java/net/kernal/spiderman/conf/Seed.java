package net.kernal.spiderman.conf;

import java.io.Serializable;

import net.kernal.spiderman.downloader.Downloader;

public class Seed implements Serializable {

	private static final long serialVersionUID = 5767332063572739463L;
	
	private String name;
	private Downloader.Request request;
	public Seed(String name, Downloader.Request request) {
		this.name = name;
		this.request = request;
	}
	public String getName() {
		return name;
	}
	public Downloader.Request getRequest() {
		return request;
	}
	
}
