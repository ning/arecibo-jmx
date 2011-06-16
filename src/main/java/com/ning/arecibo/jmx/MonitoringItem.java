package com.ning.arecibo.jmx;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MonitoringItem
{
    private final String attributeName;
	private final String attributePrettyName;
	private final String eventNamePattern;
	private final String eventPrettyName;
	private final MonitoringType[] monitoringTypes;
	private final Class<?> declaringClass;
	private final String hashKey;

    public MonitoringItem(String attributeName, String attributePrettyName, String eventNamePattern, String eventPrettyName, MonitoringType[] monitoringTypes, Class<?> declaringClass)
	{
		this.attributeName = attributeName;
		this.attributePrettyName = attributePrettyName;
		this.eventNamePattern = eventNamePattern;
		this.eventPrettyName = eventPrettyName;
		this.monitoringTypes = monitoringTypes;
		this.declaringClass = declaringClass;
		this.hashKey = initHashKey();
	}

	public String getAttributeName()
    {
        return attributeName;
    }

    public String getAttributePrettyName()
    {
        return attributePrettyName;
    }

    public String getEventNamePattern()
    {
        return eventNamePattern;
    }

    public String getEventPrettyName()
    {
        return eventPrettyName;
    }

    public MonitoringType[] getMonitoringTypes()
    {
        return monitoringTypes.clone();
    }

    public Class<?> getDeclaringClass()
    {
        return declaringClass;
    }

    private String initHashKey() {
		
		Arrays.sort(this.monitoringTypes);
		
		String monTypeConcat = "";
		for(MonitoringType monType:this.monitoringTypes) {
			monTypeConcat += monType.getCode();
		}
		
		return this.attributeName + ":" + 
				this.attributePrettyName + ":" +
				this.eventNamePattern + ":" +
				this.eventPrettyName + ":" +
				this.declaringClass.getName() + ":" +
				monTypeConcat;
	}

	public String getHashKey() {
		return this.hashKey;
	}
	
	String getEventAttributeName()
	{
		if (attributePrettyName != null && attributePrettyName.length() != 0) {
			return attributePrettyName;
		}
		return attributeName;
	}

	String getEventName(String beanName)
	{
		if (eventPrettyName != null && eventPrettyName.length() != 0) {
			return eventPrettyName;
		}
		else if (eventNamePattern != null && eventNamePattern.length() != 0) {
			try {
				Matcher m = Pattern.compile(eventNamePattern).matcher(beanName);
				if (m.matches()) {
					return m.group(1);
				}
			}
			catch (Exception e) {
				// ignored
			}
		}
		return declaringClass.getSimpleName();
	}

	public String toMonitoringConfig(String beanName)
	{
		return String.format("%s; %s; %s; %s; %s", beanName, attributeName, getEventName(beanName), getEventAttributeName(), getMonitoringType());
	}

	public String getMonitoringType()
	{
	    StringBuilder builder = new StringBuilder();

	    for (MonitoringType type : monitoringTypes) {
	        if (builder.length() > 0) {
	            builder.append(",");
	        }
            builder.append(type);
	    }
	    return builder.toString();
	}
}