package cn.orecraft.search.spider;

import io.searchbox.annotations.JestId;

public class WebSite {
    public String title;
    public String content;
    @JestId
    public String hash;
    public String url;
}
