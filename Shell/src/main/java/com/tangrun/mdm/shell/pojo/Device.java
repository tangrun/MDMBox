package com.tangrun.mdm.shell.pojo;

public class Device{
    public  String id;
    public  DeviceState state;

    @Override
    public String toString() {
        return "Device{" +
                "id='" + id + '\'' +
                ", state=" + state.desc +
                '}';
    }

    public enum DeviceState {
        device("device","已连接"),
        unauthorized("unauthorized","已连接但未授权"),
        offline("offline","设备已离线"),
        unknown("unknown","未知状态"),
        ;
        String value;
        String desc;

        DeviceState(String value,String desc) {
            this.value = value;
            this.desc = desc;
        }


        @Override
        public String toString() {
            return value;
        }
    }
}
