package com.yz.springbootdemo.util;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author yanzhu
 * @date 2020/6/29
 * @description
 */
public class GoogleDriveUtil {
    private Logger LOGGER = LoggerFactory.getLogger(GoogleDriveUtil.class);
    private static final String APPLICATION_NAME = "Google Drive API Java Quickstart";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    // 文件格式
    private static final String MINE_TYPE = "application/vnd.google-apps.file";
    private static final String CONTENT_TYPE = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
    private static final String HTML_TYPE = "text/html";

    // Google Drive 校验码 【注意：不用每次点击链接授权】
    private static final String authorizationAccessToken = "ya29.a0AfH6SMClGv_Bzf-9iGaBxk_VA85Vg-QAd7PqxDWhpZnH4usY5jQx218WcVcw2v49RHHlfBbpGyVn_VxMsz3vOxvmI2ofg9uXgVBZ9MPkHNhwIfZSAmxIIwe4_IJTZdPEdbdNc-L1aykPN2Dv--Tekd12c0BndA1wVZk";
    private static final String authorizationRefreshToken = "1//0eliBFADrT9JJCgYIARAAGA4SNwF-L9IrMSrE_I3arpSpH86Dr0492b8bgXIPv2p25wLdLvttY9Xv5nSZ1oWFKnakUZbILF74c_I";
    private static final String clientId = "867569619412-gqjnujbemcmk9vlrav61ulna70u3mll0.apps.googleusercontent.com";
    private static final String clientSecret = "yPMJ4mjCSxcDOw3JlMjH5KN6";

    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     * 【注意】：改变后要删除 /tokens/StoredCredential
     */
    private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE);
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

    // 构建NetHttpTransport
    private NetHttpTransport HTTP_TRANSPORT;

    private static volatile GoogleDriveUtil instance = null;

    public static GoogleDriveUtil getInstance(){
        if(instance == null){
            synchronized (GoogleDriveUtil.class) {
                if(instance == null) {
                    instance = new GoogleDriveUtil();
                }
            }
        }
        return instance;
    }

    public GoogleDriveUtil() {
        try {
            // Build a new authorized API client service.
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        } catch (Exception e) {
            LOGGER.error("GoogleDriveUtil.Drive.Builder异常", e);
        }
    }

    /**
     * Creates an authorized Credential object.
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    @Deprecated
    private Credential getCredentials1(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // Load client secrets.
        InputStream in = GoogleDriveUtil.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline").build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();

        // 【授权方式1】TODO 需要服务器点击授权链接跳转授权
        // return new AuthorizationCodeInstalledAppUtil(flow, receiver).authorize("user");

        // 【授权方式2】TODO 配置AccessToken+RefreshToken {生成环境报错：Caused by: java.security.cert.CertificateException: No name matching www.googleapis.com found}
        return new AuthorizationCodeInstalledAppUtil(flow, receiver).authorize("user", authorizationAccessToken, authorizationRefreshToken);
    }

    /**
     * 【授权方式3】TODO 不用token方式
     *  {生成环境报错：Caused by: java.security.cert.CertificateException: No name matching www.googleapis.com found}
     * @param HTTP_TRANSPORT
     * @return
     * @throws IOException
     */
    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // GoogleCredential.Builder
       return new GoogleCredential.Builder()
                .setClientSecrets(clientId, clientSecret)
                .setJsonFactory(JSON_FACTORY).setTransport(HTTP_TRANSPORT).build()
                .setAccessToken(authorizationAccessToken).setRefreshToken(authorizationRefreshToken);
    }

    /**
     * word转html
     * @param uploadFile
     * @param fileId
     * @return
     */
    public Map<String, String> covertDoc2Html(java.io.File uploadFile, String fileId) {
        Map<String, String> res = new HashMap<String, String>();
        try {
            Drive service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT)).setApplicationName(APPLICATION_NAME).build();
            if (StringUtils.isBlank(fileId)) {
                // 上传文件
                File fileMetadata = new File();
                fileMetadata.setMimeType(MINE_TYPE);
                FileContent mediaContent = new FileContent(CONTENT_TYPE, uploadFile);
                File file = service.files().create(fileMetadata, mediaContent).setFields("id").execute();
                // 获取上传文件ID，并加入缓存
                fileId = file.getId();
                res.put("fileId", fileId);
            }
            // 下载文件
            OutputStream outputStream = new ByteArrayOutputStream();
            service.files().export(fileId, HTML_TYPE).executeMediaAndDownloadTo(outputStream);
            String content = outputStream.toString();
            // unicode字符处理
            content = FileUtil.unicode2String(content);
            res.put("content", content);
            return res;
        } catch (Exception e) {
            LOGGER.error("GoogleDriveUtil.covertDoc2Html异常", e);
        } finally {
            // 删除原始文件
            // TODO 不删除  FileUtil.deleteFile(uploadFile);
        }
        return res;
    }

    public static void main(String[] args) {
        String name = "人教版初中英语9年级全册 unit 9  单元检测试卷（含答案）.docx";
        name = String.format("/Users/yanzhu/Downloads/其他/原文件/%s", name);
        java.io.File uploadFile = new java.io.File(name);
        Map res = GoogleDriveUtil.getInstance().covertDoc2Html(uploadFile, null);
        System.out.println(res);
    }
}
