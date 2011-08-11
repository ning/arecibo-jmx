package com.ning.arecibo.jmx;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import org.weakref.jmx.Managed;

public class AreciboProfile
{
    private final Map<String, Object> mbeans = new HashMap<String, Object>();
    private final Map<Class<?>, Map<String, MonitoringItem>> itemsByClass = new LinkedHashMap<Class<?>, Map<String, MonitoringItem>>();
    private final Map<String, Map<String, MonitoringItem>> itemsByMBeanName = new LinkedHashMap<String, Map<String, MonitoringItem>>();
    private final AtomicLong version = new AtomicLong(0);

    public synchronized void register(String name, Object monitoredObject)
    {
        Object oldObj = mbeans.put(name, monitoredObject);

        if (oldObj != monitoredObject) {
            version.getAndIncrement();
        }
    }

    public synchronized void unregister(String name)
    {
        mbeans.remove(name);
        version.getAndIncrement();
    }

    public synchronized void add(MonitoringItem item)
    {
        add(item.getAttributeName(), item.getAttributePrettyName(), item.getEventNamePattern(), item.getEventPrettyName(), item.getMonitoringTypes(), item.getDeclaringClass());
    }

    public synchronized void add(String mbeanName, MonitoringItem item)
    {
        add(mbeanName, item.getAttributeName(), item.getAttributePrettyName(), item.getEventNamePattern(), item.getEventPrettyName(), item.getMonitoringTypes(), item.getDeclaringClass());
    }

    public synchronized void add(String attributeName, String attributePrettyName, String eventNamePattern, String eventPrettyName, MonitoringType[] monitoringType, Class<?> declaringClass)
    {
        Map<String, MonitoringItem> map = itemsByClass.get(declaringClass);

        if (map == null) {
            map = new HashMap<String, MonitoringItem>();
            itemsByClass.put(declaringClass, map);
        }
        MonitoringItem newItem = new MonitoringItem(attributeName, attributePrettyName, eventNamePattern, eventPrettyName, monitoringType, declaringClass);
        MonitoringItem oldItem = map.put(newItem.getHashKey(), newItem);

        if (oldItem == null) {
            version.getAndIncrement();
        }
    }

    public synchronized void add(String mbeanName, String attributeName, String attributePrettyName, String eventNamePattern, String eventPrettyName, MonitoringType[] monitoringType, Class<?> declaringClass)
    {
        Map<String, MonitoringItem> map = itemsByMBeanName.get(mbeanName);

        if (map == null) {
            map = new HashMap<String, MonitoringItem>();
            itemsByMBeanName.put(mbeanName, map);
        }
        MonitoringItem newItem = new MonitoringItem(attributeName, attributePrettyName, eventNamePattern, eventPrettyName, monitoringType, declaringClass);
        MonitoringItem oldItem = map.put(newItem.getHashKey(), newItem);

        if (oldItem == null) {
            version.getAndIncrement();
        }
    }

    private synchronized String[] getMonitoringProfileStatic()
    {
        Set<String> configs = new LinkedHashSet<String>();

        for (Map.Entry<String, Object> entry : mbeans.entrySet()) {
            String beanName = entry.getKey();
            Object monitoredObject = entry.getValue();
            Map<String, MonitoringItem> attrs;

            if (monitoredObject != null) {
                attrs = itemsByClass.get(monitoredObject.getClass());
                if (attrs != null) {
                    for (MonitoringItem item : attrs.values()) {
                        String config = item.toMonitoringConfig(beanName);

                        configs.add(config);
                    }
                }
            }

            attrs = itemsByMBeanName.get(beanName);
            if (attrs != null) {
                for (MonitoringItem item : attrs.values()) {
                    String config = item.toMonitoringConfig(beanName);

                    configs.add(config);
                }
            }
        }
        return configs.toArray(new String[configs.size()]);
    }
    
    @Managed
    public String[] getMonitoringProfile() {
        return getMonitoringProfileStatic();
    }
    
    @Managed
    public long getMonitoringProfileVersion()
    {
        return version.get();
    }
}
