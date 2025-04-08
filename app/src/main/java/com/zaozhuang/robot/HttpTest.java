package com.zaozhuang.robot;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;
import java.util.Map;

public class HttpTest extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
                Log.e("potter",response.getRows().get(0).getImageName());
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
