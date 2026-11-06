package com.joker.emp.controller;

import com.joker.emp.common.ApiResponse;
import com.joker.emp.service.CheckinService;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/checkins")
public class CheckinController {

  private final CheckinService checkinService;

  public CheckinController(CheckinService checkinService) {
    this.checkinService = checkinService;
  }

  @GetMapping
  public ApiResponse<List<Map<String, Object>>> myList(
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
    return ApiResponse.ok(checkinService.myList(from, to));
  }

  @GetMapping("/today")
  public ApiResponse<Map<String, Object>> today() {
    return ApiResponse.ok(checkinService.today());
  }

  @PostMapping("/check-in")
  public ApiResponse<Map<String, Object>> checkIn(@RequestBody(required = false) CheckinAction request) {
    return ApiResponse.ok(checkinService.checkIn(
        request == null ? null : request.location(),
        request == null ? null : request.method()));
  }

  @PostMapping("/check-out")
  public ApiResponse<Map<String, Object>> checkOut() {
    return ApiResponse.ok(checkinService.checkOut());
  }

  @GetMapping("/stats")
  public ApiResponse<List<Map<String, Object>>> stats(
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
    return ApiResponse.ok(checkinService.stats(from, to));
  }

  public record CheckinAction(String location, Integer method) {
  }
}
