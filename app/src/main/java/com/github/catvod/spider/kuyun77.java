package com.github.catvod.spider;


import android.text.TextUtils;

import com.github.catvod.bean.Class;
import com.github.catvod.bean.Result;
import com.github.catvod.bean.Vod;
import com.github.catvod.crawler.Spider;
import com.github.catvod.net.OkHttp;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class kuyun77 extends Spider {



    private static final String siteUrl = "http://api.tyun77.cn";


    private HashMap<String, String> getHeaders() {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("User-Agent", "okhttp/3.12.0");
        return headers;
    }


    public String homeContent(boolean filter) throws JSONException {
        List<Vod> list = new ArrayList<>();
        List<Class> classes = new ArrayList<>();
        String target = siteUrl + "/api.php/provide/homeBlock?type_id=0";
        String HomeInfo = OkHttp.string(target, getHeaders());
        JSONObject JSON = new JSONObject(HomeInfo).getJSONObject("data");
        JSONArray List = JSON.getJSONArray("blocks");
        for (int i = 0; i < List.length(); i++) {
            JSONObject item = List.getJSONObject(i);
            String typeid = item.getString("id");
            String typename = item.getString("block_name");
            classes.add(new Class(typeid, typename));
            JSONArray vlist = item.getJSONArray("contents");
            for (int j = 0; j < vlist.length(); j++) {
                JSONObject vod = vlist.getJSONObject(j);
                String img = vod.getString("imgh");
                String name = vod.getString("title");
                String remark = vod.getString("msg");
                String id = vod.getString("id");
                list.add(new Vod(id, name, img, remark));
            }
        }
        return Result.string(classes, list);
    }


    public String categoryContent(String tid, String pg, boolean filter, HashMap<String, String> extend) throws JSONException {
        List<Vod> list = new ArrayList<>();
        String target = siteUrl + String.format("/api.php/provide/searchFilter?type_id=%s&pagesize=24&pagenum=%s", tid, pg);
        String CateInfo = OkHttp.string(target, getHeaders());
        JSONObject JSON = new JSONObject(CateInfo).getJSONObject("data");
        JSONArray List = JSON.getJSONArray("result");
        for (int i = 0; i < List.length(); i++) {
            JSONObject item = List.getJSONObject(i);
            String id = item.getString("id");
            String name = item.getString("title");
            String img = item.getString("videoCover");
            String remark = item.getString("msg");
            list.add(new Vod(id, name, img, remark));
        }
        return Result.string(list);
    }


    public String detailContent(List<String> ids) throws JSONException {
        String DetaInfo = OkHttp.string(siteUrl.concat("/api.php/provide/videoDetail?devid=453CA5D864457C7DB4D0EAA93DE96E66&package=com.sevenVideo.app.android&version=&ids=").concat(ids.get(0)), getHeaders());
        String PlayInfo = OkHttp.string(siteUrl.concat("/api.php/provide/videoPlaylist?devid=453CA5D864457C7DB4D0EAA93DE96E66&package=com.sevenVideo.app.android&version=&ids=").concat(ids.get(0)), getHeaders());
        JSONObject JSON = new JSONObject(DetaInfo);
        JSONObject Play = new JSONObject(PlayInfo);
        JSONObject data = JSON.getJSONObject("data");
        String name = data.getString("videoName");
        String remarks = data.getString("msg");
        String img = data.getString("videoCover");
        String type = data.getString("classifyName");
        String actor = data.getString("starName");
        String content = data.getString("brief");
        String director = data.getString("director");


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
        JSONObject Info = Play.getJSONObject("data");
        JSONArray Player = Info.getJSONArray("episodes");
        for (int i = 0; i < Player.length(); i++) {
            JSONArray URL = Player.getJSONObject(i).getJSONArray("playurls");
            List<String> vodItems = new ArrayList<>();
            for (int j = 0; j < URL.length(); j++) {
                JSONObject item = URL.getJSONObject(j);
                String sourceName = item.getString("playfrom");
                String Pname = item.getString("title");
                String Purl = item.getString("playurl");
                vodItems.add(Pname + "$" + Purl);
                sites.put(sourceName, TextUtils.join("#", vodItems));
                }
            }
        if (sites.size() > 0) {
            vod.setVodPlayFrom(TextUtils.join("$$$", sites.keySet()));
            vod.setVodPlayUrl(TextUtils.join("$$$", sites.values()));
        }
        return Result.string(vod);
    }

    public String searchContent(String key, boolean quick) throws JSONException {
        List<Vod> list = new ArrayList<>();
        String target = siteUrl.concat("search?text=").concat(key);
        String Search = OkHttp.string(target, getHeaders());
        JSONObject JSON = new JSONObject(Search);
        JSONArray List = JSON.has("data") ? JSON.getJSONArray("data") : JSON.getJSONArray("list");
        for (int i = 0; i < List.length(); i++) {
            JSONObject item = List.getJSONObject(i);
            String id = item.getString("vod_id");
            String name = item.getString("vod_name");
            String img = item.getString("vod_pic");
            String remark = item.getString("vod_remarks");
            list.add(new Vod(id, name, img, remark));
        }
        return Result.string(list);
    }

    public String playerContent(String flag, String id, List<String> vipFlags) {
        return Result.get().url(id).parse().header(getHeaders()).string();
    }


    public static boolean isApiReachable(String apiUrl) {
        try {
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("HEAD");
            int responseCode = connection.getResponseCode();
            // 如果返回码在 200 到 399 的范围内，则认为 API 可以访问
            return (responseCode >= 200 && responseCode < 400);
        } catch (IOException e) {
            // 出现异常时，也认为 API 不可访问
            return false;
        }
    }


}

