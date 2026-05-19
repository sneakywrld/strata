package com.protectcord.strata.paper.guide;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Logger;

public final class GuideRegistry {

    private static final int LINES_PER_PAGE = 10;
    private static final List<String> TOPIC_FILES = List.of(
            "setup", "profiles", "biomes", "noise", "structures",
            "features", "ores", "saplings", "zones", "water", "api", "troubleshooting"
    );

    private final Map<String, List<GuidePage>> pages = new LinkedHashMap<>();
    private final Logger logger;

    public GuideRegistry(Logger logger) {
        this.logger = logger;
    }

    public void loadFromResources() {
        pages.clear();
        for (String topic : TOPIC_FILES) {
            String path = "guide/" + topic + ".txt";
            try (InputStream is = getClass().getClassLoader().getResourceAsStream(path)) {
                if (is == null) {
                    logger.warning("Guide resource not found: " + path);
                    continue;
                }
                List<String> lines = readLines(is);
                List<GuidePage> parsed = paginate(topic, lines);
                pages.put(topic, parsed);
            } catch (IOException e) {
                logger.warning("Failed to load guide topic '" + topic + "': " + e.getMessage());
            }
        }
        logger.info("Loaded " + pages.size() + " guide topic(s)");
    }

    public Optional<GuidePage> getPage(String topic, int page) {
        List<GuidePage> topicPages = pages.get(topic.toLowerCase());
        if (topicPages == null || page < 1 || page > topicPages.size()) {
            return Optional.empty();
        }
        return Optional.of(topicPages.get(page - 1));
    }

    public List<String> getTopics() {
        return List.copyOf(pages.keySet());
    }

    private List<String> readLines(InputStream is) throws IOException {
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        }
        return lines;
    }

    private List<GuidePage> paginate(String topic, List<String> allLines) {
        String title = topic.substring(0, 1).toUpperCase() + topic.substring(1);
        List<String> relatedTopics = extractRelatedTopics(allLines);

        List<List<String>> chunks = new ArrayList<>();
        List<String> current = new ArrayList<>();
        for (String line : allLines) {
            current.add(line);
            if (current.size() >= LINES_PER_PAGE) {
                chunks.add(current);
                current = new ArrayList<>();
            }
        }
        if (!current.isEmpty()) {
            chunks.add(current);
        }
        if (chunks.isEmpty()) {
            chunks.add(List.of("No content available."));
        }

        int total = chunks.size();
        List<GuidePage> result = new ArrayList<>(total);
        for (int i = 0; i < total; i++) {
            result.add(new GuidePage(topic, i + 1, total, title, chunks.get(i), relatedTopics));
        }
        return result;
    }

    private List<String> extractRelatedTopics(List<String> lines) {
        Set<String> related = new LinkedHashSet<>();
        for (String line : lines) {
            int idx = 0;
            while ((idx = line.indexOf("{link:", idx)) != -1) {
                int end = line.indexOf('}', idx);
                if (end != -1) {
                    String linked = line.substring(idx + 6, end).trim().toLowerCase();
                    related.add(linked);
                    idx = end + 1;
                } else {
                    break;
                }
            }
        }
        return List.copyOf(related);
    }
}
