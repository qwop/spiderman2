package spiderman;

import org.openqa.selenium.WebDriver;

import net.kernal.spiderman.kit.Context;
import net.kernal.spiderman.kit.K;
import net.kernal.spiderman.worker.download.Downloader;
import net.kernal.spiderman.worker.download.Downloader.Request;
import net.kernal.spiderman.worker.download.Downloader.Response;
import net.kernal.spiderman.worker.download.impl.WebDriverDownloader;

public class MyDownloadListener implements Downloader.Listener {

	public void init(Context context) {
		final Downloader downloader = context.getDownloader();
		final WebDriverDownloader wdDownloader = (WebDriverDownloader)downloader;
		final WebDriver driver = wdDownloader.getWebDriver();
		driver.get("http://cas.qhee.com/cas/login?service=http://oa.qhee.com/oanode");
		final Long delay = K.convertToMillis(context.getParams().getString("worker.download.listener.delay", "0")).longValue();
		if (delay > 0) {
			try {
				Thread.sleep(delay);
			} catch (InterruptedException e) {
			}
		}
	}

	public void beforeDownload(Downloader downloader, Request request) {
	}

	public void afterDownload(Downloader downloader, Response response) {
	}

}
