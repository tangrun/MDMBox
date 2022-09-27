package com.tangrun.mdm.boxwindow.utils;

import com.google.common.hash.Hashing;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class SerialNumberUtil {

    private interface SerialInfo{
        String getMotherboardSN();
        String getHardDiskSN();
        String getHardDiskSN(String drive);
        String getCPUSerial();
        String getMac();
    }

    private static class SerialInfoWindowsImpl implements SerialInfo{

        @Override
        public String getMotherboardSN() {
            String result = "";
            try {
                File file = File.createTempFile("realhowto", ".vbs");
                file.deleteOnExit();
                FileWriter fw = new java.io.FileWriter(file);

                String vbs = "Set objWMIService = GetObject(\"winmgmts:\\\\.\\root\\cimv2\")\n"
                        + "Set colItems = objWMIService.ExecQuery _ \n"
                        + "   (\"Select * from Win32_BaseBoard\") \n"
                        + "For Each objItem in colItems \n"
                        + "    Wscript.Echo objItem.SerialNumber \n"
                        + "    exit for  ' do the first cpu only! \n" + "Next \n";

                fw.write(vbs);
                fw.close();
                String path = file.getPath().replace("%20", " ");
                Process p = Runtime.getRuntime().exec(
                        "cscript //NoLogo " + path);
                BufferedReader input = new BufferedReader(new InputStreamReader(
                        p.getInputStream()));
                String line;
                while ((line = input.readLine()) != null) {
                    result += line;
                }
                input.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return result.trim();
        }

        @Override
        public String getHardDiskSN() {
            return getHardDiskSN("c");
        }

        @Override
        public String getHardDiskSN(String drive) {
            String result = "";
            try {
                File file = File.createTempFile("realhowto", ".vbs");
                file.deleteOnExit();
                FileWriter fw = new java.io.FileWriter(file);

                String vbs = "Set objFSO = CreateObject(\"Scripting.FileSystemObject\")\n"
                        + "Set colDrives = objFSO.Drives\n"
                        + "Set objDrive = colDrives.item(\""
                        + drive
                        + "\")\n"
                        + "Wscript.Echo objDrive.SerialNumber"; // see note
                fw.write(vbs);
                fw.close();
                String path = file.getPath().replace("%20", " ");
                Process p = Runtime.getRuntime().exec(
                        "cscript //NoLogo " + path);
                BufferedReader input = new BufferedReader(new InputStreamReader(
                        p.getInputStream()));
                String line;
                while ((line = input.readLine()) != null) {
                    result += line;
                }
                input.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return result.trim();
        }

        @Override
        public String getCPUSerial() {
            String result = "";
            try {
                File file = File.createTempFile("tmp", ".vbs");
                file.deleteOnExit();
                FileWriter fw = new java.io.FileWriter(file);
                String vbs = "Set objWMIService = GetObject(\"winmgmts:\\\\.\\root\\cimv2\")\n"
                        + "Set colItems = objWMIService.ExecQuery _ \n"
                        + "   (\"Select * from Win32_Processor\") \n"
                        + "For Each objItem in colItems \n"
                        + "    Wscript.Echo objItem.ProcessorId \n"
                        + "    exit for  ' do the first cpu only! \n" + "Next \n";

                // + "    exit for  \r\n" + "Next";
                fw.write(vbs);
                fw.close();
                String path = file.getPath().replace("%20", " ");
                Process p = Runtime.getRuntime().exec(
                        "cscript //NoLogo " + path);
                BufferedReader input = new BufferedReader(new InputStreamReader(
                        p.getInputStream()));
                String line;
                while ((line = input.readLine()) != null) {
                    result += line;
                }
                input.close();
                file.delete();
            } catch (Exception e) {
                e.fillInStackTrace();
            }
            if (result.trim().length() < 1 || result == null) {
                result = "无CPU_ID被读取";
            }
            return result.trim();
        }

        @Override
        public String getMac() {
            try {
                String resultStr = "";
                List<String> ls = getLocalHostLANAddress();
                for (String str : ls) {
                    InetAddress ia = InetAddress.getByName(str);// 获取本地IP对象
                    // 获得网络接口对象（即网卡），并得到mac地址，mac地址存在于一个byte数组中。
                    byte[] mac = NetworkInterface.getByInetAddress(ia)
                            .getHardwareAddress();
                    // 下面代码是把mac地址拼装成String
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < mac.length; i++) {
                        if (i != 0) {
                            sb.append("-");
                        }
                        // mac[i] & 0xFF 是为了把byte转化为正整数
                        String s = Integer.toHexString(mac[i] & 0xFF);
                        sb.append(s.length() == 1 ? 0 + s : s);
                    }
                    // 把字符串所有小写字母改为大写成为正规的mac地址并返回
                    resultStr += sb.toString().toUpperCase() ;
                }
                return resultStr;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return null;
        }


        private List<String> getLocalHostLANAddress() throws UnknownHostException, SocketException {
            List<String> ips = new ArrayList<String>();
            Enumeration<NetworkInterface> interfs = NetworkInterface.getNetworkInterfaces();
            while (interfs.hasMoreElements()) {
                NetworkInterface interf = interfs.nextElement();
                Enumeration<InetAddress> addres = interf.getInetAddresses();
                while (addres.hasMoreElements()) {
                    InetAddress in = addres.nextElement();
                    if (in instanceof Inet4Address) {
                        System.out.println("v4:" + in.getHostAddress());
                        if (!"127.0.0.1".equals(in.getHostAddress())) {
                            ips.add(in.getHostAddress());
                        }
                    }
                }
            }
            return ips;
        }

    }

    private static class SerialInfoLinuxImpl implements SerialInfo{

        @Override
        public String getMotherboardSN() {
            return getSerialNumber("dmidecode |grep 'Serial Number'", "Serial Number", ":");
        }

        @Override
        public String getHardDiskSN() {
            return getSerialNumber("fdisk -l", "Disk identifier", ":");
        }

        @Override
        public String getHardDiskSN(String drive) {
            return getHardDiskSN();
        }

        @Override
        public String getCPUSerial() {
            return getSerialNumber("dmidecode -t processor | grep 'ID'", "ID", ":");
        }

        @Override
        public String getMac() {
            return getSerialNumber("ifconfig -a", "ether", " ");
        }


        /**
         * @param cmd    命令语句
         * @param record 要查看的字段
         * @param symbol 分隔符
         * @return
         */
        public String getSerialNumber(String cmd, String record, String symbol) {
            String execResult = executeLinuxCmd(cmd);
            String[] infos = execResult.split("\n");

            for (String info : infos) {
                info = info.trim();
                if (info.indexOf(record) != -1) {
                    info.replace(" ", "");
                    String[] sn = info.split(symbol);
                    return sn[1];
                }
            }

            return null;
        }

        private String executeLinuxCmd(String cmd) {
            try {
                System.out.println("got cmd job : " + cmd);
                Runtime run = Runtime.getRuntime();
                Process process;
                process = run.exec(cmd);
                InputStream in = process.getInputStream();
                BufferedReader bs = new BufferedReader(new InputStreamReader(in));
                StringBuffer out = new StringBuffer();
                byte[] b = new byte[8192];
                for (int n; (n = in.read(b)) != -1; ) {
                    out.append(new String(b, 0, n));
                }

                in.close();
                process.destroy();
                return out.toString();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

    }

    private static String SerialNumber;


    private static SerialInfo getSerialInfo(){
        String os = System.getProperty("os.name");
        os = os.toUpperCase();
        if ("LINUX".equals(os)) {
            return new SerialInfoLinuxImpl();
        }else return new SerialInfoWindowsImpl();
    }

    /**
     * 获取机器码
     *
     * @return
     */
    public static String getMachineCode() {
        if (SerialNumber == null) {
            SerialInfo serialInfo = getSerialInfo();
            String s = serialInfo.getCPUSerial() +" "+ serialInfo.getMotherboardSN()  +" "+  serialInfo.getHardDiskSN();
            SerialNumber = Hashing.sha512().hashString(s,StandardCharsets.UTF_8).toString();
        }
        return SerialNumber;
    }

}
