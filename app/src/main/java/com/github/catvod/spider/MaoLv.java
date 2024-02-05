package com.github.catvod.spider;


import android.text.TextUtils;

import com.github.catvod.bean.Class;
import com.github.catvod.bean.Result;
import com.github.catvod.bean.Vod;
import com.github.catvod.crawler.Spider;
import com.github.catvod.net.OkHttp;
import com.github.catvod.utils.Util;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;




import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class MaoLv extends Spider {


    private static final String siteUrl = "http://maolvys.com";


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
            if (element.attr("href").startsWith("/vod")) {
                String id = element.attr("href").replaceAll("\\D+", "");
                String name = element.text();
                classes.add(new Class(id, name));
            }
        }
        for (Element element : doc.select(".hide-a-8 > div")) {
            String img = element.select("div:nth-child(1) > a:nth-child(1) > img:nth-child(1)").attr("src");
            String name = element.select("div:nth-child(1) > a:nth-child(1)").attr("title");
            String id = element.select("div:nth-child(1) > a:nth-child(1)").attr("href").replaceAll("\\D+","");
            list.add(new Vod(id, name, img));
        }
        return Result.string(classes, list);

    }


    public String categoryContent(String tid, String pg, boolean filter, HashMap<String, String> extend) {
        List<Vod> list = new ArrayList<>();
        String target = siteUrl + String.format("/vod/show/id/%s/page/%s/", tid, pg);
        Document doc = Jsoup.parse(OkHttp.string(target, getHeaders()));
        for (Element element : doc.select("div.public-list-box")) {
            String img = element.select("div:nth-child(1) > a:nth-child(1) > img:nth-child(1)").attr("src");
            String name = element.select("div:nth-child(1) > a:nth-child(1)").attr("title");
            String remark = element.select("div:nth-child(1) > a:nth-child(1) > span:nth-child(3)").text();
            String id = element.select("div:nth-child(1) > a:nth-child(1)").attr("href").replaceAll("\\D+","");
            list.add(new Vod(id, name, img, remark));
        }
        return Result.string(list);
    }




    public String detailContent(List<String> ids) {
        Document doc = Jsoup.parse(OkHttp.string(siteUrl.concat("/vod/detail/id/").concat(ids.get(0)), getHeaders()));
        String name = doc.select(".slide-info-title").text();
        String remarks = doc.select("div.slide-info:nth-child(3)").text();
        String img = doc.select(".detail-pic > img:nth-child(1)").attr("src");
        String type = doc.select("div.slide-info:nth-child(2)").text();
        String actor = doc.select("div.slide-info:nth-child(4) > a").text();
        String content = doc.select("#height_limit").text();


        Vod vod = new Vod();
        vod.setVodId(ids.get(0));
        vod.setVodPic(img);
        vod.setVodName(name);
        vod.setVodActor(actor);
        vod.setVodRemarks(remarks);
        vod.setVodContent(content);
        vod.setTypeName(type);



        Map<String, String> sites = new LinkedHashMap<>();
        Elements sources = doc.select("a.swiper-slide");
        Elements sourceList = doc.select("div.anthology-list-box > div > ul");
        for (int i = 0; i < sources.size(); i++) {
            Element source = sources.get(i);
            String sourceName = source.text();
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


/*
    public String searchContent(String key, boolean quick) throws JSONException {
        List<Vod> list = new ArrayList<>();
        //String target = siteUrl.concat("/index.php/ajax/suggest?mid=1&wd=").concat(key);
        String target = siteUrl.concat("/vod/search/?wd=").concat(key);
        String Search = OkHttp.string(target, getHeaders());
        JSONObject JSON = new JSONObject(Search);
        JSONArray List = JSON.getJSONArray("list");
        for (int i = 0; i < List.length(); i++) {
            JSONObject item = List.getJSONObject(i);
            String id = item.getString("id");
            String name = item.getString("name");
            String img = item.getString("pic");
            list.add(new Vod(id, name, img));
        }
        return Result.string(list);
    }
*/

    public String searchContent(String key, boolean quick) {
        List<Vod> list = new ArrayList<>();
        String target = siteUrl.concat("/vod/search/?wd=").concat(key);
        Document doc = Jsoup.parse(OkHttp.string(target, getHeaders()));
        for (Element element : doc.select("div.public-list-box")) {
            String img = element.select("div:nth-child(2) > a:nth-child(1) > img:nth-child(1)").attr("src");
            String name = element.select("div:nth-child(3) > div:nth-child(1) > div:nth-child(1) > a:nth-child(1)").text();
            String remark = element.select("div:nth-child(2) > a:nth-child(1) > span:nth-child(2)").text();
            String id = element.select("div:nth-child(2) > a:nth-child(1)").attr("href").replaceAll("\\D+","");
            list.add(new Vod(id, name, img, remark));
        }
        return Result.string(list);
    }

    public String playerContent(String flag, String id, List<String> vipFlags) {
        return Result.get().url(siteUrl + id).parse().header(getHeaders()).string();
    }
}

