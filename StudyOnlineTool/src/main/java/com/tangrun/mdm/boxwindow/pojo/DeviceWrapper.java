package com.tangrun.mdm.boxwindow.pojo;


import com.tangrun.mdm.boxwindow.shell.pojo.Device;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeviceWrapper {
    private Device device;
    private Map<String,String> props;

    public String getDeviceId(){
        return props.getOrDefault("ro.serialno","null");
    }

    public String getAndroidVersion(){
        return props.getOrDefault("ro.build.version.release","未知版本");
    }

    public String getPhoneManufacturer(){
        return props.getOrDefault("ro.product.brand","未知厂商");
    }

    public String getPhoneModel(){
        return props.getOrDefault("ro.product.model","未知型号");
    }

}
