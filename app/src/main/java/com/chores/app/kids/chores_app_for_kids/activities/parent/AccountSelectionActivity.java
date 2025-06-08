package com.chores.app.kids.chores_app_for_kids.activities.parent;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.chores.app.kids.chores_app_for_kids.R;
import com.chores.app.kids.chores_app_for_kids.adapters.GoogleAccountAdapter;
import com.chores.app.kids.chores_app_for_kids.dialogs.InviteCodeDialog;
import com.chores.app.kids.chores_app_for_kids.models.Family;
import com.chores.app.kids.chores_app_for_kids.models.User;
import com.chores.app.kids.chores_app_for_kids.utils.Constants;
import com.chores.app.kids.chores_app_for_kids.utils.FirebaseManager;
import com.chores.app.kids.chores_app_for_kids.utils.SharedPrefManager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AccountSelectionActivity extends AppCompatActivity implements GoogleAccountAdapter.OnAccountClickListener {
    private TextView tvTitle;
    private TextView tvSubtitle;
    private RecyclerView recyclerViewAccounts;
    private Button btnAddAnotherAccount;
    private Button btnJoinFamilyCode;
    private LinearLayout layoutJoinFamily;

    private GoogleAccountAdapter accountAdapter;
    private List<GoogleSignInAccount> accounts;
    private FirebaseManager firebaseManager;
    private SharedPrefManager prefManager;
    private boolean isJoiningFamily = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_selection);

        initViews();
        initData();
        setupRecyclerView();
        setupClickListeners();
        loadGoogleAccounts();
    }

    private void initViews() {
        tvTitle = findViewById(R.id.tv_title);
        tvSubtitle = findViewById(R.id.tv_subtitle);
        recyclerViewAccounts = findViewById(R.id.recycler_view_accounts);
        btnAddAnotherAccount = findViewById(R.id.btn_add_another_account);
        btnJoinFamilyCode = findViewById(R.id.btn_join_family_code);
        layoutJoinFamily = findViewById(R.id.layout_join_family);
    }

    private void initData() {
        firebaseManager = FirebaseManager.getInstance();
        prefManager = SharedPrefManager.getInstance(this);
        accounts = new ArrayList<>();

        // Check if this is for joining family
        isJoiningFamily = getIntent().getBooleanExtra("join_family", false);

        if (isJoiningFamily) {
            layoutJoinFamily.setVisibility(View.VISIBLE);
        }
    }

    private void setupRecyclerView() {
        accountAdapter = new GoogleAccountAdapter(accounts, this);
        recyclerViewAccounts.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewAccounts.setAdapter(accountAdapter);
    }

    private void setupClickListeners() {
        btnAddAnotherAccount.setOnClickListener(v -> {
            // Add another Google account logic
            Toast.makeText(this, "Add another account functionality", Toast.LENGTH_SHORT).show();
        });

        btnJoinFamilyCode.setOnClickListener(v -> showInviteCodeDialog());
    }

    private void loadGoogleAccounts() {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null) {
            accounts.add(account);
            accountAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onAccountClick(GoogleSignInAccount account) {
        if (isJoiningFamily) {
            showInviteCodeDialog();
        } else {
            proceedWithAccount(account);
        }
    }

    private void proceedWithAccount(GoogleSignInAccount account) {
        // Navigate to family management or dashboard
        Intent intent = new Intent(this, FamilyManagementActivity.class);
        startActivity(intent);
        finish();
    }

    private void showInviteCodeDialog() {
        InviteCodeDialog dialog = new InviteCodeDialog(this, inviteCode -> {
            joinFamilyWithCode(inviteCode);
        });
        dialog.show();
    }

    private void joinFamilyWithCode(String inviteCode) {
        firebaseManager.getFamilyByInviteCode(inviteCode, task -> {
            if (task.isSuccessful() && !task.getResult().isEmpty()) {
                Family family = task.getResult().getDocuments().get(0).toObject(Family.class);
                if (family != null) {
                    joinFamily(family);
                }
            } else {
                Toast.makeText(this, getString(R.string.error_invalid_invite_code), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void joinFamily(Family family) {
        String userId = prefManager.getUserId();

        firebaseManager.joinFamily(family.getFamilyId(), userId, task -> {
            if (task.isSuccessful()) {
                // Update user's family ID
                prefManager.setFamilyId(family.getFamilyId());

                // Navigate to parent dashboard
                Intent intent = new Intent(this, ParentDashboardActivity.class);
                startActivity(intent);
                finish();

                Toast.makeText(this, getString(R.string.success_family_joined), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.error_occurred), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
