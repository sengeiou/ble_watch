package com.szip.blewatch.Activity.main;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.szip.healthy.ModuleMain.HealthyFragment;
import com.szip.sport.ModuleMain.SportFragment;
import com.szip.user.ModuleMain.MineFragment;

import java.util.ArrayList;

public class MainAdapter extends FragmentStateAdapter {


    private ArrayList<Fragment> fragmentArrayList;

    public MainAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
        initFragment();
    }

    private void initFragment() {
        fragmentArrayList = new ArrayList<>();
        fragmentArrayList.add(new HealthyFragment());
        fragmentArrayList.add(new SportFragment());
        fragmentArrayList.add(new MineFragment());
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return fragmentArrayList.get(position);
    }

    @Override
    public int getItemCount() {
        return fragmentArrayList.size();
    }
}
