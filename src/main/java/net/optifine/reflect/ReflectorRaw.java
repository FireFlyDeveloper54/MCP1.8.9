package net.optifine.reflect;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ReflectorRaw
{
    public static Field getField(Class cls, Class fieldType)
    {
        try
        {
            Field[] fields = cls.getDeclaredFields();

            for (int fieldIndex = 0; fieldIndex < fields.length; ++fieldIndex)
            {
                Field field = fields[fieldIndex];

                if (field.getType() == fieldType)
                {
                    field.setAccessible(true);
                    return field;
                }
            }

            return null;
        }
        catch (Exception caughtException)
        {
            return null;
        }
    }

    public static Field[] getFields(Class cls, Class fieldType)
    {
        try
        {
            Field[] fields = cls.getDeclaredFields();
            return getFields(fields, fieldType);
        }
        catch (Exception caughtException)
        {
            return null;
        }
    }

    public static Field[] getFields(Field[] fields, Class fieldType)
    {
        try
        {
            List matchingFields = new ArrayList();

            for (int fieldIndex = 0; fieldIndex < fields.length; ++fieldIndex)
            {
                Field field = fields[fieldIndex];

                if (field.getType() == fieldType)
                {
                    field.setAccessible(true);
                    matchingFields.add(field);
                }
            }

            Field[] fieldArray = (Field[])((Field[])matchingFields.toArray(new Field[matchingFields.size()]));
            return fieldArray;
        }
        catch (Exception caughtException)
        {
            return null;
        }
    }

    public static Field[] getFieldsAfter(Class cls, Field field, Class fieldType)
    {
        try
        {
            Field[] fields = cls.getDeclaredFields();
            List<Field> fieldList = Arrays.<Field>asList(fields);
            int fieldIndex = fieldList.indexOf(field);

            if (fieldIndex < 0)
            {
                return new Field[0];
            }
            else
            {
                List<Field> fieldsAfter = fieldList.subList(fieldIndex + 1, fieldList.size());
                Field[] fieldArray = (Field[])((Field[])fieldsAfter.toArray(new Field[fieldsAfter.size()]));
                return getFields(fieldArray, fieldType);
            }
        }
        catch (Exception caughtException)
        {
            return null;
        }
    }

    public static Field[] getFields(Object obj, Field[] fields, Class fieldType, Object value)
    {
        try
        {
            List<Field> matchingFields = new ArrayList();

            for (int fieldIndex = 0; fieldIndex < fields.length; ++fieldIndex)
            {
                Field field = fields[fieldIndex];

                if (field.getType() == fieldType)
                {
                    boolean isStaticField = Modifier.isStatic(field.getModifiers());

                    if ((obj != null || isStaticField) && (obj == null || !isStaticField))
                    {
                        field.setAccessible(true);
                        Object fieldValue = field.get(obj);

                        if (fieldValue == value)
                        {
                            matchingFields.add(field);
                        }
                        else if (fieldValue != null && value != null && fieldValue.equals(value))
                        {
                            matchingFields.add(field);
                        }
                    }
                }
            }

            Field[] fieldArray = (Field[])((Field[])matchingFields.toArray(new Field[matchingFields.size()]));
            return fieldArray;
        }
        catch (Exception caughtException)
        {
            return null;
        }
    }

    public static Field getField(Class cls, Class fieldType, int index)
    {
        Field[] fields = getFields(cls, fieldType);
        return index >= 0 && index < fields.length ? fields[index] : null;
    }

    public static Field getFieldAfter(Class cls, Field field, Class fieldType, int index)
    {
        Field[] fields = getFieldsAfter(cls, field, fieldType);
        return index >= 0 && index < fields.length ? fields[index] : null;
    }

    public static Object getFieldValue(Object obj, Class cls, Class fieldType)
    {
        ReflectorField reflectorField = getReflectorField(cls, fieldType);
        return reflectorField == null ? null : (!reflectorField.exists() ? null : Reflector.getFieldValue(obj, reflectorField));
    }

    public static Object getFieldValue(Object obj, Class cls, Class fieldType, int index)
    {
        ReflectorField reflectorField = getReflectorField(cls, fieldType, index);
        return reflectorField == null ? null : (!reflectorField.exists() ? null : Reflector.getFieldValue(obj, reflectorField));
    }

    public static boolean setFieldValue(Object obj, Class cls, Class fieldType, Object value)
    {
        ReflectorField reflectorField = getReflectorField(cls, fieldType);
        return reflectorField == null ? false : (!reflectorField.exists() ? false : Reflector.setFieldValue(obj, reflectorField, value));
    }

    public static boolean setFieldValue(Object obj, Class cls, Class fieldType, int index, Object value)
    {
        ReflectorField reflectorField = getReflectorField(cls, fieldType, index);
        return reflectorField == null ? false : (!reflectorField.exists() ? false : Reflector.setFieldValue(obj, reflectorField, value));
    }

    public static ReflectorField getReflectorField(Class cls, Class fieldType)
    {
        Field field = getField(cls, fieldType);

        if (field == null)
        {
            return null;
        }
        else
        {
            ReflectorClass reflectorClass = new ReflectorClass(cls);
            return new ReflectorField(reflectorClass, field.getName());
        }
    }

    public static ReflectorField getReflectorField(Class cls, Class fieldType, int index)
    {
        Field field = getField(cls, fieldType, index);

        if (field == null)
        {
            return null;
        }
        else
        {
            ReflectorClass reflectorClass = new ReflectorClass(cls);
            return new ReflectorField(reflectorClass, field.getName());
        }
    }
}
