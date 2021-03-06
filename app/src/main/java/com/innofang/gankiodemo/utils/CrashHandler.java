package com.innofang.gankiodemo.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.os.Process;
import android.util.Log;
import android.widget.Toast;

import com.innofang.gankiodemo.R;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static android.os.Build.VERSION.SDK_INT;

/**
 * Author: Inno Fang
 * Time: 2017/4/20 17:35
 * Description: 捕获 Crash 信息
 */


public class CrashHandler implements Thread.UncaughtExceptionHandler {

    private static final String TAG = "CrashHandler";
    private static final boolean DEBUG = true;
    private static final String FILE_NAME = "crash";
    private static final String FILE_NAME_SUFFIX = ".trace";

    private String PATH;

    private static CrashHandler sInstance = new CrashHandler();
    private Thread.UncaughtExceptionHandler mDefaultCrashHandler;
    private Context mContext;

    private CrashHandler() {
    }

    public static CrashHandler getInstance() {
        return sInstance;
    }

    public void init(Context context) {
        mContext = context.getApplicationContext();
        PATH = Environment.getExternalStorageDirectory().getPath()
                + "/"
                + mContext.getResources().getString(R.string.app_name)
                + "/log/";

        mDefaultCrashHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    /**
     * 当程序中有未被捕获的异常时，系统将会自动调用该方法
     *
     * @param thread 出现未捕获异常的线程
     * @param ex     未捕获异常
     */
    @Override
    public void uncaughtException(final Thread thread, final Throwable ex) {
        try {
            /* 导出异常信息到 SD 卡中 */
            dumpExceptionToSDCard(ex);
            /* 这里可以上传异常信息到服务器 */
            uploadExceptionToServer();
        } catch (IOException e) {
            e.printStackTrace();
        }

        ex.printStackTrace();
        if (!handleException(ex) && mDefaultCrashHandler != null) {
            mDefaultCrashHandler.uncaughtException(thread, ex);
        } else {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                Log.e(TAG, "Error : ", e);
            }
            Process.killProcess(Process.myPid());
        }
    }

    /**
     * 自定义错误处理,收集错误信息 发送错误报告等操作均在此完成.
     *
     * @param ex
     * @return true:如果处理了该异常信息;否则返回false.
     */
    private boolean handleException(Throwable ex) {
        if (ex == null) {
            return false;
        }
        //使用Toast来显示异常信息
        new Thread() {
            @Override
            public void run() {
                Looper.prepare();
                Toast.makeText(mContext, "很抱歉,程序出现异常,即将退出.", Toast.LENGTH_LONG).show();
                Looper.loop();
            }
        }.start();
        return true;
    }

    private void dumpExceptionToSDCard(Throwable ex) throws IOException {
        /* 如果 SD 卡不存在或无法使用，则无法把异常信息写入 SD 卡 */
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            if (DEBUG) {
                Log.w(TAG, "sdcard unmounted, skip dump exception");
                return;
            }
        }

        File dir = new File(PATH);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        long current = System.currentTimeMillis();
        String time = new SimpleDateFormat("yyy-MM-dd HH:mm:ss", Locale.CHINA).format(new Date(current));
        File file = new File(PATH + FILE_NAME + time + FILE_NAME_SUFFIX);

        try {
            PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file)));
            pw.println(time);
            dumpPhoneInfo(pw);
            pw.println();
            ex.printStackTrace(pw);
            pw.close();
        } catch (Exception e) {
            Log.e(TAG, "dump crash info failed");
        }
    }

    @SuppressWarnings("deprecation")
    private void dumpPhoneInfo(PrintWriter pw) throws PackageManager.NameNotFoundException {
        PackageManager pm = mContext.getPackageManager();
        PackageInfo pi = pm.getPackageInfo(mContext.getPackageName(),
                PackageManager.GET_ACTIVITIES);
        pw.print("App Version: ");
        pw.print(pi.versionName);
        pw.print('_');
        pw.println(pi.versionCode);

        /* Android 版本号 */
        pw.print("OS Version: ");
        pw.print(Build.VERSION.RELEASE);
        pw.print('_');
        pw.println(SDK_INT);

        /* 手机制造商 */
        pw.print("Vendor: ");
        pw.println(Build.MODEL);

        /* CPU 架构 */
        pw.print("CPU ABI: ");
        pw.println(Build.CPU_ABI);
    }

    private void uploadExceptionToServer() {
        // TODO:2017/4/20 Upload Exception Message To Your Web Server

    }
}
