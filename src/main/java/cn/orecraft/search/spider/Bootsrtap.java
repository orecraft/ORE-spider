package cn.orecraft.search.spider;


import cn.orecraft.search.spider.mysql.Db;
import cn.orecraft.search.spider.mysql.DbConfig;
import com.google.gson.Gson;
import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.crawler.authentication.BasicAuthInfo;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.config.HttpClientConfig;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class Bootsrtap {
    public static ArrayList<Map<String,String>> url_list=new ArrayList<>();
    public static DbConfig dbConfig;
    public static JestClient jestClient;
    public static String elHost;
    public static int elPort;
    public static DrawRule drawRule;
    public static String[] allowedWebSite;
    public static List<Pattern> patternList=new ArrayList<>();
    public static void main(String[] args) throws Exception {
        Logger.getLogger("SPIDER").info("Starting OreCraft Web Spider.");
        File file =new File("setting.properties");
        if(!file.exists()){
            Logger.getAnonymousLogger().warning("Could not found config file!");
            return;
        }
        Properties properties;
        try {
            properties=Config.loadConfig(file);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        Logger.getAnonymousLogger().info("Init jest......");
        try {
            JestClientFactory factory = new JestClientFactory();
            factory.setHttpClientConfig(new HttpClientConfig
                    .Builder(String.format("http://%s:%s",properties.getProperty("host"),properties.getProperty("port")))
                    .multiThreaded(true)
                    //Per default this implementation will create no more than 2 concurrent connections per given route
                    .defaultMaxTotalConnectionPerRoute(100)
                    // and no more 20 connections in total
                    .maxTotalConnection(512)
                        .build());
            jestClient = factory.getObject();
        }catch (Exception e){
            e.printStackTrace();
            System.exit(-2);
        }




        CrawlConfig config = new CrawlConfig();

        // Set the folder where intermediate crawl data is stored (e.g. list of urls that are extracted from previously
        // fetched pages and need to be crawled later).
        config.setCrawlStorageFolder("./tmp/crawler4j/");

        // Be polite: Make sure that we don't send more than 1 request per second (1000 milliseconds between requests).
        // Otherwise it may overload the target servers.
        config.setPolitenessDelay(200);

        // You can set the maximum crawl depth here. The default value is -1 for unlimited depth.

        // You can set the maximum number of pages to crawl. The default value is -1 for unlimited number of pages.


        // Should binary data should also be crawled? example: the contents of pdf, or the metadata of images etc
        config.setIncludeBinaryContentInCrawling(false);

        // Do you need to set a proxy? If so, you can use:
        // config.setProxyHost("proxyserver.example.com");
        // config.setProxyPort(8080);

        // If your proxy also needs authentication:
        // config.setProxyUsername(username); config.getProxyPassword(password);

        // This config parameter can be used to set your crawl to be resumable
        // (meaning that you can resume the crawl from a previously
        // interrupted/crashed crawl). Note: if you enable resuming feature and
        // want to start a fresh crawl, you need to delete the contents of
        // rootFolder manually.
        config.setResumableCrawling(false);

        // Set this to true if you want crawling to stop whenever an unexpected error
        // occurs. You'll probably want this set to true when you first start testing
        // your crawler, and then set to false once you're ready to let the crawler run
        // for a long time.

        //config.setCookiePolicy("ccdefend='0919e1583ac14dfec8ffbb1365ac4baa'");
        Collection<BasicHeader> collection = config.getDefaultHeaders();
        collection.add(new BasicHeader("Cookie","ccdefend=0919e1583ac14dfec8ffbb1365ac4baa"));
        config.setDefaultHeaders(collection);
        // Instantiate the controller for this crawl.
        PageFetcher pageFetcher = new PageFetcher(config);
        RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
        RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
        CrawlController controller = new CrawlController(config, pageFetcher, robotstxtServer);


        // For each crawl, you need to add some seed urls. These are the first
        // URLs that are fetched and then the crawler starts following links
        // which are found in these pages
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet httpget = new HttpGet(properties.getProperty("seeds"));
        HttpEntity entity =  httpclient.execute(httpget).getEntity();
        String seedsJson= EntityUtils.toString(entity);
        Seeds seeds = new Gson().fromJson(seedsJson,Seeds.class);

        for (String seed : seeds.seeds) {
            Logger.getAnonymousLogger().info(seed);
            controller.addSeed(seed);
        }

        httpget = new HttpGet(properties.getProperty("white"));
        entity =  httpclient.execute(httpget).getEntity();
        String whiteJson= EntityUtils.toString(entity);
        WhiteList whiteList = new Gson().fromJson(whiteJson,WhiteList.class);
        allowedWebSite=whiteList.white;


        httpget = new HttpGet(properties.getProperty("pattern"));
        entity =  httpclient.execute(httpget).getEntity();
        String patternJson= EntityUtils.toString(entity);
        PatternList patternList1 = new Gson().fromJson(patternJson,PatternList.class);
        for(String p:patternList1.pattern){
            patternList.add(Pattern.compile(p));
        }

        httpget = new HttpGet(properties.getProperty("rules"));
        entity =  httpclient.execute(httpget).getEntity();
        String ruleJson= EntityUtils.toString(entity);
        drawRule = new Gson().fromJson(ruleJson,DrawRule.class);

        // Number of threads to use during crawling. Increasing this typically makes crawling faster. But crawling
        // speed depends on many other factors as well. You can experiment with this to figure out what number of
        // threads works best for you.
        int numberOfCrawlers = 8;

        // To demonstrate an example of how you can pass objects to crawlers, we use an AtomicInteger that crawlers
        // increment whenever they see a url which points to an image.

        // The factory which creates instances of crawlers.
        CrawlController.WebCrawlerFactory<MyCrawler> factory = () -> new MyCrawler();

        // Start the crawl. This is a blocking operation, meaning that your code
        // will reach the line after this only when crawling is finished.
        controller.start(factory, numberOfCrawlers);

    }


}
