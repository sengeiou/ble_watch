package com.szip.blewatch.base.Notification;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;
import android.widget.RemoteViews;

import com.szip.blewatch.base.Const.BroadcastConst;
import com.szip.blewatch.base.Util.LogUtil;
import com.szip.blewatch.base.Util.MathUtil;
import com.szip.blewatch.base.db.LoadDataUtil;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import static com.szip.blewatch.base.Util.MathUtil.FILE;

public class NotificationDataManager {
    private static final String TAG = "notify******";
    // For get tile and content of notification
    private static final int NOTIFICATION_TITLE_TYPE = 9;
    private static final int NOTIFICATION_CONTENT_TYPE = 10;
    private Handler mHandler;
    private SendNotficationDataThread mSendThread = null;
    private Context mContext;

    public NotificationDataManager(Context context) {
        mContext = context;
        mSendThread = new SendNotficationDataThread();
        mSendThread.start();
        mHandler = mSendThread.getHandler();
    }

    public void sendNotificationData(NotificationData notificationData){
        // Filter notification according to ignore list and exclusion list

        if (mContext.getSharedPreferences(FILE,Context.MODE_PRIVATE).getBoolean("notificationState",false)
        && LoadDataUtil.newInstance().getNotificationAble(notificationData.getPackageName())) {
            Log.i(TAG, "Notice: notification need send, package name=" + notificationData.getPackageName());
            // mSNThread.sendNotfications();
            Message message = new Message();
            message.what = SendNotficationDataThread.MESSAGE_SEND_NOTIFICATION;
            message.obj = (Object) notificationData;
            mHandler = mSendThread.getHandler();
            if (mHandler != null) {
                mHandler.sendMessage(message);
            }
        } else {
            Log.i(TAG, "Notice: notification don't need send, package name=" + notificationData.getPackageName());
        }
    }

    public NotificationData getNotificationData(Notification notification, String packageName, String tag, int id ){


        NotificationData notificationData = new NotificationData();
        String[] textArray = null;
        if (Build.VERSION.SDK_INT >= 24 || packageName.contains("whatsapp")  || packageName.contains("linkedin") || packageName.contains("xiaomi.xmsf")) {      //todo ---  add 20180314   xiaomi.xmsf --- 为小米系统推送
           Log.d("WHAT******","GET WHAT");
            textArray = getNotidicationTextForN(notification,packageName); //该方法会直接从extra中获取title和content
        } else {
            Log.d("WHAT******","GET normal");
            textArray = getNotificationText(notification);    //7.0以下兼容
        }
        String[] pageTextArray = getNotificationPageText(notification); //android 4.4w.2 support

        if (!TextUtils.isEmpty(notification.tickerText)) {
            notificationData.setTickerText(notification.tickerText.toString());
            if(packageName.contains("whatsapp") || (Build.VERSION.SDK_INT >= 24)){
                if(textArray != null && !TextUtils.isEmpty(textArray[1]) && !TextUtils.isEmpty(textArray[0])) {
                    notification.tickerText = textArray[0] + " : " + textArray[1];
                    notificationData.setTickerText(textArray[0] + " : " + textArray[1]);
                }
            }

            if (null != pageTextArray && null != textArray && null != textArray[0]) {
                textArray = concat(textArray, pageTextArray);
            } else {
                if (null != notification && null != notification.tickerText) {
                    textArray = new String[2];
                    if (notification.tickerText.toString().contains(":")) {
                        textArray[0] = notification.tickerText.toString().split(":")[0];
                        textArray[1] = notification.tickerText.toString();
                    } else {
                        textArray[0] = notification.tickerText.toString();
                        textArray[1] = notification.tickerText.toString();
                    }
                }
            }
            if (null != textArray && null != textArray[0]) {
                notificationData.setTextList(textArray);
            }
            try {
                Log.d(TAG, "textlist = " + Arrays.toString(textArray));
            } catch (Exception e) {
                Log.d(TAG, "get textlist error");
            }

        }else{
            if(packageName.contains("linkedin.android") || packageName.contains("xiaomi.xmsf")){    // todo ----   packageName.contains("xiaomi.xmsf") 主要是对于小米手机
                if(textArray != null){
                    notificationData.setTickerText(textArray[0] + ": " + textArray[1]);
                }
                notificationData.setTextList(textArray);
            }
        }
        if(null!=notification&&null!=packageName){
            // notificationData.setGroupKey(getGroupKey(notification));
            notificationData.setActionsList(null);
            notificationData.setPackageName(packageName);
//            notificationData.setAppID(MathUitl.getKeyFromValue(notificationData.getPackageName()));
            notificationData.setWhen(notification.when);
        }

        if (id == 0) { //Maybe some app's id is 0. like: hangouts(com.google.android.talk)
            id = 1 + (int) (Math.random() * 1000000);
            Log.d(TAG, "the id is 0 and need create a random number : " + id);
        }
        notificationData.setMsgId(id);
        if(null!=tag){notificationData.setTag(tag);}

        if(null==notificationData.getPackageName()){
            notificationData.setPackageName(packageName);
        }
        if(null==notificationData.getTextList()&&null==notificationData.getPackageName()&&null==notificationData.getTickerText()){
            return  null;
        }

        Log.e(TAG,"notificationData = " + notificationData.toString());
        return notificationData;
    }

    private String[] getNotidicationTextForN(Notification notification,String packName) {
        if (notification == null) {
            Log.e(TAG, "Notification is null to get text");
            return null;
        }
        String[] retArray = null;
        retArray = new String[2];
        if(packName.contains("whatsapp") || packName.contains("linkedin") ){  //todo ---  add 20180314    ||  packName.contains("xiaomi.xmsf")
            retArray[0] = notification.extras.getString("android.title");
            CharSequence[] charSequenceArray = notification.extras.getCharSequenceArray("android.textLines");
            if(charSequenceArray != null && charSequenceArray.length > 0){
                retArray[1] = charSequenceArray[charSequenceArray.length - 1].toString();
            }else{
                retArray[1] = notification.extras.getString("android.text");
            }
        }else {
            retArray[0] = notification.extras.getString("android.title");
            retArray[1] = notification.extras.getString("android.text");
        }
        Log.i(TAG, "[getNotidicationTextForN] Title = " + retArray[0] + ", Content = " + retArray[1]);
        return retArray;
    }

    @SuppressLint("UseSparseArrays")
    @SuppressWarnings("unchecked")
    public String[] getNotificationText(Notification notification) {
        String[] textArray = null;
        RemoteViews remoteViews = notification.contentView;
        if (remoteViews == null) {
            textArray = null;
            Log.i(TAG,"remoteViews is null, set title and content to be empty. ");
        } else {
            HashMap<Integer, String> text = new HashMap<Integer, String>();
            try {
                Class<?> remoteViewsClass = Class.forName(RemoteViews.class.getName());
                Field[] outerFields = remoteViewsClass.getDeclaredFields();
                Log.i(TAG,"outerFields.length = " + outerFields.length);
                Field actionField = null;
                for (Field outerField : outerFields) {
                    if (outerField.getName().equals("mActions")) {
                        actionField = outerField;
                        break;
                    }
                }
                if (actionField == null) {
                    Log.e(TAG,"actionField is null, return null");
                    return null;
                }
                actionField.setAccessible(true);
                ArrayList<Object> actions = (ArrayList<Object>) actionField.get(remoteViews);
                int viewId = 0;
                for (Object action : actions) {
                    /*
                     * Get notification tile and content
                     */
                    Field[] innerFields = action.getClass().getDeclaredFields();
    
                    // RemoteViews curr_action = (RemoteViews)action;
                    Object value = null;
                    Integer type = null;
                    for (Field field : innerFields) {
                        field.setAccessible(true);
                        if (field.getName().equals("value")) {
                            value = field.get(action);
                        } else if (field.getName().equals("type")) {
                            type = field.getInt(action);
                        } else if (field.getName().equals("methodName")) {
                            String method = (String) field.get(action);
                            if (method.equals("setProgress")) {
                                return null;
                            }
                        }
                    }
    
                    // If this notification filed is title or content, save it to
                    // text list
                    if ((type != null)
                            && ((type == NOTIFICATION_TITLE_TYPE) || (type == NOTIFICATION_CONTENT_TYPE))) {
                        if (value != null) {
                            viewId++;
                            text.put(viewId, value.toString());
                            if (viewId == 2) {
                                break;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                LogUtil.getInstance().logd(TAG, "getText ERROR");
            }
    
            textArray = text.values().toArray(new String[0]);
            if(textArray == null){
                Log.i(TAG,"get title and content from notification is null.Set it to be empty string.");
                textArray = new String[]{"",""};
            } else{
                Log.i(TAG,"textArray is " + Arrays.toString(textArray));
            }
        }
        String[] bigTextArray = new String[2];
        if (Build.VERSION.SDK_INT  >= 19) {//android 4.4
            //get bigtextstyle title and content
            String EXTRA_TITLE = "android.title";
            String EXTRA_TITLE_BIG = EXTRA_TITLE + ".big";
            String EXTRA_BIG_TEXT = "android.bigText";
            CharSequence mBigTitle = notification.extras.getCharSequence(EXTRA_TITLE_BIG);
            CharSequence mBigText = notification.extras.getCharSequence(EXTRA_BIG_TEXT);
            if(!TextUtils.isEmpty(mBigTitle)){
                bigTextArray[0] = mBigTitle.toString();
            } else if (textArray != null && textArray.length > 0 && !TextUtils.isEmpty(textArray[0])){
                bigTextArray[0] = textArray[0];
            } else{
                bigTextArray[0] = "";
            }

            if(!TextUtils.isEmpty(mBigText)){
                bigTextArray[1] = mBigText.toString();
            } else if (textArray != null && textArray.length > 1 && !TextUtils.isEmpty(textArray[1])){
                bigTextArray[1] = textArray[1];
            } else{
                bigTextArray[1] = "";
            }

        } else{
            bigTextArray = textArray;
            Log.i(TAG,"Android platform is lower than android 4.4 and does not support bigtextstyle attribute.");
        }
        try {
            LogUtil.getInstance().logd(TAG, "getNotificationText(), text list = " + Arrays.toString(bigTextArray));
        } catch (Exception e) {
            LogUtil.getInstance().logd(TAG, "getNotificationText Exception");
        }
        return bigTextArray;
    }

    public String[] getNotificationPageText(Notification notification) {
        String[] textArray = null;
        // get title and content of Pages
        if (Build.VERSION.SDK_INT  >= 20) {//android 4.4w.2
            String EXTRA_WEARABLE_EXTENSIONS = "android.wearable.EXTENSIONS";
            String KEY_PAGES = "pages";
            Bundle wearableBundle = notification.extras.getBundle(EXTRA_WEARABLE_EXTENSIONS);
            if (wearableBundle != null) {
                Notification[] pages = getNotificationArrayFromBundle(wearableBundle,KEY_PAGES);
                if(pages!=null){
                   Log.i(TAG, "pages num = " + pages.length);
                   for(int i=0; i<pages.length; i++){
                       String[] pageTextArray = getNotificationText(pages[i]);
                       if(pageTextArray!=null){
                               if(i==0){
                                   textArray = pageTextArray;
                               } else{
                                   textArray = concat(textArray,pageTextArray);
                               }
                           }
                       }
                   }
                }
        } else{
            Log.i(TAG,"Android platform is lower than android 4.4w.2 and does not support page attribute.");
        }
        try {
            LogUtil.getInstance().logd(TAG, "getNotificationPageText(), text list = " + Arrays.toString(textArray));
        } catch (Exception e) {
            LogUtil.getInstance().logd(TAG, "getNotificationPageText Exception");
        }
        return textArray;
    }

    public Notification[] getNotificationArrayFromBundle(Bundle bundle, String key) {
            Parcelable[] array = bundle.getParcelableArray(key);
            if (array instanceof Notification[] || array == null) {
                return (Notification[]) array;
            }
            Notification[] typedArray = Arrays.copyOf(array, array.length,
                    Notification[].class);
            bundle.putParcelableArray(key, typedArray);
            return typedArray;
     }

    public  String[] concat(String[] first, String[] second) {
        String[] result= new String[first.length + second.length];
        System.arraycopy(first, 0, result, 0, first.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }

    private String messTemp = "";
    private long msgTime;
    private static long time1 = 0;
    private static long time2 = 0;
    private class SendNotficationDataThread extends Thread {
        public static final int MESSAGE_SEND_NOTIFICATION = 1;
        private NotificationData notificationData = null;

        @SuppressLint("HandlerLeak")
        private Handler mHandler;

        @SuppressLint("HandlerLeak")
        @Override
        public void run() {
            Looper.prepare();
            mHandler = new Handler() {
                public void handleMessage(Message msg) {
                    switch (msg.what) {
                        case MESSAGE_SEND_NOTIFICATION:   // arg1
                            int messType = msg.arg1;
                            notificationData = (NotificationData) msg.obj;
                            if (notificationData != null) {
                                //////////////////////////////////////////////////////////////////////////////////////////////////
                                String tickerText = null;
                                if(notificationData.getTickerText()!=null&&!notificationData.getTickerText().equals("")) {
                                    tickerText = notificationData.getTickerText().toString();
                                    time1 = System.currentTimeMillis();   // 1517555810015   todo --- 进此方法的时间
                                    Log.e(TAG, "time1的值为----" + time1);
                                    Log.e(TAG, "Math.abs(time1 - time2) 的值为--" + Math.abs(time2 - time1));
                                    Log.e(TAG, "当前消息为--" + tickerText + "旧的消息为--" + messTemp);
                                    if(Math.abs(time1 - time2) <1000  && messTemp.equals(tickerText) ){ //  && messTemp.equals(tickerText)
                                        Log.e(TAG, "短时间内有重复消息，已过滤掉了--- Math.abs(time1 - time2) 的值为--" + Math.abs(time1 - time2));
                                        return ;
                                    }else {
                                        time2 = System.currentTimeMillis();
                                        messTemp = tickerText;
                                    }
                                }
                                ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                                //mtk

//                                    notificationData.setAppID(max + "");

                                if (null != notificationData && null != notificationData.getPackageName()) {
                                    if (null != notificationData && null != notificationData.getTextList()) {

                                        Intent intent = new Intent(BroadcastConst.SEND_BLE_DATA);
                                        intent.putExtra("command","sendNotify");
                                        intent.putExtra("title",notificationData.getTickerText());
                                        intent.putExtra("label", "");
                                        intent.putExtra("id", MathUtil.newInstance().getApplicationCode(notificationData.getPackageName()));
                                        mContext.sendBroadcast(intent);
//                                        if (MyApplication.getInstance().isMtk()){
//                                            NotificationController.getInstance(mContext)
//                                                    .sendNotfications(notificationData);
//                                        }else {
//                                            PackageInfo info = null;
//                                            PackageManager pm = mContext.getPackageManager();
//                                            try {
//                                                info = pm.getPackageInfo(notificationData.getPackageName(),PackageManager.GET_ACTIVITIES);
//                                                BleClient.getInstance().writeForSendNotify(notificationData.getTickerText(),
//                                                        info.applicationInfo.loadLabel(pm).toString(),MathUitl.getApplicationCode(notificationData.getPackageName()));
//                                            } catch (PackageManager.NameNotFoundException e) {
//                                                e.printStackTrace();
//                                            } catch (RuntimeException e){
//                                                e.printStackTrace();
//                                            }
//                                        }
                                        notificationData = null;
                                    }
                                }

                            }
                    }
                }
            };
            Looper.loop();
        }

        public Handler getHandler() {
            return mHandler;
        }
    }
}
