package com.tangrun.mdm.boxwindow.utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import lombok.extern.log4j.Log4j2;

import java.io.*;
import java.net.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

@Log4j2
public class Utils {

    public static String readFile(File file) {
        try (FileReader fileReader = new FileReader(file)) {
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            StringBuilder stringBuilder = new StringBuilder();
            String line = null;
            int i = 0;
            while ((line = bufferedReader.readLine()) != null) {
                i++;
                if (i != 0) stringBuilder.append("\n");
                stringBuilder.append(line);
            }
            return stringBuilder.toString();
        } catch (Exception e) {
            log.error(e.getMessage(),e);
        }
        return null;
    }

    public static String sha(String text, String type) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance(type);
            messageDigest.update(text.getBytes());
            byte[] byteBuffer = messageDigest.digest();
            StringBuilder strHexString = new StringBuilder();
            for (byte b : byteBuffer) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    strHexString.append('0');
                }
                strHexString.append(hex);
            }
            return strHexString.toString();
        } catch (NoSuchAlgorithmException e) {
            log.error(e.getMessage(),e);
        }
        return null;
    }

    public static String encode(String s) {
        // a-z 97-122
        // A-Z 65-90
        // 0-9 48-57
        char[] chars = s.toCharArray();
        char[] newChars = new char[chars.length];
        for (int i = 0; i < chars.length; i++) {
            char origin = chars[i];
            if (origin <= 122 && origin >= 97) {
                int offset = origin - 97;
                origin = (char) (90 - offset);
            } else if (origin <= 90 && origin >= 65) {
                int offset = origin - 65;
                origin = (char) (122 - offset);
            } else if (origin <= 57 && origin >= 48) {
                origin = (char) (105 - origin);
            }
            newChars[i] = origin;
        }
        return new String(newChars);
    }


    //region


    private static String SerialNumber;

    public static String getMachineCode() {
        if (SerialNumber == null) {
            SerialInfo serialInfo = getSerialInfo();
            String s = serialInfo.getCPUSerial() + " " + serialInfo.getMotherboardSN() + " " + serialInfo.getHardDiskSN();
            SerialNumber = Utils.sha(s, "sha-512");
        }
        return SerialNumber;
    }

    private static SerialInfo getSerialInfo() {
        String os = System.getProperty("os.name");
        os = os.toUpperCase();
        if ("LINUX".equals(os)) {
            return new SerialInfoLinuxImpl();
        } else return new SerialInfoWindowsImpl();
    }

    public static <T extends Parent> T loadFXML(String s) {
        FXMLLoader fxmlLoader = new FXMLLoader(Utils.class.getResource(s));
        try {
            return fxmlLoader.load();
        } catch (IOException e) {
            log.error(e.getMessage(),e);
        }
        return null;
    }

    private interface SerialInfo {
        String getMotherboardSN();

        String getHardDiskSN();

        String getHardDiskSN(String drive);

        String getCPUSerial();

        String getMac();
    }

    private static class SerialInfoWindowsImpl implements SerialInfo {

        @Override
        public String getMotherboardSN() {
            String result = "";
            try {
                File file = File.createTempFile("realhowto", ".vbs");
                file.deleteOnExit();
                FileWriter fw = new FileWriter(file);

                String vbs = "Set objWMIService = GetObject(\"winmgmts:\\\\.\\root\\cimv2\")\n"
                        + "Set colItems = objWMIService.ExecQuery _ \n"
                        + "   (\"Select * from Win32_BaseBoard\") \n"
                        + "For Each objItem in colItems \n"
                        + "    Wscript.Echo objItem.SerialNumber \n"
                        + "    exit for  ' do the first cpu only! \n" + "Next \n";

                fw.write(vbs);
                fw.close();
                String path = file.getPath().replace("%20", " ");

                Process p = Runtime.getRuntime().exec(new String[]{"cscript","//NoLogo",path});
                BufferedReader input = new BufferedReader(new InputStreamReader(
                        p.getInputStream()));
                String line;
                while ((line = input.readLine()) != null) {
                    result += line;
                }
                input.close();
            } catch (Exception e) {
                log.error(e.getMessage(),e);
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
                FileWriter fw = new FileWriter(file);

                String vbs = "Set objFSO = CreateObject(\"Scripting.FileSystemObject\")\n"
                        + "Set colDrives = objFSO.Drives\n"
                        + "Set objDrive = colDrives.item(\""
                        + drive
                        + "\")\n"
                        + "Wscript.Echo objDrive.SerialNumber"; // see note
                fw.write(vbs);
                fw.close();
                String path = file.getPath().replace("%20", " ");
                Process p = Runtime.getRuntime().exec(new String[]{"cscript","//NoLogo",path});
                BufferedReader input = new BufferedReader(new InputStreamReader(
                        p.getInputStream()));
                String line;
                while ((line = input.readLine()) != null) {
                    result += line;
                }
                input.close();
            } catch (Exception e) {
                log.error(e.getMessage(),e);
            }
            return result.trim();
        }

        @Override
        public String getCPUSerial() {
            String result = "";
            try {
                File file = File.createTempFile("tmp", ".vbs");
                file.deleteOnExit();
                FileWriter fw = new FileWriter(file);
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
                Process p = Runtime.getRuntime().exec(new String[]{"cscript","//NoLogo",path});
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
                    resultStr += sb.toString().toUpperCase();
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

    private static class SerialInfoLinuxImpl implements SerialInfo {

        @Override
        public String getMotherboardSN() {
            return getSerialNumber("dmidecode | grep 'Serial Number'", "Serial Number", ":");
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
                log.error(e.getMessage(),e);
            }
            return null;
        }

    }


    //endregion
}
