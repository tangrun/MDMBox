package com.tangrun.mdm.boxwindow;

import com.alibaba.fastjson2.JSON;
import com.google.common.hash.Hashing;
import com.google.common.io.BaseEncoding;
import com.google.common.io.CharSource;
import com.tangrun.mdm.boxwindow.pojo.Config;
import com.tangrun.mdm.boxwindow.utils.CharUtil;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class ConfigGenerator {

    public static void main(String[] args) throws ParseException, IOException {
        // safe
        //eyJhcHBOYW1lIjoiUmFpbiIsImNsc05hbWUiOiJjb20udGFuZ3J1bi5zYWZlLmRwbS5EUE1SZXN0cmljdGlvbnNSZWNlaXZlciIsImNvbXBvbmVudCI6eyJjbGFzc05hbWUiOiJjb20udGFuZ3J1bi5zYWZlLmRwbS5EUE1SZXN0cmljdGlvbnNSZWNlaXZlciIsInBhY2thZ2VOYW1lIjoiY29tLnRhbmdydW4uc2FmZSJ9LCJkZXNjIjoiUmFpbiDmv4DmtLvlt6XlhbciLCJwa2dOYW1lIjoiY29tLnRhbmdydW4uc2FmZSJ9
        //VBqSXsylbd8OrQLRfNuKYRrHrNmHX94SYdfRlRqQY79FWtuFa6q8YR4AbdaOoNiDYh4vfv8hacm9XNOQWtOEYMmhadmOZcaOXRrHrNmEYcyEYNeFWxr3VBqQYtuAX94SYdfRlRqQY79FWtuFa6q8YR4AbdaOoNiDYh4vfv8hacm9XNOQWtOEYMmhadmOZcaOXRrHrMySb7GSa7elbd8OrQLRb70GoMiSYNWBWd5FX7uNahq0oxqPacmQrQLRfNuKYRwNE5wNGoEOG3cOSYXRoxqDZ7Wlbd8OrQLRb70GoMiSYNWBWd5FX7uNahq0
        Config config = new Config();
        config.setAppName("Rain");
        config.setDesc("Rain 激活工具");
        config.setPkgName("com.tangrun.safe");
        config.setClsName("com.tangrun.safe.dpm.DPMRestrictionsReceiver");
        config.setMachineId("cba09041d6c315be7e828914d60e29f0cb95beb9bc5ac4c1802485599ed231dc416697b531d8f19f08fbe2e3c41c9b6df56012dfbd5b185efeadbe6e36264031");
        config.setExpireTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2023-12-31 23:59:59").getTime());

        String json = JSON.toJSONString(config);
        String base64 = BaseEncoding.base64().encode(json.getBytes(StandardCharsets.UTF_8));
        String license = CharUtil.encode(base64);
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < license.length(); i += 64) {
            stringBuilder.append(i == 0 ? "" : "\n")
                    .append(license, i, Math.min(i + 64, license.length()));
        }
        String formatLicense = stringBuilder.toString();

        String safetyLicense = formatLicense.replaceAll("[\n|\t|\r]", "");
        String decodedBase64 = CharUtil.encode(safetyLicense);
        String decodedJson = BaseEncoding.base64().decodingSource(CharSource.wrap(decodedBase64)).asCharSource(StandardCharsets.UTF_8)
                .read();

        StringBuilder out = new StringBuilder();
        out
                .append("加密")
                .append("\n\t内容：").append(json)
                .append("\n\tbase64转码后：").append(base64)
                .append("\n\t字符反转后：").append(license)
                .append("\n\t格式化后：").append(formatLicense)
                .append("\n解密")
                .append("\n\t内容：").append(safetyLicense)
                .append("\n\t字符反转后：").append(decodedBase64)
                .append("\n\tbase64解密后：").append(decodedJson)
                ;
        System.out.println(out);
    }


}

