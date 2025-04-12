package com.zaozhuang.robot;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.journeyapps.barcodescanner.CaptureActivity;

public class QrCodeActivity extends AppCompatActivity {
    Button qrCodeBtn ;
    Button identifyQrCodeBtn;
    TextView qrCodeInfo;
    TextView identifyQrCodeInfo;
    ImageView qrImg;
    int REQUEST_CODE_SCAN = 1000;
    int scanWay = 1;//1:IntentIntegrator 2:CaptureActivity 3:自定义逻辑，从视频流获取采集帧，传bitmap识别。最终项目用法
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_code);
        qrCodeBtn = (Button)findViewById(R.id.generate_qr_code_btn) ;
        identifyQrCodeBtn = (Button)findViewById(R.id.identify_qr_code_btn) ;

        qrCodeInfo = (TextView) findViewById(R.id.qr_code_info) ;
        identifyQrCodeInfo = (TextView) findViewById(R.id.qr_code_info2) ;

        qrImg = (ImageView) findViewById(R.id.qr_code_img) ;
        qrCodeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                generateQrCode();
            }
        });
        identifyQrCodeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(scanWay == 1){
                    IntentIntegrator integrator = new IntentIntegrator(QrCodeActivity.this);
                    integrator.setPrompt("扫描二维码");
                    integrator.setCameraId(1);  // 指定摄像头
                    integrator.setBarcodeImageEnabled(true); // 是否生成条码扫描的缩略图。
                    integrator.initiateScan();
                } else{

                Intent intent = new Intent(QrCodeActivity.this, CaptureActivity.class);
                intent.putExtra("SCAN_CAMERA_ID", 1);  // 0为后置摄像头，1为前置摄像头
                startActivityForResult(intent, REQUEST_CODE_SCAN);
                }


            }
        });


    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(scanWay == 1){
            IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
            if(result != null) {
                if(result.getContents() == null) {
                    identifyQrCodeInfo.setText("Cancelled");
                } else {
                    identifyQrCodeInfo.setText("识别的二维码结果为:" + result.getContents());
                }
            }
        }else{
            if (requestCode == REQUEST_CODE_SCAN && resultCode == RESULT_OK) {
                if (data != null) {
                    String content = data.getStringExtra("SCAN_RESULT");
                    identifyQrCodeInfo.setText("识别的二维码结果为："+content);
                    // 处理扫描结果
                }
            }
        }


    }
    private void generateQrCode(){
        // 参数：内容、尺寸、颜色等
        BitMatrix matrix = null;
        String content = "hello world,this is cmcc!";
        qrCodeInfo.setText(content);
        try {
            matrix = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, 400, 400);
        } catch (WriterException e) {
            throw new RuntimeException(e);
        }
        Bitmap bitmap = Bitmap.createBitmap(400, 400, Bitmap.Config.RGB_565);
        for (int i = 0; i < 400; i++) {
            for (int j = 0; j < 400; j++) {
                bitmap.setPixel(i, j, matrix.get(i, j) ? Color.BLACK : Color.WHITE);
            }
        }
        qrImg.setImageBitmap(bitmap);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
