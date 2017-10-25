package com.fitbit.api.common.model.units;

public enum VolumeUnits {
    ML("ml"),
    FL_OZ("fl oz"),
    CUP("cup");

    String text;

    VolumeUnits(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }


}
