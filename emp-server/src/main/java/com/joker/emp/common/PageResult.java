package com.joker.emp.common;

import com.baomidou.mybatisplus.core.metadata.IPage;
import java.util.List;

/**
 * 分页结果。
 */
public record PageResult<T>(long total, long page, long size, List<T> records) {

  public static <T> PageResult<T> of(IPage<T> page) {
    return new PageResult<>(page.getTotal(), page.getCurrent(), page.getSize(), page.getRecords());
  }

  public static <T> PageResult<T> of(long total, long page, long size, List<T> records) {
    return new PageResult<>(total, page, size, records);
  }
}
