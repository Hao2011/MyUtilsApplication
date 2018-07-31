package com.hao.myutils.utils;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Process;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ${Hao} on 2018/7/27.
 */
public class PermissionUtils {
    private static final String TAG = "PermissionUtils";
    public static final int REQUEST_CODE_MUST_NEED = 31000;
    public static final int REQUEST_CODE_WRITE_EXTERNAL_STORAGE = 31001;
    public static final int REQUEST_CODE_CALL_PHONE = 31002;
    public static final int REQUEST_CODE_READ_SMS = 31003;
    public static final int REQUEST_CODE_READ_CONTACTS = 31006;
    public static final int REQUEST_CODE_CAMERA = 31007;
    public static final int REQUEST_CODE_RACCESS_FINE_LOCATION = 31005;
    public static final int REQUEST_CODE_BODY_SENSORS = 31008;
    public static final int REQUEST_CODE_READ_CALENDAR = 31004;
    private static void requestPermissions(Activity activity, String[] permissions, int requestCode) {
        ActivityCompat.requestPermissions(activity,permissions,requestCode);
    }

    /**
     * @Author:Hao
     * @Time:2018/7/30
     * @Description:判断权限是否已被授予
     */
    private static boolean isGranted(String permission,Activity activity) {
        return Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.M
                || PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(activity, permission);
    }

    /**
     * @Author:Hao
     * @Time:2018/7/30
     * @Description:请求单个权限
     */
    public static boolean requestPermission(Activity activity, String permission, int requestCode) {
        if (!isGranted(permission,activity)) {
            requestPermissions(activity,new String[]{permission},requestCode);
            return false;
        }
        return true;
    }

    /**
     * @Author:Hao
     * @Time:2018/7/30
     * @Description:请求必须使用的权限
     */
    public static void requestMustNeedPermission(Activity activity) {
        List<String> needRequestPermissions = new ArrayList<>();
        for (String permission :
                mustNeedPermissionList()) {
            if (!isGranted(permission,activity)) {
                needRequestPermissions.add(permission);
            }
        }
        if (needRequestPermissions.size() > 0) {
            String[] permissions = needRequestPermissions.toArray(new String[needRequestPermissions.size()]);
            requestPermissions(activity, permissions, REQUEST_CODE_MUST_NEED);
        }

    }

    /**
     * @Author:Hao
     * @Time:2018/7/30
     * @Description:检验必须的权限请求结果并处理
     */
    public static void verifyMustRequestResult(Activity activity, int[] grantResults, String[] permissions) {
        List<String> permissionList= new ArrayList<>();
        for (int i = 0; i < grantResults.length; i++) {
            if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                permissionList.add(permissions[i]);
            }
        }
        if(permissionList.size()>0){
            showDialogMustPermissionSettingPage(activity, permissionList);
        }
    }
    /**
     *@Author:Hao
     *@Time:2018/7/31
     *@Description:检验非必须权限请求结果并处理
     */
    public static void verifyNormalRequestResult(Activity activity, int[] grantResults, String[] permissions) {
        for (int i = 0; i < grantResults.length; i++) {
            if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                showDialogNormalPermissionPage(activity);
                break;
            }
        }
    }

    /**
     * @Author:Hao
     * @Time:2018/7/30
     * @Description:返回必须使用的权限列表
     */
    private static List<String> mustNeedPermissionList() {
        List<String> needPermissions = new ArrayList<>();
        needPermissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        needPermissions.add(Manifest.permission.CALL_PHONE);
        needPermissions.add(Manifest.permission.READ_SMS);
//        needPermissions.add(Manifest.permission.READ_CONTACTS);
//        needPermissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
//        needPermissions.add(Manifest.permission.CAMERA);
//        needPermissions.add(Manifest.permission.BODY_SENSORS);
//        needPermissions.add(Manifest.permission.READ_CALENDAR);
        return needPermissions;
    }
    /**
     *@Author:Hao
     *@Time:2018/7/31
     *@Description:必须的权限被拒绝后弹出说明窗口并引导用户去设置,拒绝则退出
     */
    private static void showDialogMustPermissionSettingPage(final Activity activity, List<String> permissionList) {
        String requestReason = getRequestReason(permissionList);
        AlertDialog alertDialog = new AlertDialog.Builder(activity)
                .setMessage(requestReason)
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        Process.killProcess(Process.myPid());
                    }
                })
                .setPositiveButton("前去设置", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        Uri packageURI = Uri.parse("package:" + activity.getPackageName());
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageURI);
                        activity.startActivity(intent);
                    }
                })
                .setCancelable(false)
                .create();
        alertDialog.show();
    }
    /**
     *@Author:Hao
     *@Time:2018/7/31
     *@Description:非必须权限被拒绝后弹出说明窗口并引导用户去设置
     */
    private static void showDialogNormalPermissionPage(final Activity activity) {
        AlertDialog alertDialog = new AlertDialog.Builder(activity)
                .setMessage("使用XX功能需要授权")
                .setPositiveButton("前去设置", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        Uri packageURI = Uri.parse("package:" + activity.getPackageName());
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageURI);
                        activity.startActivity(intent);
                    }
                }).create();

        alertDialog.show();
    }
    /**
     *@Author:Hao
     *@Time:2018/7/31
     *@Description:返回权限请求原因
     */
    private static String getRequestReason(List<String> permissionList){
        StringBuffer stringBuffer = new StringBuffer();
        for (String permission :
                permissionList) {
            if(permission.equals(Manifest.permission.WRITE_EXTERNAL_STORAGE) ){
                stringBuffer.append("存储功能,");
            }else if(permission.equals(Manifest.permission.CALL_PHONE)){
                stringBuffer.append("电话功能,");
            }else if(permission.equals(Manifest.permission.SEND_SMS)){
                stringBuffer.append("短信功能,");
            }else if(permission.equals(Manifest.permission.READ_CONTACTS)){
                stringBuffer.append("通讯录功能,");
            }else if(permission.equals(Manifest.permission.ACCESS_FINE_LOCATION)){
                stringBuffer.append("位置功能,");
            }else if(permission.equals(Manifest.permission.CAMERA)){
                stringBuffer.append("摄像头功能,");
            }else if(permission.equals(Manifest.permission.BODY_SENSORS)){
                stringBuffer.append("传感器功能,");
            }else if(permission.equals(Manifest.permission.READ_CALENDAR)){
                stringBuffer.append("日历功能,");
            }
        }
        stringBuffer.deleteCharAt(stringBuffer.lastIndexOf(","));
        stringBuffer.append("需要授权");
        return stringBuffer.toString();
    }

}
