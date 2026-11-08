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
import java.util.Locale;
import java.util.Map;
import java.util.Set;
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

  /** 只允许业务需要的文件类型，避免上传 HTML/SVG/脚本后在应用同源执行。 */
  private static final Map<String, String> ALLOWED_CONTENT_TYPES = Map.ofEntries(
      Map.entry(".jpg", "image/jpeg"),
      Map.entry(".jpeg", "image/jpeg"),
      Map.entry(".png", "image/png"),
      Map.entry(".gif", "image/gif"),
      Map.entry(".webp", "image/webp"),
      Map.entry(".bmp", "image/bmp"),
      Map.entry(".mp4", "video/mp4"),
      Map.entry(".webm", "video/webm"),
      Map.entry(".ogg", "video/ogg"),
      Map.entry(".mov", "video/quicktime"),
      Map.entry(".mp3", "audio/mpeg"),
      Map.entry(".wav", "audio/wav"),
      Map.entry(".m4a", "audio/mp4"),
      Map.entry(".pdf", "application/pdf"),
      Map.entry(".doc", "application/msword"),
      Map.entry(".docx",
          "application/vnd.openxmlformats-officedocument.wordprocessingml.document"),
      Map.entry(".xls", "application/vnd.ms-excel"),
      Map.entry(".xlsx",
          "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
      Map.entry(".ppt", "application/vnd.ms-powerpoint"),
      Map.entry(".pptx",
          "application/vnd.openxmlformats-officedocument.presentationml.presentation"),
      Map.entry(".txt", "text/plain"),
      Map.entry(".zip", "application/zip"),
      Map.entry(".rar", "application/vnd.rar")
  );

  private static final Set<String> ALLOWED_FOLDERS = Set.of(
      "avatar", "cover", "video", "courseware", "attachment", "notice", "common", "misc");

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
    String contentType = resolveContentType(file.getOriginalFilename());
    String objectName = buildObjectName(file.getOriginalFilename(), folder);
    try (InputStream in = file.getInputStream()) {
      client.putObject(PutObjectArgs.builder()
          .bucket(properties.getBucket())
          .object(objectName)
          .stream(in, file.getSize(), -1)
          .contentType(contentType)
          .build());
    } catch (Exception ex) {
      log.error("文件上传失败 object={}", objectName, ex);
      throw new BizException(500, "文件上传失败，请稍后重试");
    }
    return describe(objectName, file.getOriginalFilename(), file.getSize(),
        contentType, publicBaseUrl);
  }

  public Map<String, Object> presignUpload(String fileName, String contentType, String folder,
      String publicBaseUrl) {
    String resolvedContentType = resolveContentType(fileName);
    String objectName = buildObjectName(fileName, folder);
    try {
      String uploadUrl = client.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
          .method(Method.PUT)
          .bucket(properties.getBucket())
          .object(objectName)
          .expiry(properties.getPresignExpiryMinutes(), TimeUnit.MINUTES)
          .build());
      Map<String, Object> result = describe(objectName, fileName, null, resolvedContentType,
          publicBaseUrl);
      result.put("uploadUrl", uploadUrl);
      result.put("method", "PUT");
      return result;
    } catch (Exception ex) {
      log.error("生成上传地址失败 object={}", objectName, ex);
      throw new BizException(500, "生成上传地址失败，请稍后重试");
    }
  }

  public String presignDownload(String objectName) {
    String safeObjectName = requireObjectName(objectName);
    try {
      return client.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
          .method(Method.GET)
          .bucket(properties.getBucket())
          .object(safeObjectName)
          .expiry(properties.getPresignExpiryMinutes(), TimeUnit.MINUTES)
          .build());
    } catch (Exception ex) {
      log.error("生成下载地址失败 object={}", safeObjectName, ex);
      throw new BizException(500, "生成下载地址失败，请稍后重试");
    }
  }

  public void delete(String objectName) {
    if (objectName == null || objectName.isBlank()) {
      return;
    }
    String safeObjectName = requireObjectName(objectName);
    try {
      client.removeObject(RemoveObjectArgs.builder()
          .bucket(properties.getBucket())
          .object(safeObjectName)
          .build());
    } catch (Exception ex) {
      log.error("文件删除失败 object={}", safeObjectName, ex);
      throw new BizException(500, "文件删除失败，请稍后重试");
    }
  }

  public InputStream open(String objectName) {
    String safeObjectName = requireObjectName(objectName);
    try {
      return client.getObject(GetObjectArgs.builder()
          .bucket(properties.getBucket())
          .object(safeObjectName)
          .build());
    } catch (Exception ex) {
      log.debug("读取文件失败 object={}", safeObjectName, ex);
      throw BizException.notFound("文件不存在");
    }
  }

  public InputStream open(String objectName, long offset, long length) {
    String safeObjectName = requireObjectName(objectName);
    try {
      return client.getObject(GetObjectArgs.builder()
          .bucket(properties.getBucket())
          .object(safeObjectName)
          .offset(offset)
          .length(length)
          .build());
    } catch (Exception ex) {
      log.debug("分段读取文件失败 object={}", safeObjectName, ex);
      throw BizException.notFound("文件不存在");
    }
  }

  public StatObjectResponse stat(String objectName) {
    String safeObjectName = requireObjectName(objectName);
    try {
      return client.statObject(StatObjectArgs.builder()
          .bucket(properties.getBucket())
          .object(safeObjectName)
          .build());
    } catch (Exception ex) {
      log.debug("读取文件元数据失败 object={}", safeObjectName, ex);
      throw BizException.notFound("文件不存在");
    }
  }

  /** 按扩展名解析受信任的 Content-Type，不能使用上传时客户端声明的类型。 */
  public String resolveContentType(String fileName) {
    String extension = extension(fileName);
    String contentType = ALLOWED_CONTENT_TYPES.get(extension);
    if (contentType == null) {
      throw new BizException("不支持的文件类型");
    }
    return contentType;
  }

  /** 只有图片、音视频和 PDF 允许内联展示，其余一律作为附件下载。 */
  public boolean isInlineSafe(String fileName) {
    String contentType = ALLOWED_CONTENT_TYPES.get(extension(fileName));
    return contentType != null && (contentType.startsWith("image/")
        || contentType.startsWith("video/")
        || contentType.startsWith("audio/")
        || "application/pdf".equals(contentType));
  }

  public String objectFileName(String objectName) {
    String safeObjectName = requireObjectName(objectName);
    return safeObjectName.substring(safeObjectName.lastIndexOf('/') + 1);
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
    String ext = extension(originalName);
    String safeFolder = normalizeFolder(folder);
    return safeFolder + "/" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM")) + "/"
        + UUID.randomUUID().toString().replace("-", "") + ext;
  }

  private String normalizeFolder(String folder) {
    if (folder == null || folder.isBlank()) {
      return "misc";
    }
    String cleaned = folder.trim().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9/_-]", "");
    if (cleaned.isEmpty()) {
      return "misc";
    }
    if (!ALLOWED_FOLDERS.contains(cleaned)) {
      throw new BizException("不支持的文件目录");
    }
    return cleaned;
  }

  private String extension(String name) {
    if (name == null) {
      return "";
    }
    String clean = name.replace('\\', '/');
    int slash = clean.lastIndexOf('/');
    if (slash >= 0) {
      clean = clean.substring(slash + 1);
    }
    int dot = clean.lastIndexOf('.');
    if (dot < 0 || dot == clean.length() - 1) {
      return "";
    }
    return clean.substring(dot).toLowerCase(Locale.ROOT);
  }

  private String requireObjectName(String objectName) {
    if (objectName == null || objectName.isBlank()) {
      throw new BizException("缺少文件标识");
    }
    String name = objectName.trim();
    if (name.startsWith("/") || name.contains("..") || name.contains("\\")) {
      throw new BizException("非法的文件标识");
    }
    return name;
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
