/*
 * Copyright 2011 frdfsnlght <frdfsnlght@gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.frdfsnlght.transporter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.regex.PatternSyntaxException;

/**
 *
 * @author frdfsnlght <frdfsnlght@gmail.com>
 */
public final class Options {

    private Object target;
    private Set<String> names;
    private String basePerm;
    private OptionsListener listener = null;

    public Options(Object target, Set<String> names, String basePerm, OptionsListener listener) {
        this.target = target;
        this.names = names;
        this.basePerm = basePerm;
        this.listener = listener;
    }

    public void getOptions(Context ctx, String option) throws OptionsException, PermissionsException  {
        List<String> options = new ArrayList<String>();
        String opt = resolveOption(option);
        if (opt != null)
            options.add(opt);
        else {
            option = option.replaceAll("(^|[^\\.])\\*", ".*");
            for (String o : names)
                try {
                    if (o.matches(option))
                        options.add(o);
                } catch (PatternSyntaxException e) {}
            if (options.isEmpty())
                throw new OptionsException("no options match");
        }
        Collections.sort(options);
        for (String o : options) {
            try {
                ctx.send("%s=%s", o, getOption(ctx, o));
            } catch (PermissionsException e) {}
        }
    }

    private String resolveOption(String option) throws OptionsException {
        // look for literal match
        for (String opt : names) {
            if (opt.toLowerCase().equals(option.toLowerCase()))
                return opt;
        }
        // look for starting match
        String matched = null;
        for (String opt : names) {
            if (opt.toLowerCase().startsWith(option.toLowerCase())) {
                if (matched != null)
                    throw new OptionsException("option is ambiguous");
                matched = opt;
            }
        }
        return matched;
    }

    public String getOption(Context ctx, String option) throws OptionsException, PermissionsException  {
        option = resolveOption(option);
        if (option == null)
            throw new OptionsException("unknown option");
        Permissions.require(ctx.getPlayer(), basePerm + ".option.get." + listener.getOptionPermission(ctx, option));
        String methodName = "get" +
                option.substring(0, 1).toUpperCase() +
                option.substring(1);
        try {
            Class<?> cls;
            if (target instanceof Class)
                cls = (Class)target;
            else
                cls = target.getClass();
            Method m = cls.getMethod(methodName);
            Object value = m.invoke(target);
            if (value == null) return "";
            return value.toString();
        } catch (InvocationTargetException ite) {
            throw new OptionsException(ite.getCause().getMessage());
        } catch (NoSuchMethodException nsme) {
            throw new OptionsException("invalid method '%s'", methodName);
        } catch (IllegalAccessException iae) {
            throw new OptionsException("unable to read the option");
        }
    }

    @SuppressWarnings("unchecked")
    public void setOption(Context ctx, String option, String value) throws OptionsException, PermissionsException {
        option = resolveOption(option);
        if (option == null)
            throw new OptionsException("unknown option");
        Permissions.require(ctx.getPlayer(), basePerm + ".option.set." + listener.getOptionPermission(ctx, option));
        String methodName = "set" +
                option.substring(0, 1).toUpperCase() +
                option.substring(1);
        try {
            Class<?> cls;
            if (target instanceof Class)
                cls = (Class)target;
            else
                cls = target.getClass();
            Method m = cls.getMethod("g" + methodName.substring(1));
            Class rCls = m.getReturnType();
            m = cls.getMethod(methodName, rCls);
            if (rCls == Boolean.TYPE)
                m.invoke(target, Boolean.parseBoolean(value));
            else if (rCls == Integer.TYPE)
                m.invoke(target, Integer.parseInt(value));
            else if (rCls == Float.TYPE)
                m.invoke(target, Float.parseFloat(value));
            else if (rCls == Double.TYPE)
                m.invoke(target, Double.parseDouble(value));
            else if (rCls == String.class)
                m.invoke(target, value);
            else if (rCls.isEnum())
                try {
                    m.invoke(target, Utils.valueOf(rCls, value));
                } catch (IllegalArgumentException iae) {
                    throw new OptionsException(iae.getMessage() + " option value '%s'", value);
                }
            else
                throw new OptionsException("unsupported option type '%s'", rCls);

            if (listener != null)
                listener.onOptionSet(ctx, option, getOption(ctx, option));

        } catch (InvocationTargetException ite) {
            if (ite.getCause().getMessage() == null)
                throw new OptionsException(ite.getCause().toString());
            else
                throw new OptionsException(ite.getCause().getMessage());
        } catch (NoSuchMethodException nsme) {
            throw new OptionsException("invalid method '%s'", methodName);
        } catch (IllegalAccessException iae) {
            throw new OptionsException("unable to set the option");
        }
    }

}
