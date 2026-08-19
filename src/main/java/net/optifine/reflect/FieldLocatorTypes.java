package net.optifine.reflect;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import net.optifine.Log;

public class FieldLocatorTypes implements IFieldLocator
{
    private Field field = null;

    public FieldLocatorTypes(Class cls, Class[] preTypes, Class type, Class[] postTypes, String errorName)
    {
        Field[] fields = cls.getDeclaredFields();
        List<Class> fieldTypes = new ArrayList();

        for (int fieldIndex = 0; fieldIndex < fields.length; ++fieldIndex)
        {
            Field field = fields[fieldIndex];
            fieldTypes.add(field.getType());
        }

        List<Class> targetTypes = new ArrayList();
        targetTypes.addAll(Arrays.<Class>asList(preTypes));
        targetTypes.add(type);
        targetTypes.addAll(Arrays.<Class>asList(postTypes));
        int matchIndex = Collections.indexOfSubList(fieldTypes, targetTypes);

        if (matchIndex < 0)
        {
            Log.log("(Reflector) Field not found: " + errorName);
        }
        else
        {
            int duplicateMatchIndex = Collections.indexOfSubList(fieldTypes.subList(matchIndex + 1, fieldTypes.size()), targetTypes);

            if (duplicateMatchIndex >= 0)
            {
                Log.log("(Reflector) More than one match found for field: " + errorName);
            }
            else
            {
                int targetFieldIndex = matchIndex + preTypes.length;
                this.field = fields[targetFieldIndex];
            }
        }
    }

    public Field getField()
    {
        return this.field;
    }
}
