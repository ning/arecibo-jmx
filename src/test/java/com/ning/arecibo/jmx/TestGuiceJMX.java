package com.ning.arecibo.jmx;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
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
        final Injector injector  = Guice.createInjector(new MBeanModule(),
                                                        new AreciboMonitoringModule(PROFILE_MBEAN_NAME),
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

    public void testMonitoredExportBuilder() throws Exception
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
}
