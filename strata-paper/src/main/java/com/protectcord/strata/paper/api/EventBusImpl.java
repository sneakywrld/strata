package com.protectcord.strata.paper.api;

import com.protectcord.strata.api.event.EventBus;
import com.protectcord.strata.api.event.StrataEvent;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Implementation of the Strata event bus.
 */
public final class EventBusImpl implements EventBus {

    private final Map<Class<?>, List<PrioritizedHandler<?>>> handlers = new ConcurrentHashMap<>();

    @Override
    public <T extends StrataEvent> void subscribe(Class<T> eventType, Consumer<T> handler) {
        subscribe(eventType, 0, handler);
    }

    @Override
    public <T extends StrataEvent> void subscribe(Class<T> eventType, int priority, Consumer<T> handler) {
        handlers.computeIfAbsent(eventType, k -> Collections.synchronizedList(new ArrayList<>()))
                .add(new PrioritizedHandler<>(priority, handler));
    }

    @Override
    public <T extends StrataEvent> void unsubscribe(Class<T> eventType, Consumer<T> handler) {
        List<PrioritizedHandler<?>> list = handlers.get(eventType);
        if (list != null) {
            list.removeIf(ph -> ph.handler == handler);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends StrataEvent> T fire(T event) {
        List<PrioritizedHandler<?>> list = handlers.get(event.getClass());
        if (list == null) return event;

        List<PrioritizedHandler<?>> sorted = new ArrayList<>(list);
        sorted.sort(Comparator.comparingInt(PrioritizedHandler::priority));

        for (PrioritizedHandler<?> ph : sorted) {
            ((Consumer<T>) ph.handler).accept(event);
        }

        return event;
    }

    private record PrioritizedHandler<T>(int priority, Consumer<T> handler) {}
}
