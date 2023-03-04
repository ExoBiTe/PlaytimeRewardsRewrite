package com.github.exobite.mc.playtimerewards.sql;

import com.github.exobite.mc.playtimerewards.main.Config;
import com.github.exobite.mc.playtimerewards.main.PluginMaster;

import java.sql.Connection;

public class SQLMain {

    private static SQLMain instance;

    public SQLMain getInstance() {
        return instance;
    }

    public void setupSQLMain(PluginMaster main) {
        if(instance!=null) return;
        instance = new SQLMain(main);
    }

    private final PluginMaster mainInst;
    private boolean enable;
    private Connection con;


    private SQLMain(PluginMaster mainInst) {
        this.mainInst = mainInst;
        enable = Config.getInstance().useSQL();
    }


}
