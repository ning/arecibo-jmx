package com.ning.arecibo.jmx;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertFalse;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.management.ManagementFactory;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;
import org.weakref.jmx.Managed;
import org.weakref.jmx.ManagedAnnotation;
import org.weakref.jmx.guice.MBeanModule;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

@Test(groups = "fast")
public class TestGuiceJMX
{
    private static final String PROFILE_MBEAN_NAME = "com.ning.arecibo.jmx:name=AreciboProfile";

    public static class TestClass1
    {
        @Managed
        public String getMessage()
        {
            return "test";
        }
    }

    public static class TestClass2
    {
        @Monitored(monitoringType = MonitoringType.VALUE)
        public int getValue()
        {
            return 0;
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target( { ElementType.METHOD })
    @ManagedAnnotation
    public @interface MyMonitored
    {
        String description() default "";
        boolean monitored() default false;
        MonitoringType[] monitoringType() default { MonitoringType.VALUE };
    }
    
    public static class TestClass3
    {
        @MyMonitored(monitored = true, monitoringType = MonitoringType.VALUE)
        public int getValue()
        {
            return 0;
        }

        @MyMonitored(monitored = false, monitoringType = MonitoringType.VALUE)
        public int getOtherValue()
        {
            return 1;
        }
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() throws Exception
    {
        try {
            ManagementFactory.getPlatformMBeanServer().unregisterMBean(new ObjectName(PROFILE_MBEAN_NAME));
        }
        catch (InstanceNotFoundException ex) {
            // ignored
        }
    }

    public void testExportBuilder() throws Exception
    {
        final String   mbeanName = TestGuiceJMX.class.getPackage().getName() + ":name=" + TestClass1.class.getSimpleName();
        @SuppressWarnings("unchecked")
        final Injector injector  = Guice.createInjector(new MBeanModule(),
                                                        new AreciboMonitoringModule(PROFILE_MBEAN_NAME, Monitored.class),
                                                        new AbstractModule() {
            @Override
            protected void configure()
            {
                bind(TestClass1.class).asEagerSingleton();

                MBeanModule.newExporter(binder()).export(TestClass1.class).as(mbeanName);
            }
        });

        TestClass1  obj         = injector.getInstance(TestClass1.class);
        MBeanServer mbeanServer = injector.getInstance(MBeanServer.class);

        assertNotNull(obj);
        assertNotNull(mbeanServer);
        assertEquals(obj.getMessage(), mbeanServer.getAttribute(new ObjectName(mbeanName), "Message"));
    }

    public void testMonitoredExportBuilderDefaults() throws Exception
    {
        final String   mbeanName = TestGuiceJMX.class.getPackage().getName() + ":name=" + TestClass2.class.getSimpleName();
        final Injector injector  = Guice.createInjector(new MBeanModule(),
                                                        new AreciboMonitoringModule(PROFILE_MBEAN_NAME),
                                                        new AbstractModule() {
            @Override
            protected void configure()
            {
                bind(TestClass2.class).asEagerSingleton();

                MBeanModule.newExporter(binder()).export(TestClass2.class).as(mbeanName);
            }
        });

        TestClass2     obj         = injector.getInstance(TestClass2.class);
        MBeanServer    mbeanServer = injector.getInstance(MBeanServer.class);
        AreciboProfile profile     = injector.getInstance(AreciboProfile.class);

        assertNotNull(obj);
        assertNotNull(mbeanServer);
        assertNotNull(profile);
        assertEquals(obj.getValue(), mbeanServer.getAttribute(new ObjectName(mbeanName), "Value"));

        Set<String> monitored = new HashSet<String>(Arrays.asList(profile.getMonitoringProfile()));

        assertTrue(monitored.contains(mbeanName + "; Value; " + TestClass2.class.getSimpleName() + "; Value; v"));
    }

    public void testMonitoredExportBuilderCustomAnnotation() throws Exception
    {
        final String   mbeanName = TestGuiceJMX.class.getPackage().getName() + ":name=" + TestClass3.class.getSimpleName();
        @SuppressWarnings("unchecked")
        final Injector injector  = Guice.createInjector(new MBeanModule(),
                                                        new AreciboMonitoringModule(PROFILE_MBEAN_NAME, MyMonitored.class),
                                                        new AbstractModule() {
            @Override
            protected void configure()
            {
                bind(TestClass3.class).asEagerSingleton();

                MBeanModule.newExporter(binder()).export(TestClass3.class).as(mbeanName);
            }
        });

        TestClass3     obj         = injector.getInstance(TestClass3.class);
        MBeanServer    mbeanServer = injector.getInstance(MBeanServer.class);
        AreciboProfile profile     = injector.getInstance(AreciboProfile.class);

        assertNotNull(obj);
        assertNotNull(mbeanServer);
        assertNotNull(profile);
        assertEquals(obj.getValue(), mbeanServer.getAttribute(new ObjectName(mbeanName), "Value"));
        assertEquals(obj.getOtherValue(), mbeanServer.getAttribute(new ObjectName(mbeanName), "OtherValue"));

        Set<String> monitored = new HashSet<String>(Arrays.asList(profile.getMonitoringProfile()));

        assertTrue(monitored.contains(mbeanName + "; Value; " + TestClass3.class.getSimpleName() + "; Value; v"));
        assertFalse(monitored.contains(mbeanName + "; OtherValue; " + TestClass3.class.getSimpleName() + "; OtherValue; v"));
    }
}
