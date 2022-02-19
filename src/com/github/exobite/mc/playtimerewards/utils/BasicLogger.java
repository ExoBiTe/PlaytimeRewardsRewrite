package com.github.exobite.mc.playtimerewards.utils;

import java.util.logging.Logger;

public class BasicLogger {

    private static BasicLogger inst;

    private Logger log;

    private BasicLogger() {
        if (inst != null) return;
        inst = this;
        log = Logger.getLogger("Basic Logger");
    }

    private void sInfo(String msg){
        log.info(msg);
    }

    private static BasicLogger getLogger(){
        return inst == null ? new BasicLogger() : inst;
    }

    public static void sendInfo(String msg) {
        getLogger().sInfo(msg);
    }

}
