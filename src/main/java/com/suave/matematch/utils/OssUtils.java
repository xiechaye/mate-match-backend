package com.suave.matematch.utils;

import java.io.*;
import java.util.Random;

import com.aliyun.oss.*;
import com.aliyun.oss.model.*;
import com.aliyun.oss.common.auth.*;
import com.aliyun.oss.common.comm.SignVersion;
import com.suave.matematch.common.ErrorCode;
import com.suave.matematch.exception.BusinessException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class OssUtils {
    // 1. 非静态字段用于接收Spring的注入
    @Value("${alibaba.oss.endpoint}")
    private String instanceEndpoint;
    @Value("${alibaba.oss.bucket-name}")
    private String instanceBucketName;
    @Value("${alibaba.oss.access-key-id}")
    private String instanceAccessKeyId;
    @Value("${alibaba.oss.access-key-secret}")
    private String instanceAccessKeySecret;
    @Value("${alibaba.oss.region}")
    private String instanceRegion;

    // 2. 你的静态字段
    private static String endpoint;
    private static String bucketName;
    private static String accessKeyId;
    private static String accessKeySecret;
    private static String region;

    // 3. 使用@PostConstruct初始化静态字段
    @PostConstruct
    public void init() {
        OssUtils.endpoint = this.instanceEndpoint;
        OssUtils.bucketName = this.instanceBucketName;
        OssUtils.accessKeyId = this.instanceAccessKeyId;
        OssUtils.accessKeySecret = this.instanceAccessKeySecret;
        OssUtils.region = this.instanceRegion;

    }

    /**
     * 上传文件
     *
     * @return
     */
    public static String uploadImage(byte[] bytes, String objectName) {
        CredentialsProvider credentialsProvider = new DefaultCredentialProvider(accessKeyId, accessKeySecret);

        // 使用credentialsProvider初始化客户端
        ClientBuilderConfiguration clientBuilderConfiguration = new ClientBuilderConfiguration();
        // 显式声明使用 V4 签名算法
        clientBuilderConfiguration.setSignatureVersion(SignVersion.V4);
        // 创建OSSClient实例。
        OSS ossClient = OSSClientBuilder.create()
                .endpoint(endpoint)
                .credentialsProvider(credentialsProvider)
                .clientConfiguration(clientBuilderConfiguration)
                .region(region)
                .build();

        try {
            ossClient.putObject(bucketName, objectName, new ByteArrayInputStream(bytes));
            StringBuilder stringBuilder = new StringBuilder("https://");
            stringBuilder
                    .append(bucketName)
                    .append(".")
                    .append(endpoint)
                    .append("/")
                    .append(objectName);

            log.info("文件上传到:{}", stringBuilder.toString());

            return stringBuilder.toString();
        } catch (OSSException oe) {
            log.error("Caught an OSSException, which means your request made it to OSS, "
                    + "but was rejected with an error response for some reason.");
            log.error("Error Message:" + oe.getErrorMessage());
            log.error("Error Code:" + oe.getErrorCode());
            log.error("Request ID:" + oe.getRequestId());
            log.error("Host ID:" + oe.getHostId());
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "文件上传失败");
        } catch (ClientException ce) {
            log.error("Caught an ClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with OSS, "
                    + "such as not being able to access the network.");
            log.error("Error Message:" + ce.getMessage());
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "文件上传失败");
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }

    }


}