package com.yz.springbootdemo.util;


import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author yanzhu
 * @date 2020/6/16
 * @description
 */
@Component
public class GoogleDriveApi3Util {
    private static Logger LOGGER = LoggerFactory.getLogger(GoogleDriveApi3Util.class);
    private static final String APPLICATION_NAME = "Google Drive API Java Quickstart";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    // 文件格式
    private static final String MINE_TYPE = "application/vnd.google-apps.file";
    private static final String CONTENT_TYPE = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
    private static final String HTML_TYPE = "text/html";
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";
    // 授权
    @Value("${googleapis.clientId}")
    private String clientId;
    @Value("${googleapis.clientSecret}")
    private String clientSecret;
    @Value("${googleapis.accessToken}")
    private String accessToken;
    @Value("${googleapis.refreshToken}")
    private String refreshToken;

    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     * 【注意】：改变后要删除 /tokens/StoredCredential
     */
    private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE);

    /**
     * Creates an authorized Credential object.
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    private Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // GoogleCredential.Builder
        return new GoogleCredential.Builder()
                .setClientSecrets(clientId, clientSecret)
                .setJsonFactory(JSON_FACTORY).setTransport(HTTP_TRANSPORT).build()
                .setAccessToken(accessToken).setRefreshToken(refreshToken);
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
            // Build a new authorized API client service.
            NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
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
            LOGGER.error("GoogleDriveApi3Util.covertDoc2Html异常", e);
        } finally {
            // TODO 删除原始文件 FileUtil.deleteFile(uploadFile);
        }
        return res;
    }
}