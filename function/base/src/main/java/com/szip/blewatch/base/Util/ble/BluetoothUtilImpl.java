package com.szip.blewatch.base.Util.ble;

import android.annotation.SuppressLint;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.Vibrator;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.alibaba.android.arouter.launcher.ARouter;
import com.inuker.bluetooth.library.connect.listener.BleConnectStatusListener;
import com.inuker.bluetooth.library.connect.options.BleConnectOptions;
import com.inuker.bluetooth.library.connect.response.BleConnectResponse;
import com.inuker.bluetooth.library.connect.response.BleMtuResponse;
import com.inuker.bluetooth.library.connect.response.BleNotifyResponse;
import com.inuker.bluetooth.library.connect.response.BleReadResponse;
import com.inuker.bluetooth.library.connect.response.BleWriteResponse;
import com.inuker.bluetooth.library.model.BleGattCharacter;
import com.inuker.bluetooth.library.model.BleGattProfile;
import com.inuker.bluetooth.library.model.BleGattService;
import com.jieli.jl_rcsp.constant.StateCode;
import com.szip.blewatch.base.Broadcast.MyPhoneCallListener;
import com.szip.blewatch.base.Const.RouterPathConst;
import com.szip.blewatch.base.Const.SendFileConst;
import com.szip.blewatch.base.R;
import com.szip.blewatch.base.Const.BroadcastConst;
import com.szip.blewatch.base.Util.DateUtil;
import com.szip.blewatch.base.Util.LogUtil;
import com.szip.blewatch.base.Util.MathUtil;
import com.szip.blewatch.base.Util.MusicUtil;
import com.szip.blewatch.base.Util.ble.jlBleInterface.FunctionManager;
import com.szip.blewatch.base.Util.ble.jlBleInterface.ManagerInitCallback;
import com.szip.blewatch.base.Util.ble.jlBleInterface.OTAManager;
import com.szip.blewatch.base.Util.ble.jlBleInterface.WatchManager;
import com.szip.blewatch.base.Util.http.HttpClientUtils;
import com.szip.blewatch.base.Util.http.TokenInterceptor;
import com.szip.blewatch.base.View.ProgressHudModel;
import com.szip.blewatch.base.db.LoadDataUtil;
import com.szip.blewatch.base.db.SaveDataUtil;
import com.szip.blewatch.base.db.dbModel.AnimalHeatData;
import com.szip.blewatch.base.db.dbModel.BloodOxygenData;
import com.szip.blewatch.base.db.dbModel.BloodPressureData;
import com.szip.blewatch.base.db.dbModel.HeartData;
import com.szip.blewatch.base.db.dbModel.ScheduleData;
import com.szip.blewatch.base.db.dbModel.SleepData;
import com.szip.blewatch.base.db.dbModel.SportData;
import com.szip.blewatch.base.db.dbModel.SportWatchAppFunctionConfigDTO;
import com.szip.blewatch.base.db.dbModel.StepData;
import com.szip.blewatch.base.Model.BleStepModel;
import com.szip.blewatch.base.db.dbModel.UserModel;
import com.zhy.http.okhttp.BaseApi;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.builder.PostJsonBuilder;
import com.zhy.http.okhttp.callback.GenericsCallback;
import com.zhy.http.okhttp.utils.JsonGenericsSerializator;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;

import okhttp3.Call;

import static android.media.AudioManager.FLAG_PLAY_SOUND;
import static android.media.AudioManager.STREAM_MUSIC;

@SuppressLint("MissingPermission")
public class BluetoothUtilImpl implements IBluetoothUtil {
    private IBluetoothState iBluetoothState;
    private UUID serviceUUID;
    private String mMac = null;
    /**
     * ?????????????????? 0:????????? 2??????????????? 3??????????????? 4:???????????? 5???????????????
     * */
    private int connectState = 5;
    /**
     * ??????????????????
     * */
    private ArrayList<Integer> indexData;
    private boolean isSync = false;//????????????????????????

    private int mSportIndex = 0;
    private byte[] recvBuffer = new byte[1024 * 50 * 4];
    private int recvLength = 0;   //buffer????????????????????????
    private int recvState = 0;   // 0: seek sync byte; 1: seek header; 2: get data
    private int pkg_type;
    private int pkg_dataLen;
    private int pkg_timeStamp;
    private HandlerThread mHandlerThread;
    private Handler mAnalysisHandler;
    private static final int ANALYSIS_HANDLER_FLAG = 0x100;
    private static final int ANALYSIS_HANDLER_SEND_DATA = 0x101;
    private static final int ANALYSIS_HANDLER_READ_DATA = 0x102;
    private static final int ANALYSIS_HANDLER_SEND_JL_DATA = 0x103;
    private boolean isSendData;//???????????????????????????
    private boolean isSendJLData;//???????????????????????????
    private Queue<byte[]> sendDataQueue = new LinkedBlockingQueue<>();
    private Queue<byte[]> sendJLDataQueue = new LinkedBlockingQueue<>();
    private MediaPlayer mediaPlayer;
    private int volume = 0;

    private Context context;

    TelephonyManager tm;
    MyPhoneCallListener myPhoneCallListener;

    private static BluetoothUtilImpl bluetoothUtil;

    public static BluetoothUtilImpl getInstance(){
        if (null == bluetoothUtil) {
            synchronized (WatchManager.class) {
                if (null == bluetoothUtil) {
                    bluetoothUtil = new BluetoothUtilImpl();
                }
            }
        }
        return bluetoothUtil;
    }

    private BluetoothUtilImpl(){}

    public BluetoothUtilImpl init(Context context) {
        this.context = context;
        DataParser.newInstance().setmIDataResponse(iDataResponse);
        myPhoneCallListener = new MyPhoneCallListener(context);
        mHandlerThread = new HandlerThread("analysis-thread");
        mHandlerThread.start();
        mAnalysisHandler = new Handler(mHandlerThread.getLooper()) {

            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == ANALYSIS_HANDLER_FLAG) {
                    byte[] value = (byte[]) msg.obj;
                    if (value != null && (value.length != 0)) {
                        try {
                            System.arraycopy(value, 0, recvBuffer, recvLength, value.length);
                            recvLength += value.length;
                        } catch (Exception e) {
                            e.printStackTrace();
                            recvLength = 0;
                            recvState = 0;
                            pkg_dataLen = 0;
                        }
                    }
                    phaserRecvBuffer();
                }else if (msg.what==ANALYSIS_HANDLER_SEND_DATA){
                    if (sendDataQueue!=null){
                        byte[] datas = sendDataQueue.peek();
                        if (datas==null){
                            LogUtil.getInstance().logd("data******",
                                    "?????????????????????????????????????????????");
                            isSendData = false;
                            return;
                        }

                        ClientManager.getClient().write(mMac, UUID.fromString(Config.char0),
                                UUID.fromString(Config.char1), datas, new BleWriteResponse() {
                            @Override
                            public void onResponse(int code) {
                                if(code == 0){
                                    //???????????????????????????????????????????????????,?????????????????????????????????????????????????????????????????????
                                    sendDataQueue.poll();
                                }
                                mAnalysisHandler.sendEmptyMessage(ANALYSIS_HANDLER_SEND_DATA);
                            }
                        });
                    }
                }else if (msg.what==ANALYSIS_HANDLER_SEND_JL_DATA){
                    if (sendJLDataQueue!=null){
                        byte[] datas = sendJLDataQueue.peek();
                        if (datas==null){
                            LogUtil.getInstance().logd("data******",
                                    "???????????????????????????????????????????????????");
                            isSendJLData = false;
                            return;
                        }

                        ClientManager.getClient().write(mMac, UUID.fromString(Config.jlService),
                                UUID.fromString(Config.jlCharWrite), datas, new BleWriteResponse() {
                                    @Override
                                    public void onResponse(int code) {
                                        if(code == 0){
                                            //???????????????????????????????????????????????????,?????????????????????????????????????????????????????????????????????
                                            sendJLDataQueue.poll();
                                        }
                                        mAnalysisHandler.sendEmptyMessage(ANALYSIS_HANDLER_SEND_JL_DATA);
                                    }
                                });
                    }
                }else if (msg.what==ANALYSIS_HANDLER_READ_DATA){
                    ClientManager.getClient().read(mMac,UUID.fromString(Config.char0)
                            ,UUID.fromString(Config.char3),bleReadResponse);
                }
            }

        };

        return bluetoothUtil;
    }

    @Override
    public void connect(String mac, IBluetoothState iBluetoothState) {
        if (iBluetoothState!=null)
            this.iBluetoothState = iBluetoothState;
        this.mMac = mac;
        if (connectState == 5){
            LogUtil.getInstance().logd("data******","????????????mac = "+mac);
            connectState = 2;
            BleConnectOptions options = new BleConnectOptions.Builder()
                    .setConnectRetry(1)
                    .setConnectTimeout(10000)
                    .setServiceDiscoverRetry(1)
                    .setServiceDiscoverTimeout(10000)
                    .build();
            ClientManager.getClient().connect(mMac, options,bleConnectResponse);
            ClientManager.getClient().registerConnectStatusListener(mMac,connectStatusListener);
            if (iBluetoothState!=null)
                iBluetoothState.updateState(connectState);
        }
    }

    @Override
    public void disconnect() {
        if (mMac!=null){
            connectState = 5;
            ClientManager.getClient().disconnect(mMac);
            if (iBluetoothState!=null)
                iBluetoothState.updateState(connectState);
        }
    }

    private BleConnectResponse bleConnectResponse = new BleConnectResponse() {
        @Override
        public void onResponse(int code, BleGattProfile data) {
            if( code == 0 ){        // 0 ??????
                ClientManager.getClient().requestMtu(mMac, 300, new BleMtuResponse() {
                    @Override
                    public void onResponse(int code, Integer data) {
                    }
                });
                setGattProfile(data);
            }else{
                connectState = 5;
                isSync = false;
                recvLength = 0;
                recvState = 0;
                pkg_dataLen = 0;
            }
        }
    };

    /**
     * ?????????????????????????????????????????????
     * */
    public void setGattProfile(BleGattProfile profile) {
        List<BleGattService> services = profile.getServices();
        for (com.inuker.bluetooth.library.model.BleGattService service : services) {
            if(Config.char0.equalsIgnoreCase(service.getUUID().toString())||
            Config.jlService.equalsIgnoreCase(service.getUUID().toString())){
                serviceUUID = service.getUUID();
                LogUtil.getInstance().logd("data******","??????service = "+serviceUUID.toString());
                List<BleGattCharacter> characters = service.getCharacters();
                for(BleGattCharacter character : characters){
                    if( character.getUuid().toString().equalsIgnoreCase(Config.char2)||
                    character.getUuid().toString().equalsIgnoreCase(Config.jlCharNotify)){     // ???????????????????????????
                        openid(serviceUUID,character.getUuid());
                    }
                }
            }
        }
    }

    public void openid(UUID serviceUUID, UUID characterUUID) {
        ClientManager.getClient().notify(mMac,serviceUUID,characterUUID,bleNotifyResponse);
    }

    /**
     * ?????????????????????
     * */
    private BleConnectStatusListener connectStatusListener = new BleConnectStatusListener() {
        @Override
        public void onConnectStatusChanged(String mac, int status) {
            if( status == 0x10){
                final BluetoothDevice blueDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(mac);
                UserModel userInfo = LoadDataUtil.newInstance().getUserInfo(MathUtil.newInstance().getUserId(context));
                if (userInfo==null||userInfo.product==null)
                    return;
                Log.d("data******","product = "+userInfo.product);
                if (MathUtil.newInstance().isJLWatch(userInfo.product)){
                    OTAManager.getInstance(context).setConnectState(StateCode.CONNECTION_OK,blueDevice);
                    WatchManager.getInstance().init(blueDevice, context, new ManagerInitCallback() {
                        @Override
                        public void initSuccess(boolean success) {
                            if (success){
                                getDeviceInfo();
                            }else {
                                //??????sdk??????????????????????????????
                                MusicUtil.getSingle().unRegisterNotify();
                                WatchManager.getInstance().disconnect();
                                initPhoneStateListener(false);
                                connectState = 5;
                                isSync = false;
                                recvLength = 0;
                                recvState = 0;
                                pkg_dataLen = 0;
                                if (iBluetoothState!=null)
                                    iBluetoothState.updateState(connectState);
                            }
                        }
                    });
                }else {
                    getDeviceInfo();
                }

            }else{
                MusicUtil.getSingle().unRegisterNotify();
                WatchManager.getInstance().destroy();
                OTAManager.getInstance(context).setConnectState(StateCode.CONNECTION_DISCONNECT,null);
                initPhoneStateListener(false);
                connectState = 5;
                isSync = false;
                recvLength = 0;
                recvState = 0;
                pkg_dataLen = 0;
                if (iBluetoothState!=null)
                    iBluetoothState.updateState(connectState);
            }
        }
    };

    /**
     * ?????????????????????
     * */
    private BleNotifyResponse bleNotifyResponse = new BleNotifyResponse() {
        @Override
        public void onNotify(UUID service, UUID character, byte[] value) {
            if (character.toString().equalsIgnoreCase(Config.jlCharNotify)){
                LogUtil.getInstance().logd("data******","??????sdk????????????:"+ DateUtil.byteToHexString(value));
                WatchManager.getInstance().notifyData(value);
                OTAManager.getInstance(context).notifyData(value);
            }else {
                LogUtil.getInstance().logd("DATA******","????????????????????????:"+ DateUtil.byteToHexString(value));
                if (value.length == 8) {
                    if ((value[4] == -16) && (value[5] == -16) && (value[6] == -16) && (value[7] == -16)) {
                        Message message = new Message();
                        message.what = ANALYSIS_HANDLER_READ_DATA;
                        mAnalysisHandler.sendMessage(message);
                    }
                }else {
                    DataParser.newInstance().parseNotifyData(value);
                }
            }

        }

        @Override
        public void onResponse(int code) {

        }
    };


    private void getDeviceInfo(){
        connectState = 3;
        //?????????????????????????????????
        FunctionManager.getInstance().getDial();
        TimerTask timerTask= new TimerTask() {
            @Override
            public void run() {
                writeForSyncTime();
                writeForSyncTimeStyle();
                writeForSetUnit();
                writeForSetLanguage();
                writeForUpdateUserInfo();
                writeForSetAuto();
                initPhoneStateListener(true);
                writeForSetWeather();
                MusicUtil.getSingle().registerNotify();
            }
        };
        Timer timer = new Timer();
        timer.schedule(timerTask,300);
        if (iBluetoothState!=null)
            iBluetoothState.updateState(connectState);
    }


    /**
     * ???????????????
     * */
    private BleReadResponse bleReadResponse = new BleReadResponse() {
        @Override
        public void onResponse(int code, byte[] data) {
            if (data.length != 0){
                LogUtil.getInstance().logd("DATA******","??????????????????????????????:"+ DateUtil.byteToHexString(data));
                if (data.length==8&&(data[1] == 0x56||data[1] == 0x55)){
                    DataParser.newInstance().parseNotifyData(data);
                    return;
                }
                if (data.length > 0) {
                    Message message = mAnalysisHandler.obtainMessage();
                    message.what = ANALYSIS_HANDLER_FLAG;
                    message.obj = data;
                    mAnalysisHandler.sendMessage(message);
                }
            }
        }
    };

    private BleWriteResponse bleWriteResponse = new BleWriteResponse() {
        @Override
        public void onResponse(int code) {

        }
    };


    /**
     * ??????????????????
     */
    private synchronized void phaserRecvBuffer() {
        if (recvState == 0) {//??????????????????AA???????????????????????????????????????
            for (int offset = 0; offset < recvLength; offset++) {
                if (recvBuffer[offset] == (byte) 0xAA) {
                    System.arraycopy(recvBuffer, offset, recvBuffer, 0, recvLength - offset);
                    recvLength -= offset;
                    recvState = 1;
                    break;
                }
            }
            if (recvState != 1) {
                recvLength = 0;
            }
        }

        if (recvState == 1) {//????????????????????????????????????????????????????????????????????????????????????
            if (recvLength >= 8) {
                pkg_type = (recvBuffer[1] & 0xFF);
                pkg_dataLen = (recvBuffer[2] & 0xff) + ((recvBuffer[3] & 0xFF) << 8);
                pkg_timeStamp = (recvBuffer[4] & 0xff) + ((recvBuffer[5] & 0xFF) << 8) + ((recvBuffer[6] & 0xff) << 16) + ((recvBuffer[7] & 0xFF) << 24);
                if (pkg_dataLen == 0) {
                    recvState = 0;
                } else {
                    recvState = 2;
                }
                if (!checkArrayCopy(recvBuffer, 8, recvBuffer, 0, recvLength - 8)) {
                    return;
                }
                recvLength -= 8;
            } else {
                LogUtil.getInstance().logd("DATA******", "......");
                return;
            }
        }

        if (recvState == 2) {
            LogUtil.getInstance().logd("DATA******", "recvLenght=" + recvLength + " dataLength=" + pkg_dataLen+" time = "+pkg_timeStamp);
            if (recvLength >= pkg_dataLen) {
                byte[] pkg_data = new byte[pkg_dataLen];
                System.arraycopy(recvBuffer, 0, pkg_data, 0, pkg_dataLen);
                System.arraycopy(recvBuffer, pkg_dataLen, recvBuffer, 0, recvLength - pkg_dataLen);
                recvLength -= pkg_dataLen;
                recvState = 0;
                DataParser.newInstance().parseReadData(pkg_type,pkg_data,pkg_timeStamp,recvLength > 0?false:true);
            } else {
                LogUtil.getInstance().logd("DATA******", "------");
                return;
            }
        }

        if (recvLength > 0) {
            Message message = mAnalysisHandler.obtainMessage();
            message.what = ANALYSIS_HANDLER_FLAG;
            message.obj = null;
            mAnalysisHandler.sendMessage(message);
        } else {
            //??????????????????????????????
            LogUtil.getInstance().logd("DATA******", "//////");
            recvLength = 0;
            recvState = 0;
            pkg_dataLen = 0;
            syncSportDataByOrder();
            return;
        }

    }

    /**
     * ???????????????????????????
     */
    private void syncSportDataByOrder() {
        LogUtil.getInstance().logd("data******","indexData = "+indexData);
        if (indexData==null||mSportIndex == indexData.size()) {

            //????????????????????????????????????
            isSync = false;
            indexData = null;
            mSportIndex = 0;
            context.sendBroadcast(new Intent(BroadcastConst.UPDATE_UI_VIEW));
        } else {
            synSmartDeviceData(mSportIndex);
            mSportIndex++;
        }


    }

    /**
     * ????????????????????????????????????
     *
     * @param sourceByte     ???????????????
     * @param startIndex     ??????????????????????????????
     * @param destByte       ??????????????????
     * @param startDestIndex ?????????????????????????????????
     * @param destLength     ??????????????????
     * @return boolean
     * @throws ArrayIndexOutOfBoundsException ArrayIndexOutOfBoundsException
     */
    private static boolean checkArrayCopy(byte[] sourceByte, int startIndex, byte[] destByte, int startDestIndex, int destLength) {
        try {
            System.arraycopy(sourceByte, startIndex, destByte, startDestIndex, destLength);
            return true;
        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void synSmartDeviceData(final int dataType) {
        byte[] datas = new byte[0];
        int type = indexData.get(dataType);
        if (connectState==3) {
            switch (type) {
                case 0x01:
                    //????????????
                    LogUtil.getInstance().logd("DATA******", "sync step");
                    datas = CommandUtil.getCommandbyteArray(context,0x01, 8,
                            0, true);
                    break;
                case 0x02:
                    //????????????
                    LogUtil.getInstance().logd("DATA******", "sync heart_rate");
                    datas = CommandUtil.getCommandbyteArray(context,0x02, 8,
                            0, true);
                    break;
                case 0x03:
                    //????????????
                    LogUtil.getInstance().logd("DATA******", "sync sleep");
                    datas = CommandUtil.getCommandbyteArray(context,0x03, 8,
                            0, true);
                    break;
                case 0x04:
                    //????????????
                    LogUtil.getInstance().logd("DATA******", "sync run");
                    datas = CommandUtil.getCommandbyteArray(context,0x04, 8,
                            0, true);
                    break;
                case 0x05:
                    //????????????
                    LogUtil.getInstance().logd("DATA******", "sync onfoot");
                    datas = CommandUtil.getCommandbyteArray(context,0x05, 8,
                            0, true);
                    break;
                case 0x06:
                    //?????????
                    LogUtil.getInstance().logd("DATA******", "sync marathon");
                    datas = CommandUtil.getCommandbyteArray(context,0x06, 8,
                            0, true);
                    break;
                case 0x07:
                    //??????
                    LogUtil.getInstance().logd("DATA******", "sync rope_shipping");
                    datas = CommandUtil.getCommandbyteArray(context,0x07, 8,
                            0, true);
                    break;
                case 0x08:
                    //????????????
                    LogUtil.getInstance().logd("DATA******", "sync swim");
                    datas = CommandUtil.getCommandbyteArray(context,0x08, 8,
                            0, true);
                    break;
                case 0x09:
                    //??????
                    LogUtil.getInstance().logd("DATA******", "sync rock_climbing");
                    datas = CommandUtil.getCommandbyteArray(context,0x09, 8,
                            0, true);
                    break;
                case 0x0A:
                    //??????
                    LogUtil.getInstance().logd("DATA******", "sync skking");
                    datas = CommandUtil.getCommandbyteArray(context,0x0A, 8,
                            0, true);
                    break;
                case 0x0B:
                    //??????
                    LogUtil.getInstance().logd("DATA******", "sync riding");
                    datas = CommandUtil.getCommandbyteArray(context,0x0B, 8,
                            0, true);
                    break;
                case 0x0C:
                    //??????
                    LogUtil.getInstance().logd("DATA******", "sync rowing");
                    datas = CommandUtil.getCommandbyteArray(context,0x0C, 8,
                            0, true);
                    break;
                case 0x0D:
                    //??????
                    LogUtil.getInstance().logd("DATA******", "sync bungee");
                    datas = CommandUtil.getCommandbyteArray(context,0x0D, 8,
                            0, true);
                    break;
                case 0x0E:
                    //??????
                    LogUtil.getInstance().logd("DATA******", "sync mountaineer");
                    datas = CommandUtil.getCommandbyteArray(context,0x0E, 8,
                            0, true);
                    break;
                case 0x0F:
                    //??????
                    LogUtil.getInstance().logd("DATA******", "sync parachute");
                    datas = CommandUtil.getCommandbyteArray(context,0x0F, 8,
                            0, true);
                    break;
                case 0x10:
                    //?????????
                    LogUtil.getInstance().logd("DATA******", "sync golf");
                    datas = CommandUtil.getCommandbyteArray(context,0x10, 8,
                            0, true);
                    break;

                case 0x11:
                    //??????
                    LogUtil.getInstance().logd("DATA******", "sync surf");
                    datas = CommandUtil.getCommandbyteArray(context,0x11, 8,
                            0, true);
                    break;
                case 0x14:
                    //?????????
                    LogUtil.getInstance().logd("DATA******", "sync treadmill");
                    datas = CommandUtil.getCommandbyteArray(context,0x14, 8,
                            0, true);
                    break;
                case 0x19:
                    //?????????
                    LogUtil.getInstance().logd("DATA******", "sync step on day");
                    datas = CommandUtil.getCommandbyteArray(context,0x19, 8,
                            0, true);
                    break;
                case 0x21:
                    //?????????
                    LogUtil.getInstance().logd("DATA******", "sync badnition on day");
                    datas = CommandUtil.getCommandbyteArray(context,0x21, 8,
                            0, true);
                    break;
                case 0x22:
                    //??????
                    LogUtil.getInstance().logd("DATA******", "sync basket on day");
                    datas = CommandUtil.getCommandbyteArray(context,0x22, 8,
                            0, true);
                    break;
                case 0x23:
                    //??????
                    LogUtil.getInstance().logd("DATA******", "sync foot on day");
                    datas = CommandUtil.getCommandbyteArray(context,0x23, 8,
                            0, true);
                    break;
                case 0x25:
                    //??????
                    LogUtil.getInstance().logd("DATA******", "sync temp on day");
                    datas = CommandUtil.getCommandbyteArray(context,0x25, 8,
                            0, true);
                    break;
                case 0x26:
                    //??????
                    LogUtil.getInstance().logd("DATA******", "sync blood pressure on day");
                    datas = CommandUtil.getCommandbyteArray(context,0x26, 8,
                            0, true);
                    break;
                case 0x27:
                    //??????
                    LogUtil.getInstance().logd("DATA******", "sync blood oxygen on day");
                    datas = CommandUtil.getCommandbyteArray(context,0x27, 8,
                            0, true);
                    break;
                default:
                    break;
            }
            sendCommand(datas);
        }
    }


    public boolean sendCommandToSdk(byte[] datas){
        if (datas!=null){
            if (sendJLDataQueue!=null)
                sendJLDataQueue.add(datas);
            if(!isSendJLData){//????????????????????????????????????????????????
                isSendJLData = true;
                mAnalysisHandler.sendEmptyMessage(ANALYSIS_HANDLER_SEND_JL_DATA);
            }
        }
        return  true;
    }

    @Override
    public void sendCommand(byte[] datas) {
        if (datas!=null){
            if (sendDataQueue!=null)
                sendDataQueue.add(datas);
            if(!isSendData){//????????????????????????????????????????????????
                isSendData = true;
                mAnalysisHandler.sendEmptyMessage(ANALYSIS_HANDLER_SEND_DATA);
            }
        }
    }

    private synchronized void sendCommandBig(byte[] datas) {
        if (datas!=null){
            ClientManager.getClient().write(mMac,UUID.fromString(Config.char0)
                    ,UUID.fromString(Config.char1), datas,bleWriteResponse);
        }
    }

    @Override
    public void writeForUpdate() {
        sendCommand( CommandUtil.getCommandbyteArray(context,0x32, 8, 0, true));
    }

    @Override
    public void writeForGetSchedule() {
        sendCommand( CommandUtil.getCommandbyteArray(context,0x55, 8, 0, true));
    }

    @Override
    public void writeForAddSchedule(ScheduleData scheduleData){
        if (scheduleData==null)
            return;
        sendCommand(CommandUtil.getCommandByteSchedule(0x52, scheduleData));
    }
    @Override
    public void writeForDeleteSchedule(ScheduleData scheduleData){
        if (scheduleData==null)
            return;
        sendCommand(CommandUtil.getCommandByteSchedule(0x53, scheduleData));
    }
    @Override
    public void writeForEditSchedule(ScheduleData scheduleData){
        if (scheduleData==null)
            return;
        sendCommand(CommandUtil.getCommandByteSchedule(0x54, scheduleData));
    }

    @Override
    public void writeForSetAuto() {
        sendCommand(CommandUtil.getCommandbyteArray(context,0x3A, 36, 28, true));
    }

    @Override
    public void onDestroy() {
        iBluetoothState = null;
        Log.d("data******","ble onDestroy");
        ClientManager.getClient().disconnect(mMac);
        WatchManager.getInstance().destroy();
    }

    private void writeForSyncTime(){
        sendCommand(CommandUtil.getCommandbyteArray(context,0x30, 21, 13, true));
    }
    private void writeForSyncTimeStyle(){
        sendCommand(CommandUtil.getCommandbyteArray(context,0x44, 9, 1, true));
    }
    public void writeForUpdateUserInfo(){
        sendCommand(CommandUtil.getCommandbyteArray(context,0x31, 20, 12, true));
    }

    private void writeForSetLanguage(){
        sendCommand(CommandUtil.getCommandbyteArray(context,0x39, 13, 5, true));
    }

    public void writeForSetWeather(){
        sendCommand(CommandUtil.getCommandbyteArray(context,0x33, 13, 13, true));
        sendCommand(CommandUtil.getCommandbyteArray(context,0x43, 12, 4, true));
    }

    public void writeForSetUnit(){
        sendCommand(CommandUtil.getCommandbyteArray(context,0x41, 10, 2, true));
    }

    private void writeForSendPhoneCall(String num,String name){
        sendCommand( CommandUtil.getCommandbyteArray(0x49,num,name,false));
    }



    @Override
    public void writeForFindWatch(int state) {
        byte[] data = new byte[9];
        data[0] = (byte) 0xAA;
        data[1] = (byte) 0x38;
        data[2] = (byte) 1;
        data[3] = 0;
        data[4] = (byte) (0xF0);
        data[5] = (byte) (0xF0);
        data[6] = (byte) (0xF0);
        data[7] = (byte) (0xF0);
        data[8] = (byte) state;
        sendCommand(data);
    }


    @Override
    public void writeToSendNotify(String title, String label, int id) {
        sendCommand(CommandUtil.getCommandbyteArray(title,label,id));
    }

    @Override
    public void writeForSendDialFile(int type, byte clockId, int address, int num, byte[] data) {
        if (type == 3){
            sendCommandBig(CommandUtil.getCommandbyteDialFile(2,type,clockId,address,num,data));
        }else if (type == 4){
            sendCommandBig(CommandUtil.getCommandbyteDialFile(data.length+7,type,clockId,address,num,data));
        }else if (type == 5){
            sendCommandBig(CommandUtil.getCommandbyteDialFile(2,type,clockId,address,num,data));
        }else if (type == 6){
            sendCommandBig(CommandUtil.getCommandbyteDialFile(2,type,clockId,address,num,data));
        }else if (type == 7){
            sendCommandBig(CommandUtil.getCommandbyteDialFile(data.length+7,type,clockId,address,num,data));
        }else if (type == 8){
            sendCommandBig(CommandUtil.getCommandbyteDialFile(2,type,clockId,address,num,data));
        }
    }

    @Override
    public void writeForSendDialBackground(int type,int clockType,int clockIndex,int num,byte[] datas) {
        if (type == 0){
            sendCommand(CommandUtil.getCommandbytePicture(13,5,type,clockType,clockType,num,datas));
        }else if (type == 1){
            sendCommand( CommandUtil.getCommandbytePicture(datas.length+11,datas.length+3,type,clockType,clockType,num,datas));
        }else {
            sendCommand(CommandUtil.getCommandbytePicture(10,2,type,clockType,clockType,num,datas));
        }
    }


    private void initPhoneStateListener(boolean flag) {

        if (flag){
            tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if(tm != null) {
                try {
                    myPhoneCallListener.setCallListener(new MyPhoneCallListener.CallListener() {
                        @Override
                        public void onCallRinging(String num,String name) {
                            if (num == null&&name==null){
                                writeForSendPhoneCall(num,name);
                            } else if (!num.equals("")){
                                writeForSendPhoneCall(num,name);
                            }
                        }
                    });
                    // ??????????????????
                    tm.listen(myPhoneCallListener, PhoneStateListener.LISTEN_CALL_STATE);
                } catch(Exception e) {
                    // ????????????
                    LogUtil.getInstance().logd("DATA******",e.getMessage());
                }
            }
        }else {
            tm = null;
            myPhoneCallListener.setCallListener(null);
        }
    }


    private IDataResponse iDataResponse = new IDataResponse() {

        @Override
        public void onSaveStepDatas(ArrayList<BleStepModel> datas) {
            ArrayList<StepData> stepData = new ArrayList<>();
            for (int i = 0;i<datas.size();i++){
                HashMap<Integer,Integer> hashMap = datas.get(i).getStepInfo();
                StringBuffer stepString = new StringBuffer();
                for (int key:hashMap.keySet()){
                    stepString.append(String.format(Locale.ENGLISH,",%02d:%d",key,hashMap.get(key)));
                }
                stepData.add(new StepData(datas.get(i).getTime(),datas.get(i).getStep(),datas.get(i).getDistance()*10,
                        datas.get(i).getCalorie(),stepString.substring(1)));
                LogUtil.getInstance().logd("DATA******","???????????????????????????  "+"time = "+datas.get(i).getTime()+" ;step = "+datas.get(i).getStep()+
                        " ;distance = "+datas.get(i).getDistance()+" ;calorie = "+datas.get(i).getCalorie()+
                        " ;stepInfo = "+stepString.substring(1));
            }
            SaveDataUtil.newInstance().saveStepInfoDataListData1(stepData);
        }

        @Override
        public void onSaveDayStepDatas(ArrayList<StepData> datas) {
            SaveDataUtil.newInstance().saveStepDataListData(datas);
        }

        @Override
        public void onSaveHeartDatas(ArrayList<HeartData> datas) {
            SaveDataUtil.newInstance().saveHeartDataListData(datas);
        }

        @Override
        public void onSaveTempDatas(ArrayList<AnimalHeatData> datas) {
            SaveDataUtil.newInstance().saveAnimalHeatDataListData(datas);
        }

        @Override
        public void onSaveBpDatas(ArrayList<BloodPressureData> datas) {
            SaveDataUtil.newInstance().saveBloodPressureDataListData(datas);
        }

        @Override
        public void onSaveSleepDatas(ArrayList<SleepData> datas) {
            SaveDataUtil.newInstance().saveSleepDataListData(datas);
        }

        @Override
        public void onSaveRunDatas(ArrayList<SportData> datas) {
            SaveDataUtil.newInstance().saveSportDataListData(datas);
        }

        @Override
        public void onSaveBloodOxygenDatas(ArrayList<BloodOxygenData> datas) {
            SaveDataUtil.newInstance().saveBloodOxygenDataListData(datas);
        }

        @Override
        public void onGetDataIndex(String deviceNum, ArrayList<Integer> dataIndex) {
            if (indexData==null){
                indexData = dataIndex;
                if (indexData.size()>0){
                    isSync = true;
                }else {
                    indexData = null;
                }
            }
        }

        @Override
        public void onCamera(int flag) {
            LogUtil.getInstance().logd("data******","??????????????????");
            SportWatchAppFunctionConfigDTO data =
                    LoadDataUtil.newInstance().getSportConfig(MathUtil.newInstance().getUserId(context));
            if (data==null)
                return;
            LogUtil.getInstance().logd("data******","cameraSwitch = "+data.cameraSwitch+" ;flag = "+flag);
            if (data.cameraSwitch&& !ProgressHudModel.newInstance().isShow())
                if (flag == 1){//????????????
                    ARouter.getInstance().build(RouterPathConst.PATH_ACTIVITY_USER_CAMERA)
                            .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP|Intent.FLAG_ACTIVITY_NEW_TASK)
                            .navigation(context.getApplicationContext());

                }else if (flag == 0){//????????????
                    Intent intent = new Intent(BroadcastConst.UPDATE_UI_VIEW);
                    intent.putExtra("state",false);
                    context.sendBroadcast(intent);
                }else {//??????
                    Intent intent = new Intent(BroadcastConst.UPDATE_UI_VIEW);
                    intent.putExtra("state",true);
                    context.sendBroadcast(intent);
                }
        }

        @Override
        public void findPhone(int flag) {
            final AudioManager am = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
            if (flag == 1){
                LogUtil.getInstance().logd("data******","??????");
                MathUtil.newInstance().speaker(am);
                starVibrate(new long[]{500,500,500});
                volume  = am.getStreamVolume(STREAM_MUSIC);//???????????????????????????
                am.setStreamVolume (STREAM_MUSIC, am.getStreamMaxVolume(STREAM_MUSIC), FLAG_PLAY_SOUND);//????????????????????????
                if (mediaPlayer==null){
                    mediaPlayer = MediaPlayer.create(context, R.raw.dang_ring);
                    mediaPlayer.start();
                    mediaPlayer.setVolume(1f,1f);
                    mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            stopVibrate();
                            am.setStreamVolume (STREAM_MUSIC, volume, FLAG_PLAY_SOUND);//???????????????????????????????????????
                            mediaPlayer = null;
                            MathUtil.newInstance().offSpeaker(am);
                        }
                    });
                }
            }else{
                LogUtil.getInstance().logd("data******","??????");
                if (mediaPlayer!=null){
                    mediaPlayer.stop();
                    stopVibrate();
                    am.setStreamVolume (STREAM_MUSIC, volume, FLAG_PLAY_SOUND);//???????????????????????????????????????
                    mediaPlayer = null;
                    MathUtil.newInstance().offSpeaker(am);
                }
            }
        }

        @Override
        public void updateUserInfo(final UserModel userModel) {

            final UserModel userModelSql = LoadDataUtil.newInstance().getUserInfo(MathUtil.newInstance().getUserId(context));
            if (userModel.weightBritish!=userModelSql.weightBritish||userModel.weight!=userModelSql.weight||
                    userModel.heightBritish!=userModelSql.heightBritish||userModel.height!=userModelSql.height||
                    userModel.sex!=userModelSql.sex){

                PostJsonBuilder builder = OkHttpUtils
                        .jpost()
                        .addParams("userName",userModel.userName)
                        .addParams("lastName","")
                        .addParams("firstName","")
                        .addParams("sex",userModel.sex+"")
                        .addParams("birthday",userModelSql.birthday)
                        .addParams("nation","")
                        .addParams("height",userModel.height+"")
                        .addParams("weight",userModel.weight+"")
                        .addParams("heightBritish",userModel.heightBritish+"")
                        .addParams("weightBritish",userModel.weightBritish+"")
                        .addParams("blood","")
                        .addInterceptor(new TokenInterceptor());


                HttpClientUtils.newInstance().buildRequest(builder, "v2/user/updateUserInfo", new GenericsCallback<BaseApi>(new JsonGenericsSerializator()) {
                    @Override
                    public void onError(Call call, Exception e, int id) {

                    }

                    @Override
                    public void onResponse(BaseApi response, int id) {
                        if (response.getCode() == 200){
                            userModelSql.weight = userModel.weight;
                            userModelSql.weightBritish = userModel.weightBritish;
                            userModelSql.height = userModel.height;
                            userModelSql.heightBritish = userModel.heightBritish;
                            userModelSql.sex = userModel.sex;
                            userModelSql.update();
                        }
                    }
                });

            }
            if (userModel.unit!=userModelSql.unit||userModel.tempUnit!=userModelSql.tempUnit){
                PostJsonBuilder builder = OkHttpUtils
                        .jpost()
                        .addParams("unit",userModel.unit+"")
                        .addParams("tempUnit",userModel.tempUnit+"")
                        .addInterceptor(new TokenInterceptor());
                HttpClientUtils.newInstance().buildRequest(builder, "v2/user/setUnit", new GenericsCallback<BaseApi>(new JsonGenericsSerializator()) {
                    @Override
                    public void onError(Call call, Exception e, int id) {

                    }

                    @Override
                    public void onResponse(BaseApi response, int id) {
                        if (response.getCode() == 200){
                            userModelSql.unit = userModel.unit;
                            userModelSql.tempUnit = userModel.tempUnit;
                            userModelSql.update();
                        }
                    }
                });
            }
            if (userModel.stepsPlan!=userModelSql.stepsPlan){
                PostJsonBuilder builder = OkHttpUtils
                        .jpost()
                        .addParams("stepsPlan",userModelSql.stepsPlan+"")
                        .addInterceptor(new TokenInterceptor());
                HttpClientUtils.newInstance().buildRequest(builder, "v2/user/updateStepsPlan", new GenericsCallback<BaseApi>(new JsonGenericsSerializator()) {
                    @Override
                    public void onError(Call call, Exception e, int id) {

                    }

                    @Override
                    public void onResponse(BaseApi response, int id) {
                        if (response.getCode() == 200){
                            userModelSql.stepsPlan = userModel.stepsPlan;
                            userModelSql.update();
                        }
                    }
                });
            }

        }

        @Override
        public void onSaveScheduleData(ArrayList<ScheduleData> scheduleDataArrayList) {
            if (scheduleDataArrayList==null||scheduleDataArrayList.size()==0)
                return;
            SaveDataUtil.newInstance().saveScheduleListData(scheduleDataArrayList);
        }

        @Override
        public void onScheduleRefresh(boolean refresh) {
            if (refresh){
                writeForGetSchedule();
            }else {
                context.sendBroadcast(new Intent(BroadcastConst.UPDATE_UI_VIEW));
            }
        }

        @Override
        public void updateOtaProgress(int type,int state, int address) {
//            if (iOtaResponse!=null){
//                if (type==0){
//                    iOtaResponse.onSendFail();
//                }else if (type == 1){
//                    iOtaResponse.onSendProgress();
//                }else if (type == 2){
//                    iOtaResponse.onSendSccuess();
//                }else {
//                    iOtaResponse.onStartToSendFile(state,address);
//                }
//            }

        }

        @Override
        public void onMusicControl(int cmd, int voiceValue) {
            if (cmd == 0){//??????
                MusicUtil.getSingle().controlMusic(127);
            }else if (cmd == 1){//??????
                MusicUtil.getSingle().controlMusic(126);
            }else if (cmd == 2){//?????????
                MusicUtil.getSingle().controlMusic(88);
            }else if (cmd == 3){//?????????
                MusicUtil.getSingle().controlMusic(87);
            }else if (cmd == 4){//??????
                MusicUtil.getSingle().setVoiceValue(voiceValue);
            }
        }

        @Override
        public void endCall() {
//            toEndCall(MyApplication.getInstance().getApplicationContext());
        }

        @Override
        public void onScheduleCallback(int type, int state) {
            Intent intent = new Intent(BroadcastConst.UPDATE_UI_VIEW);
            intent.putExtra("type",type);
            intent.putExtra("state",state);
            context.sendBroadcast(intent);
        }

        @Override
        public void sendDialFinish() {
            Intent intent = new Intent(BroadcastConst.UPDATE_DIAL_STATE);
            intent.putExtra("command", SendFileConst.FINISH);
            context.sendBroadcast(intent);
        }

        @Override
        public void sendDialError() {
            Intent intent = new Intent(BroadcastConst.UPDATE_DIAL_STATE);
            intent.putExtra("command", SendFileConst.ERROR);
            context.sendBroadcast(intent);
        }

        @Override
        public void sendDialStart(int address) {
            Intent intent = new Intent(BroadcastConst.UPDATE_DIAL_STATE);
            intent.putExtra("command", SendFileConst.START_SEND);
            intent.putExtra("address",address);
            context.sendBroadcast(intent);
        }

        @Override
        public void sendDialContinue(int page) {
            Intent intent = new Intent(BroadcastConst.UPDATE_DIAL_STATE);
            intent.putExtra("command", SendFileConst.CONTINUE);
            intent.putExtra("page",page);
            context.sendBroadcast(intent);
        }

        @Override
        public void sendDialProgress() {
            Intent intent = new Intent(BroadcastConst.UPDATE_DIAL_STATE);
            intent.putExtra("command", SendFileConst.PROGRESS);
            context.sendBroadcast(intent);
        }

        @Override
        public void sendBackgroundFinish() {
            Intent intent = new Intent(BroadcastConst.UPDATE_BACKGROUND_STATE);
            intent.putExtra("command", SendFileConst.FINISH);
            context.sendBroadcast(intent);
        }

        @Override
        public void sendBackgroundError() {
            Intent intent = new Intent(BroadcastConst.UPDATE_BACKGROUND_STATE);
            intent.putExtra("command", SendFileConst.ERROR);
            context.sendBroadcast(intent);
        }

        @Override
        public void sendBackgroundStart() {
            Intent intent = new Intent(BroadcastConst.UPDATE_BACKGROUND_STATE);
            intent.putExtra("command", SendFileConst.START_SEND);
            context.sendBroadcast(intent);
        }

        @Override
        public void startSendFile() {
            Intent intent = new Intent(BroadcastConst.UPDATE_BACKGROUND_STATE);
            intent.putExtra("command", SendFileConst.SEND_BIN);
            context.sendBroadcast(intent);
        }

        @Override
        public void sendBackgroundProgress() {
            Intent intent = new Intent(BroadcastConst.UPDATE_BACKGROUND_STATE);
            intent.putExtra("command", SendFileConst.PROGRESS);
            context.sendBroadcast(intent);
        }

        @Override
        public void pairBluetooth(String mac) {
            LogUtil.getInstance().logd("data******","mac = "+mac);
            if (mac.equals("00:00:00:00:00:00"))
                return;
            BluetoothAdapter btAdapt = BluetoothAdapter.getDefaultAdapter();
            try {
                if (mac!=null) {
                    BluetoothDevice btDev = btAdapt.getRemoteDevice(mac);
                    Boolean returnValue = false;
                    if (btDev.getBondState() == BluetoothDevice.BOND_NONE) {
                        //????????????????????????BluetoothDevice.createBond(BluetoothDevice remoteDevice);
                        Method createBondMethod = BluetoothDevice.class
                                .getMethod("createBond");
                        Log.d("DATA******", "????????????");
                        returnValue = (Boolean) createBondMethod.invoke(btDev);
                    }
                }
            }catch (IllegalArgumentException e){
                Log.e("DATA******",e.getMessage());
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    private void starVibrate(long[] pattern) {
        Vibrator vib = (Vibrator) context.getSystemService(Service.VIBRATOR_SERVICE);
        vib.vibrate(pattern, 1);
    }


    private void stopVibrate() {
        Vibrator vib = (Vibrator) context.getSystemService(Service.VIBRATOR_SERVICE);
        vib.cancel();
    }
}
