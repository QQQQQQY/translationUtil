package com.translation.plugin

class ColumnModel {

    public def name // 这个类必须有个字段为 name.
    public def columnNum

    ColumnModel(String name) {
        this.name = name
    }

    @Override
    String toString() {
        return "ColumnModel{" +
                "name=" + name +
                ", columnNum=" + columnNum +
                '}'
    }
}