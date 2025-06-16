package com.chores.app.kids.chores_app_for_kids.fragments;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.chores.app.kids.chores_app_for_kids.R;
import com.chores.app.kids.chores_app_for_kids.adapters.TaskDayPagerAdapter;
import com.chores.app.kids.chores_app_for_kids.dialogs.KidProfilesParentDialog;
import com.chores.app.kids.chores_app_for_kids.fragments.RewardsFragment;
import com.chores.app.kids.chores_app_for_kids.models.ChildProfile;
import com.chores.app.kids.chores_app_for_kids.models.User;
import com.chores.app.kids.chores_app_for_kids.utils.FirebaseHelper;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


public class MainRewardFragment extends Fragment implements KidProfilesParentDialog.OnKidSelectedListener {

    private static final String SELECTED_KID_PREF = "selected_kid";
    private static final String SELECTED_KID_ID_PREF = "selected_kid_id";

    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private final String[] tabTitles = new String[]{"Rewards", "Reddem History"};

    private CircleImageView ivKidProfile;
    private LinearLayout layoutKidProfile;
    private TextView  tvKidName, tvStarsBalance;
    // Current selected kid
    private ChildProfile selectedKid;
    private List<ChildProfile> kidProfiles;
    private FirebaseHelper firebaseHelper;
    private TaskDayPagerAdapter pagerAdapter;


    public MainRewardFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_main_reward, container, false);
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        tabLayout = view.findViewById(R.id.tabLayout);
        viewPager = view.findViewById(R.id.viewPager);

        tvKidName = view.findViewById(R.id.tvKidName);
        tvStarsBalance = view.findViewById(R.id.tvStarsBalance);
        ivKidProfile = view.findViewById(R.id.ivKidProfile);
        layoutKidProfile = view.findViewById(R.id.layoutKidProfile);

        firebaseHelper = new FirebaseHelper();
        kidProfiles = new ArrayList<>();

        // Set up the ViewPager with the sections adapter
        setupViewPager();
        setupKidProfileClick();
        loadKidProfiles();


        // Connect the TabLayout with ViewPager2
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            tab.setText(tabTitles[position]);
        }).attach();
    }

    private void setupViewPager() {
        RewardsPagerAdapter pagerAdapter = new RewardsPagerAdapter(requireActivity());
        viewPager.setAdapter(pagerAdapter);

        // Prevent destroying fragments when not visible (optional, but recommended for performance)
        viewPager.setOffscreenPageLimit(tabTitles.length - 1);
    }

    /**
     * A {@link FragmentStateAdapter} that returns fragments for the tabs
     */
    private class RewardsPagerAdapter extends FragmentStateAdapter {

        public RewardsPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0:
                    return new RewardsFragment();
                case 1:
                    return new RewardRedeemFragment();
                default:
                    return new RewardsFragment(); // Default case, should never happen
            }
        }

        @Override
        public int getItemCount() {
            return tabTitles.length;
        }
    }

    private void setupKidProfileClick() {
        layoutKidProfile.setOnClickListener(v -> {
            KidProfilesParentDialog dialog = new KidProfilesParentDialog(requireContext(), kidProfiles, selectedKid);
            dialog.setOnKidSelectedListener(this);
            dialog.show();
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

                    // Notify child fragments about the initial selection
                    notifyChildFragmentsOfSelectionChange();

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

        // Reload the child's current data from Firebase to get updated star balance
        FirebaseHelper.getUserById(selectedKid.getChildId(), new FirebaseHelper.CurrentUserCallback() {
            @Override
            public void onUserLoaded(User user) {
                if (isAdded() && getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        // Update the selected child profile with fresh data
                        selectedKid.setStarBalance(user.getStarBalance());

                        // Update UI
                        tvKidName.setText(selectedKid.getName());
                        tvStarsBalance.setText(String.valueOf(selectedKid.getStarBalance()));

                        // Load profile image
                        if (selectedKid.getProfileImageUrl() != null && !selectedKid.getProfileImageUrl().isEmpty()) {
                            Glide.with(MainRewardFragment.this)
                                    .load(selectedKid.getProfileImageUrl())
                                    .circleCrop()
                                    .into(ivKidProfile);
                        } else {
                            ivKidProfile.setImageResource(R.drawable.default_avatar);
                        }
                    });
                }
            }

            @Override
            public void onError(String error) {
                // Fallback to original updateKidProfileUI method
                updateKidProfileUIOriginal();
            }
        });
    }

    private void updateKidProfileUIOriginal() {
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

    @Override
    public void onKidSelected(ChildProfile kidProfile) {
        // Check if fragment is still attached before updating
        if (!isAdded() || getActivity() == null) {
            Log.w(TAG, "Fragment not attached when kid selected, ignoring update");
            return;
        }

        setSelectedKid(kidProfile);

        // Notify child fragments about the selection change
        notifyChildFragmentsOfSelectionChange();
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

            // Notify child fragments about the selection change
            notifyChildFragmentsOfSelectionChange();
        }
    }

    private void notifyChildFragmentsOfSelectionChange() {
        // Notify both fragments to refresh their data
        try {
            ViewPager2 viewPager = getView().findViewById(R.id.viewPager);
            if (viewPager != null) {
                FragmentActivity activity = requireActivity();
                FragmentManager fragmentManager = activity.getSupportFragmentManager();

                // Find the RewardsFragment (position 0)
                String rewardsFragmentTag = "f" + 0; // ViewPager2 uses "f" + position as tag
                Fragment rewardsFragment = fragmentManager.findFragmentByTag(rewardsFragmentTag);
                if (rewardsFragment instanceof RewardsFragment) {
                    ((RewardsFragment) rewardsFragment).onChildSelectionChanged();
                }

                // Find the RewardRedeemFragment (position 1)
                String redeemFragmentTag = "f" + 1; // ViewPager2 uses "f" + position as tag
                Fragment redeemFragment = fragmentManager.findFragmentByTag(redeemFragmentTag);
                if (redeemFragment instanceof RewardRedeemFragment) {
                    ((RewardRedeemFragment) redeemFragment).refreshRedeemedRewards();
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "Error notifying child fragments of selection change", e);
        }
    }

}
