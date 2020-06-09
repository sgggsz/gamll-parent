package com.atguigu.gmall.product.test;

import org.csource.common.MyException;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;

import java.io.IOException;

/**
 * TestDfs
 *
 * @Author: 郭思钊
 * @CreateTime: 2020-06-03
 * @Description:
 */
public class TestDfs {
    public static void main(String[] args) throws IOException, MyException {

        fileUpload();
    }

private static String fileUpload() throws IOException, MyException{
    String path = TestDfs.class.getClassLoader().getResource("tracker.conf").getPath();

    System.out.println(path);

    ClientGlobal.init(path);
    //创建tracker链接
    TrackerClient trackerClient = new TrackerClient();
    TrackerServer connection = trackerClient.getConnection();

    //创建storage
    StorageClient storageClient = new StorageClient(connection,null);

    String[] jpgs = storageClient.upload_appender_file("D:/GSZtest/a.jpg", "jpg", null);
    String imageUrl = "http://192.168.200.128:8080/";
    for (String jpg : jpgs) {
        imageUrl = imageUrl+"/"+jpg;
    }
    return imageUrl;
}
}