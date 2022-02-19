package com.github.exobite.mc.playtimerewards.gui;

import java.util.Map;

public interface guiHolder {
    Map<String, GUIManagerOLD.GUI> getGuis();

    Map<String, String> getPlaceholders();

    void addGui(String internalName, GUIManagerOLD.GUI Gui);
}
