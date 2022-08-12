package com.szip.healthy.Model;

public class DateBean {

    private int year = 2019;
    private int month;
    private int day;
    private int type;
    private boolean isToday = false;
    private boolean isChooseDay = false;

    private String groupName;
    private String date;

    public DateBean(int year, int month, int day, int type, boolean isToday, boolean isChooseDay) {
        this.year = year;
        this.month = month;
        this.day = day;
        this.type = type;
        this.isToday = isToday;
        this.isChooseDay = isChooseDay;
    }

    public DateBean(int year, int month, int day, int type) {
        this.year = year;
        this.month = month;
        this.day = day;
        this.type = type;
    }

    public String getGroupName() {
        String sMonth = String.format("%02d", month);
        return year + "年" + sMonth + "月";
    }

    public String getDate() {
        return String.format("%04d-%0d2-%02d",year,month,day);
    }


    public int getYear() {
        return year;
    }

    public int getMonth() {
        return month;
    }

    public int getDay() {
        return day;
    }

    public int getType() {
        return type;
    }

    public void setToday(boolean today) {
        isToday = today;
    }

    public void setChooseDay(boolean chooseDay) {
        isChooseDay = chooseDay;
    }

    public boolean isToday() {
        return isToday;
    }

    public boolean isChooseDay() {
        return isChooseDay;
    }
}
