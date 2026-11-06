package com.joker.emp.controller;

import com.joker.emp.common.ApiResponse;
import com.joker.emp.storage.StorageService;
import io.minio.StatObjectResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/files")
public class FileController {

  private final StorageService storageService;

  public FileController(StorageService storageService) {
    this.storageService = storageService;
  }

  /** 小文件（头像、图片、PDF）服务端转发上传。 */
  @PostMapping("/upload")
  public ApiResponse<Map<String, Object>> upload(
      @RequestParam("file") MultipartFile file,
      @RequestParam(value = "folder", required = false) String folder) {
    String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
    return ApiResponse.ok(storageService.upload(file, folder, baseUrl));
  }

  /** 大文件（课程视频）直传：服务端只签发预签名 PUT 地址。 */
  @PostMapping("/presign-upload")
  public ApiResponse<Map<String, Object>> presignUpload(@RequestBody PresignUploadRequest request) {
    String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
    return ApiResponse.ok(storageService.presignUpload(
        request.fileName(), request.contentType(), request.folder(), baseUrl));
  }

  @GetMapping("/presign")
  public ApiResponse<Map<String, Object>> presignDownload(@RequestParam String objectName) {
    return ApiResponse.ok(Map.of(
        "objectName", objectName,
        "url", storageService.presignDownload(objectName)
    ));
  }

  @DeleteMapping
  public ApiResponse<Void> delete(@RequestParam String objectName) {
    storageService.delete(objectName);
    return ApiResponse.ok();
  }

  /**
   * 稳定访问地址：/api/files/view/{objectName}。
   * 该路径在鉴权白名单中，便于 <img>/<video>/<a download> 直接访问。
   */
  @GetMapping("/view/**")
  public ResponseEntity<InputStreamResource> view(HttpServletRequest request) {
    String uri = request.getRequestURI();
    String prefix = request.getContextPath() + "/files/view/";
    String raw = uri.startsWith(prefix) ? uri.substring(prefix.length()) : uri;
    String objectName = URLDecoder.decode(raw, StandardCharsets.UTF_8);

    StatObjectResponse stat = storageService.stat(objectName);
    String contentType = stat.contentType() == null ? "application/octet-stream" : stat.contentType();
    InputStreamResource resource = new InputStreamResource(storageService.open(objectName));

    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_TYPE, contentType)
        .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(stat.size()))
        .header(HttpHeaders.CACHE_CONTROL, "public, max-age=86400")
        .header(HttpHeaders.CONTENT_DISPOSITION,
            "inline; filename=\"" + objectName.substring(objectName.lastIndexOf('/') + 1) + "\"")
        .contentType(MediaType.parseMediaType(contentType))
        .body(resource);
  }

  public record PresignUploadRequest(String fileName, String contentType, String folder) {
  }
}
