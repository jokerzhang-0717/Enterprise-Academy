package com.joker.emp.storage;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "emp.minio")
public class MinioProperties {

  private String endpoint = "http://127.0.0.1:9000";
  private String accessKey = "zhang";
  private String secretKey = "12345678";
  private String bucket = "emp-academy";
  private int presignExpiryMinutes = 60;

  /** 是否将桶设置为公共只读（演示环境建议 true，生产请改用预签名 URL）。 */
  private boolean publicRead = true;
}
