package com.yz.springbootdemo.util;


import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import java.io.*;
import java.util.Collections;
import java.util.List;

@Deprecated
public class GoogleDriveApi3Util {
    private static final String APPLICATION_NAME = "Google Drive API Java Quickstart";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    private static String REDIRECT_URI = "http://127.0.0.1:8080";

    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     * 【注意】：改变后要删除 /tokens/StoredCredential
     */
    private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE);
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

    // 构建driver
    private Drive service;
    {
        try {
            // Build a new authorized API client service.
            final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT)).setApplicationName(APPLICATION_NAME).build();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates an authorized Credential object.
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // Load client secrets.
        InputStream in = GoogleDriveApi3Util.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline").build();

        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    /**
     * 查询文件
     * @throws Exception
     */
    public void getFiles() throws Exception {
        // Print the names and IDs for up to 10 files.
        FileList result = service.files().list().setPageSize(10).setFields("nextPageToken, files(id, name)").execute();
        List<File> files = result.getFiles();
        if (files == null || files.isEmpty()) {
            System.out.println("No files found.");
        } else {
            System.out.println("Files:");
            for (File file : files) {
                System.out.printf("%s (%s)\n", file.getName(), file.getId());
            }
        }
    }

    /**
     * word转html-指定本地文件位置
     * @param uploadFile
     * @return
     */
    public String covertDoc2Html(java.io.File uploadFile) {
        try {
            // 上传文件
            File fileMetadata = new File();
            fileMetadata.setMimeType("application/vnd.google-apps.file");
            // doc
            FileContent mediaContent = new FileContent("application/vnd.openxmlformats-officedocument.wordprocessingml.document", uploadFile);
            // 富文本 FileContent mediaContent = new FileContent("application/msword", filePath);
            File file = service.files().create(fileMetadata, mediaContent).setFields("id").execute();
            System.out.println("File ID: " + file.getId());//14lLjnWN9bWDs58sS-ec7JQBHIvoBX-w2xIw_Z5fA194
            // 下载文件
            String fp = String.format("/Users/yanzhu/Downloads/其他/转换/%s-2.html", uploadFile.getName());
            //String path = System.getProperty("user.dir") + "tmp";
            //String fp = String.format("%s/%s-covert.html", path, uploadFile.getName());
            OutputStream outputStream = new FileOutputStream(fp);
            //OutputStream outputStream = new ByteArrayOutputStream();
            service.files().export(file.getId(), "text/html").executeMediaAndDownloadTo(outputStream);

            // FileUtil.writeFile(fp, content);
            // 修改文件内容
            String content = FileUtil.readFileContent(fp);
            // 删除文件：使用完后删除
            //FileUtil.deleteFile(fp);
            return content;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * word转html-不在本地生成文件
     * @param uploadFile
     * @return
     */
    public String covertDoc2Html2(java.io.File uploadFile) {
        try {
            // 上传文件
            File fileMetadata = new File();
            fileMetadata.setMimeType("application/vnd.google-apps.file");
            // doc
            FileContent mediaContent = new FileContent("application/vnd.openxmlformats-officedocument.wordprocessingml.document", uploadFile);
            File file = service.files().create(fileMetadata, mediaContent).setFields("id").execute();
            // 下载文件
            OutputStream outputStream = new ByteArrayOutputStream();
            service.files().export(file.getId(), "text/html").executeMediaAndDownloadTo(outputStream);
            String content = outputStream.toString();
            // unicode字符处理
            content = FileUtil.unicode2String(content);
            return content;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static void main(String... args) throws Exception {
        GoogleDriveApi3Util util = new GoogleDriveApi3Util();
        String name = "语文4.doc";
        java.io.File uploadFile = new java.io.File(String.format("/Users/yanzhu/Downloads/其他/原文件/%s", name));
        System.out.println(util.covertDoc2Html(uploadFile));
    }
}