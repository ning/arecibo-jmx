package com.ning.arecibo.jmx;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.weakref.jmx.ManagedAnnotation;

@Retention(RetentionPolicy.RUNTIME)
@Target( { ElementType.METHOD })
@ManagedAnnotation
public @interface Monitored
{
    public static final String DEFAULT_DESCRIPTION = "";
    public static final String DEFAULT_EVENT_ATTRIBUTE_NAME = "";
    public static final String DEFAULT_EVENT_NAME = "";
    public static final String DEFAULT_EVENT_NAME_PATTERN = ".*[Nn]ame=([a-zA-Z0-9_]*).*";
    public static final MonitoringType[] DEFAULT_MONITORING_TYPE = { MonitoringType.VALUE };

    String description() default DEFAULT_DESCRIPTION;
    String eventAttributeName() default DEFAULT_EVENT_ATTRIBUTE_NAME;
    String eventName() default DEFAULT_EVENT_NAME;
    String eventNamePattern() default DEFAULT_EVENT_NAME_PATTERN;
    MonitoringType[] monitoringType() default { MonitoringType.VALUE };
}
