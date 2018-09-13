/*******************************************************************************
 *                 jMCS project ( http://www.jmmc.fr/dev/jmcs )
 *******************************************************************************
 * Copyright (c) 2013, CNRS. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     - Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     - Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     - Neither the name of the CNRS nor the names of its contributors may be
 *       used to endorse or promote products derived from this software without
 *       specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL CNRS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/
package fr.jmmc.jmcs.util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides helper functions related to object introspection.
 *
 * For example, you can easily execute a method of a class given the path to the
 * sought class. and the method name. Or you can also discover at runtime if a
 * field exists, retrieve a method execution result, etc ...
 * 
 * @author Brice COLUCCI, Sylvain LAFRASSE, Laurent BOURGES.
 */
public final class IntrospectionUtils {

    /** Logger */
    private static final Logger _logger = LoggerFactory.getLogger(IntrospectionUtils.class.getName());
    /** empty class array */
    private final static Class<?>[] EMPTY_CLASS_ARRAY = new Class<?>[]{};
    /** empty object array */
    private final static Object[] EMPTY_OBJECT_ARRAY = new Object[]{};

    /**
     * Returns a class object according to a given class path.
     *
     * @param classPath path to the sought class.
     *
     * @return found class, null otherwise.
     */
    public static Class<?> getClass(final String classPath) {
        Class<?> searchedClass = null;

        try {
            searchedClass = Class.forName(classPath);
        } catch (ClassNotFoundException cnfe) {
            _logger.warn("Cannot find class '{}'", classPath, cnfe);
        } catch (NoClassDefFoundError ncdfe) {
            _logger.warn("Cannot find class '{}'", classPath, ncdfe);
        }

        return searchedClass;
    }

    /**
     * Test whether a class exists, from a given class path.
     *
     * @param classPath path to the sought class.
     *
     * @return true if the class exists, false otherwise.
     */
    public static boolean hasClass(final String classPath) {
        if (getClass(classPath) != null) {
            _logger.debug("Found class '{}'.", classPath);

            return true;
        }

        return false;
    }

    /**
     * Returns the package containing the class identified by the given class
     * path.
     *
     * @param classPath path to the sought class.
     *
     * @return found class package, null otherwise.
     */
    public static Package getClassPackage(final String classPath) {
        final Class<?> clazz = getClass(classPath);
        return (clazz != null) ? clazz.getPackage() : null;
    }

    /**
     * Returns the package containing the given class.
     *
     * @param seekedClass class.
     *
     * @return found class package.
     */
    public static Package getClassPackage(final Class<?> seekedClass) {
        return seekedClass.getPackage();
    }

    /**
     * Returns the name of the package containing the class identified by the
     * given class path.
     *
     * @param classPath path to the sought class.
     *
     * @return found class package name, null otherwise.
     */
    public static String getClassPackageName(final String classPath) {
        final Package p = getClassPackage(classPath);
        return (p != null) ? p.getName() : null;
    }

    /**
     * Returns the name of the package containing the given class.
     *
     * @param seekedClass class.
     *
     * @return found class package name.
     */
    public static String getClassPackageName(final Class<?> seekedClass) {
        return seekedClass.getPackage().getName();
    }

    /**
     * Returns a new instance of the class identified by the gien class path.
     *
     * @param classPath path to the sought class.
     *
     * @return new instance of the class, null otherwise.
     */
    public static Object getInstance(final String classPath) {
        final Class<?> clazz = getClass(classPath);
        if (clazz != null) {
            try {
                return clazz.newInstance();
            } catch (InstantiationException ie) {
                _logger.warn("Cannot get instance of class '{}'", classPath, ie);
            } catch (IllegalAccessException iae) {
                _logger.warn("Cannot get instance of class '{}'", classPath, iae);
            }
        }
        return null;
    }

    /**
     * Returns the list of methods of the class identified by the given class
     * path.
     *
     * @param classPath path to the sought class.
     *
     * @return Array of found methods, null otherwise.
     */
    public static Method[] getMethods(final String classPath) {
        final Class<?> clazz = getClass(classPath);
        return (clazz != null) ? clazz.getMethods() : null;
    }

    /**
     * Returns the sought method with EMPTY argument list in the class
     * identified by the given class path.
     *
     * @param classPath path to the sought class.
     * @param methodName sought method name.
     *
     * @return sought method in class, null otherwise.
     */
    public static Method getMethod(final String classPath, final String methodName) {
        return getMethod(classPath, methodName, EMPTY_CLASS_ARRAY);
    }

    /**
     * Returns the sought method with given argument list in the class
     * identified by the given class path.
     *
     * @param classPath path to the sought class.
     * @param methodName sought method name.
     * @param parameters sought method parameter Class array.
     *
     * @return sought method with parameters in class, null otherwise.
     */
    public static Method getMethod(final String classPath, final String methodName,
                                   final Class<?>[] parameters) {
        final Class<?> clazz = getClass(classPath);
        if (clazz != null) {
            return getMethod(clazz, methodName, parameters);
        }

        return null;
    }

    /**
     * Returns the sought method with given argument list in the given class.
     *
     * @param clazz class.
     * @param methodName sought method name.
     * @param parameters sought method parameter Class array.
     *
     * @return sought method with parameters in class, null otherwise.
     */
    public static Method getMethod(final Class<?> clazz, final String methodName,
                                   final Class<?>[] parameters) {
        if (clazz != null) {
            try {
                return clazz.getMethod(methodName, parameters);
            } catch (NoSuchMethodException nsme) {
                _logger.warn("Cannot find method '{}' of class '{}'", methodName, clazz, nsme);
            }
        }

        return null;
    }

    /**
     * Test whether a method exists, from a given class path.
     *
     * @param classPath path to the sought class.
     * @param methodName sought method name.
     *
     * @return true if the method exists, false otherwise.
     */
    public static boolean hasMethod(final String classPath, final String methodName) {
        return hasMethod(classPath, methodName, EMPTY_CLASS_ARRAY);
    }

    /**
     * Test whether a method with guven parameters list exists, from a given
     * class path.
     *
     * @param classPath path to the sought class.
     * @param methodName sought method name.
     * @param parameters sought parameters array.
     *
     * @return true if the method exists, false otherwise.
     */
    public static boolean hasMethod(final String classPath, final String methodName,
                                    final Class<?>[] parameters) {
        if (getMethod(classPath, methodName, parameters) != null) {
            if (_logger.isDebugEnabled()) {
                _logger.debug("Found method '{}' in class '{}'.", methodName, classPath);
            }

            return true;
        }

        return false;
    }

    /**
     * Returns the execution result of the method in the class identified by
     * their own names.
     *
     * @param classPath path to the sought class.
     * @param methodName sought method name.
     *
     * @return result of the method execution, null otherwise.
     */
    public static Object getMethodValue(final String classPath, final String methodName) {
        return getMethodValue(classPath, methodName, EMPTY_CLASS_ARRAY);
    }

    /**
     * Returns the execution result of the method with given parameters in the
     * class identified by their own names.
     *
     * @param classPath path to the sought class.
     * @param methodName sought method name.
     * @param parameters parameters array.
     *
     * @return result of the method execution, null otherwise.
     */
    public static Object getMethodValue(final String classPath, final String methodName,
                                        final Class<?>[] parameters) {
        return getMethodValue(classPath, methodName, parameters, EMPTY_OBJECT_ARRAY);
    }

    /**
     * Returns the execution result of the method with given parameters and
     * argument values in the class identified by their own names.
     *
     * @param classPath path to the sought class.
     * @param methodName sought method name.
     * @param parameters parameters array.
     * @param arguments arguments array.
     *
     * @return result of the method execution, null otherwise.
     */
    public static Object getMethodValue(final String classPath, final String methodName,
                                        final Class<?>[] parameters, final Object[] arguments) {
        final Method method = getMethod(classPath, methodName, parameters);

        return getMethodValue(method, getInstance(classPath), arguments);
    }

    /**
     * Returns the execution result of the method (no-arguments) with the given class instance.
     *
     * @param method method to invoke.
     * @param instance class instance to use
     *
     * @return result of the method execution, null otherwise.
     */
    public static Object getMethodValue(final Method method, final Object instance) {
        return getMethodValue(method, instance, EMPTY_OBJECT_ARRAY);
    }

    /**
     * Returns the execution result of the method with given argument values (static method).
     *
     * @param method method to invoke.
     * @param arguments arguments array.
     *
     * @return result of the method execution, null otherwise.
     */
    public static Object getMethodValue(final Method method, final Object[] arguments) {
        return getMethodValue(method, null, arguments);
    }

    /**
     * Returns the execution result of the method with given argument values.
     *
     * @param method method to invoke.
     * @param instance class instance to use
     * @param arguments arguments array.
     *
     * @return result of the method execution, null otherwise.
     */
    public static Object getMethodValue(final Method method, final Object instance, final Object[] arguments) {
        if (method != null) {
            try {
                return method.invoke(instance, arguments);
            } catch (IllegalAccessException iae) {
                _logger.warn("Cannot get result of method '{}'", method.getName(), iae);
            } catch (IllegalArgumentException iae) {
                _logger.warn("Cannot get result of method '{}'", method.getName(), iae);
            } catch (InvocationTargetException ite) {
                _logger.warn("Cannot get result of method '{}'", method.getName(), ite);
            }
        }
        return null;
    }

    /**
     * Execute a method in the class identified by their own names, without
     * returning the result.
     *
     * @param classPath path to the sought class.
     * @param methodName sought method name.
     *
     * @return true if invocation succeeded, false otherwise.
     */
    public static boolean executeMethod(final String classPath, final String methodName) {
        return executeMethod(classPath, methodName, EMPTY_CLASS_ARRAY);
    }

    /**
     * Execute a method with given parameters in the class identified by their
     * own names, without returning the result.
     *
     * @param classPath path to the sought class.
     * @param methodName sought method name.
     * @param parameters parameters array.
     *
     * @return true if invocation succeeded, false otherwise.
     */
    public static boolean executeMethod(final String classPath, final String methodName,
                                        final Class<?>[] parameters) {
        return executeMethod(classPath, methodName, parameters, EMPTY_OBJECT_ARRAY);
    }

    /**
     * Execute a method with given parameters and argument values in the class
     * identified by their own names, without returning the result.
     *
     * @param classPath path to the sought class.
     * @param methodName sought method name.
     * @param parameters parameters array.
     * @param arguments arguments array.
     *
     * @return true if invocation succeeded, false otherwise.
     */
    public static boolean executeMethod(final String classPath, final String methodName,
                                        final Class<?>[] parameters, final Object[] arguments) {
        final Method method = getMethod(classPath, methodName, parameters);

        return executeMethod(method, null, arguments);
    }

    /**
     * Execute the given method with given argument values, without returning the result.
     *
     * @param method method to invoke.
     * @param arguments arguments array.
     *
     * @return true if invocation succeeded, false otherwise.
     */
    public static boolean executeMethod(final Method method, final Object[] arguments) {
        return executeMethod(method, null, arguments);
    }

    /**
     * Execute the given method with given argument values, without returning the result.
     *
     * @param method method to invoke.
     * @param instance class instance to use
     * @param arguments arguments array.
     *
     * @return true if invocation succeeded, false otherwise.
     */
    public static boolean executeMethod(final Method method, final Object instance, final Object[] arguments) {
        boolean ok = false;
        if (method != null) {
            try {
                method.invoke(instance, arguments);

                ok = true;

            } catch (IllegalAccessException iae) {
                _logger.warn("Cannot invoke method '{}'", method.getName(), iae);
            } catch (IllegalArgumentException iae) {
                _logger.warn("Cannot invoke method '{}'", method.getName(), iae);
            } catch (InvocationTargetException ite) {
                _logger.warn("Cannot invoke method '{}'", method.getName(), ite);
            }
        }
        return ok;
    }

    /**
     * Returns the list of fields of the class identified by the given class
     * path.
     *
     * @param classPath path to the sought class.
     *
     * @return Array of found fields, null otherwise.
     */
    public static Field[] getFields(final String classPath) {
        final Class<?> clazz = getClass(classPath);
        return (clazz != null) ? clazz.getFields() : null;
    }

    /**
     * Returns the sought field in the class identified by the given class path.
     *
     * @param classPath path to the sought class.
     * @param fieldName sought field name.
     *
     * @return sought field in class, null otherwise.
     */
    public static Field getField(final String classPath, final String fieldName) {
        return getField(getClass(classPath), fieldName);
    }

    /**
     * Returns the sought field in the given class.
     *
     * @param clazz class.
     * @param fieldName sought field name.
     *
     * @return sought field in class, null otherwise.
     */
    public static Field getField(final Class<?> clazz, final String fieldName) {
        if (clazz != null) {
            try {
                return clazz.getDeclaredField(fieldName);
            } catch (NoSuchFieldException nsfe) {
                _logger.warn("Cannot get field '{}'", fieldName, nsfe);
            }
        }
        return null;
    }

    /**
     * Test whether a field exists, from a given class path.
     *
     * @param classPath path to the sought class.
     * @param fieldName sought field name.
     *
     * @return true if the field exists, false otherwise.
     */
    public static boolean hasField(final String classPath, final String fieldName) {
        if (getField(classPath, fieldName) != null) {
            if (_logger.isDebugEnabled()) {
                _logger.debug("Found field '{}' in class '{}'.", fieldName, classPath);
            }

            return true;
        }

        return false;
    }

    /**
     * Returns the value of the field in the class identified by the given class
     * path.
     *
     * @param classPath sought class name.
     * @param fieldName sought field name.
     *
     * @return value of the sought field, null otherwise.
     */
    public static Object getFieldValue(final String classPath, final String fieldName) {
        final Field field = getField(classPath, fieldName);
        Object value = null;

        try {
            value = field.get(getInstance(classPath));
        } catch (IllegalArgumentException iae) {
            _logger.warn("Cannot get value of field '{}'", fieldName, iae);
        } catch (IllegalAccessException iae) {
            _logger.warn("Cannot get value of field '{}'", fieldName, iae);
        }

        return value;
    }

    /**
     * Private constructor
     */
    private IntrospectionUtils() {
        super();
    }
}
