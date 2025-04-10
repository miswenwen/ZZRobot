package com.zaozhuang.robot.view;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.text.style.BackgroundColorSpan;
import android.text.style.ImageSpan;

import androidx.annotation.NonNull;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.style.ReplacementSpan;

public class OffsetBackgroundSpan extends ReplacementSpan {
    private final int backgroundColor;
    private final float offsetX;
    private final float offsetY;
    private final float textSize;
    public OffsetBackgroundSpan(int backgroundColor, float offsetX, float offsetY,float textSize) {
        this.backgroundColor = backgroundColor;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.textSize = textSize;
    }

    @Override
    public int getSize(Paint paint, CharSequence text, int start, int end, Paint.FontMetricsInt fm) {
        // 返回文本的测量宽度
        return (int) paint.measureText(text, start, end);
    }

    @Override
    public void draw(Canvas canvas, CharSequence text, int start, int end,
                     float x, int top, int y, int bottom, Paint paint) {
        // 保存原始颜色
        int originalColor = paint.getColor();

        // 绘制背景
        paint.setColor(backgroundColor);
        float textWidth = paint.measureText(text, start, end);
        RectF bgRect = new RectF(
                x + offsetX,          // left
                top + offsetY,        // top
                x + textWidth + offsetX,  // right
                (float) (bottom -textSize*0.9 + offsetY)      // bottom
        );
        canvas.drawRect(bgRect, paint);

        // 恢复颜色并绘制文本
        paint.setColor(originalColor);
        canvas.drawText(text, start, end, x, y, paint);
    }
}
