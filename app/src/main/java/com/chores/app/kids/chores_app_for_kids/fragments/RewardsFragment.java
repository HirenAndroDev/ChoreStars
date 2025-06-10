package com.chores.app.kids.chores_app_for_kids.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.chores.app.kids.chores_app_for_kids.R;
import com.chores.app.kids.chores_app_for_kids.activities.CreateRewardActivity;
import com.chores.app.kids.chores_app_for_kids.adapters.RewardAdapter;
import com.chores.app.kids.chores_app_for_kids.models.Reward;
import java.util.ArrayList;
import java.util.List;

public class RewardsFragment extends Fragment {

    private RecyclerView recyclerViewRewards;
    private FloatingActionButton fabAddReward;
    private RewardAdapter rewardAdapter;
    private List<Reward> rewardList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_rewards, container, false);

        initializeViews(view);
        setupRecyclerView();
        setupClickListeners();
        loadRewards();

        return view;
    }

    private void initializeViews(View view) {
        recyclerViewRewards = view.findViewById(R.id.recycler_view_rewards);
        fabAddReward = view.findViewById(R.id.fab_add_reward);
    }

    private void setupRecyclerView() {
        rewardList = new ArrayList<>();
        rewardAdapter = new RewardAdapter(rewardList, getContext());
        recyclerViewRewards.setLayoutManager(new GridLayoutManager(getContext(), 2));
        recyclerViewRewards.setAdapter(rewardAdapter);
    }

    private void setupClickListeners() {
        fabAddReward.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), CreateRewardActivity.class);
            startActivity(intent);
        });
    }

    private void loadRewards() {
        // TODO: Load rewards from Firebase
        // For now, add sample data
        addSampleRewards();
    }

    private void addSampleRewards() {
        Reward reward1 = new Reward("Ice Cream", "ic_ice_cream", 10, "sample_family");
        Reward reward2 = new Reward("Extra Screen Time", "ic_screen", 15, "sample_family");
        Reward reward3 = new Reward("Choose Dinner", "ic_dinner", 20, "sample_family");

        rewardList.add(reward1);
        rewardList.add(reward2);
        rewardList.add(reward3);

        rewardAdapter.notifyDataSetChanged();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh rewards when fragment becomes visible
        loadRewards();
    }
}