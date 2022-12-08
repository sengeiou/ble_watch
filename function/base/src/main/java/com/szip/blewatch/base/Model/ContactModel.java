package com.szip.blewatch.base.Model;

import android.text.TextUtils;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ContactModel implements Serializable {
    private String mobile;
    private String name;
    private String ping;
    private boolean isCheck;
    private boolean isSelect;

    public boolean isSelect() {
        return isSelect;
    }

    public void setSelect(boolean select) {
        isSelect = select;
    }

    public ContactModel(String mobile, String name) {
        this.mobile = mobile;
        this.name = name;
    }

    public ContactModel() {
    }

    public boolean isCheck() {
        return isCheck;
    }

    public void setCheck(boolean check) {
        isCheck = check;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String toString() {
        return "DeviceContacts{name=" + this.name + '\'' + ", mobile='" + this.mobile + '\'' + '}';
    }

    public void setPing(String ping) {
        this.ping = ping;
    }




    public String getSection() {
        if (TextUtils.isEmpty(ping)) {
            return "#";
        } else {
            String c = ping.substring(0, 1);
            Pattern p = Pattern.compile("[a-zA-Z]");
            Matcher m = p.matcher(c);
            if (m.matches()) {
                return c.toUpperCase();
            } else
                return "#";
        }
    }
}
