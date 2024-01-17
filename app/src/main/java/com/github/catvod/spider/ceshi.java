package com.github.catvod.spider;


import android.text.TextUtils;

import com.github.catvod.bean.Class;
import com.github.catvod.bean.Result;
import com.github.catvod.bean.Vod;
import com.github.catvod.crawler.Spider;
import com.github.catvod.net.OkHttp;
import com.github.catvod.utils.Util;


import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;




import java.util.ArrayList;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class ceshi extends Spider {


    private static final String siteUrl = "https://www.panghuys.com";


    private HashMap<String, String> getHeaders() {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("User-Agent", Util.CHROME);
        return headers;
    }


    public String homeContent(boolean filter) {
        List<Vod> list = new ArrayList<>();
        List<Class> classes = new ArrayList<>();
        Document doc = Jsoup.parse(OkHttp.string(siteUrl, getHeaders()));
        for (Element element : doc.select("li.swiper-slide > a")) {
            if (element.attr("href").startsWith("/vodtype")) {
                String id = element.attr("href").replaceAll("\\D+", "");
                String name = element.attr("title");
                classes.add(new Class(id, name));
            }
        }
        for (Element element : doc.select("div.module > div:nth-child(2) > div:nth-child(1) > a")) {
            String img = element.select("div:nth-child(1) > div:nth-child(2) > img:nth-child(1)").attr("data-original");
            String name = element.attr("title");
            String remark = element.select("div:nth-child(1) > div:nth-child(1)").text();
            String id = element.attr("href").replaceAll("\\D+","");
            list.add(new Vod(id, name, img, remark));
        }
        return Result.string(classes, list);

    }


    public String categoryContent(String tid, String pg, boolean filter, HashMap<String, String> extend) {
        List<Vod> list = new ArrayList<>();
        String target = siteUrl + String.format("/vodshow/%s/page/%s.html", tid, pg);
        Document doc = Jsoup.parse(OkHttp.string(target, getHeaders()));
        for (Element element : doc.select("div.module > a")) {
            String img = element.select("div:nth-child(1) > div:nth-child(2) > img:nth-child(1)").attr("data-original");
            String name = element.attr("title");
            String remark = element.select("div:nth-child(1) > div:nth-child(1)").text();
            String id = element.attr("href").replaceAll("\\D+","");
            list.add(new Vod(id, name, img, remark));
        }
        return Result.string(list);
    }




    public String detailContent(List<String> ids) {
        Document doc = Jsoup.parse(OkHttp.string(siteUrl.concat("/v/").concat(ids.get(0)).concat(".html"), getHeaders()));
        String name = doc.select(".module-info-heading > h1:nth-child(1)").text();
        String remarks = doc.select("div.module-info-item:nth-child(8) > div:nth-child(2)").text();
        String img = doc.select(".ls-is-cached").attr("data-original");
        String type = doc.select("div.module-info-tag-link:nth-child(3) > a").text();
        String actor = doc.select("div.module-info-item:nth-child(5) > div:nth-child(2) > a").text();
        String content = doc.select(".show-desc > p:nth-child(1)").text();
        String director = doc.select("div.module-info-item:nth-child(4) > div:nth-child(2) > a").text();

        Vod vod = new Vod();
        vod.setVodId(ids.get(0));
        vod.setVodPic(img);
        vod.setVodName(name);
        vod.setVodActor(actor);
        vod.setVodRemarks(remarks);
        vod.setVodContent(content);
        vod.setVodDirector(director);
        vod.setTypeName(type);



        Map<String, String> sites = new LinkedHashMap<>();
        Elements sources = doc.select("div.tab-item");
        Elements sourceList = doc.select("div.module-list > div:nth-child(1) > div:nth-child(1)");
        List<String> blockedKeywords = Collections.singletonList("推荐");
        for (int i = 0; i < sources.size(); i++) {
            Element source = sources.get(i);
            String sourceName = source.text();
            boolean block = false;
            for (String blockedKeyword : blockedKeywords) {
                if (sourceName.contains(blockedKeyword)) {
                    block = true;
                    break;
                }
            }
            if (block) {
                continue;
            }

            Elements playList = sourceList.get(i).select("a");
            List<String> vodItems = new ArrayList<>();
            for (int j = 0; j < playList.size(); j++) {
                Element e = playList.get(j);
                vodItems.add(e.text() + "$" + e.attr("href"));
            }
            if (vodItems.size() > 0) {
                sites.put(sourceName, TextUtils.join("#", vodItems));
            }
        }
        if (sites.size() > 0) {
            vod.setVodPlayFrom(TextUtils.join("$$$", sites.keySet()));
            vod.setVodPlayUrl(TextUtils.join("$$$", sites.values()));
        }


        return Result.string(vod);
    }



    public String searchContent(String key, boolean quick) {
        List<Vod> list = new ArrayList<>();
        String target = siteUrl.concat("/phsch/wd/").concat(key).concat(".html");
        Document doc = Jsoup.parse(OkHttp.string(target, getHeaders()));
        for (Element element : doc.select("div.module-card-item")) {
            String img = element.select("a:nth-child(2) > div:nth-child(1) > div:nth-child(2) > img:nth-child(1)").attr("data-original");
            String name = element.select("div:nth-child(3) > div:nth-child(1) > a:nth-child(1) > strong:nth-child(1)").text();
            String remark = element.select("a:nth-child(2) > div:nth-child(1) > div:nth-child(1)").text();
            String id = element.select("a:nth-child(2)").attr("href").replaceAll("\\D+","");
            list.add(new Vod(id, name, img, remark));
        }
        return Result.string(list);
    }

    public String playerContent(String flag, String id, List<String> vipFlags) {
        return Result.get().url(siteUrl + id).parse().header(getHeaders()).string();
    }
}
