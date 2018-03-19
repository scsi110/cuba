package com.haulmont.cuba.web.app.embedded.transport;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.haulmont.cuba.web.AppUI;
import com.vaadin.ui.JavaScript;
import com.vaadin.ui.JavaScriptFunction;
import elemental.json.JsonArray;
import elemental.json.JsonValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.stream.Collectors;

public class RemoteApp implements CallbackHolder {
    public static final String HOST_APP_NAME = "host";

    private static final Logger log = LoggerFactory.getLogger(RemoteApp.class);

    private final Gson gson = new Gson();

    private final String remoteAppName;

    private final Map<Object, List<String>> registeredMethods = new HashMap<>();
    private final Map<String, Callback> callbacks = new HashMap<>();

    private final CallbackHolder callbackHolder;

    private int callbacksCounter = 0;

    public RemoteApp(String remoteAppName) {
        this.remoteAppName = remoteAppName;
        this.callbackHolder = get(CallbackHolder.class);
        this.register(this, CallbackHolder.class);
    }

    public <T> T get(Class<T> remote) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Object proxy = Proxy.newProxyInstance(
                classLoader,
                new Class[]{remote},
                new RemoteObjectInvocationHandler());
        return remote.cast(proxy);
    }

    public <T> void register(T local, Class<T> aClass) {
        if (registeredMethods.containsKey(local)) {
            log.warn(String.format("Tried to register same object twice {object = %s, class = %s}", local, aClass));
            return;
        }
        registeredMethods.put(local, new ArrayList<>());

        Arrays.stream(aClass.getMethods())
                .forEach(method -> registerMethod(local, method));
    }

    public void destroy() {
        registeredMethods.values()
                .stream()
                .flatMap(Collection::stream)
                .forEach(JavaScript.getCurrent()::removeFunction);

        registeredMethods.clear();
        callbacks.clear();
    }

    private <T> void registerMethod(T local, Method method) {
        String methodName = method.getName();
        String callbackName = "_" + remoteAppName + "_" + methodName;

        JavaScript.getCurrent().addFunction(
                callbackName,
                new JavaScriptFunction() {
                    private static final long serialVersionUID = -4907617879291725427L;

                    @Override
                    public void call(JsonArray arguments) {
                        callLocalMethod(arguments, method, local);
                    }
                });

        registeredMethods.get(local).add(callbackName);

        JavaScript.eval(String.format("registerMethod('%s','%s', %s);", methodName, remoteAppName, callbackName));
    }

    private <T> void callLocalMethod(JsonArray arguments, Method method, T local) {
        try {
            Class<?>[] parameterTypes = method.getParameterTypes();
            if (parameterTypes.length == 0) {
                method.invoke(local);
            } else {
                Annotation[][] annotatedTypes = method.getParameterAnnotations();

                JsonArray jargs = arguments.get(0);
                Object args[] = new Object[jargs.length()];
                for (int i = 0; i < jargs.length(); i++) {
                    args[i] = parseArg(parameterTypes[i], annotatedTypes[i], jargs.get(i));
                }
                method.invoke(local, args);
            }
        } catch (Exception e) {
            log.error("Error invoking method", e);
        }
    }

    private Object parseArg(Class<?> type, Annotation[] annotations, JsonValue jsonValue) {
        boolean isCallback = hasAnnotation(annotations, RemoteCallback.class);

        if (isCallback) {
            String callbackId = jsonValue.asString();
            return Proxy.newProxyInstance(
                    Thread.currentThread().getContextClassLoader(),
                    new Class[]{type},
                    (proxy, method, args) -> {
                        callbackHolder.execute(callbackId, gson.toJson(args));
                        return null;
                    });
        } else {
            return gson.fromJson(jsonValue.toJson(), type);
        }
    }

    private boolean hasAnnotation(Annotation[] annotations, Class<? extends Annotation> aClass) {
        return Arrays.stream(annotations)
                .anyMatch(annotation -> annotation.annotationType().equals(aClass));
    }

    @Override
    public void execute(String callbackId, String jsonArgs) {
        Callback callback = callbacks.get(callbackId);
        callback.run(jsonArgs);
    }

    private class RemoteObjectInvocationHandler implements InvocationHandler {
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (args == null) {
                args = new Object[0];
            }
            createRemoteCallbackIfNeeded(method, args);
            callMethod(method.getName(), args);
            return null;
        }

        private void createRemoteCallbackIfNeeded(Method method, Object[] args) {
            Annotation[][] annotations = method.getParameterAnnotations();
            Class<?>[] parameterTypes = method.getParameterTypes();

            for (int i = 0; i < args.length; i++) {
                if (hasAnnotation(annotations[i], RemoteCallback.class)) {
                    String callbackId = Integer.toString(callbacksCounter++);
                    callbacks.put(callbackId, new Callback(callbackId, args[i], parameterTypes[i].getMethods()[0]));
                    args[i] = callbackId;
                }
            }
        }

        private void callMethod(String methodName, Object[] params) {
            JavaScript.eval(String.format("callMethod('%s', '%s', '%s', %s)", remoteAppName, AppUI.getCurrent().getAppId(), methodName, toJson(params)));
        }

        private String toJson(Object[] args) {
            return Arrays.stream(args)
                    .map(gson::toJson)
                    .collect(Collectors.joining(",", "[", "]"));
        }
    }

    private class Callback {
        final String id;
        final Object callback;
        final Method method;

        private Callback(String id, Object callback, Method method) {
            this.id = id;
            this.callback = callback;
            this.method = method;
        }

        public void run(String jsonArgs) {
            try {
                Object[] args = new Object[method.getParameterCount()];
                Class<?>[] parameterTypes = method.getParameterTypes();
                JsonReader reader = gson.newJsonReader(new StringReader(jsonArgs));
                reader.beginArray();
                for (int i = 0; i < method.getParameterCount(); i++) {
                    args[i] = gson.fromJson(reader, parameterTypes[i]);
                }
                reader.endArray();
                reader.close();
                method.invoke(callback, args);
            } catch (Exception e) {
                log.error(String.format("Callback execution error {id = %s}", id), e);
            }
        }
    }
}
