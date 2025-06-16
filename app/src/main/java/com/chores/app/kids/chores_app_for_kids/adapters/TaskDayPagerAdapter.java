package com.chores.app.kids.chores_app_for_kids.adapters;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.chores.app.kids.chores_app_for_kids.fragments.DayTaskFragment;

import java.time.LocalDate;

public class TaskDayPagerAdapter extends FragmentStatePagerAdapter {

    private static final int PAGE_COUNT = 1000;
    private LocalDate baseDate;
    private String selectedChildId;

    public TaskDayPagerAdapter(FragmentManager fm, LocalDate baseDate) {
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        this.baseDate = baseDate;
    }

    public void setSelectedChildId(String childId) {
        this.selectedChildId = childId;
        notifyDataSetChanged();
    }

    @Override
    public Fragment getItem(int position) {
        int offset = position - PAGE_COUNT / 2;
        LocalDate date = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O && baseDate != null) {
            date = baseDate.plusDays(offset);
        }
        return DayTaskFragment.newInstance(date, selectedChildId);
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

    @Override
    public int getItemPosition(Object object) {
        // Force recreation of fragments when child changes
        return POSITION_NONE;
    }
}