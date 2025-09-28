package com.gdse.aad.backend.util;

import com.gdse.aad.backend.entity.FoundItem;
import com.gdse.aad.backend.entity.LostItem;

public class ItemMatcher {

    private ItemMatcher() {
        // utility class → prevent instantiation
    }

    public static boolean isMatch(LostItem lost, FoundItem found) {
        String lostTitle = lost.getTitle() != null ? lost.getTitle().toLowerCase() : "";
        String foundTitle = found.getTitle() != null ? found.getTitle().toLowerCase() : "";

        // Exact match OR one contains the other
        if (lostTitle.equals(foundTitle)) return true;      // exact match
        if (lostTitle.contains(foundTitle) || foundTitle.contains(lostTitle)) return true;      // one contains the other

        // Description-based check
        String lostDesc = lost.getDescription() != null ? lost.getDescription().toLowerCase() : "";
        String foundDesc = found.getDescription() != null ? found.getDescription().toLowerCase() : "";

        return !lostDesc.isEmpty() && !foundDesc.isEmpty() &&
                (lostDesc.contains(foundDesc) || foundDesc.contains(lostDesc));     // both descriptions exist and one contains the other
    }
}
