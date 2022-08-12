package com.szip.healthy.Model;

import java.util.ArrayList;

public class MonthBean {
    private int year;
    private int month;
    private ArrayList<DateBean> dateBeans;

    public MonthBean(int year, int month, ArrayList<DateBean> dateBeans) {
        this.year = year;
        this.month = month;
        this.dateBeans = dateBeans;
    }

    public MonthBean(int year, int month) {
        this.year = year;
        this.month = month;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public ArrayList<DateBean> getDateBeans() {
        return dateBeans;
    }

    public void setDateBeans(ArrayList<DateBean> dateBeans) {
        this.dateBeans = dateBeans;
    }
}
