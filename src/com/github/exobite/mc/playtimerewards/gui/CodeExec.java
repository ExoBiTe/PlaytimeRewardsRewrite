package com.github.exobite.mc.playtimerewards.gui;

public abstract class CodeExec {

    private Object param;

    public CodeExec() {};

    public CodeExec(Object param) {
        this.param = param;
    }

    public abstract Object execCode();

    public CodeExec setParam(Object param) {
        this.param = param;
        return this;
    }

    public Object getParam() {
        return param;
    }

}
