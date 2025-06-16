package com.chores.app.kids.chores_app_for_kids.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chores.app.kids.chores_app_for_kids.R;
import com.chores.app.kids.chores_app_for_kids.adapters.PreRewardAdapter;
import com.chores.app.kids.chores_app_for_kids.models.PreReward;
import com.chores.app.kids.chores_app_for_kids.utils.FirebaseHelper;

import java.util.ArrayList;
import java.util.List;

public class NewRewardActivity extends AppCompatActivity {

    private ImageView btnClose;
    private CardView cardAddCustomReward;
    private RecyclerView recyclerViewPreRewards;
    private PreRewardAdapter preRewardAdapter;
    private List<PreReward> preRewardList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_reward);

        initializeViews();
        setupRecyclerView();
        setupClickListeners();
        loadPreRewards();
    }

    private void initializeViews() {
        btnClose = findViewById(R.id.btn_close);
        cardAddCustomReward = findViewById(R.id.card_add_custom_reward);
        recyclerViewPreRewards = findViewById(R.id.recycler_view_pre_rewards);
    }

    private void setupRecyclerView() {
        preRewardList = new ArrayList<>();
        preRewardAdapter = new PreRewardAdapter(preRewardList, this, new PreRewardAdapter.OnPreRewardClickListener() {
            @Override
            public void onPreRewardClick(PreReward preReward) {
                openRewardDetail(preReward);
            }
        });
        recyclerViewPreRewards.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerViewPreRewards.setAdapter(preRewardAdapter);
    }

    private void setupClickListeners() {
        btnClose.setOnClickListener(v -> finish());

        cardAddCustomReward.setOnClickListener(v -> {
            Intent intent = new Intent(this, RewardDetailActivity.class);
            startActivity(intent);
        });
    }

    private void loadPreRewards() {
        // Load pre-rewards from Firebase
        FirebaseHelper.getPreRewards(new FirebaseHelper.PreRewardsCallback() {
            @Override
            public void onPreRewardsLoaded(List<PreReward> preRewards) {
                preRewardList.clear();
                preRewardList.addAll(preRewards);
                preRewardAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(String error) {
                // If no pre-rewards in Firebase, show empty list
                preRewardList.clear();
                preRewardAdapter.notifyDataSetChanged();
            }
        });
    }

    private void addSamplePreRewards() {
        // This method is no longer needed as we load from Firebase
        // Keeping it for reference but it won't be called
        preRewardList.clear();

        preRewardList.add(new PreReward("1", "Buy a toy", "ic_reward_toy", 10));
        preRewardList.add(new PreReward("2", "Ice cream", "ic_ice_cream", 1));
        preRewardList.add(new PreReward("3", "Day-off chores", "ic_reward_dayoff", 20));
        preRewardList.add(new PreReward("4", "Money", "ic_reward_money", 15));
        preRewardList.add(new PreReward("5", "Pizza", "ic_reward_pizza", 5));
        preRewardList.add(new PreReward("6", "Play games", "ic_reward_games", 5));
        preRewardList.add(new PreReward("7", "Watch a movie", "ic_reward_movie", 3));
        preRewardList.add(new PreReward("8", "Buy a book", "ic_reward_book", 5));
        preRewardList.add(new PreReward("9", "Buy clothes", "ic_reward_clothes", 10));
        preRewardList.add(new PreReward("10", "Extra screen time", "ic_reward_screen", 5));

        preRewardAdapter.notifyDataSetChanged();
    }

    private void openRewardDetail(PreReward preReward) {
        Intent intent = new Intent(this, RewardDetailActivity.class);
        intent.putExtra("rewardName", preReward.getName());
        intent.putExtra("rewardIcon", preReward.getIconUrl() != null && !preReward.getIconUrl().isEmpty()
                ? preReward.getIconUrl() : preReward.getIconName());
        intent.putExtra("rewardStars", preReward.getStarCost());
        startActivity(intent);
    }
}
