package com.github.catvod.debug;

import android.app.Activity;
import android.os.*;

import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.github.catvod.R;
import com.github.catvod.net.OkHttp;
import com.github.catvod.spider.*;
import org.apache.commons.lang3.StringEscapeUtils;
import org.json.JSONException;

import static com.github.catvod.utils.Util.addView;
import static com.github.catvod.utils.Util.removeView;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Logger.addLogAdapter(new AndroidLogAdapter());
        Init.init(getApplicationContext());
        // It is usually init in the application.
        new Thread(() -> {

            MaoLv z = new MaoLv();

            try {
                z.init(MainActivity.this, "http://maolvys.com");//https://dm84.tv
            } catch (Exception e) {
                throw new RuntimeException(e);
            }


//                String json = z.homeContent(true);
//               System.out.println(json);



            //      HashMap<String, String> map = new HashMap<>();
            //          String s = null;
            //         try {
            //            s = z.categoryContent("2", "1", true, map);
            //       } catch (JSONException e) {
            //            e.printStackTrace();
            //        }
            //        System.out.println(s);



//                    HashMap<String, String> map = new HashMap<>();
            // map.put("area", "中国香港");
 //                    String s = z.categoryContent("2", "1",  true, map);
 //                 System.out.println(s);



            //          ArrayList<String> ids = new ArrayList<>();
            //         ids.add("4358");//4070
            //    String s = null;
            //       try {
            //          s = z.detailContent(ids);
            //       } catch (JSONException e) {
            //           e.printStackTrace();
            //       }
            //      System.out.println(s);


 //                ArrayList<String> ids = new ArrayList<>();
 //                 ids.add("150898");//4070
  //                String s = z.detailContent(ids);
  //                 System.out.println(s);



 //                  String s = null;
 //                   try {
 //                        s = z.searchContent("我的", true);
  //                   } catch (JSONException e) {
  //                      e.printStackTrace();
  //                 }
 //                    System.out.println(s);


//                String s = z.searchContent("我的", true);
 //               System.out.println(s);


            //   String s = z.playerContent("", "http://42.157.128.109:2323/CH/app/app.php?url\u003dcaihong-3c0b6e19221a9166", null);
            //  System.out.println(s);



        }).start();
    }
}