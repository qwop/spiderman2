package net.kernal.spiderman.worker.download.impl;

import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import net.kernal.spiderman.kit.K;
import net.kernal.spiderman.kit.Properties;
import net.kernal.spiderman.worker.download.Downloader;

/**
 * 最后的绝招
 * @author 赖伟威 l.weiwei@163.com 2016-03-28
 *
 */
public class WebDriverDownloader extends Downloader {

	public static void main(String[] args) {
		final String url = "http://tianyancha.com/search/page/1?base=%E5%B9%BF%E4%B8%9C";
		final Properties opts = new Properties();
		opts.put("worker.download.chrome.driver", "D:/projects/spiderman/spiderman-webapp/dist/chromedriver.exe");
		Downloader downloader = new WebDriverDownloader(opts);
		Downloader.Request request = new Downloader.Request(url);
		Downloader.Response response = downloader.download(request);
		final String body = response.getBodyStr();
		System.out.println(body);
	}
	
	private ChromeDriver client;
	private long delay;
	
	public WebDriverDownloader() {
		this(new Properties());
	}
	
	public WebDriverDownloader(Properties props) {
		final String driver = props.getString("worker.download.chrome.driver");
		if (K.isNotBlank(driver)) {
			System.getProperties().setProperty("webdriver.chrome.driver", driver);
		}
		this.client = new ChromeDriver();
		final int timeout = props.getInt("worker.download.timeout.implicitlyWait", 10);
		this.client.manage().timeouts().implicitlyWait(timeout, TimeUnit.SECONDS);
		this.delay = K.convertToMillis(props.getString("worker.download.selector.delay", "0")).longValue();
	}
	
	public WebDriver getWebDriver() {
		return this.client;
	}
	
	@Override
	public Response download(Request request) {
		final Downloader.Response response = new Downloader.Response(request);
		try {
			// 让浏览器打开页面
			this.client.navigate().to(request.getUrl());
			// TODO 执行一段脚本，控制浏览器行为
			// delay 一段时间再来select元素
			Thread.sleep(this.delay);
			final WebElement html = this.client.findElement(By.tagName("html"));
			final String bodyStr = html.getAttribute("outerHTML");
			response.setBodyStr(bodyStr);
			response.setBody(bodyStr.getBytes());
		} catch (Throwable e) {
			e.printStackTrace();
			response.setException(e);
		} finally {
		}
		return response;
	}
	
	@Override
	public Downloader keepHeader(Header header) {
		return this;
	}

	@Override
	public Downloader keepCookie(Cookie c) {
		org.openqa.selenium.Cookie cookie = new org.openqa.selenium.Cookie(c.getName(), c.getValue(), c.getDomain(), c.getPath(), c.getExpiryDate(), c.isSecure());
		this.client.manage().addCookie(cookie);
		return this;
	}
	
	public void close() {
		this.client.quit();
		this.client.close();
    }

}
