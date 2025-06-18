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

        // Set parent fragment reference for RewardsFragment when ViewPager is ready
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                // Set parent reference when page is selected
                setParentReferenceForFragments();
            }
        });

        // Also set it immediately after setup
        viewPager.post(() -> setParentReferenceForFragments());
    }

    private void setParentReferenceForFragments() {
        try {
            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();

            // Find the RewardsFragment (position 0)
            String rewardsFragmentTag = "f" + 0;
            Fragment rewardsFragment = fragmentManager.findFragmentByTag(rewardsFragmentTag);
            if (rewardsFragment instanceof RewardsFragment) {
                ((RewardsFragment) rewardsFragment).setParentFragment(this);
                Log.d(TAG, "Set parent reference for RewardsFragment");
            }

        } catch (Exception e) {
            Log.w(TAG, "Error setting parent reference for fragments", e);
        }
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
        if (selectedKid != null) {
            // Refresh the selected child's data from server
            FirebaseHelper.getChildProfile(selectedKid.getChildId(), new FirebaseHelper.ChildProfileCallback() {
                @Override
                public void onChildProfileLoaded(ChildProfile updatedChild) {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            updateKidProfileUIOriginal();
                            selectedKid.setStarBalance(updatedChild.getStarBalance());
                            if (tvStarsBalance != null) {
                                tvStarsBalance.setText(String.valueOf(updatedChild.getStarBalance()));
                            }
                            Log.d("MainRewardFragment", "Updated UI with fresh star balance: " + updatedChild.getStarBalance());
                        });
                    }
                }

                @Override
                public void onError(String error) {
                    Log.w("MainRewardFragment", "Could not refresh child profile: " + error);
                }
            });
        }
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

    // In MainRewardFragment.java - update the notifyChildFragmentsOfSelectionChange method
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
                    // Set parent fragment reference first
                    ((RewardsFragment) rewardsFragment).setParentFragment(this);
                    // Set the selected child directly
                    ((RewardsFragment) rewardsFragment).setSelectedChild(selectedKid);
                    // Then trigger the refresh
                    ((RewardsFragment) rewardsFragment).onChildSelectionChanged();
                }

                // Find the RewardRedeemFragment (position 1)
                String redeemFragmentTag = "f" + 1; // ViewPager2 uses "f" + position as tag
                Fragment redeemFragment = fragmentManager.findFragmentByTag(redeemFragmentTag);
                if (redeemFragment instanceof RewardRedeemFragment) {
                    // Set the selected child directly
                    ((RewardRedeemFragment) redeemFragment).setSelectedChild(selectedKid);
                    // Then trigger the refresh
                    ((RewardRedeemFragment) redeemFragment).onChildSelectionChanged();
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "Error notifying child fragments of selection change", e);
        }
    }

    public void updateSelectedChildStarBalance(int newBalance) {
        Log.d("MainRewardFragment", "updateSelectedChildStarBalance called with: " + newBalance);

        if (selectedKid != null) {
            selectedKid.setStarBalance(newBalance);

            // Update the UI
            if (tvStarsBalance != null) {
                tvStarsBalance.setText(String.valueOf(newBalance));
            }

            Log.d("MainRewardFragment", "Updated selected child star balance to: " + newBalance);
        }
    }

    // Method for instant star balance update without server refresh
    public void setSelectedKidStarBalanceInstantly(int newBalance) {
        Log.d("MainRewardFragment", "setSelectedKidStarBalanceInstantly called with: " + newBalance);

        if (selectedKid != null) {
            selectedKid.setStarBalance(newBalance);

            // Update the UI instantly
            if (tvStarsBalance != null) {
                tvStarsBalance.setText(String.valueOf(newBalance));
            }

            Log.d("MainRewardFragment", "Instantly updated selected child star balance to: " + newBalance);
        }
    }
}
