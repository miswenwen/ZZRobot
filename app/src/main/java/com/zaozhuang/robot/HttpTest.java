package com.zaozhuang.robot;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;
import java.util.Map;

public class HttpTest extends AppCompatActivity {
    Button mButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mButton = (Button) findViewById(R.id.btn_open_meeting);
        mButton.setOnClickListener(new View.OnClickListener() {
            /*
            以
            点击链接入会，或添加至会议列表：
            https://meeting.tencent.com/dm/qJT0Xnya9c5I
            #腾讯会议：304-220-918
            为例。
            提供两种方式：
            1.https形式叫起网页，通过网页打开腾讯会议
            2.wemeet scheme形式，直接唤起腾讯会议(网上找的，官网并没有找到)
             */
            @Override
            public void onClick(View view) {
                String meetingId = "304-220-918";
                meetingId = "304220918";//会议号要移除-
                String url = "https://meeting.tencent.com/dm/qJT0Xnya9c5I";
                String schemeUrl = "wemeet://page/inmeeting?meeting_code=" + meetingId;
                Intent intent = new Intent(Intent.ACTION_VIEW);
                if (false) {
                    intent.setData(Uri.parse(url)); // 或 Uri.parse(schemeUrl)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    try {
                        startActivity(intent);
                    } catch (ActivityNotFoundException e) {
                    }
                } else {
                    intent.setData(Uri.parse(schemeUrl)); // 或 Uri.parse(schemeUrl)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    try {
                        startActivity(intent);
                    } catch (ActivityNotFoundException e) {
                    }
                }
            }
        });
        // 初始化配置
        HttpClient client = HttpClient.getInstance();
        client.setAuthToken("your_token");
        client.setTokenExpiredHandler(() -> {
            // 处理Token过期，跳转到登录页面
        });
        Map<String, String> params = new HashMap<>();
        params.put("imageStatus", "0");
        params.put("imageType", "1");
        params.put("tabId", "4");
        //app/ui/image/list?imageStatus=0&imageType=1&tabId=4
        // 发起GET请求
        client.get("app/ui/image/list", params, TestAAA.class, new HttpClient.HttpCallback<TestAAA>() {
            @Override
            public void onSuccess(TestAAA response) {
                // 处理成功结果
                Log.e("potter", response.getRows().get(0).getImageName());
            }

            @Override
            public void onFailure(Throwable throwable) {
                if (throwable instanceof HttpClient.AuthException) {
                    // 处理认证错误
                } else if (throwable instanceof HttpClient.ApiException) {
                    // 处理业务错误
                }
                // 其他错误处理
            }
        });
        // 发起POST请求
//        User user = new User("name", "email");
//        client.post("user/create", user, CreateResponse.class, new HttpCallback<CreateResponse>() {
//            @Override
//            public void onSuccess(CreateResponse response) {
//                // 处理创建成功
//            }
//
//            @Override
//            public void onFailure(Throwable throwable) {
//                // 处理错误
//            }
//        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
