package com.zaozhuang.robot.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ListView;

public class NestedListView extends ListView {

    public NestedListView(Context context) {
        super(context);
    }

    public NestedListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        // 请求父容器在滑动时不拦截事件
        requestDisallowInterceptTouchEvent(true);
        return super.onTouchEvent(ev);
    }
}
