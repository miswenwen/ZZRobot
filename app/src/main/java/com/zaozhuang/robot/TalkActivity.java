package com.zaozhuang.robot;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.helang.lib.IMyAidlCallBackInterface;
import com.helang.lib.IMyAidlInterface;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/*
语音通话逻辑：
onCreate里wakeup,然后startRecord开始接收。接收到后触发callback，stopRecord，callback的msg展示在recyclerview。
msg用websocket的sendMsg给到后台，后台返回response。response展示在recyclerview。然后startRecord。
以此循环。
问题点：
1.模拟打字效果如何设计。假设人0s的时候说话，3s的时候说完。 3s的时候callback才触发，这时候是直接显示还是模拟打字？其实已经是滞后了，真想模拟打字应该是
接收一个字就callback一个字。  看了下豆包，也是3s的时候触发callback。然后一瞬间展示所有人的文本。
这说明人这块不需要模拟打字。仅机器人回答的时候模拟打字即可。合理
 */
public class TalkActivity extends AppCompatActivity {
    private IMyAidlInterface iMyAidlInterface;
    private ServiceCallBack serviceCallBack;
    private MyServiceConnection myServiceConnection;
    //    private Handler handler = new Handler();
    private String welComeStr = "欢迎使用枣庄人社局智能机器人";
    // 初始化对话数据
    private ChatAdapter adapter;
    private int currentStep = 0;
    private final Handler handler = new Handler(Looper.getMainLooper());
    RecyclerView recyclerView;
    private TextView tvTime, tvDate;
    private TextView logText;
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    private int mTalkingState = 0;

    private static final int IDLE = 0;//闲置中，人和机器人都没说话
    private static final int MEN_TALKING = 1;//人在说话中
    private static final int ROBOT_THINKING = 2;//机器人请求大模型中
    private static final int ROBOT_ANSWERING = 3;//机器人回答中
    private TextView mTalkingStateText;
    private LottieAnimationView mWaveAnim;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        bindService();
        initView();
        updateTime();
        startRealtimeUpdates();
        setTalkingState(IDLE);
        wakeUpMic();
    }

    private void wakeUpMic() {
//        wake up
        Log.e("potter wakeup", iMyAidlInterface != null ? "true" : "false");
        //post runnable到消息队列尾部，因为不这样bindService还没结束
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.e("potter wakeup", iMyAidlInterface != null ? "true" : "false");
                if (iMyAidlInterface != null) {
                    try {
                        /**
                         *  direct 为波束方位 以4麦阵列为例，取值0,1,2, 0为麦克风的第一与第二麦头之间波速,1为中间波速，2为三和四麦头的波速
                         */
                        String direct = "0";
                        iMyAidlInterface.sendMessage("wakeup", direct);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, 2000);
    }

    private void startRecord() {
        setTalkingState(MEN_TALKING);
        if (iMyAidlInterface != null) {
            try {
                iMyAidlInterface.sendMessage("startrecord", "");
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    private void stopRecord() {
        if (iMyAidlInterface != null) {
            try {
                iMyAidlInterface.sendMessage("stoprecord", "");
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    private void initView() {
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ChatAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);
        //很重要，解决模拟打字的闪烁问题
        recyclerView.setItemAnimator(null);
        tvTime = findViewById(R.id.tv_time);
        tvDate = findViewById(R.id.tv_date);
        logText = (TextView) findViewById(R.id.logs);
        logText.setMovementMethod(ScrollingMovementMethod.getInstance());
        mTalkingStateText = (TextView) findViewById(R.id.talking_stata_text);
        mWaveAnim = (LottieAnimationView) findViewById(R.id.wave_anim);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService();
        handler.removeCallbacksAndMessages(null); // 防止内存泄漏
    }

    // 更新时间显示
    private void updateTime() {
        Date now = new Date();
        tvTime.setText(timeFormat.format(now));
        tvDate.setText(dateFormat.format(now));
    }

    private void setTalkingState(int state) {
        mTalkingState = state; // 更新当前状态
        switch (state) {
            case IDLE:
                mTalkingStateText.setText("请说话");
                mWaveAnim.setVisibility(View.INVISIBLE);
                break;
            case MEN_TALKING:
                mTalkingStateText.setText("语音接收中...");
                mWaveAnim.setVisibility(View.VISIBLE);
//                mWaveAnim.setProgress(0f);    // 重置到起点
//                mWaveAnim.playAnimation();    // 重新播放
                break;
            case ROBOT_THINKING:
                mTalkingStateText.setText("大模型思考中...");
                mWaveAnim.setVisibility(View.VISIBLE);
//                mWaveAnim.setProgress(0f);    // 重置到起点
//                mWaveAnim.playAnimation();    // 重新播放
                break;
            case ROBOT_ANSWERING:
                mTalkingStateText.setText("机器人回答中...");
                mWaveAnim.setVisibility(View.VISIBLE);
//                mWaveAnim.setProgress(0f);    // 重置到起点
//                mWaveAnim.playAnimation();    // 重新播放
                break;
            default:
        }
    }

    // 启动定时更新
    private void startRealtimeUpdates() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                updateTime();
                handler.postDelayed(this, 1000); // 每1秒更新一次
            }
        }, 1000);
    }

    private void bindService() {
        myServiceConnection = new MyServiceConnection();
        serviceCallBack = new ServiceCallBack();
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.aobo.robot.ai3",
                "com.aobo.aibot.aidl.MyService"));
        startService(intent);
        bindService(intent, myServiceConnection, BIND_AUTO_CREATE);
    }

    private void unbindService() {
        if (myServiceConnection != null) {
            try {
                iMyAidlInterface.unregisterListener(serviceCallBack);
            } catch (Exception e) {
                e.printStackTrace();
            }
            unbindService(myServiceConnection);
        }
    }

    class MyServiceConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            iMyAidlInterface = IMyAidlInterface.Stub.asInterface(iBinder);

            handler.post(new Runnable() {
                @Override
                public void run() {
                    //注册回调
                    if (iMyAidlInterface != null) {
                        try {
                            iMyAidlInterface.registerListener(serviceCallBack);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    }

    /**
     * service回调client的类
     * tag
     * 为1是返回识别音量
     * 为2是返回识别的内容
     * 为3是合成消息
     * 为4是唤醒消息
     * 为front_ultrasound 是前超声波消息
     * 为back_ultrasound 为后超声波消息
     * 为ultrasound_distance 为超声波距离
     */
    class ServiceCallBack extends IMyAidlCallBackInterface.Stub {

        @Override
        public void callback(final String tag, final String message) throws RemoteException {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (!tag.equals("1") && logText != null) {
                        logText.append("tag = " + tag + "  message=" + message + "\n");
                    }
                    //唤醒时说欢迎(人脸识别后迎宾)
                    if (tag.equals("4")) {
                        if (message.startsWith("wake up")) {
                            startTTs(welComeStr);
                        }
                    }
                    //语音播报结束-->开启录制新的一轮人的说话，语音识别
                    if (tag.equals("3") && message.equals("ttsover")) {
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                setTalkingState(MEN_TALKING);
                                startRecord();
                            }
                        }, 50);
//                        startRecord();
                    }
                    //语音识别过程，startRecord,callback第一次(tag:2,message:"startrecord")-->
                    // 人说话-->callback第二次(tag:2,message:msg)-->
                    // 底层自己stoprecord-->callback第三次((tag:2,message:"stoprecord")
                    if (tag.equals("2")) {
                        if (message.equals("stoprecord") || message.equals("startrecord")) {
                            return;
                        }
                        setTalkingState(ROBOT_THINKING);
                        String menInput = message;
                        // 添加用户消息
                        ChatMessage userMessage = ChatMessage.createUserMessage(menInput);
                        adapter.messages.add(userMessage);
                        int position = adapter.messages.size() - 1;
                        adapter.notifyItemInserted(position);
                        adapter.updateMessage(position, String.valueOf(userMessage.getContent()));
                        ((LinearLayoutManager) recyclerView.getLayoutManager())
                                .scrollToPosition(position);
                        //websocket.sendMsg(menInput);
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                onReceiveWebsocketMsg("你好，我是机器人小枣");
                            }
                        },2000);

                    }
                }
            });
        }
    }

    //websocket onMessage，用接口实现，或者广播转发都可以
    private void onReceiveWebsocketMsg(String str) {
        String robotResponse = str;
        setTalkingState(ROBOT_ANSWERING);
        ChatMessage botMessage = ChatMessage.createBotTextMessage(robotResponse);
        adapter.messages.add(botMessage);
        int position = adapter.messages.size() - 1;
        adapter.notifyItemInserted(position);
        adapter.updateMessage(position, String.valueOf(robotResponse));
        ((LinearLayoutManager) recyclerView.getLayoutManager())
                .scrollToPosition(position);
        startTTs(str);
    }

    private void startTTs(String str) {
//        语音合成，机器人说话
//        startTTs
        if (iMyAidlInterface != null) {
            try {
                iMyAidlInterface.sendMessage("starttts", str);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }
}