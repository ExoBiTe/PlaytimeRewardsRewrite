package com.github.exobite.mc.playtimerewards.rewards;

public record RewardData(String rewardName,
                         RewardType type,
                         boolean isRepeating,
                         boolean grantFirst) {
}

