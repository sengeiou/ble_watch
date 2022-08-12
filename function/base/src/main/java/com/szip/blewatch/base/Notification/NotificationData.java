package com.szip.blewatch.base.Notification;

import java.util.ArrayList;
import java.util.Arrays;

public class NotificationData {
    private static final long serialVersionUID = 1L;
    private String[] ud;
    private String ue;
    private String uf;
    private String ug;
    private String uh;
    private String ui;
    private long uj;
    private int ts;
    private ArrayList uk;

    public NotificationData() {
    }

    public String getTag() {
        return this.ui;
    }

    public void setTag(String var1) {
        this.ui = var1;
    }

    public String[] getTextList() {
        return this.ud;
    }

    public void setTextList(String[] var1) {
        this.ud = var1;
    }

    public String getPackageName() {
        return this.ue;
    }

    public void setPackageName(String var1) {
        this.ue = var1;
    }

    public String getTickerText() {
        return this.uf;
    }

    public void setTickerText(String var1) {
        this.uf = var1;
    }

    public String getAppID() {
        return this.uh;
    }

    public void setAppID(String var1) {
        this.uh = var1;
    }

    public long getWhen() {
        return this.uj;
    }

    public void setWhen(long var1) {
        this.uj = var1;
    }

    public ArrayList getActionsList() {
        return this.uk;
    }

    public void setActionsList(ArrayList var1) {
        this.uk = var1;
    }

    public String getGroupKey() {
        return this.ug;
    }

    public void setGroupKey(String var1) {
        this.ug = var1;
    }

    public int getMsgId() {
        return this.ts;
    }

    public void setMsgId(int var1) {
        this.ts = var1;
    }

    public int hashCode() {
        boolean var1 = true;
        byte var2 = 1;
        int var3 = 31 * var2 + (this.uk == null ? 0 : this.uk.hashCode());
        var3 = 31 * var3 + (this.ug == null ? 0 : this.ug.hashCode());
        var3 = 31 * var3 + (this.ue == null ? 0 : this.ue.hashCode());
        var3 = 31 * var3 + Arrays.hashCode(this.ud);
        var3 = 31 * var3 + (this.uf == null ? 0 : this.uf.hashCode());
        var3 = 31 * var3 + (int)(this.uj ^ this.uj >>> 32);
        return var3;
    }

    public boolean equals(Object var1) {
        if (this == var1) {
            return true;
        } else if (var1 == null) {
            return false;
        } else if (this.getClass() != var1.getClass()) {
            return false;
        } else {
            NotificationData var2 = (NotificationData)var1;
            if (this.uj != var2.uj) {
                return false;
            } else if (this.ts != var2.ts) {
                return false;
            } else {
                if (this.ue == null) {
                    if (var2.ue != null) {
                        return false;
                    }
                } else if (!this.ue.equals(var2.ue)) {
                    return false;
                }

                return true;
            }
        }
    }

    public String toString() {
        return "NotificationData [mTextList=" + Arrays.toString(this.ud) + ", mPackageName=" + this.ue + ", mTickerText=" + this.uf + ", mGroupKey=" + this.ug + ", mAppID=" + this.uh + ", mTag=" + this.ui + ", mWhen=" + this.uj + ", mMsgId=" + this.ts + ", mActionsList=" + this.uk + "]";
    }
}
