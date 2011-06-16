package com.ning.arecibo.jmx;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.management.MBeanServer;
import org.weakref.jmx.MBeanExporter;
import com.google.inject.Inject;

public class AreciboMBeanExporter extends MBeanExporter
{
    private final Pattern getterOrSetterPattern = Pattern.compile("(get|set|is)(.)(.*)");
    private final Set<AnnotationType> annotationTypes;
    private final AreciboProfile areciboProfile;

    @Inject
    public AreciboMBeanExporter(MBeanServer mbeanServer, AreciboProfile areciboProfile, Set<AnnotationType> annotationTypes)
    {
        super(mbeanServer);
        this.annotationTypes = new HashSet<AnnotationType>(annotationTypes);
        this.areciboProfile = areciboProfile;
    }

    @Override
    public void export(String name, Object object)
    {
        super.export(name, object);

        boolean foundMonitoredAnnotation = false;

        for (final Method method : object.getClass().getDeclaredMethods()) {
            final Annotation annotation = findRelevantAnnotation(method);

            if (annotation != null) {
                boolean monitoringOn = getAttrValue(annotation, "monitored", boolean.class, true);

                if (monitoringOn) {
                    String  methodName = method.getName();
                    Matcher matcher    = getterOrSetterPattern.matcher(methodName);
    
                    if (matcher.matches()) {
                        String methodType = matcher.group(1);
    
                        if ((methodType.equals("get") || methodType.equals("is")) && method.getParameterTypes().length == 0) {
                            String first = matcher.group(2);
                            String rest = matcher.group(3);
                            String attributeName = first + (rest != null ? rest : "");
    
                            areciboProfile.add(attributeName,
                                               getAttrValue(annotation, "eventAttributeName", String.class, Monitored.DEFAULT_EVENT_ATTRIBUTE_NAME),
                                               getAttrValue(annotation, "eventNamePattern", String.class, Monitored.DEFAULT_EVENT_NAME_PATTERN),
                                               getAttrValue(annotation, "eventName", String.class, Monitored.DEFAULT_EVENT_NAME),
                                               getAttrValue(annotation, "eventName", MonitoringType[].class, Monitored.DEFAULT_MONITORING_TYPE),
                                               method.getDeclaringClass());
                            foundMonitoredAnnotation = true;
                        }
                    }
                }
            }
        }
        if (foundMonitoredAnnotation) {
            areciboProfile.register(name, object);
        }
    }

    private Annotation findRelevantAnnotation(Method method)
    {
        for (final Annotation annotation : method.getAnnotations()) {
            if (annotationTypes.contains(new AnnotationType(annotation.annotationType()))) {
                return annotation;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public static <T> T getAttrValue(Annotation annotation, String attrName, Class<? extends T> valueType, T defaultValue)
    {
        try {
            Method attrMethod = annotation.annotationType().getMethod(attrName);

            if ((attrMethod != null) && attrMethod.getReturnType().isAssignableFrom(valueType)) {
                // can't use valueType.cast as it doesn't handle things like boolean vs. Boolean
                return (T)attrMethod.invoke(annotation);
            }
        }
        catch (InvocationTargetException e) {
            // ignore
        }
        catch (NoSuchMethodException e) {
            // ignore
        }
        catch (IllegalAccessException e) {
            // ignore
        }
        return defaultValue;
    }

    @Override
    public void unexport(String name)
    {
        super.unexport(name);
        areciboProfile.unregister(name);
    }
}
