package com.joker.emp.storage;

import com.joker.emp.common.BizException;
import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.SetBucketPolicyArgs;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import io.minio.http.Method;
import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * MinIO 文件存储：上传、预签名、下载、删除。
 * 数据库里保存稳定的 /api/files/view/{objectName} 地址，避免预签名 URL 过期。
 */
@Service
public class StorageService {

  private static final Logger log = LoggerFactory.getLogger(StorageService.class);

  private final MinioClient client;
  private final MinioProperties properties;

  public StorageService(MinioClient client, MinioProperties properties) {
    this.client = client;
    this.properties = properties;
  }

  @PostConstruct
  public void ensureBucket() {
    String bucket = properties.getBucket();
    try {
      boolean exists = client.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
      if (!exists) {
        client.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
        log.info("MinIO bucket created: {}", bucket);
      }
      if (properties.isPublicRead()) {
        client.setBucketPolicy(SetBucketPolicyArgs.builder()
            .bucket(bucket)
            .config(publicReadPolicy(bucket))
            .build());
      }
    } catch (Exception ex) {
      log.warn("MinIO 初始化失败（请确认服务已启动且账号正确）：{}", ex.getMessage());
    }
  }

  public Map<String, Object> upload(MultipartFile file, String folder, String publicBaseUrl) {
    if (file == null || file.isEmpty()) {
      throw new BizException("请选择要上传的文件");
    }
    String objectName = buildObjectName(file.getOriginalFilename(), folder);
    try (InputStream in = file.getInputStream()) {
      client.putObject(PutObjectArgs.builder()
          .bucket(properties.getBucket())
          .object(objectName)
          .stream(in, file.getSize(), -1)
          .contentType(file.getContentType() == null ? "application/octet-stream" : file.getContentType())
          .build());
    } catch (Exception ex) {
      throw new BizException(500, "文件上传失败：" + ex.getMessage());
    }
    return describe(objectName, file.getOriginalFilename(), file.getSize(),
        file.getContentType(), publicBaseUrl);
  }

  public Map<String, Object> presignUpload(String fileName, String contentType, String folder,
      String publicBaseUrl) {
    String objectName = buildObjectName(fileName, folder);
    try {
      String uploadUrl = client.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
          .method(Method.PUT)
          .bucket(properties.getBucket())
          .object(objectName)
          .expiry(properties.getPresignExpiryMinutes(), TimeUnit.MINUTES)
          .build());
      Map<String, Object> result = describe(objectName, fileName, null, contentType, publicBaseUrl);
      result.put("uploadUrl", uploadUrl);
      result.put("method", "PUT");
      return result;
    } catch (Exception ex) {
      throw new BizException(500, "生成上传地址失败：" + ex.getMessage());
    }
  }

  public String presignDownload(String objectName) {
    try {
      return client.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
          .method(Method.GET)
          .bucket(properties.getBucket())
          .object(objectName)
          .expiry(properties.getPresignExpiryMinutes(), TimeUnit.MINUTES)
          .build());
    } catch (Exception ex) {
      throw new BizException(500, "生成下载地址失败：" + ex.getMessage());
    }
  }

  public void delete(String objectName) {
    if (objectName == null || objectName.isBlank()) {
      return;
    }
    try {
      client.removeObject(RemoveObjectArgs.builder()
          .bucket(properties.getBucket())
          .object(objectName)
          .build());
    } catch (Exception ex) {
      throw new BizException(500, "文件删除失败：" + ex.getMessage());
    }
  }

  public InputStream open(String objectName) {
    try {
      return client.getObject(GetObjectArgs.builder()
          .bucket(properties.getBucket())
          .object(objectName)
          .build());
    } catch (Exception ex) {
      throw BizException.notFound("文件不存在：" + objectName);
    }
  }

  public InputStream open(String objectName, long offset, long length) {
    try {
      return client.getObject(GetObjectArgs.builder()
          .bucket(properties.getBucket())
          .object(objectName)
          .offset(offset)
          .length(length)
          .build());
    } catch (Exception ex) {
      throw BizException.notFound("文件不存在：" + objectName);
    }
  }

  public StatObjectResponse stat(String objectName) {
    try {
      return client.statObject(StatObjectArgs.builder()
          .bucket(properties.getBucket())
          .object(objectName)
          .build());
    } catch (Exception ex) {
      throw BizException.notFound("文件不存在：" + objectName);
    }
  }

  public String directUrl(String objectName) {
    String endpoint = properties.getEndpoint().replaceAll("/+$", "");
    return endpoint + "/" + properties.getBucket() + "/" + encodePath(objectName);
  }

  private Map<String, Object> describe(String objectName, String originalName, Long size,
      String contentType, String publicBaseUrl) {
    String base = publicBaseUrl == null ? "" : publicBaseUrl.replaceAll("/+$", "");
    Map<String, Object> result = new LinkedHashMap<>();
    result.put("objectName", objectName);
    result.put("fileName", originalName);
    result.put("size", size);
    result.put("contentType", contentType);
    result.put("url", base + "/files/view/" + encodePath(objectName));
    result.put("directUrl", directUrl(objectName));
    result.put("presignedUrl", presignDownload(objectName));
    return result;
  }

  private String buildObjectName(String originalName, String folder) {
    String ext = "";
    if (originalName != null) {
      int dot = originalName.lastIndexOf('.');
      if (dot >= 0 && dot < originalName.length() - 1) {
        ext = originalName.substring(dot).toLowerCase();
      }
    }
    String safeFolder = (folder == null || folder.isBlank())
        ? "misc"
        : folder.replaceAll("[^a-zA-Z0-9/_-]", "");
    return safeFolder + "/" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM")) + "/"
        + UUID.randomUUID().toString().replace("-", "") + ext;
  }

  private String encodePath(String objectName) {
    String[] parts = objectName.split("/");
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < parts.length; i++) {
      if (i > 0) {
        sb.append('/');
      }
      sb.append(URLEncoder.encode(parts[i], StandardCharsets.UTF_8).replace("+", "%20"));
    }
    return sb.toString();
  }

  private String publicReadPolicy(String bucket) {
    return """
        {
          "Version": "2012-10-17",
          "Statement": [
            {
              "Effect": "Allow",
              "Principal": {"AWS": ["*"]},
              "Action": ["s3:GetObject"],
              "Resource": ["arn:aws:s3:::%s/*"]
            }
          ]
        }
        """.formatted(bucket);
  }
}
