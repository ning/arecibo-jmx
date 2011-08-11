package com.ning.arecibo.jmx;

import java.util.ArrayList;
import java.util.List;

import com.ning.arecibo.jmx.AreciboProfile;
import com.ning.arecibo.jmx.Monitored;
import com.ning.arecibo.jmx.MonitoringItem;
import com.ning.arecibo.jmx.MonitoringType;

/**
 * Helper class that makes it simple to register arbitrary mbeans with arecibo.
 */
public class MBeanRegistrar
{
    private final String mbeanName;
    private final List<MonitoringItem> items = new ArrayList<MonitoringItem>();
    private String eventName;

    public MBeanRegistrar(String mbeanName)
    {
        this.mbeanName = mbeanName;
        // org.mortbay.jetty.handler:type=contexthandlercollection,id=0 -> org.mortbay.jetty.handler.contexthandlercollection-0
        // java.lang:type=MemoryPool,name=CMS Old Gen -> java.lang.MemoryPool-CMS_Old_Gen
        this.eventName = mbeanName.replace(":type=", ".")
                                  .replace("name=", "-")
                                  .replace("id=", "-")
                                  .replaceAll("\\s+", "_")
                                  .replace(",", "");
    }

    public MBeanRegistrar setEventName(String eventName)
    {
        this.eventName = eventName;
        return this;
    }

    public MBeanRegistrar addValue(String attrName)
    {
        MonitoringItem item = new MonitoringItem(attrName,
                                                 attrName,
                                                 Monitored.DEFAULT_EVENT_NAME_PATTERN,
                                                 eventName,
                                                 new MonitoringType[] { MonitoringType.VALUE },
                                                 null);
        items.add(item);
        return this;
    }

    public MBeanRegistrar addCounter(String attrName)
    {
        MonitoringItem item = new MonitoringItem(attrName,
                                                 attrName,
                                                 Monitored.DEFAULT_EVENT_NAME_PATTERN,
                                                 eventName,
                                                 new MonitoringType[] { MonitoringType.COUNTER, MonitoringType.RATE },
                                                 null);
        items.add(item);
        return this;
    }

    public void register(AreciboProfile areciboProfile)
    {
        areciboProfile.register(mbeanName, null);
        for (MonitoringItem item : items) {
            areciboProfile.add(mbeanName, item);
        }
    }
}
