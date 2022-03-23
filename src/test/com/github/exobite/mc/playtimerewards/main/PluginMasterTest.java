package com.github.exobite.mc.playtimerewards.main;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.logging.Level;

public class PluginMasterTest {

    private ServerMock server;
    private PluginMaster plugin;

    @BeforeAll
    public void setUp()
    {
        server = MockBukkit.mock();
        plugin = (PluginMaster) MockBukkit.load(PluginMaster.class);
    }

    @Test
    public void firstTest() {
        PluginMaster.sendConsoleMessage(Level.INFO, "Test is running!");
    }

    @AfterAll
    public void tearDown()
    {
        MockBukkit.unmock();
    }

}