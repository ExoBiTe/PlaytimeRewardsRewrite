package com.github.exobite.mc.playtimerewards.rewards;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RewardManager {

    private static RewardManager instance;

    public static RewardManager getInstance() {
        if(instance==null) {
            instance = new RewardManager();
        }
        return instance;
    }

    private List<Reward> registeredRewards = new ArrayList<>();

    private RewardManager(){}



}
