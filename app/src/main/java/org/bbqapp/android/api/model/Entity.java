/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 bbqapp
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.bbqapp.android.api.model;

import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public abstract class Entity {
    private static final String TAG = Entity.class.getName();

    private String getMethodName(String prefix, String fieldName) {
        return prefix + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
    }

    private String getFieldName(String prefix, String methodName) {
        String fieldName = methodName.substring(prefix.length());
        fieldName = fieldName.substring(0, 1).toLowerCase() + fieldName.substring(1);
        return fieldName;
    }

    protected Object get(String fieldName) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method m;
        try {
            m = getClass().getMethod(getMethodName("get", fieldName));
        } catch (NoSuchMethodException e) {
            m = getClass().getMethod(getMethodName("is", fieldName));
        }

        return m.invoke(this);
    }

    protected void set(String fieldName, Object value) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method m = getClass().getMethod(getMethodName("set", fieldName), value.getClass());
        m.invoke(this, value);
    }

    protected Set<String> getFieldNames() {
        Set<String> getters = new HashSet<>();
        Set<String> setters = new HashSet<>();

        String methodName;
        for (Method method : getClass().getMethods()) {
            methodName = method.getName();
            if (!Modifier.isPublic(method.getModifiers())) {
                continue;
            }

            if (methodName.startsWith("set") && method.getParameterTypes().length == 1 && method.getReturnType().equals(Void.TYPE)) {
                setters.add(getFieldName("set", methodName));
            } else if (methodName.startsWith("get") && method.getParameterTypes().length == 0 && !method.getReturnType().equals(Void.TYPE)) {
                getters.add(getFieldName("get", methodName));
            } else if (methodName.startsWith("is") && method.getParameterTypes().length == 0 && !method.getReturnType().equals(Void.TYPE)) {
                getters.add(getFieldName("is", methodName));
            }
        }

        getters.retainAll(setters);
        return Collections.unmodifiableSet(getters);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append('[');

        Set<String> fields = getFieldNames();
        for (String fieldName : fields) {
            sb.append(fieldName);
            sb.append('=');
            try {
                sb.append(get(fieldName));
            } catch (NoSuchMethodException e) {
                Log.w(TAG, "Getter method for field " + fieldName + " not found.", e);
            } catch (InvocationTargetException e) {
                Log.w(TAG, "Error occurred during invocation getter method of " + fieldName + " field.", e);
            } catch (IllegalAccessException e) {
                Log.w(TAG, "Cannot access to getter method of " + fieldName + " field.", e);
            }
            sb.append(',');
        }

        if (!fields.isEmpty()) {
            sb.deleteCharAt(sb.length() - 1);
        }

        sb.append(']');

        return sb.toString();
    }
}
