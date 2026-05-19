package com.protectcord.strata.api.event.bus;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method as an event handler to be registered with the
 * {@link com.protectcord.strata.api.event.EventBus}.
 *
 * <p>Annotated methods must accept exactly one parameter whose type is a subtype of
 * {@link com.protectcord.strata.api.event.StrataEvent}.</p>
 *
 * @since 1.0.0
 * @see EventPriority
 * @see com.protectcord.strata.api.event.EventBus
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Subscribe {

    /**
     * The priority at which this handler is invoked relative to other handlers for the same event.
     *
     * @return the handler priority, defaults to {@link EventPriority#NORMAL}
     */
    EventPriority priority() default EventPriority.NORMAL;
}
