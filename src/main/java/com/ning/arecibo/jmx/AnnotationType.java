package com.ning.arecibo.jmx;

import java.lang.annotation.Annotation;

public class AnnotationType
{
    private final Class<? extends Annotation> wrappedType;

    public AnnotationType(Class<? extends Annotation> wrappedType)
    {
        this.wrappedType = wrappedType;
    }

    public Class<? extends Annotation> getWrappedType()
    {
        return wrappedType;
    }

    @Override
    public int hashCode()
    {
        return wrappedType.hashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        if ((obj != null) && getClass().equals(obj.getClass())) {
            return wrappedType.equals(((AnnotationType)obj).wrappedType);
        }
        else {
            return false;
        }
    }

    @Override
    public String toString()
    {
        return wrappedType.toString();
    }
}
