package io.github.genkidoudou.common.file;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.http.Method;

/**
 * MinIO 存储；依赖 {@code io.minio:minio}（可选依赖，运行时由类路径决定）。
 */
public class MinioFileStorageBackend implements FileStorageOperations {

    private final MinioClient client;
    private final String bucket;

    public MinioFileStorageBackend(QcFileProperties props) {
        props.validateMinioIfNeeded();
        QcFileProperties.MinioProperties m = props.getMinio();
        MinioClient.Builder b = MinioClient.builder()
                .endpoint(m.getEndpoint())
                .credentials(m.getAccessKey(), m.getSecretKey());
        this.client = b.build();
        this.bucket = m.getBucket();
    }

    private void ensureBucket() throws Exception {
        if (!client.bucketExists(BucketExistsArgs.builder().bucket(bucket).build())) {
            client.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
        }
    }

    @Override
    public void put(String relativePath, InputStream inputStream, long size, String contentType) throws Exception {
        ensureBucket();
        PutObjectArgs.Builder builder = PutObjectArgs.builder()
                .bucket(bucket)
                .object(relativePath)
                .stream(inputStream, size, -1);
        if (contentType != null && !contentType.isEmpty()) {
            builder.contentType(contentType);
        }
        client.putObject(builder.build());
    }

    @Override
    public InputStream openStream(String relativePath) throws Exception {
        return client.getObject(GetObjectArgs.builder().bucket(bucket).object(relativePath).build());
    }

    @Override
    public void remove(String relativePath) throws Exception {
        client.removeObject(RemoveObjectArgs.builder().bucket(bucket).object(relativePath).build());
    }

    @Override
    public boolean objectExists(String relativePath) throws Exception {
        try {
            client.statObject(StatObjectArgs.builder().bucket(bucket).object(relativePath).build());
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    @Override
    public String presignedGetUrl(String relativePath, int expireSeconds) throws Exception {
        return client.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                .method(Method.GET)
                .bucket(bucket)
                .object(relativePath)
                .expiry(expireSeconds, TimeUnit.SECONDS)
                .build());
    }
}
