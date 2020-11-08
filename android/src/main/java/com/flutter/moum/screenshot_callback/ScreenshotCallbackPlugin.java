package com.flutter.moum.screenshot_callback;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.FileObserver;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
//import android.util.Log;

import java.io.File;
import java.util.List;
import java.util.ArrayList;

public class ScreenshotCallbackPlugin implements MethodCallHandler {
    private static MethodChannel channel;

    private Handler handler;
    private FileObserver fileObserver;
    private String TAG = "tag";
    private static Context context;


    public static void registerWith(Registrar registrar) {
        channel = new MethodChannel(registrar.messenger(), "flutter.moum/screenshot_callback");
        channel.setMethodCallHandler(new ScreenshotCallbackPlugin());
        context = registrar.activity().getApplicationContext();
    }

    @Override
    public void onMethodCall(MethodCall call, Result result) {
        //Log.d(TAG, "onMethodCall: ");

        if (call.method.equals("initialize")) {
            handler = new Handler(Looper.getMainLooper());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                //Log.d(TAG, "android x");
                List<File> files = new ArrayList<>();
                List<String> paths = new ArrayList<>();
                paths.add(context.getExternalFilesDir(Environment.DIRECTORY_DCIM) + File.separator + "Screenshots" + File.separator);
                paths.add(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES) + File.separator + "Screenshots" + File.separator);
                for (String path : paths) {
                    Log.e("Paths", path);
                    files.add(new File(path));
                }

                fileObserver = new FileObserver(files, FileObserver.CREATE) {
                    @Override
                    public void onEvent(int event, String path) {
                        //Log.d(TAG, "androidX onEvent");
                        if (event == FileObserver.CREATE) {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    channel.invokeMethod("onCallback", null);
                                }
                            });
                        }
                    }
                };
                fileObserver.startWatching();
            } else {
                //Log.d(TAG, "android others");
                for (Path path : Path.values()) {
                    //Log.d(TAG, "onMethodCall: "+path.getPath());
                    fileObserver = new FileObserver(path.getPath(), FileObserver.CREATE) {
                        @Override
                        public void onEvent(int event, String path) {
                            //Log.d(TAG, "android others onEvent");
                            if (event == FileObserver.CREATE) {
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        channel.invokeMethod("onCallback", null);
                                    }
                                });
                            }
                        }
                    };
                    fileObserver.startWatching();
                }
            }
            result.success("initialize");
        } else if (call.method.equals("dispose")) {
            fileObserver.stopWatching();
            result.success("dispose");
        } else {
            result.notImplemented();
        }
    }
}
