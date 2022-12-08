package com.szip.blewatch.base.Util;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.util.Log;

import com.jieli.jl_rcsp.task.contacts.DeviceContacts;
import com.jieli.jl_rcsp.util.JL_Log;
import com.szip.blewatch.base.Model.ContactModel;

import java.util.ArrayList;
import java.util.List;

/**
 * @author : chensenhua
 * @e-mail : chensenhua@zh-jieli.com
 * @date : 3/16/21 5:26 PM
 * @desc :
 */
public class ContactUtil {

    public static ArrayList<ContactModel> queryContacts(Context context, String selection, String[] selectionArgs) {
        String[] projection = {
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.SORT_KEY_PRIMARY,
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI,
                ContactsContract.CommonDataKinds.Phone.PHOTO_ID,
                ContactsContract.CommonDataKinds.Phone._ID
        };

        Cursor cursor = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, projection,
                selection, selectionArgs, ContactsContract.CommonDataKinds.Phone.SORT_KEY_PRIMARY + " ASC");
        if (cursor == null || cursor.getCount() < 1) {
            return new ArrayList<>();
        }
        cursor.moveToFirst();
        ArrayList<ContactModel> list = new ArrayList<>();
        do {
            ContactModel contacts = new ContactModel();
            contacts.setName(cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)));
            contacts.setMobile(cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
            long photoId = cursor.getLong(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_ID));
            String uri = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI));
            Log.e("data******", "name = " + contacts.getName());
//            PinyinBean pinyinBean = PinyinUtil.getPinYin(contacts.getName());
            contacts.setPing(PinyinUtil.getPinyin(contacts.getName()));
            list.add(contacts);
        } while (cursor.moveToNext());

        cursor.close();
        return list;
    }


    public static ArrayList<ContactModel> searchContactByName(Context context, String name) {
        String selection = "";
        selection = selection + ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " like ?";
        ArrayList<ContactModel> list = queryContacts(context, selection, new String[]{"%" + name + "%"});
        return list;
    }

    public static ArrayList<ContactModel> searchContactByNumber(Context context, String number) {
        String selection = "";
        selection = selection + ContactsContract.CommonDataKinds.Phone.NUMBER + " like ?";
        ArrayList<ContactModel> list = queryContacts(context, selection, new String[]{"%" + number + "%"});
        return list;
    }

    public static ArrayList<ContactModel> filterContact(ArrayList<ContactModel> contactModels,ArrayList<ContactModel> filter){
        for (ContactModel contact : contactModels) {
            for (ContactModel f : filter) {
                if (contact.getName().equals(f.getName()) && contact.getMobile().equals(f.getMobile())) {
                    contact.setCheck(true);
                    break;
                }
            }
        }
        return contactModels;
    }

    /**
     * 获取通讯录编码后数据
     *
     * @param list 通讯列表
     * @return 编码后数据
     */
    public static byte[] contactsToBytes(List<ContactModel> list) {
        if (list == null || list.isEmpty()) return new byte[20];
        int dataSize = 20;//每个字段的大小，包含一个空格
        byte[] data = new byte[dataSize * 2 * list.size()];
        int index = 0;
        for (ContactModel ContactModel : list) {
            String name = removeMoreString(ContactModel.getName());
            String number = removeMoreString(ContactModel.getMobile());
            byte[] nameData = name.getBytes();
            byte[] numberData = number.getBytes();
            System.arraycopy(nameData, 0, data, index, Math.min(nameData.length, dataSize - 1));
            index += dataSize;
            System.arraycopy(numberData, 0, data, index, Math.min(numberData.length, dataSize - 1));
            index += dataSize;
        }
        return data;
    }

    /**
     * 将字节数组转化为联系人列表
     *
     * @param data
     * @return
     */
    public static List<ContactModel> byteToContacts(byte[] data) {
        List<ContactModel> contacts = new ArrayList<>();
        if (data == null || data.length < 40) {
            return contacts;
        }
        for (int i = 0; i <= data.length - 40; i += 40) {
            byte[] nameData = new byte[20];
            byte[] numberData = new byte[20];
            System.arraycopy(data, i, nameData, 0, nameData.length);
            System.arraycopy(data, i + 20, numberData, 0, numberData.length);
            String name = new String(nameData).trim();
            String number = new String(numberData).trim();
            ContactModel ContactModel = new ContactModel();
            ContactModel.setName(name);
            ContactModel.setMobile(number);
            contacts.add(ContactModel);
        }

        return contacts;
    }

    private static String removeMoreString(String text) {
        while (text.getBytes().length > 19) {
            text = text.substring(0, text.length() - 1);
        }
        return text;
    }

}
