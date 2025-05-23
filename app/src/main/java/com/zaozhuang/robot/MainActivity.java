package com.zaozhuang.robot;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
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

public class MainActivity extends AppCompatActivity {
    private IMyAidlInterface iMyAidlInterface;
    private ServiceCallBack serviceCallBack;
    private MyServiceConnection myServiceConnection;
    //    private Handler handler = new Handler();
    private String[] HXAnwar = new String[]{
            "在呢，你的聊天小伙伴已到位",
            "嘿嘿，我是大白,冬瓜、西瓜、哈密瓜，你是大白的小傻瓜 ",
            "你先说什么事，我在决定在不在",
            "在呢，只要是你，我随时都在哦",
            "在呢，我就是大白，大是大白的大，白是大白的白"
    };
    // 初始化对话数据
    String[] personArr = {
            "你好！",
            "今天天气怎么样？",
            "能推荐下岗位吗？",
            "谢谢你的帮助",
            "再见！",
    };

    String[] robotArr = {
            "您好！很高兴为您服务",
            "今天晴转多云，气温25-30℃",
            "适合您的岗位如下",
            "不客气，随时为您服务",
            "再见！祝您有美好的一天",
    };
    List<RobotMsgItem> robotMsgList = new ArrayList<>();

    private ChatAdapter adapter;
    private int currentStep = 0;
    private final Handler handler = new Handler(Looper.getMainLooper());
    RecyclerView recyclerView;
    private TextView tvTime, tvDate;
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
        //setContentView(R.layout.activity_main);
        setContentView(R.layout.activity_chat);
        bindService();
        initView();
        startConversation();
        updateTime();
        startRealtimeUpdates();
        setTalkingState(IDLE);
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
        mTalkingStateText = (TextView) findViewById(R.id.talking_stata_text);
        mWaveAnim = (LottieAnimationView) findViewById(R.id.wave_anim);
        robotMsgList = new ArrayList<>();
        for (String msg : robotArr) {
            RobotMsgItem item = new RobotMsgItem(msg);
            if (msg.contains("适合您的岗位如下")) {
                item.type = 1;
            }
            robotMsgList.add(item);
        }
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

    private void startConversation() {
        handler.postDelayed(() -> {
            if (currentStep < personArr.length) {
                simulateUserInput(currentStep);
            }
        }, 1000);
    }

    private void simulateUserInput(int step) {
        setTalkingState(IDLE);
        handler.postDelayed(() -> {
            // 添加用户消息
            ChatMessage userMessage = ChatMessage.createUserMessage(personArr[step]);
            adapter.messages.add(userMessage);
            int position = adapter.messages.size() - 1;
            adapter.notifyItemInserted(position);
            setTalkingState(MEN_TALKING);
            // 逐个字符显示
            new Thread(() -> {
                String text = userMessage.getContent();
                Log.e("potteraaa", text);
                for (int i = 0; i < text.length(); i++) {
                    final int finalI = i;
                    handler.post(() -> {
                        adapter.updateMessage(position, String.valueOf(text.charAt(finalI)));
                        ((LinearLayoutManager) recyclerView.getLayoutManager())
                                .scrollToPosition(position);
                    });
                    try {
                        Thread.sleep(150);
                    } catch (InterruptedException e) {
                    }
                }
                handler.post(() -> {
                    userMessage.setCompleted(true);
                    simulateBotResponse(step);
                });
            }).start();
        }, 2000);

    }

    private void simulateBotResponse(int step) {
        setTalkingState(ROBOT_THINKING);
        handler.postDelayed(() -> {
                    // 添加机器人消息
                    ChatMessage botMessage;
                    if (robotMsgList.get(step).type == 0) {
                        botMessage = ChatMessage.createBotTextMessage(robotMsgList.get(step).content);
                    } else if (robotMsgList.get(step).type == 1) {
                        List<Job> jobs = new ArrayList<Job>() {{
                            add(new Job(
                                    "Java开发工程师",
                                    "字节跳动",
                                    "北京",
                                    "25-40K·16薪",
                                    true,
                                    true,
                                    true
                            ));

                            add(new Job(
                                    "Android开发专家",
                                    "腾讯科技",
                                    "深圳",
                                    "30-50K·14薪",
                                    true,
                                    false,
                                    true
                            ));

                            add(new Job(
                                    "大数据平台开发",
                                    "阿里巴巴集团",
                                    "杭州",
                                    "20-35K·股票期权",
                                    false,
                                    true,
                                    true
                            ));

                            add(new Job(
                                    "移动端架构师",
                                    "美团平台",
                                    "上海",
                                    "40-60K·技术分红"
                            ));

                            add(new Job(
                                    "跨平台开发工程师",
                                    "快手科技",
                                    "广州",
                                    "18-30K·弹性工作"
                            ));
                        }};
                        botMessage = ChatMessage.createBotJobMessage(robotMsgList.get(step).content, jobs);
                    } else {
                        botMessage = ChatMessage.createUserMessage(robotMsgList.get(step).content);
                    }
                    adapter.messages.add(botMessage);
                    int position = adapter.messages.size() - 1;
                    adapter.notifyItemInserted(position);
                    setTalkingState(ROBOT_ANSWERING);
                    // 逐个字符显示
                    new Thread(() -> {
                        String text = botMessage.getContent();
                        for (int i = 0; i < text.length(); i++) {
                            final int finalI = i;
                            handler.post(() -> {
                                adapter.updateMessage(position, String.valueOf(text.charAt(finalI)));
                                ((LinearLayoutManager) recyclerView.getLayoutManager())
                                        .scrollToPosition(position);
                            });
                            try {
                                Thread.sleep(150);
                            } catch (InterruptedException e) {
                            }
                        }
                        handler.post(() -> {
                            botMessage.setCompleted(true);
                            currentStep++;
                            if (currentStep < personArr.length) {
                                handler.postDelayed(() -> simulateUserInput(currentStep), 1000);
                            } else {
                                setTalkingState(IDLE);
                            }
                        });
                    }).start();
                }, 1000
        );

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
                    if (!tag.equals("1")) {
//                        text.append("tag = "+ tag+"  message="+message+"\n");
                        Log.d("TAG", "callback: " + "tag=" + tag + "  message=" + message);
                        if (tag.equals("4")) {
//                            sendMessageToRobot("starttts","唤醒了啊少时诵诗书所");
                            if (message.startsWith("wake up")) {
                                final Random random = new Random();
                                final String s = HXAnwar[random.nextInt(5)];
                                if (iMyAidlInterface != null) {
                                    try {
                                        Log.i("TAG", "run: 发送消息 wake up");
                                        iMyAidlInterface.sendMessage("starttts", s);
                                    } catch (RemoteException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                    } else {
                        Log.d("TAG", "run: " + "tag=" + tag + "  message=" + message + "\n");
                        //语音识别的音量反馈
                    }
                }
            });
        }
    }
}