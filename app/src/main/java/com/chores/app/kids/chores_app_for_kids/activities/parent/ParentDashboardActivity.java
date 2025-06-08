package com.chores.app.kids.chores_app_for_kids.activities.parent;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.chores.app.kids.chores_app_for_kids.R;
import com.chores.app.kids.chores_app_for_kids.activities.LandingActivity;
import com.chores.app.kids.chores_app_for_kids.fragments.parent.ParentTasksFragment;
import com.chores.app.kids.chores_app_for_kids.fragments.parent.ParentRewardsFragment;
import com.chores.app.kids.chores_app_for_kids.fragments.parent.ParentSettingsFragment;
import com.chores.app.kids.chores_app_for_kids.utils.AuthManager;
import com.chores.app.kids.chores_app_for_kids.utils.SharedPrefManager;

import java.util.ArrayList;
import java.util.List;

public class ParentDashboardActivity extends AppCompatActivity {
    private static final String TAG = "ParentDashboardActivity";

    private Toolbar toolbar;
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private BottomNavigationView bottomNavigation;
    private FloatingActionButton fab;

    private ParentPagerAdapter pagerAdapter;
    private AuthManager authManager;
    private SharedPrefManager prefManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_dashboard);

        initViews();
        initManagers();
        setupToolbar();
        setupViewPager();
        setupBottomNavigation();
        setupFab();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        viewPager = findViewById(R.id.view_pager);
        tabLayout = findViewById(R.id.tab_layout);
        bottomNavigation = findViewById(R.id.bottom_navigation);
        fab = findViewById(R.id.fab);
    }

    private void initManagers() {
        authManager = AuthManager.getInstance(this);
        prefManager = SharedPrefManager.getInstance(this);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Family Dashboard");
            getSupportActionBar().setDisplayShowTitleEnabled(true);
        }
    }

    private void setupViewPager() {
        pagerAdapter = new ParentPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(pagerAdapter);
        tabLayout.setupWithViewPager(viewPager);

        // Set custom tab icons
        setupTabIcons();

        // Listen for page changes to update FAB
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            @Override
            public void onPageSelected(int position) {
                updateFabForCurrentPage(position);
                updateBottomNavigationSelection(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {}
        });
    }

    private void setupTabIcons() {
        if (tabLayout.getTabCount() >= 3) {
            tabLayout.getTabAt(0).setIcon(R.drawable.ic_tasks);
            tabLayout.getTabAt(1).setIcon(R.drawable.ic_rewards);
            tabLayout.getTabAt(2).setIcon(R.drawable.ic_settings);
        }
    }

    private void setupBottomNavigation() {
        bottomNavigation.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_tasks) {
                viewPager.setCurrentItem(0, true);
                return true;
            } else if (itemId == R.id.nav_rewards) {
                viewPager.setCurrentItem(1, true);
                return true;
            } else if (itemId == R.id.nav_settings) {
                viewPager.setCurrentItem(2, true);
                return true;
            }
            return false;
        });

        // Set initial selection
        bottomNavigation.setSelectedItemId(R.id.nav_tasks);
    }

    private void setupFab() {
        fab.setOnClickListener(v -> {
            int currentPage = viewPager.getCurrentItem();
            handleFabClick(currentPage);
        });

        // Set initial FAB state
        updateFabForCurrentPage(0);
    }

    private void updateFabForCurrentPage(int position) {
        switch (position) {
            case 0: // Tasks
                fab.setImageResource(R.drawable.ic_add_task);
                fab.show();
                break;
            case 1: // Rewards
                fab.setImageResource(R.drawable.ic_add_reward);
                fab.show();
                break;
            case 2: // Settings
                fab.hide();
                break;
        }
    }

    private void updateBottomNavigationSelection(int position) {
        switch (position) {
            case 0:
                bottomNavigation.setSelectedItemId(R.id.nav_tasks);
                break;
            case 1:
                bottomNavigation.setSelectedItemId(R.id.nav_rewards);
                break;
            case 2:
                bottomNavigation.setSelectedItemId(R.id.nav_settings);
                break;
        }
    }

    private void handleFabClick(int currentPage) {
        switch (currentPage) {
            case 0: // Tasks
                Intent createTaskIntent = new Intent(this, CreateTaskActivity.class);
                startActivity(createTaskIntent);
                break;
            case 1: // Rewards
                Intent createRewardIntent = new Intent(this, CreateRewardActivity.class);
                startActivity(createRewardIntent);
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_parent_dashboard, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.action_family) {
            Intent familyIntent = new Intent(this, FamilyManagementActivity.class);
            startActivity(familyIntent);
            return true;
        } else if (itemId == R.id.action_notifications) {
            // Handle notifications
            Toast.makeText(this, "Notifications", Toast.LENGTH_SHORT).show();
            return true;
        } else if (itemId == R.id.action_logout) {
            showLogoutConfirmation();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showLogoutConfirmation() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout", (dialog, which) -> logout())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void logout() {
        // Clear all stored data
        prefManager.clearAll();

        // Sign out from Firebase Auth
        authManager.signOut(task -> {
            // Navigate to landing screen
            Intent intent = new Intent(this, LandingActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();

            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh current fragment data
        refreshCurrentFragment();
    }

    private void refreshCurrentFragment() {
        int currentItem = viewPager.getCurrentItem();
        Fragment fragment = pagerAdapter.getItem(currentItem);
        if (fragment instanceof ParentTasksFragment) {
            ((ParentTasksFragment) fragment).refreshData();
        } else if (fragment instanceof ParentRewardsFragment) {
            ((ParentRewardsFragment) fragment).refreshData();
        }
    }

    // ViewPager Adapter
    private class ParentPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> fragments = new ArrayList<>();
        private final List<String> fragmentTitles = new ArrayList<>();

        public ParentPagerAdapter(@NonNull FragmentManager fm) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
            setupFragments();
        }

        private void setupFragments() {
            fragments.add(new ParentTasksFragment());
            fragments.add(new ParentRewardsFragment());
            fragments.add(new ParentSettingsFragment());

            fragmentTitles.add(getString(R.string.nav_tasks));
            fragmentTitles.add(getString(R.string.nav_rewards));
            fragmentTitles.add(getString(R.string.nav_settings));
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return fragmentTitles.get(position);
        }
    }

    @Override
    public void onBackPressed() {
        // Show confirmation dialog before exiting
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Exit App")
                .setMessage("Are you sure you want to exit?")
                .setPositiveButton("Exit", (dialog, which) -> super.onBackPressed())
                .setNegativeButton("Cancel", null)
                .show();
    }
}