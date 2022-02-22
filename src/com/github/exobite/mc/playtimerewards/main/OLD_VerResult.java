package com.github.exobite.mc.playtimerewards.main;

public enum OLD_VerResult {

    UP_TO_DATE(1, 13, 0),
    NO_PARTICLES_SOUND(1, 9, 0),
    OUTDATED(0, 0, 0),
    UNKNOWN_VERSION(0, 0, 0);

    private int minMajor = 0;
    private int minMinor = 0;
    private int minPatch = 0;

    private OLD_VerResult(int minMajor, int minMinor, int minPatch){
        this.minMajor = minMajor;
        this.minMinor = minMinor;
        this.minPatch = minPatch;
    }

    public int getMinMajor() {
        return minMajor;
    }

    public int getMinMinor() {
        return minMinor;
    }

    public int getMinPatch() {
        return minPatch;
    }

    public String getVersion() {
        StringBuilder sb = new StringBuilder(minMajor).append(".").append(minMinor);
        if(minPatch>0) sb.append(".").append(minPatch);
        return sb.toString();
    }

}
