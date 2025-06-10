package com.chores.app.kids.chores_app_for_kids.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
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
import com.chores.app.kids.chores_app_for_kids.adapters.FamilyMemberAdapter;
import com.chores.app.kids.chores_app_for_kids.models.User;
import com.chores.app.kids.chores_app_for_kids.utils.FirebaseHelper;
import java.util.ArrayList;
import java.util.List;

public class SettingsFragment extends Fragment {

    private TextView tvInviteCode;
    private Button btnGenerateCode;
    private LinearLayout btnSignOut;
    private RecyclerView recyclerViewMembers;
    private FamilyMemberAdapter memberAdapter;
    private List<User> familyMembers;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        initializeViews(view);
        setupRecyclerView();
        setupClickListeners();
        loadFamilyData();

        return view;
    }

    @SuppressLint("WrongViewCast")
    private void initializeViews(View view) {
        tvInviteCode = view.findViewById(R.id.tv_invite_code);
        btnGenerateCode = view.findViewById(R.id.btn_generate_code);
        btnSignOut = view.findViewById(R.id.btn_sign_out);
        recyclerViewMembers = view.findViewById(R.id.recycler_view_members);
    }

    private void setupRecyclerView() {
        familyMembers = new ArrayList<>();
        memberAdapter = new FamilyMemberAdapter(familyMembers, getContext());
        recyclerViewMembers.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewMembers.setAdapter(memberAdapter);
    }

    private void setupClickListeners() {
        btnGenerateCode.setOnClickListener(v -> generateNewInviteCode());

        btnSignOut.setOnClickListener(v -> {
            // TODO: Implement sign out functionality
            Toast.makeText(getContext(), "Sign out functionality coming soon", Toast.LENGTH_SHORT).show();
        });
    }

    private void loadFamilyData() {
        // TODO: Load family members and invite code from Firebase
        // For now, show sample data
        tvInviteCode.setText("123456");
        addSampleMembers();
    }

    private void addSampleMembers() {
        User parent = new User("1", "Mom", "mom@example.com", "parent", "family1");
        User child1 = new User("2", "Alex", "", "child", "family1");
        User child2 = new User("3", "Sam", "", "child", "family1");

        familyMembers.add(parent);
        familyMembers.add(child1);
        familyMembers.add(child2);

        memberAdapter.notifyDataSetChanged();
    }

    private void generateNewInviteCode() {
        // TODO: Implement invite code generation
        String newCode = String.format("%06d", (int)(Math.random() * 1000000));
        tvInviteCode.setText(newCode);
        Toast.makeText(getContext(), "New invite code generated", Toast.LENGTH_SHORT).show();
    }
}