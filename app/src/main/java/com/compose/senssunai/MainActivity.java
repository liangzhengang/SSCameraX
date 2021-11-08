package com.compose.senssunai;

import android.Manifest;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.lifecycle.ProcessCameraProvider;

import com.google.common.util.concurrent.ListenableFuture;
import com.permissionx.guolindev.PermissionX;
import com.permissionx.guolindev.callback.ExplainReasonCallback;
import com.permissionx.guolindev.request.ExplainScope;
import com.qw.photo.CoCo;
import com.qw.photo.callback.CoCoCallBack;
import com.qw.photo.callback.TakeCallBack;
import com.qw.photo.constant.Face;
import com.qw.photo.pojo.TakeResult;
import com.senssun.camera.SSCameraX;
import com.senssun.camera.TakeCallback;
import com.trechina.freshgoodsdistinguishsdk.FreshGoodsManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import me.jessyan.autosize.AutoSizeCompat;

public class MainActivity extends AppCompatActivity {
    FreshGoodsManager freshGoodsManager;
    ImageView img;
    ImageCapture imageCapture;
    YuvToRgbConverter converter;
    Executor cameraExecutor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        converter = new YuvToRgbConverter(this);
        imageCapture = new ImageCapture.Builder().setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY).build();
        ListenableFuture<ProcessCameraProvider> providerListenableFuture = ProcessCameraProvider.getInstance(this);
        ProcessCameraProvider processCameraProvider = null;
        try {
            processCameraProvider = providerListenableFuture.get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        processCameraProvider.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA, imageCapture);

        setContentView(R.layout.activity_main);
        Button btnLearn = findViewById(R.id.btn1);
        Button btnInit = findViewById(R.id.btn2);
        Button btnUInit = findViewById(R.id.btn3);
        Button btnPhoto = findViewById(R.id.btn4);
        EditText etID = findViewById(R.id.et_id);
        EditText etName = findViewById(R.id.et_content);
        img = findViewById(R.id.img);

        requestPermission();

        freshGoodsManager = new FreshGoodsManager(MainActivity.this);
        btnLearn.setOnClickListener(v -> {
            CoCo.with(MainActivity.this).take(createFile()).cameraFace(Face.FRONT).callBack(new TakeCallBack() {
                @Override
                public void onFinish(TakeResult takeResult) {

                }

                @Override
                public void onCancel() {

                }

                @Override
                public void onStart() {

                }
            }).start(new CoCoCallBack<TakeResult>() {
                @Override
                public void onSuccess(TakeResult takeResult) {

                    if (sid == null)
                        return;
                    runWork(new Runnable() {
                        @Override
                        public void run() {

                            int ret = freshGoodsManager.RetailBot_Api_SelectAndUpload(sid.toString(), etID.getText().toString(), "10", "false", etName.getText().toString(), bitmap);
                            Log.i(TAG, "RetailBot_Api_SelectAndUpload ret:" + ret);
                        }
                    });
                }

                @Override
                public void onFailed(Exception e) {

                }
            });

        });
        btnInit.setOnClickListener(v -> initAI());
        btnUInit.setOnClickListener(v -> unInit());


        // 拍摄
//        androidx.camera.view.CameraView mVideoView;
        btnPhoto.setOnClickListener(v ->
//                // 修复了前置摄像头拍照后  预览左右镜像的问题
//                Integer lensFacing = mVideoView.getCameraLensFacing();
//         if (lensFacing == null) {
//            lensFacing = CameraSelector.LENS_FACING_BACK;
//        }
//        ImageCapture.OutputFileOptions.Builder outputFileOptions = new ImageCapture.OutputFileOptions.Builder(photoFile = initTakePicPath(mContext));
//        ImageCapture.Metadata metadata = new ImageCapture.Metadata();
//        metadata.setReversedHorizontal(CameraSelector.LENS_FACING_FRONT == lensFacing);
//        outputFileOptions.setMetadata(metadata)
                        takePhoto()
//                        imageCapture.takePicture(cameraExecutor, new ImageCapture.OnImageCapturedCallback() {
//                            @Override
//                            public void onCaptureSuccess(@NonNull ImageProxy image) {
//                                super.onCaptureSuccess(image);
////                                Bitmap bmSrc = BitmapFactory.decodeFile(createFile().getAbsolutePath());
////                                Bitmap bmCopy = Bitmap.createBitmap(bmSrc.getWidth(), bmSrc.getHeight(), bmSrc.getConfig());
//                                Bitmap bm = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
//                                ByteBuffer buffer = image.getPlanes()[0].getBuffer();
//                                byte[] bytes = new byte[buffer.capacity()];
//                                buffer.get(bytes);
//                                Bitmap bitmapImage = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, null);
//
////                                converter.yuvToRgb(image.getImage(), bm);
//
//                                runOnUiThread(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        img.setImageBitmap(bitmapImage);
//                                        image.close();
//                                    }
//                                });
//
//                            }
//
//                            @Override
//                            public void onError(@NonNull ImageCaptureException exception) {
//                                super.onError(exception);
//                                Log.i(TAG, "onError" + exception.getMessage());
//                            }
//                        })


//                CoCo.with(MainActivity.this).take(createFile())
//                .cameraFace(Face.FRONT).callBack(new TakeCallBack() {
//                    @Override
//                    public void onFinish(TakeResult takeResult) {
//
//                    }
//
//                    @Override
//                    public void onCancel() {
//
//                    }
//
//                    @Override
//                    public void onStart() {
//
//                    }
//                }).start(new CoCoCallBack<TakeResult>() {
//                    @Override
//                    public void onSuccess(TakeResult takeResult) {
//                        bitmap = Utils.INSTANCE.getBitmapFromFile(takeResult.getSavedFile().getPath());
//                        img.setImageBitmap(bitmap);
//
//                        new Thread(new Runnable() {
//                            @Override
//                            public void run() {
//                                recognition();
//                            }
//                        }).start();
//                    }
//
//                    @Override
//                    public void onFailed(Exception e) {
//
//                    }
//             })
        );
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
        SSCameraX.takePhoto(null, this).callback(new TakeCallback() {
            @Override
            public void onSuccess(@NonNull Bitmap bitmap, @NonNull ImageProxy image) {
                runOnUiThread(() -> {
                    img.setImageBitmap(bitmap);
                    image.close();
                });
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {

            }
        }).start();
    }

    File createFile() {
        try {
            return File.createTempFile(
                    "JPEG__demo", /* prefix */
                    ".jpg", /* suffix */
                    getCacheDir()/* directory */
            );
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    void unInit() {
        freshGoodsManager.RetailBot_Api_ModelUnInit();
    }

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

    //    void createFile(){
//        String path = getCacheDir() + "/cache";
//        File file = new File(path);
//        if (!file.exists()) {
//            file.mkdirs();
//        }
//        File picture=new File(path,"cache.jpg");
//    }
    private static final String TAG = "MainActivity";
    String imageData;
    Bitmap bitmap;

    StringBuffer sid;

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

    public void onDecodeClicked(String data) {
        byte[] decode = Base64.decode(data, Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(decode, 0, decode.length);
        //save to image on sdcard
        img.setImageBitmap(bitmap);
//        saveBitmap(bitmap);
    }

    private void saveBitmap(Bitmap bitmap) {
        try {
            String path = getCacheDir()
                    + "/decodeImage.jpg";
//            Log.d("linc","path is "+path);
            OutputStream stream = new FileOutputStream(path);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream);
            stream.close();
//            Log.e("linc","jpg okay!");
        } catch (IOException e) {
            e.printStackTrace();
//            Log.e("linc","failed: "+e.getMessage());
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