package com.zaozhuang.robot;

import android.content.Context;
import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import okhttp3.*;
import okio.Buffer;
import okio.BufferedSource;

import org.greenrobot.eventbus.android.BuildConfig;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class HttpClient {
    private static volatile HttpClient instance;
    private final OkHttpClient client;
    private final Gson gson = new Gson();
    private String baseUrl = "https://nanjian.onecity.ioclab.cn:8785/pro/api/";
    private String authToken;
    private TokenExpiredHandler tokenExpiredHandler;

    // 接口：Token过期处理器
    public interface TokenExpiredHandler {
        void onTokenExpired();
    }

    private HttpClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS);

        // 添加通用请求头拦截器
        builder.addInterceptor(chain -> {
            Request original = chain.request();
            Request.Builder requestBuilder = original.newBuilder();

            // 添加公共请求头
            Map<String, String> headers = getCommonHeaders();
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                requestBuilder.addHeader(entry.getKey(), entry.getValue());
            }

            // 添加Token
            if (authToken != null && !original.headers().names().contains("Authorization")) {
                requestBuilder.addHeader("Authorization", "Bearer " + authToken);
            }

            Request request = requestBuilder.build();
            return chain.proceed(request);
        });

        // 添加响应处理拦截器
        builder.addInterceptor(chain -> {
            Response response = chain.proceed(chain.request());
            return handleResponse(response);
        });

        client = builder.build();
    }

    public static HttpClient getInstance() {
        if (instance == null) {
            synchronized (HttpClient.class) {
                if (instance == null) {
                    instance = new HttpClient();
                }
            }
        }
        return instance;
    }

    // 设置基础URL
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    // 设置Token
    public void setAuthToken(String token) {
        this.authToken = token;
    }

    // 设置Token过期处理器
    public void setTokenExpiredHandler(TokenExpiredHandler handler) {
        this.tokenExpiredHandler = handler;
    }

    // 获取公共请求头
    private Map<String, String> getCommonHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "application/json");
        headers.put("Content-Type", "application/json; charset=utf-8");
        return headers;
    }

    // 统一响应处理
    private Response handleResponse(Response response) throws IOException {
        if (!response.isSuccessful()) {
            parseErrorResponse(response);
        }

        // 处理业务状态码（根据具体业务协议修改）
        try {
            BufferedSource source = response.body().source();
            source.request(Long.MAX_VALUE);
            Buffer buffer = source.buffer();
            String jsonString = buffer.clone().readString(StandardCharsets.UTF_8);

            JSONObject json = new JSONObject(jsonString);
            int code = json.optInt("code", -1);
            String message = json.optString("message", "");

            if (code != 0) { // 假设0表示成功
                throw new ApiException(code, message);
            }
        } catch (JSONException e) {
            throw new DataParseException("Data parsing error");
        }

        return response;
    }

    // 解析错误响应
    private void parseErrorResponse(Response response) throws IOException {
        switch (response.code()) {
            case 401:
                if (tokenExpiredHandler != null) {
                    tokenExpiredHandler.onTokenExpired();
                }
                throw new AuthException("Authentication expired");
            case 403:
                throw new AuthException("Forbidden");
            case 404:
                throw new ResourceNotFoundException("Resource not found");
            case 500:
                throw new ServerException("Internal server error");
            default:
                throw new HttpException("HTTP error: " + response.code());
        }
    }

    // GET请求（异步）
    public <T> void get(String url, Map<String, String> params, Class<T> clazz,
                        HttpCallback<T> callback) {
        Request request = buildGetRequest(url, params);
        Log.e("potter","222");
        doRequest(request, clazz, callback);
    }

    // POST请求（异步）
    public <T> void post(String url, Object body, Class<T> clazz,
                         HttpCallback<T> callback) {
        Request request = buildPostRequest(url, body);
        doRequest(request, clazz, callback);
    }

    // 构建GET请求
    private Request buildGetRequest(String url, Map<String, String> params) {
        HttpUrl.Builder httpBuilder = HttpUrl.parse(baseUrl + url).newBuilder();
        if (params != null) {
            for (Map.Entry<String, String> param : params.entrySet()) {
                httpBuilder.addQueryParameter(param.getKey(), param.getValue());
            }
        }
        return new Request.Builder()
                .url(httpBuilder.build())
                .get()
                .build();
    }

    // 构建POST请求
    private Request buildPostRequest(String url, Object body) {
        RequestBody requestBody = RequestBody.create(
                MediaType.parse("application/json"),
                gson.toJson(body)
        );
        return new Request.Builder()
                .url(baseUrl + url)
                .post(requestBody)
                .build();
    }

    // 执行请求
    private <T> void doRequest(Request request, Class<T> clazz, HttpCallback<T> callback) {
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String responseBody = response.body().string();
                    Log.e("potter responseBody",responseBody);
                    T result = gson.fromJson(responseBody, clazz);
                    callback.onSuccess(result);
                } catch (JsonSyntaxException e) {
                    Log.e("potter","JSON parse error");
                    callback.onFailure(new DataParseException("JSON parse error"));
                } finally {
                    if (response.body() != null) {
                        response.body().close();
                    }
                }
            }
        });
    }

    // 自定义回调接口
    public interface HttpCallback<T> {
        void onSuccess(T response);
        void onFailure(Throwable throwable);
    }

    // 自定义异常类
    public static class ApiException extends IOException {
        private final int code;

        public ApiException(int code, String message) {
            super(message);
            this.code = code;
        }

        public int getCode() { return code; }
    }

    public static class AuthException extends IOException {
        public AuthException(String message) { super(message); }
    }

    public static class ServerException extends IOException {
        public ServerException(String message) { super(message); }
    }

    public static class DataParseException extends IOException {
        public DataParseException(String message) { super(message); }
    }

    public static class HttpException extends IOException {
        public HttpException(String message) { super(message); }
    }

    public static class ResourceNotFoundException extends IOException {
        public ResourceNotFoundException(String message) { super(message); }
    }
}
