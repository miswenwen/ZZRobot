package com.zaozhuang.robot;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.helang.lib.IMyAidlCallBackInterface;
import com.helang.lib.IMyAidlInterface;

import java.util.Random;

public class MainActivity extends AppCompatActivity {
    private IMyAidlInterface iMyAidlInterface;
    private ServiceCallBack serviceCallBack;
    private MyServiceConnection myServiceConnection;
    private Handler handler = new Handler();
    private String[] HXAnwar = new String[]{
            "在呢，你的聊天小伙伴已到位",
            "嘿嘿，我是大白,冬瓜、西瓜、哈密瓜，你是大白的小傻瓜 ",
            "你先说什么事，我在决定在不在",
            "在呢，只要是你，我随时都在哦",
            "在呢，我就是大白，大是大白的大，白是大白的白"
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        /**
         * 只需要初始化一次
         *
         */
        bindService();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService();
    }
    private void bindService(){
        myServiceConnection = new MyServiceConnection();
        serviceCallBack = new ServiceCallBack();
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.aobo.robot.ai3",
                "com.aobo.aibot.aidl.MyService"));
        startService(intent);
        bindService(intent,myServiceConnection,BIND_AUTO_CREATE);

    }
    private void unbindService(){
        if (myServiceConnection != null){
            try {
                iMyAidlInterface.unregisterListener(serviceCallBack);
            } catch (RemoteException e) {
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
                    if (iMyAidlInterface != null){
                        try {
                            iMyAidlInterface.registerListener(serviceCallBack);
                        } catch (RemoteException e) {
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
    class ServiceCallBack extends IMyAidlCallBackInterface.Stub{

        @Override
        public void callback(final String tag, final String message) throws RemoteException {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (!tag.equals("1")){
//                        text.append("tag = "+ tag+"  message="+message+"\n");
                        Log.d("TAG", "callback: "+"tag="+tag+"  message="+message);
                        if(tag.equals("4")){
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
                    }else{
                        Log.d("TAG", "run: "+"tag="+tag+"  message="+message+"\n");
                        //语音识别的音量反馈
                    }
                }
            });
        }
    }
}