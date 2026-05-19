package com.protectcord.strata.paper.guide;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class GuideRenderer {

    private static final Pattern LINK_PATTERN = Pattern.compile("\\{link:(\\w+)}");
    private static final String SEPARATOR = "----------------------------------------";

    private GuideRenderer() {}

    public static void renderPage(Player player, GuidePage page) {
        player.sendMessage(Component.empty());

        player.sendMessage(Component.text(SEPARATOR, NamedTextColor.DARK_GRAY));
        player.sendMessage(
                Component.text(" Strata Guide: ", NamedTextColor.GOLD, TextDecoration.BOLD)
                        .append(Component.text(page.title(), NamedTextColor.YELLOW, TextDecoration.BOLD))
                        .append(Component.text(" (" + page.pageNumber() + "/" + page.totalPages() + ")",
                                NamedTextColor.GRAY).decoration(TextDecoration.BOLD, false))
        );
        player.sendMessage(Component.text(SEPARATOR, NamedTextColor.DARK_GRAY));

        for (String line : page.content()) {
            player.sendMessage(renderLine(line));
        }

        player.sendMessage(Component.text(SEPARATOR, NamedTextColor.DARK_GRAY));
        player.sendMessage(buildNavigationBar(page));

        List<String> related = page.relatedTopics();
        if (!related.isEmpty()) {
            TextComponent.Builder builder = Component.text()
                    .append(Component.text(" See also: ", NamedTextColor.GRAY));
            for (int i = 0; i < related.size(); i++) {
                if (i > 0) {
                    builder.append(Component.text(", ", NamedTextColor.GRAY));
                }
                String topic = related.get(i);
                builder.append(Component.text(topic, NamedTextColor.AQUA)
                        .clickEvent(ClickEvent.runCommand("/strata guide " + topic))
                        .hoverEvent(HoverEvent.showText(
                                Component.text("Open guide: " + topic, NamedTextColor.YELLOW))));
            }
            player.sendMessage(builder.build());
        }
    }

    private static Component renderLine(String line) {
        if (line.isEmpty()) {
            return Component.empty();
        }

        if (line.startsWith("# ")) {
            return Component.text(" " + line.substring(2), NamedTextColor.GOLD, TextDecoration.BOLD);
        }

        if (line.startsWith("> ")) {
            return Component.text(" " + line.substring(2), NamedTextColor.GREEN);
        }

        if (line.startsWith("$ ")) {
            String command = line.substring(2);
            return Component.text(" " + command, NamedTextColor.AQUA)
                    .clickEvent(ClickEvent.suggestCommand(command))
                    .hoverEvent(HoverEvent.showText(
                            Component.text("Click to insert command", NamedTextColor.YELLOW)));
        }

        return renderInlineLinks(line);
    }

    private static Component renderInlineLinks(String line) {
        Matcher matcher = LINK_PATTERN.matcher(line);
        if (!matcher.find()) {
            return Component.text(" " + line, NamedTextColor.WHITE);
        }

        TextComponent.Builder builder = Component.text().append(Component.text(" "));
        int lastEnd = 0;
        matcher.reset();
        while (matcher.find()) {
            if (matcher.start() > lastEnd) {
                builder.append(Component.text(line.substring(lastEnd, matcher.start()), NamedTextColor.WHITE));
            }
            String topic = matcher.group(1);
            builder.append(Component.text("[" + topic + "]", NamedTextColor.AQUA)
                    .clickEvent(ClickEvent.runCommand("/strata guide " + topic))
                    .hoverEvent(HoverEvent.showText(
                            Component.text("Open guide: " + topic, NamedTextColor.YELLOW))));
            lastEnd = matcher.end();
        }
        if (lastEnd < line.length()) {
            builder.append(Component.text(line.substring(lastEnd), NamedTextColor.WHITE));
        }
        return builder.build();
    }

    private static Component buildNavigationBar(GuidePage page) {
        TextComponent.Builder bar = Component.text().append(Component.text(" "));

        if (page.pageNumber() > 1) {
            bar.append(Component.text("[Previous]", NamedTextColor.GREEN)
                    .clickEvent(ClickEvent.runCommand(
                            "/strata guide " + page.topic() + " " + (page.pageNumber() - 1)))
                    .hoverEvent(HoverEvent.showText(
                            Component.text("Page " + (page.pageNumber() - 1), NamedTextColor.YELLOW))));
        } else {
            bar.append(Component.text("[Previous]", NamedTextColor.DARK_GRAY));
        }

        bar.append(Component.text("  ", NamedTextColor.GRAY));

        bar.append(Component.text("[Topics]", NamedTextColor.YELLOW)
                .clickEvent(ClickEvent.runCommand("/strata guide"))
                .hoverEvent(HoverEvent.showText(
                        Component.text("Back to topic list", NamedTextColor.YELLOW))));

        bar.append(Component.text("  ", NamedTextColor.GRAY));

        if (page.pageNumber() < page.totalPages()) {
            bar.append(Component.text("[Next]", NamedTextColor.GREEN)
                    .clickEvent(ClickEvent.runCommand(
                            "/strata guide " + page.topic() + " " + (page.pageNumber() + 1)))
                    .hoverEvent(HoverEvent.showText(
                            Component.text("Page " + (page.pageNumber() + 1), NamedTextColor.YELLOW))));
        } else {
            bar.append(Component.text("[Next]", NamedTextColor.DARK_GRAY));
        }

        return bar.build();
    }
}
