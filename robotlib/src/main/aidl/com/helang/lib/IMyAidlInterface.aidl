// IMyAidlInterface.aidl
package com.helang.lib;

import com.helang.lib.IMyAidlCallBackInterface;

// Declare any non-default types here with import statements

interface IMyAidlInterface {
    //发送消息
    void sendMessage(String tag,String message);

//    //接受消息
//     void receiveMessage(int tag,int type);
//
//     //接受消息
//     void receiveMessage(int tag,String message);

    //注册回调接口
    void registerListener(in IMyAidlCallBackInterface listener);
    //取消回调接口
    void unregisterListener(in IMyAidlCallBackInterface listener);

}
