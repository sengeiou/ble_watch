package com.szip.blewatch.base.View.character;

import android.view.View;

import com.szip.blewatch.base.R;
import com.szip.blewatch.base.Util.DateUtil;


import java.util.ArrayList;
import java.util.List;

/**
 * @version 0.1 king 2015-11
 * @version 0.2 imkarl 2017-9
 */
final class WheelOptions {
    private final CharacterPickerView view;
    private LoopView wv_option1;
    private LoopView wv_option2;
    private LoopView wv_option3;

    private List<String> mOptions1Items;
    private List<List<String>> mOptions2Items;
    private List<List<List<String>>> mOptions3Items;

    private List<String> mOptions1Items0;
    private List<String> mOptions2Items0;
    private List<String> mOptions3Items0;
    private OnOptionChangedListener mOnOptionChangedListener;


    private int yearPos;
    private int monthPos;

    public View getView() {
        return view;
    }

    public WheelOptions(CharacterPickerView view) {
        super();
        this.view = view;
    }

    public void setOnOptionChangedListener(OnOptionChangedListener listener) {
        this.mOnOptionChangedListener = listener;
    }

    public void setPicker(ArrayList<String> optionsItems) {
        setPicker(optionsItems, null, null);
    }

    public void setPicker(List<String> options1Items,
                          List<List<String>> options2Items) {
        setPicker(options1Items, options2Items, null);
    }

    public void setPicker(List<String> options1Items,
                          List<List<String>> options2Items,
                          List<List<List<String>>> options3Items) {
        this.mOptions1Items = options1Items == null ? new ArrayList<String>() : options1Items;
        this.mOptions2Items = options2Items == null ? new ArrayList<List<String>>() : options2Items;
        this.mOptions3Items = options3Items == null ? new ArrayList<List<List<String>>>() : options3Items;
        // 选项1
        wv_option1 = (LoopView) view.findViewById(R.id.j_options1);
        wv_option1.setItems(mOptions1Items);// 设置显示数据
        //设置是否循环播放
        wv_option1.setNotLoop();

        //滚动监听
        wv_option1.setListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(int index) {
                if (index == -1) {
                    return;
                }
                doItemChange();
                if (mOptions2Items.isEmpty()) {
                    return;
                }

                wv_option2.setItems(mOptions2Items.get(index));
                wv_option2.setCurrentPosition(0);
            }
        });

        // 选项2
        wv_option2 = (LoopView) view.findViewById(R.id.j_options2);
        if (!mOptions2Items.isEmpty()) {
            wv_option2.setItems(mOptions2Items.get(0));// 设置显示数据
            //设置是否循环播放
            wv_option2.setNotLoop();
            //滚动监听
            wv_option2.setListener(new OnItemSelectedListener() {
                @Override
                public void onItemSelected(int index) {
                    if (index == -1) {
                        return;
                    }
                    doItemChange();
                    if (mOptions3Items.isEmpty()) {
                        return;
                    }

                    if (wv_option1.getSelectedItem() < mOptions3Items.size()) {
                        List<List<String>> allItems3 = mOptions3Items.get(wv_option1.getSelectedItem());
                        if (index >= allItems3.size()) {
                            index = 0;
                        }
                        wv_option3.setItems(allItems3.get(index));
                        wv_option3.setCurrentPosition(0);
                    }
                }
            });
        }

        // 选项3
        wv_option3 = (LoopView) view.findViewById(R.id.j_options3);
        if (!mOptions3Items.isEmpty()) {
            wv_option3.setItems(mOptions3Items.get(0).get(0));// 设置显示数据
            wv_option3.setCurrentPosition(0);// 初始化时显示的数据
            //设置是否循环播放
            wv_option3.setNotLoop();
            //滚动监听
            wv_option3.setListener(new OnItemSelectedListener() {
                @Override
                public void onItemSelected(int index) {
                    doItemChange();
                }
            });
        }

        if (mOptions2Items.isEmpty())
            view.findViewById(R.id.j_layout2).setVisibility(View.GONE);
        if (mOptions3Items.isEmpty())
            view.findViewById(R.id.j_layout3).setVisibility(View.GONE);

        // 初始化时显示的数据
        setCurrentPositions(0, 0, 0);
    }

    public void setPickerForDate(List<String> options1Items) {
        this.mOptions1Items = options1Items == null ? new ArrayList<String>() : options1Items;
        // 选项1
        wv_option1 = (LoopView) view.findViewById(R.id.j_options1);
        wv_option1.setItems(mOptions1Items);// 设置显示数据
        //设置是否循环播放
        wv_option1.setNotLoop();

        //滚动监听
        wv_option1.setListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(int index) {
                if (index == -1) {
                    return;
                }

                yearPos = index;
                wv_option3.setItems(DateUtil.getDayList(1930+yearPos,wv_option2.getSelectedItem()+1));
                wv_option3.setCurrentPosition(0);

            }
        });

        // 选项2
        wv_option2 = (LoopView) view.findViewById(R.id.j_options2);

            wv_option2.setItems(DateUtil.getMonthList());// 设置显示数据
            //设置是否循环播放
            wv_option2.setNotLoop();
            //滚动监听
            wv_option2.setListener(new OnItemSelectedListener() {
                @Override
                public void onItemSelected(int index) {
                    if (index == -1) {
                        return;
                    }

                    monthPos = index;
                    wv_option3.setItems(DateUtil.getDayList(1930+wv_option1.getSelectedItem(),index+1));
                    wv_option3.setCurrentPosition(0);
                }
            });


        // 选项3
        wv_option3 = (LoopView) view.findViewById(R.id.j_options3);
            wv_option3.setItems(DateUtil.getDayList(1930,1));// 设置显示数据
            wv_option3.setCurrentPosition(0);// 初始化时显示的数据
            //设置是否循环播放
            wv_option3.setNotLoop();
            //滚动监听
            wv_option3.setListener(new OnItemSelectedListener() {
                @Override
                public void onItemSelected(int index) {
//                    doItemChange();
                }
            });


        view.findViewById(R.id.j_layout2).setVisibility(View.VISIBLE);
        view.findViewById(R.id.j_layout3).setVisibility(View.VISIBLE);

        // 初始化时显示的数据
        setCurrentPositions(0, 0, 0);
    }

    public void setPickerWithoutLink(List<String> options1Items,
                          List<String> options2Items,
                          List<String> options3Items) {
        this.mOptions1Items0 = options1Items == null ? new ArrayList<String>() : options1Items;
        this.mOptions2Items0 = options2Items == null ? new ArrayList<String>() : options2Items;
        this.mOptions3Items0 = options3Items == null ? new ArrayList<String>() : options3Items;
        // 选项1
        wv_option1 = (LoopView) view.findViewById(R.id.j_options1);
        wv_option1.setItems(mOptions1Items0);// 设置显示数据
        //设置是否循环播放
        wv_option1.setNotLoop();

        //滚动监听
        wv_option1.setListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(int index) {
                if (index == -1) {
                    return;
                }
                doItemChange();
//                if (mOptions2Items0.isEmpty()) {
//
//                    return;
//                }

//                wv_option2.setItems(mOptions2Items.get(index));
//                wv_option2.setCurrentPosition(0);
            }
        });

        // 选项2
        wv_option2 = (LoopView) view.findViewById(R.id.j_options2);
        if (!mOptions2Items0.isEmpty()) {
            wv_option2.setItems(mOptions2Items0);// 设置显示数据
            //设置是否循环播放
            wv_option2.setNotLoop();
            //滚动监听
            wv_option2.setListener(new OnItemSelectedListener() {
                @Override
                public void onItemSelected(int index) {
                    if (index == -1) {
                        return;
                    }
                    if (mOptions3Items0.isEmpty()) {
                        doItemChange();
                        return;
                    }

//                    if (wv_option1.getSelectedItem() < mOptions3Items.size()) {
//                        List<List<String>> allItems3 = mOptions3Items.get(wv_option1.getSelectedItem());
//                        if (index >= allItems3.size()) {
//                            index = 0;
//                        }
//                        wv_option3.setItems(allItems3.get(index));
//                        wv_option3.setCurrentPosition(0);
//                    }
                }
            });
        }

        // 选项3
        wv_option3 = (LoopView) view.findViewById(R.id.j_options3);
        if (!mOptions3Items0.isEmpty()) {
            wv_option3.setItems(mOptions3Items0);// 设置显示数据
            wv_option3.setCurrentPosition(0);// 初始化时显示的数据
            //设置是否循环播放
            wv_option3.setNotLoop();
            //滚动监听
            wv_option3.setListener(new OnItemSelectedListener() {
                @Override
                public void onItemSelected(int index) {
                    doItemChange();
                }
            });
        }

        if (mOptions2Items0.isEmpty())
            view.findViewById(R.id.j_layout2).setVisibility(View.GONE);
        if (mOptions3Items0.isEmpty())
            view.findViewById(R.id.j_layout3).setVisibility(View.GONE);

        // 初始化时显示的数据
        setCurrentPositions(0, 0, 0);
    }

    /**
     * 选中项改变
     */
    private void doItemChange() {
        if (mOnOptionChangedListener != null) {
            int option1 = wv_option1.getSelectedItem();
            int option2 = wv_option2.getSelectedItem();
            int option3 = wv_option3.getSelectedItem();
            mOnOptionChangedListener.onOptionChanged(option1, option2, option3);
        }
    }

    /**
     * 设置是否循环滚动
     */
    public void setCyclic(boolean cyclic) {
        wv_option1.setLoop(cyclic);
        wv_option2.setLoop(cyclic);
        wv_option3.setLoop(cyclic);
    }

    @Deprecated
    public int[] getCurrentItems() {
        int[] currentItems = new int[3];
        currentItems[0] = wv_option1.getSelectedItem();
        currentItems[1] = wv_option2.getSelectedItem();
        currentItems[2] = wv_option3.getSelectedItem();
        return currentItems;
    }

    /**
     * 返回当前选中的结果对应的位置数组 因为支持三级联动效果，分三个级别索引，0，1，2
     */
    public int[] getCurrentPositions() {
        int[] currentItems = new int[3];
        currentItems[0] = wv_option1.getSelectedItem();
        currentItems[1] = wv_option2.getSelectedItem();
        currentItems[2] = wv_option3.getSelectedItem();
        return currentItems;
    }

    public void setCurrentPositions(int option1, int option2, int option3) {
        if (option1 < 0) {
            option1 = 0;
        }
        if (option2 < 0) {
            option2 = 0;
        }
        if (option3 < 0) {
            option3 = 0;
        }

        if (wv_option1.getSelectedItem() == -1) {
            wv_option1.setInitPosition(option1);
            wv_option2.setInitPosition(option2);
            wv_option3.setInitPosition(option3);
        } else {
            wv_option1.setCurrentPosition(option1);
            wv_option2.setCurrentPosition(option2);
            wv_option3.setCurrentPosition(option3);
        }
    }
}
