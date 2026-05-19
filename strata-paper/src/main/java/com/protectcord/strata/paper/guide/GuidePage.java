package com.protectcord.strata.paper.guide;

import java.util.List;

public record GuidePage(
        String topic,
        int pageNumber,
        int totalPages,
        String title,
        List<String> content,
        List<String> relatedTopics
) {}
