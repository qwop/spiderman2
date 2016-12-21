package test;

import net.kernal.spiderman.kit.Properties;
import net.kernal.spiderman.worker.download.Downloader;
import net.kernal.spiderman.worker.download.impl.WebDriverDownloader;

/**
 * @author 赖伟威 l.weiwei@163.com 2016-03-28
 * @author <a href='http://krbit.github.io'>Krbit</a>
 * @version V0.1.0
 */
public class TestWebDriverDownloader {
    public static void main(String[] args) {
        final String url = "https://www.baidu.com/";
        final Properties opts = new Properties();
        opts.put("worker.download.chrome.driver", "dist/chromedriver.exe");
        Downloader downloader = new WebDriverDownloader(opts);
        Downloader.Request request = new Downloader.Request(url);
        Downloader.Response response = downloader.download(request);
        final Throwable err = response.getException();
        if (err != null) {
        	err.printStackTrace();
        	return;
        }
        final String body = response.getBodyStr();
        System.out.println(body);
    }
}
