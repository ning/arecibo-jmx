package com.ning.arecibo.jmx;

import java.lang.management.ManagementFactory;
import javax.management.MBeanServer;
import org.weakref.jmx.MBeanExporter;
import org.weakref.jmx.guice.ExportBuilder;
import org.weakref.jmx.guice.MBeanModule;
import com.google.inject.AbstractModule;

public class AreciboMonitoringModule extends AbstractModule
{
    private final String profileMbeanName;

    public AreciboMonitoringModule(String profileMbeanName)
    {
        this.profileMbeanName = profileMbeanName;
    }

    @Override
    protected void configure()
    {
        bind(MBeanServer.class).toInstance(ManagementFactory.getPlatformMBeanServer());
        bind(AreciboProfile.class).asEagerSingleton();
        bind(AreciboMBeanExporter.class).asEagerSingleton();
        bind(MBeanExporter.class).to(AreciboMBeanExporter.class);

        ExportBuilder builder = MBeanModule.newExporter(binder());

        builder.export(AreciboProfile.class).as(profileMbeanName);
    }
}
