package com.yz.springbootdemo.util;

import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.List;

/**
 * @author yanzhu
 * @date 2020/6/23
 * @description
 */
public class FileUtil {

    // MS Word/Excel (xls.or.doc/docx)，文件头
    private static List<String> wordFileHeads = Arrays.asList("D0CF11", "504B03");

    /**
     * 文件读取
     * @param filePath
     * @return
     */
    public static String readFileContent(String filePath) {
        BufferedReader br = null;
        String res = "";
        try {
            String line = null;
            StringBuffer bufAll = new StringBuffer(); //保存修改过后的所有内容，不断增加
            br = new BufferedReader(new FileReader(filePath));
            while ((line = br.readLine()) != null) {
                //修改内容核心代码
                String output = unicode2String(line);
                bufAll.append(output);
            }
            res = bufAll.toString();
            // 特殊处理：最后一个字符为";"，需要剔除
            String last = res.substring(res.length()-1, res.length());
            if (StringUtils.isNoneBlank(res) && ";".equals(last)) {
                res = res.substring(0,res.length()-1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    br = null;
                }
            }
        }
        return res;
    }

    /**
     * 文件写入
     * @param filePath
     * @param content
     */
    public static void writeFile(String filePath, String content) {
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(filePath));
            bw.write(content);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException e) {
                    bw = null;
                }
            }
        }
    }

    /**
     * unicode字符处理
     * @param str
     * @return
     * @throws UnsupportedEncodingException
     */
    public static String unicode2String(String str) throws UnsupportedEncodingException {
        String[] unicodeArr = str.split(";");
        StringBuffer string = new StringBuffer();
        for (String unicode : unicodeArr) {
            if (unicode.startsWith("&#x")) {
                String[] hex = unicode.replace("&#x", "").split(";");
                for (int i = 0; i < hex.length; i++) {
                    int data = Integer.parseInt(hex[i], 16);
                    string.append((char) data);
                }
            } else if (unicode.startsWith("&#")) {
                String[] hex = unicode.replace("&#", "").split(";");
                for (int i = 0; i < hex.length; i++) {
                    int data = Integer.parseInt(hex[i], 10);
                    string.append((char) data);
                }
            } else {
                // 原内容不变
                string.append(unicode);
                string.append(";");
            }
        }
        // 特殊处理
        String result = string.toString();
        // 解决问题：URLDecoder: Illegal hex characters in escape (%) pattern - For input string: "&#"
        result = result.replaceAll("%(?![0-9a-fA-F]{2})", "%25");
        result = result.replaceAll("\\+", "%2B");
        // 转码
        result = URLDecoder.decode(result, "utf-8");
        result = result.replaceAll("&amp;","&");
        // 特殊处理：最后一个字符为";"，需要剔除
        String last = result.substring(result.length() - 1, result.length());
        if (";".equals(last)) {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }

    /**
     * 删除文件
     * @param file
     */
    public static void deleteFile(File file) {
        if (file != null && file.exists()) {
            file.delete();
        }
    }

    /**
     * 获取文件头
     * @param src
     * @return
     */
    public static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder();
        if (src == null || src.length <= 0) {
            return "";
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }

    /**
     * 判断是否word：根据文件头
     * @param file
     * @return
     */
    public static boolean isWord(File file) {
        try {
            FileInputStream is = new FileInputStream(file);
            byte[] b = new byte[3];
            is.read(b, 0, b.length);
            String fileHead = bytesToHexString(b).toUpperCase();
            return wordFileHeads.contains(fileHead);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}

