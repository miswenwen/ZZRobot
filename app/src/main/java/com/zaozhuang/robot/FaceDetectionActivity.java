package com.zaozhuang.robot;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.media.Image;
import android.os.Bundle;
import android.Manifest;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.media.FaceDetector;

public class FaceDetectionActivity extends AppCompatActivity {
    private static final int CAMERA_PERMISSION_REQUEST = 100;
    private PreviewView previewView;
    private TextView tvWelcome;
    private ExecutorService cameraExecutor;

    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        previewView = findViewById(R.id.previewView);
        tvWelcome = findViewById(R.id.tvWelcome);
        imageView = (ImageView) findViewById(R.id.previewImg);
        cameraExecutor = Executors.newSingleThreadExecutor();

        // 请求摄像头权限
        if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        }
    }

    private long lastProcessTime = 0;
    private final long MIN_INTERVAL_MS = 500; // 例如：500ms 处理一次

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                //设置为COMPATIBLE和setTargetRotation(previewView.getDisplay().getRotation())，fix 机器人预览图像方向不对的问题
                previewView.setImplementationMode(PreviewView.ImplementationMode.COMPATIBLE);
                // 配置预览
                Preview preview = new Preview.Builder().
                        setTargetRotation(previewView.getDisplay().getRotation()).
                        build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                // 配置图像分析
                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();
                imageAnalysis.setAnalyzer(cameraExecutor, new ImageAnalysis.Analyzer() {
                    @Override
                    public void analyze(@NonNull ImageProxy imageProxy) {

                        long currentTime = System.currentTimeMillis();
                        if (currentTime - lastProcessTime >= MIN_INTERVAL_MS) {
                            Log.e("potter", "111111");
                            lastProcessTime = currentTime;
                            processImage(imageProxy);
                        } else {
                            imageProxy.close(); // 直接丢弃未处理的帧
                        }
                    }
                });

                // 绑定生命周期
                cameraProvider.unbindAll();
                //LENS_FACING_BACK/LENS_FACING_FRONT/LENS_FACING_EXTERNAL
                //实测机器人的外接摄像头竟然是LENS_FACING_BACK，而不是LENS_FACING_EXTERNAL
                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                        .build();
                cameraProvider.bindToLifecycle(
                        this,
                        cameraSelector,//前后摄
                        preview,
                        imageAnalysis
                );
            } catch (ExecutionException | InterruptedException e) {
                Log.e("CameraX", "Error starting camera", e);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    public Bitmap convertToRGB565(Bitmap srcBitmap) {
        // 确保宽度为偶数（FaceDetector 强制要求）
        int width = srcBitmap.getWidth();
        if (width % 2 != 0) {
            width--; // 或 width = srcBitmap.getWidth() - 1;
        }

        // 创建目标 Bitmap
        Bitmap rgb565Bitmap = Bitmap.createBitmap(
                width,
                srcBitmap.getHeight(),
                Bitmap.Config.RGB_565
        );

        // 通过 Canvas 复制像素
        Canvas canvas = new Canvas(rgb565Bitmap);
        Rect srcRect = new Rect(0, 0, width, srcBitmap.getHeight());
        Rect dstRect = new Rect(0, 0, width, srcBitmap.getHeight());
        canvas.drawBitmap(srcBitmap, srcRect, dstRect, null);

        // 回收原图（可选）
        srcBitmap.recycle();

        return rgb565Bitmap;
    }

    private void processImage(ImageProxy imageProxy) {
        // 注意：imageProxy.toBitmap生成的Bitmap格式是ARGB_8888的，FaceDetector 仅支持 Bitmap.Config.RGB_565 格式。得转下。
        Bitmap bitmap = convertToRGB565(imageProxyToBitmap(imageProxy));
        Log.e("potter Bitmap Config", bitmap.getConfig().toString());
        if (bitmap == null) {
            imageProxy.close();
            return;
        }

        // 检测人脸
        FaceDetector faceDetector = new FaceDetector(bitmap.getWidth(), bitmap.getHeight(), 1);
        FaceDetector.Face[] faces = new FaceDetector.Face[1];
        int detectedCount = faceDetector.findFaces(bitmap, faces);
        Log.e("potteraaaa detectedCount", "" + detectedCount);

        // 更新 UI
        runOnUiThread(() -> {
            imageView.setImageBitmap(bitmap);
            if (detectedCount > 0) {
                tvWelcome.setVisibility(View.VISIBLE);
            } else {
                tvWelcome.setVisibility(View.GONE);
            }
            imageProxy.close();
        });
//        imageProxy.close();
    }

    // 将 ImageProxy（YUV）转换为 Bitmap（RGB）
    private Bitmap imageProxyToBitmap(ImageProxy imageProxy) {
        return imageProxy.toBitmap();
//        //实测手机上前摄toBitmap的图片是横着的，得旋转下
//        int rotationDegrees = imageProxy.getImageInfo().getRotationDegrees();
//        Bitmap bitmap = imageProxy.toBitmap();
//        Matrix matrix = new Matrix();
//        matrix.postRotate(rotationDegrees);
//        // 创建一个新的Bitmap，其内容是旋转后的图像
//        Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
//        // 如果不再需要原始bitmap，可以回收它
//        bitmap.recycle();
//        return rotatedBitmap;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
    }
}
