package com.diffblue.java_testcase;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Optional;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMember;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.NotFoundException;
import javassist.bytecode.BadBytecode;

import org.objenesis.ObjenesisStd;

/**
 * The <code>Reflector</code> class instantiates any Java object and sets any
 * field.
 *
 * @author Matthias Guedemann
 * @version 1.0
 */
public final class Reflector {

  /**
   * private <code>Reflector</code> constructor.
   *
   */
  private Reflector() { }

  /**
   * Sets a field of an object instance.
   *
   * @param <T> type parameter of the class
   * @param c the <code>Class</code> of the object to set
   * @param o the <code>Object</code> whose field is set
   * @param fieldName a <code>String</code> as the name of the field
   * @param newVal an <code>Object</code> holding the new value for the field
   *
   * @exception NoSuchFieldException if a field with the specified name is not
   * found.
   * @exception IllegalArgumentException if the specified object is not an
   * instance of the class or interface declaring the underlying field (or a
   * subclass or implementor thereof), or if an unwrapping conversion fails.
   * @exception IllegalAccessException if an error occurs
   */
  private static <T> void setInstanceField(
          final Class<T> c, final Object o, final String fieldName,
          final Object newVal) throws
          NoSuchFieldException, IllegalArgumentException,
          IllegalAccessException {

    if (c == null) {
      throw new NoSuchFieldException();
    }
    Optional<Field> field =
      Arrays.stream(c.getDeclaredFields())
        .filter(f -> f.getName().equals(fieldName)).findAny();
    if (!field.isPresent()) {
      setInstanceField(c.getSuperclass(), o, fieldName, newVal);
    } else {
      Field property = field.get();
      property.setAccessible(true);

      // remove final modifier
      Field modifiersField = Field.class.getDeclaredField("modifiers");
      modifiersField.setAccessible(true);
      modifiersField
        .setInt(property, property.getModifiers() & ~Modifier.FINAL);
      try {
        property.set(o, newVal);
      } catch (IllegalAccessException e) {
        throw new RuntimeException(e); // Should never happen.
      }
    }
  }

  /**
   * Get the value of a field from an object instance.
   *
   * @param <T> type parameter of the class
   * @param c the <code>Class</code> of the object to set
   * @param o the <code>Object</code> whose field is set
   * @param fieldName a <code>String</code> as the name of the field
   * @return the value the field holds
   *
   * @exception NoSuchFieldException if a field with the specified name is not
   * found.
   * @exception IllegalArgumentException if the specified object is not an
   * instance of the class or interface declaring the underlying field (or a
   * subclass or implementor thereof), or if an unwrapping conversion fails.
   * @exception IllegalAccessException if an error occurs
   */
  private static <T> Object getInstanceField(
    final Class<T> c,
    final Object o,
    final String fieldName)
    throws
    NoSuchFieldException,
    IllegalArgumentException,
    IllegalAccessException {
    if (c == null) {
      throw new NoSuchFieldException();
    }
    Optional<Field> field =
      Arrays.stream(c.getDeclaredFields())
        .filter(f -> f.getName().equals(fieldName)).findAny();
    if (!field.isPresent()) {
      return getInstanceField(c.getSuperclass(), o, fieldName);
    } else {
      Field property = field.get();
      property.setAccessible(true);

      try {
        return property.get(o);
      } catch (IllegalAccessException e) {
        throw new RuntimeException(e); // Should never happen.
      }
    }
  }

  /**
   * Changes a given field of an object instance via reflection, bypassing the
   * private modifier.
   *
   * @param o an <code>Object</code> instance to change
   * @param fieldName a <code>String</code> the name of the field to change
   * @param newVal an <code>Object</code> the new value for the field
   *
   * @exception NoSuchFieldException if a field with the specified name is not
   * found.
   * @exception IllegalArgumentException if the specified object is not an
   * instance of the class or interface declaring the underlying field (or a
   * subclass or implementor thereof), or if an unwrapping conversion fails.
   * @exception IllegalAccessException if an error occurs
   */
  public static void setInstanceField(
    final Object o,
    final String fieldName,
    final Object newVal)
    throws
    NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
    setInstanceField(o.getClass(), o, fieldName, newVal);
  }

  /**
   * Describe <code>getInstanceField</code> method here.
   *
   * @param o an <code>Object</code> value
   * @param fieldName a <code>String</code> value
   * @return an <code>Object</code> value
   * @exception NoSuchFieldException if an error occurs
   * @exception IllegalArgumentException if an error occurs
   * @exception IllegalAccessException if an error occurs
   */
  public static Object getInstanceField(final Object o, final String fieldName)
    throws
    NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
    return getInstanceField(o.getClass(), o, fieldName);
  }

  /**
   * <code>forName</code> returns class of atomic types.
   *
   * @param className name of class as <code>String</code>
   * @return the <code>Class</code> object
   * @exception ClassNotFoundException if class cannot be found
   */
  public static Class<?> forName(final String className)
    throws ClassNotFoundException {
    if (className.equals("float")) {
      return float.class;
    }
    if (className.equals("byte")) {
      return byte.class;
    }
    if (className.equals("char")) {
      return char.class;
    }
    if (className.equals("short")) {
      return short.class;
    }
    if (className.equals("double")) {
      return double.class;
    }
    if (className.equals("int")) {
      return int.class;
    }
    if (className.equals("long")) {
      return long.class;
    }
    if (className.equals("boolean")) {
      return boolean.class;
    }
    // TODO add arrays2

    return Class.forName(className);
  }

  /**
   * Describe <code>removePackageFromName</code> method here.
   *
   * @param className a <code>String</code> value
   * @return a <code>String</code> value
   */
  public static String removePackageFromName(final String className) {
    int lastSeparator = className.lastIndexOf('.');
    if (lastSeparator != -1) {
      return className.substring(lastSeparator + 1);
    } else {
      return className;
    }
  }

  /**
   * <code>classMap</code> keeps a cache of created classes.
   *
   */
  private static HashMap<String, Class<?>> classMap = new HashMap<>();

  /**
   * <code>makePublic</code> sets member flag to public.
   *
   * @param m a <code>CtMember</code> value
   */
  private static void makePublic(final CtMember m) {
    int modifier = m.getModifiers();
    modifier = modifier & ~(javassist.Modifier.PRIVATE
                            | javassist.Modifier.PROTECTED);
    modifier = modifier | javassist.Modifier.PUBLIC;
    m.setModifiers(modifier);
  }

  /**
   * <code>makePublic</code> sets class flag to public.
   *
   * @param c a <code>CtClass</code> value
   */
  private static void makePublic(final CtClass c) {
    int modifier = c.getModifiers();
    modifier = modifier & ~(javassist.Modifier.PRIVATE
                            | javassist.Modifier.PROTECTED);
    modifier = modifier | javassist.Modifier.PUBLIC;
    c.setModifiers(modifier);
  }

  /**
   * This forces the creation of an instance for a given class name.
   *
   * @param <T> type parameter of the return value
   * @param className a <code>String</code> giving the name of the class
   * @return an <code>Object</code> which is an instance of the specified class
   *
   * @throws ClassNotFoundException if the class cannot be found in the
   * @throws NotFoundException if the class cannot be found in the
   * @throws CannotCompileException if the class cannot be found in the
   * @throws InstantiationException if the class cannot be found in the
   * @throws IllegalAccessException if the class cannot be found in the
   * @throws BadBytecode if the class cannot be found in the
   * classpath
   */
  public static <T> Object forceInstance(final String className)
    throws
    ClassNotFoundException,
    NotFoundException,
    CannotCompileException,
    InstantiationException,
    IllegalAccessException,
    BadBytecode {
    ClassPool pool = ClassPool.getDefault();
    CtClass c = pool.get(className);

    for (CtMethod m : c.getDeclaredMethods()) {
      makePublic(m);
    }

    for (CtConstructor ctor : c.getDeclaredConstructors()) {
      makePublic(ctor);
    }

    for (CtField f : c.getDeclaredFields()) {
      makePublic(f);
    }

    makePublic(c);

    // we consider a class abstract if any method has no body
    if (isAbstract(c) || c.isInterface()) {
      String packageName = "com.diffblue.test_gen.";
      String newClassName = packageName + removePackageFromName(className);

      CtClass implementation = pool.getOrNull(newClassName + "_implementation");
      if (implementation == null) {
        implementation = pool.makeClass(newClassName + "_implementation");

        if (c.isInterface()) {
          implementation.setInterfaces(new CtClass[] {c });
        } else {
          implementation.setSuperclass(c);
        }

        // look for constructor
        // create default constructor if none exists
        boolean foundDefault = false;
        if (!c.isInterface()) {
          for (CtConstructor ctor : c.getConstructors()) {
            if (ctor.getParameterTypes().length == 0
                && (ctor.getModifiers() & javassist.Modifier.ABSTRACT) == 0
                && !ctor.isEmpty()) {
              foundDefault = true;
              break;
            }
          }
        }
        if (!foundDefault) {
          CtConstructor newCtor =
            new CtConstructor(new CtClass[] {}, implementation);
          newCtor.setBody("{}");
          implementation.addConstructor(newCtor);
        }

        // declared methods or only methods ?
        for (CtMethod m : c.getDeclaredMethods()) {
          if (isAbstract(m)) {
            CtMethod method = CtNewMethod.make(javassist.Modifier.PUBLIC,
                                               m.getReturnType(),
                                               m.getName(),
                                               m.getParameterTypes(),
                                               m.getExceptionTypes(),
                                               null,
                                               implementation);
            implementation.addMethod(method);
          }
        }

        Class<?> ic = pool.toClass(implementation);

        classMap.put(newClassName + "_implementation", ic);
        return forceInstance(ic);
      } else {
        return forceInstance((Class<?>) classMap
          .get(newClassName + "_implementation"));
      }
    } else {
      return forceInstance(Class.forName(className));
    }
  }

  /**
   * <code>isAbstract</code> checks whether the <code>ABSTRACT</code> flag of a
   * method is set.
   *
   * @param m a <code>CtMethod</code> value
   * @return a <code>boolean</code> value
   */
  private static boolean isAbstract(final CtMethod m) {
    return ((m.getModifiers() & javassist.Modifier.ABSTRACT) != 0);
  }

  /**
   * <code>isAbstract</code> checks whether the <code>ABSTRACT</code> flag of a
   * class is set.
   *
   * @param c a <code>CtClass</code> value
   * @return a <code>boolean</code> value
   */
  private static boolean isAbstract(final CtClass c) {
    for (CtMethod m : c.getDeclaredMethods()) {
      if (isAbstract(m)) {
        return true;
      }
    }
    return false;
  }

  /**
   * <code>getDefaultConstructor</code> returns the default constructor if one
   * exists.
   *
   * @param c a <code>CtClass</code> value
   * @return an <code>Optional</code> value holding a <code>Constructor</code>
   * object (or not)
   */
  private static Optional<Constructor<?>> getDefaultConstructor(
    final Class<?> c) {
    return Arrays.stream(c.getDeclaredConstructors())
      .filter(ctor -> ctor.getParameterCount() == 0).findAny();
  }

  /**
   * This forces the creation of an instance for a given class name. If the
   * class provides a public default constructor, it is called. If the class has
   * a private default constructor, it is made accessible and then called.
   *
   * @param <T> type parameter of the class
   * @param c a <code>Class</code> the class to instantiate
   * @return an <code>Object</code> which is an instance of the specified class
   */
  @SuppressWarnings("unchecked")
  public static <T> T forceInstance(final Class<T> c) {
    Optional<Constructor<?>> ctor = getDefaultConstructor(c);
    if (ctor.isPresent()) {
      Constructor<?> defaultCtor = ctor.get();
      defaultCtor.setAccessible(true);
      try {
        return (T) defaultCtor.newInstance();
      } catch (InstantiationException
               | InvocationTargetException
               | IllegalAccessException e)  { }
    }
    return (T) new ObjenesisStd().newInstance(c);
  }
}
