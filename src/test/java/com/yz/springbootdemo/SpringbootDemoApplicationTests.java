package com.yz.springbootdemo;

import com.alibaba.fastjson.JSONObject;
import com.yz.springbootdemo.util.GoogleDriveApi3Util;
import okhttp3.*;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jodconverter.DocumentConverter;
import org.jodconverter.office.OfficeException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@SpringBootTest
class SpringbootDemoApplicationTests {
    @Autowired
    private GoogleDriveApi3Util googleDriveApi3Util;

    @Autowired
    private DocumentConverter documentConverter;

    /**
     *  GoogleDriveApi3：word转html
     */
    @Test
    void testGoogleDrive() {
        String name = "高中数学试卷2020年05月07日.docx";
        name = String.format("/Users/yanzhu/Downloads/其他/1/%s", name);
        java.io.File uploadFile = new java.io.File(name);
        Map res = googleDriveApi3Util.covertDoc2Html(uploadFile, null);
        System.out.println(res);
    }

    /**
     * OpenOffice文件转化
     * 1、pom依赖配置：3个包
     * 2、yml文件jodconverter相关配置，注意"office-home"指定安装等openOffice目录不要错了
     * 3、中文乱码问题：参考https://blog.csdn.net/laoyang360/article/details/73555598/
     * @throws OfficeException
     */
    @Test
    void testOpenOffice() throws OfficeException {
        // 源文件 （office）
        String name = "高中数学试卷2020年05月07日.docx";
        File source = new File(String.format("/Users/yanzhu/Downloads/其他/1/%s", name));
        // 目标文件 （html）
        File target = new File(String.format("/Users/yanzhu/Downloads/其他/转换对比/OpenOffice-%s.html", name));
        // 转换文件
        if (!target.exists()) {
            documentConverter.convert(source).to(target).execute();
        }
    }

    /**
     * OkHttpClient：请求接口
     */
    @Test
    void testOkHttpClient() {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS) //设置连接超时时间
                .readTimeout(60, TimeUnit.SECONDS) //设置读取超时时间
                .build();
        // TODO 测试文件链接
        String downloadLink = "http://www.baidu.com";
        FormBody requestBody = new FormBody.Builder()
                .add("downloadLink", downloadLink)
                .add("fileName", "test.doc")
                .add("fileId", "")
                .build();
        String url = "http://resource.test.seewo.com/api/googleapis/v1/resource/googleapis/covertLink2Html";
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .addHeader("content-type", "application/json;charset=UTF-8")
                .addHeader("cache-control", "no-cache")
                .build();
        try {
            Call call = client.newCall(request);
            Response response = call.execute();
            if (response.isSuccessful()) {
                //解析返回的json数据
                String StringTemp = response.body().string();
                JSONObject jsonObjectTemp = (JSONObject) JSONObject.parse(StringTemp);
                Map resultMap = new HashMap();
                resultMap.put("content", jsonObjectTemp.getString("content"));
                resultMap.put("fileId", jsonObjectTemp.getString("fileId"));
                System.out.println(resultMap);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Java HttpClient 发送multipart/form-data文件的Post请求
     * @throws IOException
     */
    @Test
    void testMultipartFile() throws IOException {
        String sURL = "http://ppt2courseware-dev.test.seewo.com/testpptx2courseware/ConvertWordFile";
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost uploadFile = new HttpPost(sURL);
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.addTextBody("field1", "yes", ContentType.TEXT_PLAIN);

        // 把文件加到HTTP的post请求中
        File f = new File("/Users/yanzhu/Downloads/其他/1/高中数学试卷2020年05月07日.docx");
        builder.addBinaryBody(
                "file",
                new FileInputStream(f),
                ContentType.APPLICATION_OCTET_STREAM,
                f.getName()
        );

        HttpEntity multipart = builder.build();
        uploadFile.setEntity(multipart);
        CloseableHttpResponse response = httpClient.execute(uploadFile);
        HttpEntity responseEntity = response.getEntity();
        String sResponse = EntityUtils.toString(responseEntity, "UTF-8");
        System.out.println("Post 返回结果"+sResponse);
    }
}
