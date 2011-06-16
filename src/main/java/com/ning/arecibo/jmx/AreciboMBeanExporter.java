package com.ning.arecibo.jmx;

import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.management.MBeanServer;
import org.weakref.jmx.MBeanExporter;
import com.google.inject.Inject;

public class AreciboMBeanExporter extends MBeanExporter
{
    private final Pattern getterOrSetterPattern = Pattern.compile("(get|set|is)(.)(.*)");
    private final AreciboProfile areciboProfile;

    @Inject
    public AreciboMBeanExporter(MBeanServer mbeanServer, AreciboProfile areciboProfile)
    {
        super(mbeanServer);
        this.areciboProfile = areciboProfile;
    }

    @Override
    public void export(String name, Object object)
    {
        super.export(name, object);

        boolean foundMonitoredAnnotation = false;

        for (final Method method : object.getClass().getDeclaredMethods()) {
            final Monitored annotation = method.getAnnotation(Monitored.class);

            if (annotation != null) {
                String  methodName = method.getName();
                Matcher matcher    = getterOrSetterPattern.matcher(methodName);

                if (matcher.matches()) {
                    String methodType = matcher.group(1);

                    if ((methodType.equals("get") || methodType.equals("is")) && method.getParameterTypes().length == 0) {
                        String first = matcher.group(2);
                        String rest = matcher.group(3);
                        String attributeName = first + (rest != null ? rest : "");

                        areciboProfile.add(attributeName,
                                           annotation.eventAttributeName(),
                                           annotation.eventNamePattern(),
                                           annotation.eventName(),
                                           annotation.monitoringType(),
                                           method.getDeclaringClass());
                        foundMonitoredAnnotation = true;
                    }
                }
            }
        }
        if (foundMonitoredAnnotation) {
            areciboProfile.register(name, object);
        }
    }

    @Override
    public void unexport(String name)
    {
        super.unexport(name);
        areciboProfile.unregister(name);
    }
}
