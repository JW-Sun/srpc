package cn.jw.factory;

import lombok.Data;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/**
 *  获得一个类的对象实例
 *  双重检测的单例
 *  通过反射来创建类的实例对象
 */
public class SingletonFactory {

    // 保存单例对象的map
    private static final Map<String, Object> SINGLETON_MAP = new HashMap<>();

    private SingletonFactory() {}

    /**
     *  获得某个类对象的单例对象
     * @param clazz
     * @param <T>
     * @return
     */
    public static  <T> T getSingleton(Class<T> clazz) {
        String classKey = clazz.toString();
        Object instance = null;
        if (instance == null) {
            synchronized (SingletonFactory.class) {
                instance = SINGLETON_MAP.get(classKey);
                if (instance == null) {
                    try {
                        instance = clazz.getDeclaredConstructor().newInstance();
                        SINGLETON_MAP.put(classKey, instance);
                    } catch (InstantiationException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    } catch (NoSuchMethodException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return clazz.cast(instance);
    }

    public static void main(String[] args) {
        Person singleton = SingletonFactory.getSingleton(Person.class);
        Person singleton2 = SingletonFactory.getSingleton(Person.class);
        System.out.println(singleton.hashCode());
        System.out.println(singleton2.hashCode());
    }
}

@Data
class Person {
    String name;
    String id;
}