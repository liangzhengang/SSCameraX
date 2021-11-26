package com.compose.senssunai;

import android.Manifest;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.view.PreviewView;

import com.permissionx.guolindev.PermissionX;
import com.permissionx.guolindev.callback.ExplainReasonCallback;
import com.permissionx.guolindev.request.ExplainScope;
import com.senssun.camera.CameraBuilder;
import com.senssun.camera.SSCameraX;
import com.senssun.camera.TakeCallback;
import com.trechina.freshgoodsdistinguishsdk.FreshGoodsManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.List;

import me.jessyan.autosize.AutoSizeCompat;

public class MainActivity extends AppCompatActivity {
    FreshGoodsManager freshGoodsManager;
    ImageView img;
    private static final String TAG = "MainActivity";
    String imageData;
    Bitmap bitmap;

    StringBuffer sid;
    CameraBuilder cameraBuilder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button btnLearn = findViewById(R.id.btn1);
        Button btnInit = findViewById(R.id.btn2);
        Button btnUInit = findViewById(R.id.btn3);
        Button btnPhoto = findViewById(R.id.btn4);
        EditText etID = findViewById(R.id.et_id);
        EditText etName = findViewById(R.id.et_content);
        PreviewView previewView = findViewById(R.id.view_finder);
        img = findViewById(R.id.img);
        requestPermission();
        freshGoodsManager = new FreshGoodsManager(MainActivity.this);
        btnLearn.setOnClickListener(v -> {
            runWork(() -> {
                int ret = freshGoodsManager.RetailBot_Api_SelectAndUpload(sid.toString(), etID.getText().toString(), "10", "false", etName.getText().toString(), bitmap);
                Log.i(TAG, "RetailBot_Api_SelectAndUpload ret:" + ret);
            });
        });
        btnInit.setOnClickListener(v -> initAI());
        btnUInit.setOnClickListener(v -> unInit());
        // 拍摄
        btnPhoto.setOnClickListener(v ->
                takePhoto()
        );
        previewView.post(new Runnable() {
            @Override
            public void run() {
                cameraBuilder = SSCameraX.with(null, this).setTargetRotation(Surface.ROTATION_90).setTargetAspectRatio(AspectRatio.RATIO_16_9).bind();
                cameraBuilder.setPreview(previewView);
            }
        });

    }


    private void requestPermission() {
        PermissionX.init(this).permissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_PHONE_STATE, Manifest.permission.CAMERA).onExplainRequestReason(new ExplainReasonCallback() {
            @Override
            public void onExplainReason(@NonNull ExplainScope scope, @NonNull List<String> deniedList) {

            }
        }).request((allGranted, grantedList, deniedList) -> {
            if (allGranted) {

            }
        });
    }


    void takePhoto() {
        cameraBuilder.callback(new TakeCallback() {
            @Override
            public void onSuccess(@NonNull Bitmap bit, @NonNull ImageProxy image) {
                runOnUiThread(() -> {
                    img.setImageBitmap(bit);
                    bitmap = bit;
                    runWork(() -> recognition());
                    image.close();
                });

            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {

            }
        }).start();
    }

    // 解绑
    private void unInit() {
        freshGoodsManager.RetailBot_Api_ModelUnInit();
    }

    // 绑定
    private void initAI() {
        String companyID = "t2yW+tjkQ/IKK2U1tZVidw==";
        JSONObject keyInfo = new JSONObject();
        try {
            keyInfo.put("appKey", "77ffea8c6b5c4c3795fc1c0f221e84c0");
            keyInfo.put("secretKey", "c23e18931f0548ea9f73a350b0c2914a");
            keyInfo.put("serviceKey", "4c495ca26fb2efb21e3b90edf299e7ed");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String path = getCacheDir() + "/cache";
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }
        Log.i(TAG, keyInfo.toString());
        freshGoodsManager.RetailBot_Api_ModelInit(companyID, keyInfo.toString(), path);
    }


    void recognition() {
        if (bitmap != null) {
            sid = new StringBuffer();
            StringBuffer result = new StringBuffer(1024);
            Log.i(TAG, "recognition: start ");
            int ret = freshGoodsManager.RetailBot_Api_Recognition(sid, result, bitmap);
            Log.i(TAG, "recognition result :" + result.toString());
            Log.i(TAG, " recognition sid:" + sid.toString());
            Log.i(TAG, " recognition ret:" + ret);

        }
    }


    void getImage() {
        try {
            JSONObject jo = new JSONObject();
            jo.put("method", "GetImage");
            StringBuffer strBuf = new StringBuffer(1024 * 300);
            int ret = freshGoodsManager.RetailBot_Api_Exec(jo.toString(), strBuf, 13000);
            if (ret == 0) {
                JSONObject joRoot = new JSONObject(strBuf.toString());
                String md5 = joRoot.getString("ImageMD5");
                imageData = joRoot.getString("Image");
            }
        } catch (Exception e) {
        }
    }


    @Override
    public Resources getResources() {
        //需要升级到 v1.1.2 及以上版本才能使用 AutoSizeCompat
//        AutoSizeCompat.autoConvertDensityOfGlobal(super.getResources()); //如果没有自定义需求用这个方法
        AutoSizeCompat.autoConvertDensity(super.getResources(), 1024, true); //如果有自定义需求就用这个方法
        return super.getResources();
    }

    void runWork(Runnable runnable) {
        new Thread(runnable).start();
    }
}