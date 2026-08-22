package net.optifine.reflect;

import java.lang.reflect.Field;

public class FieldLocatorType implements IFieldLocator
{
    private ReflectorClass reflectorClass;
    private Class targetFieldType;
    private int targetFieldIndex;

    public FieldLocatorType(ReflectorClass reflectorClass, Class targetFieldType)
    {
        this(reflectorClass, targetFieldType, 0);
    }

    public FieldLocatorType(ReflectorClass reflectorClass, Class targetFieldType, int targetFieldIndex)
    {
        this.reflectorClass = null;
        this.targetFieldType = null;
        this.reflectorClass = reflectorClass;
        this.targetFieldType = targetFieldType;
        this.targetFieldIndex = targetFieldIndex;
    }

    public Field getField()
    {
        Class targetClass = this.reflectorClass.getTargetClass();

        if (targetClass == null)
        {
            return null;
        }
        else
        {
            try
            {
                Field[] fields = targetClass.getDeclaredFields();
                int matchingFieldIndex = 0;

                for (int fieldIndex = 0; fieldIndex < fields.length; ++fieldIndex)
                {
                    Field field = fields[fieldIndex];

                    if (field.getType() == this.targetFieldType)
                    {
                        if (matchingFieldIndex == this.targetFieldIndex)
                        {
                            field.setAccessible(true);
                            return field;
                        }

                        ++matchingFieldIndex;
                    }
                }

                // Log.log("(Reflector) Field not present: " + oclass.getName() + ".(type: " + this.targetFieldType + ", index: " + this.targetFieldIndex + ")");
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
}
