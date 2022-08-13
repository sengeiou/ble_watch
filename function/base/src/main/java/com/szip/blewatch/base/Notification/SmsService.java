
package com.szip.blewatch.base.Notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.Nullable;

import com.szip.blewatch.base.Const.BroadcastConst;
import com.szip.blewatch.base.Util.MathUtil;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * This class will receive and process all new SMS.
 */
public class SmsService extends ContentObserver {
    // Debugging
    private static final String TAG = "DATA******";


    // Received parameters
    private Context mContext = null;

    /**
     * Creates a content observer.
     *
     * @param handler The handler to run {@link #onChange} on, or null if none.
     */
    public SmsService(Handler handler,Context context) {
        super(handler);
        mContext = context;
    }

    @Override
    public void onChange(boolean selfChange, @Nullable Uri uri) {
        super.onChange(selfChange, uri);
        Log.d(TAG, "SMS has changed!");
        Log.d(TAG, uri.toString());
        //判断uri.toString()是否等于手机的短信库
        if (uri.toString().equals("content://sms/raw")) {
            return;
        }
        sendSms();
    }


    void sendSms() {
        String msgbody;
        String address;
        String id;

        Cursor cursor = null;
        try {

            String[] projection = new String[] { "_id", "address", "person",
                    "body", "date", "type"};
            cursor = mContext.getContentResolver().query(Uri.parse("content://sms/inbox"), projection,
                    null, null, "date desc");
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    msgbody = cursor.getString(cursor.getColumnIndex("body"));
                    address = cursor.getString(cursor.getColumnIndex("address"));
                    Log.d(TAG, "发件人为：" + address + " " + "短信内容为：" + msgbody);
                    if ((msgbody != null) && (address != null)) {
                        Log.i(TAG, "SmsReceiver(),sendSmsMessage, msgbody = " + msgbody
                                + ", address = " + address);
                        Intent intent = new Intent(BroadcastConst.SEND_BLE_DATA);
                        intent.putExtra("command","sendNotify");
                        intent.putExtra("title",msgbody);
                        intent.putExtra("label", address);
                        intent.putExtra("id", 0);
                        mContext.sendBroadcast(intent);
                    }


                }
            }
        } catch (Exception e) {
            Log.d("data******","e = "+e.getMessage());
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }

        }
    }

}
