package com.chores.app.kids.chores_app_for_kids.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.chores.app.kids.chores_app_for_kids.R;
import com.chores.app.kids.chores_app_for_kids.activities.AddKidProfileActivity;
import com.chores.app.kids.chores_app_for_kids.activities.ManageChildrenActivity;
import com.chores.app.kids.chores_app_for_kids.activities.ParentLoginActivity;
import com.chores.app.kids.chores_app_for_kids.adapters.FamilyMemberAdapter;
import com.chores.app.kids.chores_app_for_kids.models.User;
import com.chores.app.kids.chores_app_for_kids.utils.AuthHelper;
import com.chores.app.kids.chores_app_for_kids.utils.FirebaseHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SettingsFragment extends Fragment {

    private static final String TAG = "SettingsFragment";
    private static final int REQUEST_ADD_KID = 1001;

    private Button btnAddKid;
    private LinearLayout btnSignOut;
    private RecyclerView recyclerViewMembers;
    private TextView tvNoMembers;
    private LinearLayout layoutEmptyState;

    private FamilyMemberAdapter memberAdapter;
    private List<User> familyMembers;
    private String familyId;
    private Handler mainHandler;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        mainHandler = new Handler(Looper.getMainLooper());

        initializeViews(view);
        setupRecyclerView();
        setupClickListeners();
        loadUserData();
        loadFamilyData();

        return view;
    }

    private void initializeViews(View view) {
        btnAddKid = view.findViewById(R.id.btn_add_kid);
        btnSignOut = view.findViewById(R.id.btn_sign_out);
        recyclerViewMembers = view.findViewById(R.id.recycler_view_members);
        tvNoMembers = view.findViewById(R.id.tv_no_members);
        layoutEmptyState = view.findViewById(R.id.layout_empty_state);
    }

    private void setupRecyclerView() {
        familyMembers = new ArrayList<>();
        memberAdapter = new FamilyMemberAdapter(familyMembers, getContext());
        recyclerViewMembers.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewMembers.setAdapter(memberAdapter);

        // Set up member action listener
        memberAdapter.setOnMemberActionListener(new FamilyMemberAdapter.OnMemberActionListener() {
            @Override
            public void onViewMemberDetails(User member) {
                // Handle view member details
            }

            @Override
            public void onEditMember(User member) {
                // Handle edit member
            }

            @Override
            public void onRemoveMember(User member) {
                // Handle remove member
            }

            @Override
            public void onViewMemberStats(User member) {
                // Handle view member stats
            }

            @Override
            public void onManagePermissions(User member) {
                // Handle manage permissions
            }

            @Override
            public void onGenerateInviteCode(User member) {
                generateInviteCodeForChild(member);
            }
        });

        // Fix RecyclerView height issues inside ScrollView
        recyclerViewMembers.setHasFixedSize(true);
        recyclerViewMembers.setNestedScrollingEnabled(false);

        // Set a reasonable max height to prevent height calculation issues
        ViewGroup.LayoutParams layoutParams = recyclerViewMembers.getLayoutParams();
        layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        recyclerViewMembers.setLayoutParams(layoutParams);

        Log.d(TAG, "RecyclerView setup completed");
    }

    private void setupClickListeners() {
        btnAddKid.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), AddKidProfileActivity.class);
            intent.putExtra("familyId", familyId);
            startActivityForResult(intent, REQUEST_ADD_KID);
        });

        btnSignOut.setOnClickListener(v -> signOut());
    }

    private void loadUserData() {
        // Try to get family ID from current user first
        FirebaseHelper.getCurrentUser(new FirebaseHelper.CurrentUserCallback() {
            @Override
            public void onUserLoaded(User user) {
                familyId = user.getFamilyId();
                Log.d(TAG, "Family ID from current user: " + familyId);

                if (familyId != null && !familyId.isEmpty()) {
                    loadFamilyData();
                } else {
                    // Fallback to AuthHelper
                    familyId = AuthHelper.getFamilyId(getContext());
                    Log.d(TAG, "Family ID from AuthHelper: " + familyId);
                    loadFamilyData();
                }
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Failed to get current user: " + error);
                // Fallback to AuthHelper
                familyId = AuthHelper.getFamilyId(getContext());
                Log.d(TAG, "Family ID from AuthHelper (fallback): " + familyId);
                loadFamilyData();
            }
        });
    }

    private void loadFamilyData() {
        if (familyId == null || familyId.isEmpty()) {
            Log.e(TAG, "Family ID is null or empty");
            showEmptyState("No family data found");
            return;
        }

        // Load family members
        loadFamilyMembers();
    }

    private void loadFamilyMembers() {
        Log.d(TAG, "Loading family members for family: " + familyId);

        FirebaseHelper.getFamilyMembers(familyId, new FirebaseHelper.FamilyMembersCallback() {
            @Override
            public void onMembersLoaded(List<User> members) {
                Log.d(TAG, "Loaded " + members.size() + " family members");

                if (getActivity() != null && isAdded()) {
                    mainHandler.post(() -> {
                        try {
                            // Clear and update the family members list
                            familyMembers.clear();
                            familyMembers.addAll(members);

                            Log.d(TAG, "familyMembers list size after adding: " + familyMembers.size());

                            // Notify adapter of data change
                            memberAdapter.notifyDataSetChanged();

                            // Sort members to show parents first, then children
                            memberAdapter.sortMembers();

                            // Ensure RecyclerView shows all items
                            recyclerViewMembers.post(() -> {
                                // Force layout and measurement
                                recyclerViewMembers.measure(
                                        View.MeasureSpec.makeMeasureSpec(recyclerViewMembers.getWidth(), View.MeasureSpec.EXACTLY),
                                        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                                );

                                // Set height to measured height
                                ViewGroup.LayoutParams params = recyclerViewMembers.getLayoutParams();
                                params.height = recyclerViewMembers.getMeasuredHeight();
                                recyclerViewMembers.setLayoutParams(params);

                                Log.d(TAG, "RecyclerView height set to measured height: " + recyclerViewMembers.getMeasuredHeight());
                            });

                            if (members.isEmpty()) {
                                showEmptyState("No family members found");
                            } else {
                                hideEmptyState();
                            }

                            Log.d(TAG, "RecyclerView updated with " + familyMembers.size() + " members");
                            Log.d(TAG, "Adapter item count: " + memberAdapter.getItemCount());
                        } catch (Exception e) {
                            Log.e(TAG, "Error updating RecyclerView", e);
                        }
                    });
                }
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error loading family members: " + error);

                if (getActivity() != null && isAdded()) {
                    mainHandler.post(() -> {
                        showEmptyState("Failed to load family members");
                        Toast.makeText(getContext(), "Error: " + error, Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    private void signOut() {
        Log.d(TAG, "Signing out user");

        AuthHelper.signOut(getContext());

        // Navigate to login screen
        Intent intent = new Intent(getContext(), ParentLoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

        if (getActivity() != null) {
            getActivity().finish();
        }
    }

    private void showEmptyState(String message) {
        if (layoutEmptyState != null) {
            layoutEmptyState.setVisibility(View.VISIBLE);
            recyclerViewMembers.setVisibility(View.GONE);
            tvNoMembers.setText(message);
        }
    }

    private void hideEmptyState() {
        if (layoutEmptyState != null) {
            layoutEmptyState.setVisibility(View.GONE);
            recyclerViewMembers.setVisibility(View.VISIBLE);
        }
    }

    private void generateInviteCodeForChild(User child) {
        if (!"child".equals(child.getRole())) {
            Toast.makeText(getContext(), "Invite codes are only for children", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "Generating invite code for child: " + child.getName());

        FirebaseHelper.generateChildInviteCode(child.getUserId(), task -> {
            if (getActivity() != null && isAdded()) {
                mainHandler.post(() -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Invite code generated successfully for " + child.getName());
                        Toast.makeText(getContext(), "Invite code generated for " + child.getName(), Toast.LENGTH_SHORT).show();

                        // Refresh the family members list to show the new code
                        loadFamilyMembers();
                    } else {
                        Log.e(TAG, "Failed to generate invite code for " + child.getName(), task.getException());
                        Toast.makeText(getContext(), "Failed to generate invite code", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_ADD_KID) {
            Log.d(TAG, "Returned from AddKidProfileActivity with result: " + resultCode);

            // Force refresh the family members list after adding a kid
            if (familyId != null && !familyId.isEmpty()) {
                // Add a small delay to ensure Firebase has time to update
                mainHandler.postDelayed(() -> {
                    Log.d(TAG, "Refreshing family members after adding kid");
                    loadFamilyMembers();
                }, 500); // 500ms delay
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume called");

        // Only reload if we have a valid family ID
        if (familyId != null && !familyId.isEmpty()) {
            // Add a small delay to ensure any Firebase writes have completed
            mainHandler.postDelayed(() -> {
                Log.d(TAG, "Reloading family members in onResume");
                loadFamilyMembers();
            }, 300); // 300ms delay
        }
    }

    // Public method to refresh data (can be called from parent activity)
    public void refreshData() {
        Log.d(TAG, "refreshData called");
        if (familyId != null && !familyId.isEmpty()) {
            loadFamilyMembers();
        } else {
            loadUserData(); // Reload everything if family ID is missing
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mainHandler != null) {
            mainHandler.removeCallbacksAndMessages(null);
        }
    }
}
