package com.github.catvod.spider;

import android.content.Context;
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


import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class appcs extends Spider {


    public void init(Context context, String extend) throws Exception {
        super.init(context, extend);
        siteUrl = extend;
    }


    private static String siteUrl = "";


    private HashMap<String, String> getHeaders() {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("User-Agent", Util.CHROME);
        return headers;
    }


    public String homeContent(boolean filter) throws JSONException {
        List<Vod> list = new ArrayList<>();
        List<Class> classes = new ArrayList<>();
        String target = siteUrl + "index_video";
        String HomeInfo = OkHttp.string(target, getHeaders());
        JSONObject JSON = new JSONObject(HomeInfo);
        JSONArray List = JSON.has("data") ? JSON.getJSONArray("data") : JSON.getJSONArray("list");
        for (int i = 0; i < List.length(); i++) {
            JSONObject item = List.getJSONObject(i);
            String typeid = item.has("type_id") ? item.getString("type_id") : item.getString("id");
            String typename = item.has("type_name") ? item.getString("type_name") : item.getString("name");
            classes.add(new Class(typeid, typename));
            JSONArray vlist = item.getJSONArray("vlist");
            for (int j = 0; j < vlist.length(); j++) {
                JSONObject vod = vlist.getJSONObject(j);
                String img = vod.getString("vod_pic");
                String name = vod.getString("vod_name");
                String remark = vod.getString("vod_remarks");
                String id = vod.getString("vod_id");
                list.add(new Vod(id, name, img, remark));
            }
        }
        return Result.string(classes, list);
    }


    public String categoryContent(String tid, String pg, boolean filter, HashMap<String, String> extend) throws JSONException {
        List<Vod> list = new ArrayList<>();
        String target = siteUrl + String.format("video?tid=%s&class=&area=&lang=&year=&limit=20&pg=%s", tid, pg);
        String CateInfo = OkHttp.string(target, getHeaders());
        JSONObject JSON = new JSONObject(CateInfo);
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


    public String detailContent(List<String> ids) throws JSONException {
        String DetaInfo = OkHttp.string(siteUrl.concat("video_detail?id=").concat(ids.get(0)), getHeaders());
        JSONObject JSON = new JSONObject(DetaInfo);
        JSONObject Data = JSON.getJSONObject("data");
        JSONObject data;
        if (Data.has("vod_info")) {
            data = Data.getJSONObject("vod_info");
        } else {
            data = Data;
        }
        String name = data.getString("vod_name");
        String remarks = data.getString("vod_remarks");
        String img = data.getString("vod_pic");
        String type = data.getString("vod_class");
        String actor = data.getString("vod_actor");
        String content = data.getString("vod_content");
        String director = data.getString("vod_director");


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
        JSONArray Player = data.getJSONArray("vod_url_with_player");
        for (int i = 0; i < Player.length(); i++) {
            JSONObject item = Player.getJSONObject(i);
            String Pname = item.getString("name");
            String Parse = item.getString("parse_api");
            String Purl = item.getString("url");
            if (Parse.startsWith("http") && isApiReachable(Parse)) {
                // 将 Purl 中的 $api_key 和 $api_value 替换成 $parsed_api_key 和 $parsed_api_value
                Purl = Purl.replaceAll("\\$(\\w+)", "\\$" + Parse + "$1");
            }
            sites.put(Pname, Purl);
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

