package cn.orecraft.search.spider;

import cn.edu.hfut.dmic.webcollector.model.CrawlDatums;
import cn.edu.hfut.dmic.webcollector.model.Page;
import cn.edu.hfut.dmic.webcollector.plugin.berkeley.BreadthCrawler;

public class Spider extends BreadthCrawler {
    public Spider(String crawlPath, boolean autoParse) {
        super(crawlPath, autoParse);

    }

    public void visit(Page page, CrawlDatums crawlDatums) {

    }
}
