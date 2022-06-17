package com.tangrun.mdm.shell.enums;

public enum PackageFilterParam {
    disabled("-d"),
    enabled("-e"),
    system("-s"),
    user("-3"),
    ;
    public final String value;

    PackageFilterParam(String value) {
        this.value = value;
    }
}
