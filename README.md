The `arecibo-maven` library integrates [jmxutils](https://github.com/martint/jmxutils) with Arecibo.
Basically, it makes mbeans that are annotated with Arecibo-relevant annotations and exported
with jmxutils, visible to Arecibo.

In order to use it, you need to do this:

1.  Annotate your mbeans.

    You can use the provided `com.ning.arecibo.jmx.Monitored` annotation or define your own. If you
    choose to define your own, then you can optionally define these fields in it and the library will
    pick them up:

            boolean enabled();
            boolean monitored();
            String description();
            String eventAttributeName();
            String eventName();
            String eventNamePattern();
            MonitoringType[] monitoringType();

    `enabled` and `monitored` specify both if the annotation is enabled or not (`monitored` is the
    deprecated way).

2.  Export your mbeans.

    See [jmxutils](https://github.com/martint/jmxutils) for how this works.

3.  Install the guice module:

            install(new AreciboMonitoringModule("com.ning.arecibo.jmx:name=AreciboProfile", Monitored.class);

    The first argument is the name of the mbean that exports the mbeans for Arecibo (which you'll need
    to configure in Arecibo).

    Any additional arguments specify the (custom) annotations that the library should look for. If
    none are specified, it will default to `Monitored`.

After this, all  annotated attributes of the exported mbeans should be listed in the specified mbean
in the `MonitoringProfile` attribute.
