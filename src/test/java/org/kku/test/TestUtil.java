package org.kku.test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.List;

public class TestUtil
{
  static public <T> T invokePrivateMethod(Object object, String methodName)
  {
    return invokePrivateMethod(object, methodName, Collections.emptyList(), Collections.emptyList());
  }

  static public <T> T invokePrivateMethod(Object object, String methodName, List<Class<?>> parameterTypeList,
      List<Object> parameterList)
  {
    Class<?> clazz;
    Method method;
    Object[] parameters;
    Class<?>[] parameterTypes;

    clazz = object.getClass();

    parameterTypes = new Class<?>[parameterTypeList.size()];
    parameterTypes = parameterTypeList.toArray(parameterTypes);

    parameters = new Object[parameterTypeList.size()];
    parameters = parameterTypeList.toArray(parameters);

    try
    {
      method = clazz.getDeclaredMethod(methodName, parameterTypes);
      if (!Modifier.isPrivate(method.getModifiers()))
      {
        throw new TestFailedException(
            "Fix this! No need to call invokeMethod (" + clazz + "." + methodName + ") on a non private method");
      }
      method.setAccessible(true);

      return (T) method.invoke(object, parameters);
    }
    catch (TestFailedException ex)
    {
      throw ex;
    }
    catch (Throwable ex)
    {
      throw new TestFailedException("Failed to invoke method on " + clazz + "." + methodName + " (parameterTypes="
          + parameterTypeList + ")" + " (parameters=" + parameterTypeList + ")", ex);
    }
  }

  static private class TestFailedException
    extends RuntimeException
  {
    TestFailedException(String message)
    {
      super(message);
    }

    TestFailedException(String message, Throwable throwable)
    {
      super(message, throwable);
    }
  }
}
