package com.su60.quickboot.data.file.strategy;

import com.su60.quickboot.common.exception.Assert;
import com.su60.quickboot.data.file.AbstractFileTemplate;
import com.su60.quickboot.data.file.FileInfoVo;
import com.su60.quickboot.data.file.FileProperties;
import io.minio.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

/**
 * MinIO 存储
 *
 * @author luyanan
 * @since 2025/01/07
 */
@Slf4j
@RequiredArgsConstructor
public class MinioFileTemplateStorage extends AbstractFileTemplate {

    private final FileProperties fileProperties;

    private MinioClient buildClient() {
        FileProperties.S3FileProperties s3 = fileProperties.getS3();
        Assert.notNull(s3, "s3配置文件不能为空");
        MinioClient.Builder builder = MinioClient.builder()
                .endpoint(s3.getEndpoint())
                .credentials(s3.getAccessKey(), s3.getSecretKey());
        return builder.build();
    }

    @Override
    protected void doUpload(byte[] bytes, FileInfoVo fileInfoVo) {
        FileProperties.S3FileProperties s3 = fileProperties.getS3();
        Assert.notNull(s3, "s3配置文件不能为空");
        String objectName = fileInfoVo.getPath() + "/" + fileInfoVo.getFileName();
        try {
            MinioClient client = buildClient();
            // bucket 不存在则创建
            boolean exists = client.bucketExists(BucketExistsArgs.builder().bucket(s3.getBucket()).build());
            if (!exists) {
                client.makeBucket(MakeBucketArgs.builder().bucket(s3.getBucket()).build());
            }
            client.putObject(PutObjectArgs.builder()
                    .bucket(s3.getBucket())
                    .object(objectName)
                    .stream(new ByteArrayInputStream(bytes), bytes.length, -1)
                    .contentType(fileInfoVo.getContentType())
                    .build());
        } catch (Exception e) {
            throw new RuntimeException("MinIO 上传失败", e);
        }
    }

    @Override
    protected byte[] doDownload(String fileName) {
        FileProperties.S3FileProperties s3 = fileProperties.getS3();
        Assert.notNull(s3, "s3配置文件不能为空");
        try {
            MinioClient client = buildClient();
            GetObjectResponse resp = client.getObject(GetObjectArgs.builder()
                    .bucket(s3.getBucket())
                    .object(fileName)
                    .build());
            return resp.readAllBytes();
        } catch (Exception e) {
            throw new RuntimeException("MinIO 下载失败", e);
        }
    }
}


