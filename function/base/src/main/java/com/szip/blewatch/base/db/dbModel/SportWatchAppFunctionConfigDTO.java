package com.szip.blewatch.base.db.dbModel;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.szip.blewatch.base.Util.LogUtil;
import com.szip.blewatch.base.db.AppDatabase;
import com.szip.blewatch.base.Model.HealthyConfig;


/**
 * @Author JoeChou
 * @Date 2020/6/18 11:41
 */
@Table(database = AppDatabase.class)
public class SportWatchAppFunctionConfigDTO extends BaseModel {

    @PrimaryKey(autoincrement = true)
    public long id;

    /**
     * APP名称，根据APP名称返回
     */
    @Column
    public String appName;

    /**
     * 主控平台
     */
    @Column
    public String mainControlPlatform;

    /**
     * 主板信息
     */
    @Column
    public String mainboard;

    /**
     * 标识符
     */
    @Column
    public int identifier;

    /**
     * 显示屏规格
     */
    @Column
    public String screen;

    /**
     * 屏幕类型，1 方屏， 0 圆屏
     */
    @Column
    public byte screenType;

    /**
     * 是否支持健康监测，1支持 0不支持，有子表
     */
    @Column
    public byte healthMonitor;

    /**
     * 是否支持多运动，1支持 0不支持，有子表
     */
    @Column
    public byte multiSport;

    /**
     * 是否支持基本蓝牙功能，1支持 0不支持，有子表
     */
    @Column
    public byte bluetooth;

    /**
     * 是否支持GPS功能，1支持 0不支持，有子表
     */
    @Column
    public byte gps;

    /**
     * 是否支持消息通知，1支持，0不支持
     */
    @Column
    public byte notification;

    /**
     * 是否支持SIM卡，1支持，0不支持
     */
    @Column
    public byte sim;

    /**
     * 是否支持本机音乐播放，1支持，0不支持
     */
    @Column
    public byte musicPlayer;

    /**
     * 是否使用mtk的jar包连接，1 是，0否
     */
    @Column
    public byte useMtkConnect;

    /**
     * 表盘列表的ID
     */
    @Column
    public int watchPlateGroupId;

    /**
     * 是否支持同步运动
     */
    @Column
    public int sportSync;

    @Column
    public String mac;

    @Column
    public String  productImg;

    @Column
    public String  dialImg;
    @Column
    public boolean  cameraSwitch;

    @Column
    public byte heartRateAutoTest;
    @Column
    public byte ecgAutoTest;
    @Column
    public byte bloodOxygenAutoTest;
    @Column
    public byte bloodPressureAutoTest;
    @Column
    public byte stepCounterAutoTest;
    @Column
    public byte temperatureAutoTest;
    @Column
    public byte sleepAutoTest;

    private HealthyConfig healthMonitorConfig;

    public HealthyConfig getHealthMonitorConfig() {
        return healthMonitorConfig;
    }

    public String getAppName() {
        return this.appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getMainControlPlatform() {
        return this.mainControlPlatform;
    }

    public void setMainControlPlatform(String mainControlPlatform) {
        this.mainControlPlatform = mainControlPlatform;
    }

    public String getMainboard() {
        return this.mainboard;
    }

    public void setMainboard(String mainboard) {
        this.mainboard = mainboard;
    }

    public int getIdentifier() {
        return this.identifier;
    }

    public void setIdentifier(int identifier) {
        this.identifier = identifier;
    }

    public String getScreen() {
        return this.screen;
    }

    public void setScreen(String screen) {
        this.screen = screen;
    }

    public byte getScreenType() {
        return screenType;
    }

    public void setScreenType(byte screenType) {
        this.screenType = screenType;
    }

    public byte getHealthMonitor() {
        return this.healthMonitor;
    }

    public void setHealthMonitor(byte healthMonitor) {
        this.healthMonitor = healthMonitor;
    }

    public byte getMultiSport() {
        return this.multiSport;
    }

    public void setMultiSport(byte multiSport) {
        this.multiSport = multiSport;
    }

    public byte getBluetooth() {
        return this.bluetooth;
    }

    public void setBluetooth(byte bluetooth) {
        this.bluetooth = bluetooth;
    }

    public byte getGps() {
        return this.gps;
    }

    public void setGps(byte gps) {
        this.gps = gps;
    }

    public byte getNotification() {
        return this.notification;
    }

    public void setNotification(byte notification) {
        this.notification = notification;
    }

    public byte getSim() {
        return this.sim;
    }

    public void setSim(byte sim) {
        this.sim = sim;
    }

    public byte getMusicPlayer() {
        return this.musicPlayer;
    }

    public void setMusicPlayer(byte musicPlayer) {
        this.musicPlayer = musicPlayer;
    }

    public byte getUseMtkConnect() {
        return useMtkConnect;
    }

    public void setUseMtkConnect(byte useMtkConnect) {
        this.useMtkConnect = useMtkConnect;
    }

    public int getWatchPlateGroupId() {
        return watchPlateGroupId;
    }

    public void setWatchPlateGroupId(int watchPlateGroupId) {
        this.watchPlateGroupId = watchPlateGroupId;
    }

    public void setSportSync(int sportSync) {
        this.sportSync = sportSync;
    }

    public int getSportSync() {
        return sportSync;
    }
}
