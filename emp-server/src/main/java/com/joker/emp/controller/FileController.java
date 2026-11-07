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
import org.springframework.http.HttpStatus;
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
   * 稳定访问地址：/api/files/view/{objectName}，支持视频播放器的 Range 分段读取。
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
    long fileSize = stat.size();
    ByteRange range;
    try {
      range = parseRange(request.getHeader(HttpHeaders.RANGE), fileSize);
    } catch (IllegalArgumentException ex) {
      return ResponseEntity.status(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE)
          .header(HttpHeaders.ACCEPT_RANGES, "bytes")
          .header(HttpHeaders.CONTENT_RANGE, "bytes */" + fileSize)
          .build();
    }

    boolean partial = range != null;
    long start = partial ? range.start() : 0;
    long length = partial ? range.end() - range.start() + 1 : fileSize;
    InputStreamResource resource = new InputStreamResource(
        partial ? storageService.open(objectName, start, length) : storageService.open(objectName));

    ResponseEntity.BodyBuilder response = ResponseEntity.status(
            partial ? HttpStatus.PARTIAL_CONTENT : HttpStatus.OK)
        .header(HttpHeaders.ACCEPT_RANGES, "bytes")
        .header(HttpHeaders.CONTENT_TYPE, contentType)
        .contentLength(length)
        .header(HttpHeaders.CACHE_CONTROL, "public, max-age=86400")
        .header(HttpHeaders.CONTENT_DISPOSITION,
            "inline; filename=\"" + objectName.substring(objectName.lastIndexOf('/') + 1) + "\"");
    if (partial) {
      response.header(HttpHeaders.CONTENT_RANGE,
          "bytes " + range.start() + "-" + range.end() + "/" + fileSize);
    }
    return response.contentType(MediaType.parseMediaType(contentType)).body(resource);
  }

  private ByteRange parseRange(String header, long fileSize) {
    if (header == null || header.isBlank()) {
      return null;
    }
    if (fileSize <= 0 || !header.startsWith("bytes=") || header.indexOf(',') >= 0) {
      throw new IllegalArgumentException("Invalid byte range");
    }

    String[] bounds = header.substring("bytes=".length()).split("-", -1);
    if (bounds.length != 2) {
      throw new IllegalArgumentException("Invalid byte range");
    }
    try {
      long start;
      long end;
      if (bounds[0].isEmpty()) {
        long suffixLength = Long.parseLong(bounds[1]);
        if (suffixLength <= 0) {
          throw new IllegalArgumentException("Invalid byte range");
        }
        start = Math.max(0, fileSize - suffixLength);
        end = fileSize - 1;
      } else {
        start = Long.parseLong(bounds[0]);
        end = bounds[1].isEmpty() ? fileSize - 1 : Long.parseLong(bounds[1]);
        if (start < 0 || start >= fileSize || end < start) {
          throw new IllegalArgumentException("Invalid byte range");
        }
        end = Math.min(end, fileSize - 1);
      }
      return new ByteRange(start, end);
    } catch (NumberFormatException ex) {
      throw new IllegalArgumentException("Invalid byte range", ex);
    }
  }

  private record ByteRange(long start, long end) {}

  public record PresignUploadRequest(String fileName, String contentType, String folder) {
  }
}
