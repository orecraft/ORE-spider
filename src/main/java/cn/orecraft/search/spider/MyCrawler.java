package cn.orecraft.search.spider;

import cn.orecraft.search.spider.mysql.Db;
import cn.orecraft.search.spider.mysql.DbConfig;
import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;
import io.searchbox.core.Index;
import org.apache.commons.codec.digest.DigestUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import static cn.orecraft.search.spider.Bootsrtap.*;

public class MyCrawler extends WebCrawler {
  //  private final static Pattern FILTERS = Pattern.compile("http://www.mcbbs.net/forum.*.html");
  //  private final static Pattern FILTERS_T = Pattern.compile("http://www.mcbbs.net/thread.*.html");


    @Override
    public boolean shouldVisit(Page referringPage, WebURL url) {

        String href = url.getURL().toLowerCase();
        Logger.getAnonymousLogger().info("URL："+href+" 正在被处理");
        Boolean flag=false;
        for(String as:allowedWebSite){
            if(href.startsWith(as)) {
                flag=true;
                break;
            }
        }
        if(!flag)
            return false;
        flag=false;
        for (Pattern p : patternList){
            if(p.matcher(href).matches()) {
                flag = true;
                break;
            }
        }
        if(!flag)
            return false;
        Logger.getAnonymousLogger().info("URL："+href+" 被允许访问");
        return true;

    }

    /**
     * This function is called when a page is fetched and ready
     * to be processed by your program.
     */
    @Override
    public void visit(Page page) {
        String url = page.getWebURL().getURL();
        System.out.println("URL: " + url);

        if (page.getParseData() instanceof HtmlParseData) {
            HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
            String html = htmlParseData.getHtml();
            Document document = Jsoup.parse(html);
            String text="";
            for(DrawRule.Rule rule: drawRule.rules){
                if(url.startsWith(rule.site)){
                    text=document.select(rule.select).text().replace(" ","");
                    //这里写提交给引擎的代码
                    WebSite webSite=new WebSite();
                    webSite.title=htmlParseData.getTitle();
                    webSite.content=text;

                    webSite.url=page.getWebURL().getURL();
                    webSite.hash= DigestUtils.md5Hex(webSite.url);
                    Index index = new Index.Builder(webSite).index("web").type("web").build();
                    try {
                        jestClient.execute(index);
                        Logger.getAnonymousLogger().info("地址:"+webSite.url+"已经成功入库");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                    break;
                }
            }


        }
    }
}
