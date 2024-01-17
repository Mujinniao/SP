package com.github.catvod.spider;


import android.text.TextUtils;


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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class ruixing extends Spider {


    private static final String siteUrl = "https://www.se9913.com";


    private HashMap<String, String> getHeaders() {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("User-Agent", Util.CHROME);
        return headers;
    }



    public String detailContent(List<String> ids) {
        Document doc = Jsoup.parse(OkHttp.string(siteUrl.concat("/vod/detail/id/").concat(ids.get(0)).concat(".html"), getHeaders()));
        String name = doc.select(".page-title").text();
        String remarks = doc.select("div.video-info-items:nth-child(5) > div:nth-child(2)").text();
        String img = doc.select("img.lazyload:nth-child(2)").attr("data-src");
        String type = doc.select("div.tag-link").text();
        String actor = doc.select("div.video-info-items:nth-child(2) > div:nth-child(2)").text();
        String content = doc.select(".video-info-content > span:nth-child(1)").text();
        String director = doc.select("div.video-info-items:nth-child(1) > div:nth-child(2) > a:nth-child(2)").text();

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
        Elements sources = doc.select("div.module-tab-item");
        Elements sourceList = doc.select("div.module-list > div.module-blocklist > div:nth-child(1)");
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



    public String searchContent(String key, boolean quick) {
        List<Vod> list = new ArrayList<>();
        String target = siteUrl.concat("/vod/search.html?wd=").concat(key);
        Document doc = Jsoup.parse(OkHttp.string(target, getHeaders()));
        for (Element element : doc.select("div.module-search-item")) {
            String img = element.select("div:nth-child(1) > div:nth-child(1) > div:nth-child(1) > img:nth-child(2)").attr("data-src");
            String name = element.select("div:nth-child(2) > div:nth-child(1) > h3:nth-child(2) > a:nth-child(1)").attr("title");
            String remark = element.select("div:nth-child(2) > div:nth-child(1) > a:nth-child(1)").attr("title");
            String id = element.select("div:nth-child(2) > div:nth-child(1) > a:nth-child(1)").attr("href").replaceAll("\\D+","");
            list.add(new Vod(id, name, img, remark));
        }
        return Result.string(list);
    }

    public String playerContent(String flag, String id, List<String> vipFlags) {
        return Result.get().url(siteUrl + id).parse().header(getHeaders()).string();
    }
}

