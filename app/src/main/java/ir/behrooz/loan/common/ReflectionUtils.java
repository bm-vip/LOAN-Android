package ir.behrooz.loan.common;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class ReflectionUtils {
    private static boolean isGetter(Method method) {
        // identify get methods
        if ((method.getName().startsWith("get") || method.getName().startsWith("is")) && !method.getReturnType().equals(void.class)) {
            return true;
        }
        return false;
    }

    private static boolean isSetter(Method method) {
        if (method.getName().startsWith("set") && method.getReturnType().equals(void.class)) {
            return true;
        }
        return false;
    }

    public static Object invokeMethod(Object obj, String property, Object... args) {
        Method[] methods = obj.getClass().getDeclaredMethods();
        for (Method method : methods) {
            if (property.equals(method.getName())) {
                try {
                    return method.invoke(obj, args);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public static <I, O> List<O> select(List<I> inputs, String property) {
        List<O> outputs = new ArrayList<>();
        for (I obj : inputs) {
            outputs.add((O) invokeMethod(obj, property));
        }
        return outputs;
    }

    public static <I, O> List<O> convert(List<I> inputs, Class<O> clazz) {
        List<O> outputs = new ArrayList<>();
        for (I obj : inputs) {
           if(clazz == String.class)
               outputs.add((O) obj.toString());
           else if(clazz == Long.class)
               outputs.add((O) Long.valueOf(obj.toString()));
           else if(clazz == Integer.class)
               outputs.add((O) Integer.valueOf(obj.toString()));
        }
        return outputs;
    }
}
