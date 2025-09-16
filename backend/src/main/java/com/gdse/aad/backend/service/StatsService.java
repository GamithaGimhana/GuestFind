package com.gdse.aad.backend.service;

import java.util.Map;

public interface StatsService {
    Map<String, Long> getAdminStats();
    Map<String, Long> getStaffStats(String email);
    Map<String, Long> getGuestStats(String email);
}
