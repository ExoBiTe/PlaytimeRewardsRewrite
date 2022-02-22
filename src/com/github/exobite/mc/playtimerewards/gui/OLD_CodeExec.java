package com.github.exobite.mc.playtimerewards.gui;

public abstract class OLD_CodeExec {

    private Object param;

    public OLD_CodeExec() {};

    public OLD_CodeExec(Object param) {
        this.param = param;
    }

    public abstract Object execCode();

    public OLD_CodeExec setParam(Object param) {
        this.param = param;
        return this;
    }

    public Object getParam() {
        return param;
    }

}
