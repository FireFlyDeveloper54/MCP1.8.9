package net.optifine.reflect;

import java.lang.reflect.Constructor;
import net.optifine.Log;
import net.optifine.util.ArrayUtils;

public class ReflectorConstructor implements IResolvable
{
    private ReflectorClass reflectorClass = null;
    private Class[] parameterTypes = null;
    private boolean checked = false;
    private Constructor targetConstructor = null;

    public ReflectorConstructor(ReflectorClass reflectorClass, Class[] parameterTypes)
    {
        this.reflectorClass = reflectorClass;
        this.parameterTypes = parameterTypes;
        ReflectorResolver.register(this);
    }

    public Constructor getTargetConstructor()
    {
        if (this.checked)
        {
            return this.targetConstructor;
        }
        else
        {
            this.checked = true;
            Class targetClass = this.reflectorClass.getTargetClass();

            if (targetClass == null)
            {
                return null;
            }
            else
            {
                try
                {
                    this.targetConstructor = findConstructor(targetClass, this.parameterTypes);

                    if (this.targetConstructor == null)
                    {
                        Log.dbg("(Reflector) Constructor not present: " + targetClass.getName() + ", params: " + ArrayUtils.arrayToString((Object[])this.parameterTypes));
                    }

                    if (this.targetConstructor != null)
                    {
                        this.targetConstructor.setAccessible(true);
                    }
                }
                catch (Throwable throwable)
                {
                    net.minecraft.src.Config.warn(throwable.getClass().getName() + ": " + throwable.getMessage(), throwable);
                }

                return this.targetConstructor;
            }
        }
    }

    private static Constructor findConstructor(Class cls, Class[] paramTypes)
    {
        Constructor[] constructors = cls.getDeclaredConstructors();

        for (int constructorIndex = 0; constructorIndex < constructors.length; ++constructorIndex)
        {
            Constructor constructor = constructors[constructorIndex];
            Class[] constructorParamTypes = constructor.getParameterTypes();

            if (Reflector.matchesTypes(paramTypes, constructorParamTypes))
            {
                return constructor;
            }
        }

        return null;
    }

    public boolean exists()
    {
        return this.checked ? this.targetConstructor != null : this.getTargetConstructor() != null;
    }

    public void deactivate()
    {
        this.checked = true;
        this.targetConstructor = null;
    }

    public Object newInstance(Object... params)
    {
        return Reflector.newInstance(this, params);
    }

    public void resolve()
    {
        Constructor constructor = this.getTargetConstructor();
    }
}
