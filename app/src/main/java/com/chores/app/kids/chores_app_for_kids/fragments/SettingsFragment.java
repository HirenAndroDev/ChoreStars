package com.chores.app.kids.chores_app_for_kids.fragments;

import android.content.Intent;
import android.os.Bundle;
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
import com.chores.app.kids.chores_app_for_kids.activities.ParentLoginActivity;
import com.chores.app.kids.chores_app_for_kids.adapters.FamilyMemberAdapter;
import com.chores.app.kids.chores_app_for_kids.models.User;
import com.chores.app.kids.chores_app_for_kids.utils.AuthHelper;
import com.chores.app.kids.chores_app_for_kids.utils.FirebaseHelper;
import java.util.ArrayList;
import java.util.List;

public class SettingsFragment extends Fragment {

    private static final String TAG = "SettingsFragment";

    private TextView tvInviteCode;
    private Button btnGenerateCode;
    private Button btnAddKid;
    private LinearLayout btnSignOut;
    private RecyclerView recyclerViewMembers;
    private TextView tvNoMembers;
    private LinearLayout layoutEmptyState;

    private FamilyMemberAdapter memberAdapter;
    private List<User> familyMembers;
    private String familyId;
    private String currentInviteCode;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        initializeViews(view);
        setupRecyclerView();
        setupClickListeners();
        loadUserData();
        loadFamilyData();

        return view;
    }

    private void initializeViews(View view) {
        tvInviteCode = view.findViewById(R.id.tv_invite_code);
        btnGenerateCode = view.findViewById(R.id.btn_generate_code);
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
    }

    private void setupClickListeners() {
        btnGenerateCode.setOnClickListener(v -> generateNewInviteCode());

        btnAddKid.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), AddKidProfileActivity.class);
            intent.putExtra("familyId", familyId);
            startActivity(intent);
        });

        btnSignOut.setOnClickListener(v -> signOut());
    }

    private void loadUserData() {
        familyId = AuthHelper.getFamilyId(getContext());
        Log.d(TAG, "Family ID: " + familyId);
    }

    private void loadFamilyData() {
        if (familyId == null || familyId.isEmpty()) {
            Log.e(TAG, "Family ID is null or empty");
            showEmptyState("No family data found");
            return;
        }

        // Load family members
        loadFamilyMembers();

        // Load invite code
        loadInviteCode();
    }

    private void loadFamilyMembers() {
        Log.d(TAG, "Loading family members for family: " + familyId);

        FirebaseHelper.getFamilyMembers(familyId, new FirebaseHelper.FamilyMembersCallback() {
            @Override
            public void onMembersLoaded(List<User> members) {
                Log.d(TAG, "Loaded " + members.size() + " family members");

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        familyMembers.clear();
                        familyMembers.addAll(members);
                        memberAdapter.notifyDataSetChanged();

                        if (members.isEmpty()) {
                            showEmptyState("No family members found");
                        } else {
                            hideEmptyState();
                        }
                    });
                }
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error loading family members: " + error);

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        showEmptyState("Failed to load family members");
                        Toast.makeText(getContext(), "Error: " + error, Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    private void loadInviteCode() {
        Log.d(TAG, "Loading invite code for family: " + familyId);

        FirebaseHelper.getFamilyInviteCode(familyId, new FirebaseHelper.InviteCodeCallback() {
            @Override
            public void onInviteCodeLoaded(String inviteCode, long expiryTime) {
                Log.d(TAG, "Invite code loaded: " + inviteCode);

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        currentInviteCode = inviteCode;

                        // Check if code is still valid
                        if (expiryTime > System.currentTimeMillis()) {
                            tvInviteCode.setText(inviteCode);
                        } else {
                            tvInviteCode.setText("Code Expired");
                            generateNewInviteCode(); // Auto-generate new code
                        }
                    });
                }
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error loading invite code: " + error);

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        tvInviteCode.setText("Error");
                        Toast.makeText(getContext(), "Failed to load invite code", Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    private void generateNewInviteCode() {
        if (familyId == null || familyId.isEmpty()) {
            Toast.makeText(getContext(), "Family not found", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "Generating new invite code");
        btnGenerateCode.setEnabled(false);
        btnGenerateCode.setText("Generating...");

        FirebaseHelper.generateInviteCode(familyId, task -> {
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    btnGenerateCode.setEnabled(true);
                    btnGenerateCode.setText("Generate New Code");

                    if (task.isSuccessful()) {
                        Log.d(TAG, "New invite code generated successfully");
                        Toast.makeText(getContext(), "New invite code generated!", Toast.LENGTH_SHORT).show();
                        loadInviteCode(); // Reload the code
                    } else {
                        Log.e(TAG, "Failed to generate invite code", task.getException());
                        Toast.makeText(getContext(), "Failed to generate code", Toast.LENGTH_SHORT).show();
                    }
                });
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

    @Override
    public void onResume() {
        super.onResume();
        // Reload data when returning from AddKidProfileActivity
        if (familyId != null && !familyId.isEmpty()) {
            loadFamilyMembers();
        }
    }
}