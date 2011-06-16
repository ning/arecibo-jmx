package com.ning.arecibo.jmx;

import static com.google.inject.multibindings.Multibinder.newSetBinder;
import java.lang.annotation.Annotation;
import java.lang.management.ManagementFactory;
import javax.management.MBeanServer;
import org.weakref.jmx.MBeanExporter;
import org.weakref.jmx.guice.ExportBuilder;
import org.weakref.jmx.guice.MBeanModule;
import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

public class AreciboMonitoringModule extends AbstractModule
{
    private final AnnotationType[] annotationTypes;
    private final String profileMbeanName;

    public AreciboMonitoringModule(String profileMbeanName, Class<? extends Annotation>... annotationTypes)
    {
        this.profileMbeanName = profileMbeanName;
        if (annotationTypes.length == 0) {
            this.annotationTypes = new AnnotationType[] { new AnnotationType(Monitored.class) };
        }
        else {
            this.annotationTypes = new AnnotationType[annotationTypes.length];
            for (int idx = 0; idx < annotationTypes.length; idx++) {
                this.annotationTypes[idx] = new AnnotationType(annotationTypes[idx]);
            }
        }
    }

    @Override
    protected void configure()
    {
        bind(MBeanServer.class).toInstance(ManagementFactory.getPlatformMBeanServer());
        bind(AreciboProfile.class).asEagerSingleton();
        bind(AreciboMBeanExporter.class).asEagerSingleton();
        bind(MBeanExporter.class).to(AreciboMBeanExporter.class);

        Multibinder<AnnotationType> multiBinder = newSetBinder(binder(), AnnotationType.class);
        for (AnnotationType annotationType : annotationTypes) {
            multiBinder.addBinding().toInstance(annotationType);
        }

        ExportBuilder builder = MBeanModule.newExporter(binder());

        builder.export(AreciboProfile.class).as(profileMbeanName);
    }
}
