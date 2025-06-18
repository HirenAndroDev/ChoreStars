package com.chores.app.kids.chores_app_for_kids.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.chores.app.kids.chores_app_for_kids.R;
import com.chores.app.kids.chores_app_for_kids.activities.CreateTaskActivity;
import com.chores.app.kids.chores_app_for_kids.activities.StarBalanceActivity;
import com.chores.app.kids.chores_app_for_kids.adapters.TaskDayPagerAdapter;
import com.chores.app.kids.chores_app_for_kids.dialogs.KidProfilesParentDialog;
import com.chores.app.kids.chores_app_for_kids.models.ChildProfile;
import com.chores.app.kids.chores_app_for_kids.models.WeekDay;
import com.chores.app.kids.chores_app_for_kids.utils.FirebaseHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class TaskManageFragment extends Fragment implements KidProfilesParentDialog.OnKidSelectedListener {

    private static final String TAG = "TaskManageFragment";
    private static final String SELECTED_KID_PREF = "selected_kid";
    private static final String SELECTED_KID_ID_PREF = "selected_kid_id";
    private static final int CENTER_POSITION = 500;

    private ViewPager vpTask;
    private ImageView ivLeftArrow, ivRightArrow;
    private TextView tvWeekLabel, tvKidName, tvStarsBalance;
    private CircleImageView ivKidProfile;
    private LinearLayout layoutKidProfile, layoutStarsBalance;
    private FloatingActionButton fabAddTask;
    private View calendarContainer;

    private List<WeekDay> weekDays;
    private LocalDate selectedDate;
    private int weekOffset = 0;
    private View rootView;
    private LinearLayout[] dayContainers = new LinearLayout[7];
    private TextView[] dayNumberViews = new TextView[7];
    private View calendarRowView;

    // Current selected kid
    private ChildProfile selectedKid;
    private List<ChildProfile> kidProfiles;
    private FirebaseHelper firebaseHelper;
    private TaskDayPagerAdapter pagerAdapter;

    public TaskManageFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_task_manage, container, false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            selectedDate = LocalDate.now();
        }

        firebaseHelper = new FirebaseHelper();
        kidProfiles = new ArrayList<>();

        initViews();
        setupKidProfileClick();
        setupWeekDays();
        setupWeekNavigation();
        setupViewPager();
        setupFAB();
        loadKidProfiles();

        return rootView;
    }

    private void initViews() {
        ivLeftArrow = rootView.findViewById(R.id.ivLeftArrow);
        ivRightArrow = rootView.findViewById(R.id.ivRightArrow);
        tvWeekLabel = rootView.findViewById(R.id.tvWeekLabel);
        tvKidName = rootView.findViewById(R.id.tvKidName);
        tvStarsBalance = rootView.findViewById(R.id.tvStarsBalance);
        ivKidProfile = rootView.findViewById(R.id.ivKidProfile);
        layoutKidProfile = rootView.findViewById(R.id.layoutKidProfile);
        layoutStarsBalance = rootView.findViewById(R.id.layoutStarsBalance);
        calendarContainer = rootView.findViewById(R.id.calendarContainer);
        vpTask = rootView.findViewById(R.id.vpTask);
        fabAddTask = rootView.findViewById(R.id.fabAddTask);

        updateTodayLabel();
    }

    private void setupKidProfileClick() {
        layoutKidProfile.setOnClickListener(v -> {
            KidProfilesParentDialog dialog = new KidProfilesParentDialog(requireContext(), kidProfiles, selectedKid);
            dialog.setOnKidSelectedListener(this);
            dialog.show();
        });

        // Add click listener for star balance layout
        layoutStarsBalance.setOnClickListener(v -> {
            if (selectedKid != null) {
                Intent intent = new Intent(requireContext(), StarBalanceActivity.class);
                intent.putExtra(StarBalanceActivity.EXTRA_CHILD_ID, selectedKid.getChildId());
                intent.putExtra(StarBalanceActivity.EXTRA_CHILD_NAME, selectedKid.getName());
                intent.putExtra(StarBalanceActivity.EXTRA_CHILD_PROFILE_URL, selectedKid.getProfileImageUrl());
                intent.putExtra(StarBalanceActivity.EXTRA_CURRENT_BALANCE, selectedKid.getStarBalance());
                startActivity(intent);
            }
        });
    }

    private void setupFAB() {
        fabAddTask.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), CreateTaskActivity.class);
            if (selectedKid != null) {
                intent.putExtra("selectedKidId", selectedKid.getChildId());
                intent.putExtra("selectedKidName", selectedKid.getName());
            }
            startActivity(intent);
        });
    }

    private void loadKidProfiles() {
        firebaseHelper.getChildProfiles(new FirebaseHelper.OnChildProfilesLoadedListener() {
            @Override
            public void onChildProfilesLoaded(List<ChildProfile> profiles) {
                // Check if fragment is still attached before updating UI
                if (!isAdded() || getActivity() == null) {
                    Log.w(TAG, "Fragment not attached when profiles loaded, ignoring update");
                    return;
                }

                kidProfiles.clear();
                kidProfiles.addAll(profiles);

                if (!kidProfiles.isEmpty()) {
                    if (selectedKid == null) {
                        // Load saved selected kid
                        String savedKidId = getSavedSelectedKidId();

                        if (savedKidId != null) {
                            for (ChildProfile profile : kidProfiles) {
                                if (profile.getChildId().equals(savedKidId)) {
                                    selectedKid = profile;
                                    break;
                                }
                            }
                        }

                        // If no saved selection or saved kid not found, use first kid
                        if (selectedKid == null && !kidProfiles.isEmpty()) {
                            selectedKid = kidProfiles.get(0);
                            saveSelectedKidProfile(selectedKid.getChildId()); // Save the default selection
                        }
                    } else {
                        // Verify that the currently selected kid is still in the profiles list
                        boolean kidStillExists = false;
                        for (ChildProfile profile : kidProfiles) {
                            if (profile.getChildId().equals(selectedKid.getChildId())) {
                                selectedKid = profile; // Update with fresh data
                                kidStillExists = true;
                                break;
                            }
                        }

                        // If selected kid no longer exists, select first available
                        if (!kidStillExists) {
                            selectedKid = kidProfiles.get(0);
                            saveSelectedKidProfile(selectedKid.getChildId());
                        }
                    }

                    updateKidProfileUI();
                    refreshTasksForSelectedKid();
                }
            }

            @Override
            public void onError(String error) {
                if (isAdded()) {
                    Log.e(TAG, "Error loading kid profiles: " + error);
                }
            }
        });
    }

    public void updateKidProfileUI() {
        if (selectedKid == null || !isAdded() || getActivity() == null) return;

        tvKidName.setText(selectedKid.getName());
        tvStarsBalance.setText(String.valueOf(selectedKid.getStarBalance()));

        // Load profile image
        if (selectedKid.getProfileImageUrl() != null && !selectedKid.getProfileImageUrl().isEmpty()) {
            Glide.with(this)
                    .load(selectedKid.getProfileImageUrl())
                    .circleCrop()
                    .into(ivKidProfile);
        } else {
            ivKidProfile.setImageResource(R.drawable.default_avatar);
        }
    }

    private void refreshTasksForSelectedKid() {
        if (pagerAdapter != null && selectedKid != null) {
            pagerAdapter.setSelectedChildId(selectedKid.getChildId());
            // Also refresh the current fragment if it exists and fragment is attached
            if (vpTask != null && isAdded() && getActivity() != null) {
                try {
                    int currentItem = vpTask.getCurrentItem();
                    Fragment currentFragment = getChildFragmentManager().findFragmentByTag("android:switcher:" + vpTask.getId() + ":" + currentItem);
                    if (currentFragment instanceof DayTaskFragment) {
                        ((DayTaskFragment) currentFragment).setChildId(selectedKid.getChildId());
                    }
                } catch (IllegalStateException e) {
                    Log.w(TAG, "Fragment not attached when trying to refresh tasks: " + e.getMessage());
                    // Schedule refresh for when fragment is properly attached
                    if (getView() != null) {
                        getView().post(() -> {
                            if (isAdded() && getActivity() != null) {
                                refreshTasksForSelectedKid();
                            }
                        });
                    }
                }
            }
        }
    }

    private void updateTodayLabel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LocalDate today = LocalDate.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, MMM d", Locale.getDefault());
            String todayText = today.equals(selectedDate != null ? selectedDate : today) ?
                    "Today, " + today.format(DateTimeFormatter.ofPattern("MMM d")) :
                    selectedDate.format(formatter);
          //  tvTodayLabel.setText(todayText);
        }
    }

    private void setupViewPager() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            pagerAdapter = new TaskDayPagerAdapter(getChildFragmentManager(), LocalDate.now());
        }
        vpTask.setAdapter(pagerAdapter);
        vpTask.setCurrentItem(CENTER_POSITION);

        vpTask.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            LocalDate lastDate = selectedDate;

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    int offset = position - CENTER_POSITION;
                    LocalDate newDate = LocalDate.now().plusDays(offset);

                    int lastWeekNumber = getWeekNumber(lastDate);
                    int newWeekNumber = getWeekNumber(newDate);
                    int lastYear = lastDate.getYear();
                    int newYear = newDate.getYear();

                    if (lastWeekNumber != newWeekNumber || lastYear != newYear) {
                        LocalDate today = LocalDate.now();
                        LocalDate startOfWeekToday = getStartOfWeek(today);
                        LocalDate startOfWeekNew = getStartOfWeek(newDate);

                        long weeksBetween = ChronoUnit.WEEKS.between(startOfWeekToday, startOfWeekNew);
                        weekOffset = (int) weeksBetween;

                        updateWeekLabel();
                        updateWeekDays();
                    }

                    lastDate = newDate;
                    selectedDate = newDate;
                    updateSelectedDayInWeekDays(selectedDate);
                    updateTodayLabel();
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    @android.annotation.TargetApi(Build.VERSION_CODES.O)
    private int getWeekNumber(LocalDate date) {
        WeekFields weekFields = WeekFields.of(Locale.getDefault());
        return date.get(weekFields.weekOfWeekBasedYear());
    }

    @android.annotation.TargetApi(Build.VERSION_CODES.O)
    private LocalDate getStartOfWeek(LocalDate date) {
        return date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    private void setupWeekNavigation() {
        ivLeftArrow.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (selectedDate == null) {
                    selectedDate = LocalDate.now();
                }
                selectedDate = selectedDate.minusWeeks(1);
                weekOffset--;
                updateWeekLabel();
                updateWeekDays();
                updateSelectedDayInWeekDays(selectedDate);
                updateTodayLabel();

                int position = CENTER_POSITION + (int) ChronoUnit.DAYS.between(LocalDate.now(), selectedDate);
                vpTask.setCurrentItem(position, true);
            }
        });

        ivRightArrow.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (selectedDate == null) {
                    selectedDate = LocalDate.now();
                }
                selectedDate = selectedDate.plusWeeks(1);
                weekOffset++;
                updateWeekLabel();
                updateWeekDays();
                updateSelectedDayInWeekDays(selectedDate);
                updateTodayLabel();

                int position = CENTER_POSITION + (int) ChronoUnit.DAYS.between(LocalDate.now(), selectedDate);
                vpTask.setCurrentItem(position, true);
            }
        });

        updateWeekLabel();
    }

    private void updateWeekLabel() {
        String label;
        if (weekOffset == 0) {
            label = "This Week";
        } else if (weekOffset > 0) {
            label = "In " + weekOffset + " Week" + (weekOffset > 1 ? "s" : "");
        } else {
            int absOffset = Math.abs(weekOffset);
            label = absOffset + " Week" + (absOffset > 1 ? "s" : "") + " Ago";
        }
        tvWeekLabel.setText(label);
    }

    private void updateSelectedDayInWeekDays(LocalDate selectedDate) {
        if (weekDays == null || selectedDate == null || dayContainers[0] == null) return;

        boolean anySelected = false;

        for (int i = 0; i < weekDays.size(); i++) {
            WeekDay day = weekDays.get(i);
            boolean isSelected = day.getDate() != null && day.getDate().equals(selectedDate);
            day.setSelected(isSelected);

            if (isSelected) anySelected = true;

            if (day.isSelected()) {
                dayContainers[i].setBackgroundResource(R.drawable.bg_day_selected);
                dayContainers[i].setPadding(12, 14, 12, 14);
                ((TextView) dayContainers[i].getChildAt(0)).setTextColor(getResources().getColor(android.R.color.white));
                dayNumberViews[i].setBackgroundResource(R.drawable.bg_day_circle_selected);
                dayNumberViews[i].setTextColor(getResources().getColor(android.R.color.black));

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    dayContainers[i].setElevation(8f);
                }
            } else {
                dayContainers[i].setBackgroundResource(0);
                ((TextView) dayContainers[i].getChildAt(0)).setTextColor(getResources().getColor(android.R.color.black));
                dayNumberViews[i].setBackgroundResource(R.drawable.bg_day_circle_normal);
                dayNumberViews[i].setTextColor(getResources().getColor(android.R.color.black));

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    dayContainers[i].setElevation(0f);
                }
            }
        }

        if (!anySelected && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LocalDate today = LocalDate.now();
            LocalDate startOfWeekToday = getStartOfWeek(today);
            LocalDate startOfWeekSelected = getStartOfWeek(selectedDate);
            weekOffset = (int) ChronoUnit.WEEKS.between(startOfWeekToday, startOfWeekSelected);

            updateWeekLabel();
            updateWeekDays();
            updateSelectedDayInWeekDays(selectedDate);
        }
    }

    private void setupWeekDays() {
        calendarRowView = LayoutInflater.from(requireContext()).inflate(R.layout.main_calendar_row, null);
        ViewGroup parent = (ViewGroup) calendarContainer.getParent();
        int index = parent.indexOfChild(calendarContainer);

        parent.removeView(calendarContainer);
        parent.addView(calendarRowView, index);

        dayContainers[0] = calendarRowView.findViewById(R.id.dayMon);
        dayContainers[1] = calendarRowView.findViewById(R.id.dayTue);
        dayContainers[2] = calendarRowView.findViewById(R.id.dayWed);
        dayContainers[3] = calendarRowView.findViewById(R.id.dayThu);
        dayContainers[4] = calendarRowView.findViewById(R.id.dayFri);
        dayContainers[5] = calendarRowView.findViewById(R.id.daySat);
        dayContainers[6] = calendarRowView.findViewById(R.id.daySun);

        dayNumberViews[0] = calendarRowView.findViewById(R.id.tvDayMon);
        dayNumberViews[1] = calendarRowView.findViewById(R.id.tvDayTue);
        dayNumberViews[2] = calendarRowView.findViewById(R.id.tvDayWed);
        dayNumberViews[3] = calendarRowView.findViewById(R.id.tvDayThu);
        dayNumberViews[4] = calendarRowView.findViewById(R.id.tvDayFri);
        dayNumberViews[5] = calendarRowView.findViewById(R.id.tvDaySat);
        dayNumberViews[6] = calendarRowView.findViewById(R.id.tvDaySun);

        for (int i = 0; i < 7; i++) {
            final int position = i;
            dayContainers[i].setOnClickListener(v -> onDayClick(position));
        }

        weekDays = new ArrayList<>();
        updateWeekDays();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            updateSelectedDayInWeekDays(LocalDate.now());
        }
    }

    private void onDayClick(int position) {
        if (weekDays == null || position >= weekDays.size()) return;

        LocalDate clickedDate = weekDays.get(position).getDate();
        if (clickedDate == null) return;

        int daysOffset = 0;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            daysOffset = (int) ChronoUnit.DAYS.between(LocalDate.now(), clickedDate);
        }
        int pagerPosition = CENTER_POSITION + daysOffset;
        vpTask.setCurrentItem(pagerPosition, true);
    }

    private void updateWeekDays() {
        if (weekDays == null) {
            weekDays = new ArrayList<>();
        } else {
            weekDays.clear();
        }

        LocalDate today = null;
        LocalDate startOfWeek = null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LocalDate referenceDay = (selectedDate != null) ? selectedDate : LocalDate.now().plusWeeks(weekOffset);
            startOfWeek = getStartOfWeek(referenceDay);
            today = LocalDate.now();
        }

        for (int i = 0; i < 7; i++) {
            LocalDate date = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && startOfWeek != null) {
                date = startOfWeek.plusDays(i);
            }
            boolean isToday = (today != null && date != null && date.equals(today));
            weekDays.add(new WeekDay(date, isToday));

            if (date != null && dayNumberViews[i] != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    dayNumberViews[i].setText(String.valueOf(date.getDayOfMonth()));
                }
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Restore selected kid if it was lost
        if (selectedKid == null && !kidProfiles.isEmpty()) {
            String savedKidId = getSavedSelectedKidId();
            if (savedKidId != null) {
                for (ChildProfile profile : kidProfiles) {
                    if (profile.getChildId().equals(savedKidId)) {
                        selectedKid = profile;
                        updateKidProfileUI();
                        break;
                    }
                }
            }
        }

        // Refresh tasks when returning from other activities
        if (selectedKid != null && isAdded() && getActivity() != null) {
            refreshTasksForSelectedKid();
            // Refresh kid profile data to get updated star balance
            refreshSelectedKidProfile();
        }
    }

    private void refreshSelectedKidProfile() {
        if (selectedKid != null && firebaseHelper != null) {
            firebaseHelper.getChildProfile(selectedKid.getChildId(), new FirebaseHelper.ChildProfileCallback() {
                @Override
                public void onChildProfileLoaded(ChildProfile childProfile) {
                    if (isAdded() && getActivity() != null) {
                        selectedKid = childProfile;
                        updateKidProfileUI();
                    }
                }

                @Override
                public void onError(String error) {
                    Log.e(TAG, "Error refreshing child profile: " + error);
                }
            });
        }
    }

    @Override
    public void onKidSelected(ChildProfile kidProfile) {
        // Check if fragment is still attached before updating
        if (!isAdded() || getActivity() == null) {
            Log.w(TAG, "Fragment not attached when kid selected, ignoring update");
            return;
        }

        setSelectedKid(kidProfile);
    }

    public ChildProfile getSelectedKid() {
        return selectedKid;
    }

    // Save selected kid profile to SharedPreferences
    private void saveSelectedKidProfile(String kidId) {
        SharedPreferences prefs = requireContext().getSharedPreferences(SELECTED_KID_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(SELECTED_KID_ID_PREF, kidId);
        editor.apply();
        Log.d(TAG, "Saved selected kid ID: " + kidId);
    }

    // Load selected kid profile from SharedPreferences
    private String getSavedSelectedKidId() {
        SharedPreferences prefs = requireContext().getSharedPreferences(SELECTED_KID_PREF, Context.MODE_PRIVATE);
        return prefs.getString(SELECTED_KID_ID_PREF, null);
    }

    // Update selected kid and save preference
    public void setSelectedKid(ChildProfile kidProfile) {
        if (kidProfile != null) {
            selectedKid = kidProfile;
            saveSelectedKidProfile(kidProfile.getChildId());
            updateKidProfileUI();
            refreshTasksForSelectedKid();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Clean up any pending operations to prevent crashes
        if (vpTask != null) {
            vpTask.clearOnPageChangeListeners();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        // Don't reset selected kid to maintain selection across fragment lifecycle
        // selectedKid = null; // Removed to maintain persistence
    }
}
