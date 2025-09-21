package com.gdse.aad.backend.service.impl;

import com.gdse.aad.backend.entity.LostItem;
import com.gdse.aad.backend.repository.FoundItemRepository;
import com.gdse.aad.backend.repository.GuestRepository;
import com.gdse.aad.backend.repository.LostItemRepository;
import com.gdse.aad.backend.service.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {

    private final LostItemRepository lostItemRepository;
    private final FoundItemRepository foundItemRepository;
    private final GuestRepository guestRepository;

    // --- Admin stats ---
    @Override
    public Map<String, Long> getAdminStats() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("lostItems", lostItemRepository.count()); // total lost items
        stats.put("foundItems", foundItemRepository.count()); // total found items
        stats.put("matchedItems", lostItemRepository.countByStatus(LostItem.Status.MATCHED)); // matched lost items
        stats.put("guests", guestRepository.count()); // total registered guests
        return stats;
    }

    // --- Staff stats ---
    @Override
    public Map<String, Long> getStaffStats(String email) {
        Map<String, Long> stats = new HashMap<>();
        stats.put("lostItems", lostItemRepository.count()); // staff see all lost items
        stats.put("foundItems", foundItemRepository.count()); // staff see all found items
        stats.put("matchedItems", lostItemRepository.countByStatus(LostItem.Status.MATCHED)); // all matched
        return stats;
    }

    // --- Guest stats ---
    @Override
    public Map<String, Long> getGuestStats(String email) {
        Map<String, Long> stats = new HashMap<>();
        stats.put("lostItems", lostItemRepository.countByGuestEmail(email)); // only guest's lost items
        stats.put("matchedItems", lostItemRepository.countByGuestEmailAndStatus(email, LostItem.Status.MATCHED)); // only guest's matched
        return stats;
    }
}
