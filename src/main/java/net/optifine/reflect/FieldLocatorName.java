package net.optifine.reflect;

import java.lang.reflect.Field;

public class FieldLocatorName implements IFieldLocator
{
    private ReflectorClass reflectorClass = null;
    private String targetFieldName = null;

    public FieldLocatorName(ReflectorClass reflectorClass, String targetFieldName)
    {
        this.reflectorClass = reflectorClass;
        this.targetFieldName = targetFieldName;
    }

    public Field getField()
    {
        Class oclass = this.reflectorClass.getTargetClass();

        if (oclass == null)
        {
            return null;
        }
        else
        {
            try
            {
                Field field = this.getDeclaredField(oclass, this.targetFieldName);
                field.setAccessible(true);
                return field;
            }
            catch (NoSuchFieldException caughtNoSuchFieldException)
            {

                return null;
            }
            catch (SecurityException securityexception)
            {
                net.minecraft.src.Config.warn(securityexception.getClass().getName() + ": " + securityexception.getMessage(), securityexception);
                return null;
            }
            catch (Throwable throwable)
            {
                net.minecraft.src.Config.warn(throwable.getClass().getName() + ": " + throwable.getMessage(), throwable);
                return null;
            }
        }
    }

    private Field getDeclaredField(Class cls, String name) throws NoSuchFieldException
    {
        Field[] afield = cls.getDeclaredFields();

        for (int i = 0; i < afield.length; ++i)
        {
            Field field = afield[i];

            if (field.getName().equals(name))
            {
                return field;
            }
        }

        if (cls == Object.class)
        {
            throw new NoSuchFieldException(name);
        }
        else
        {
            return this.getDeclaredField(cls.getSuperclass(), name);
        }
    }
}
