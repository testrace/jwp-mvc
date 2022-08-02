package core.mvc.tobe;

import static org.reflections.ReflectionUtils.Methods;
import static org.reflections.util.ReflectionUtilsPredicates.withAnnotation;

import com.google.common.collect.Maps;
import core.annotation.web.Controller;
import core.annotation.web.RequestMapping;
import core.annotation.web.RequestMethod;
import core.mvc.tobe.exception.InvalidInstanceException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import org.reflections.ReflectionUtils;
import org.reflections.Reflections;

public class AnnotationHandlerMapping {

    private final Object[] basePackage;

    private final Map<HandlerKey, HandlerExecution> handlerExecutions = Maps.newHashMap();

    public AnnotationHandlerMapping(Object... basePackage) {
        this.basePackage = basePackage;
    }

    public void initialize() {
        final Reflections reflections = new Reflections(basePackage);
        final Set<Class<?>> controllers = reflections.getTypesAnnotatedWith(Controller.class);

        for (final Class<?> controller : controllers) {
            final Set<Method> methods = ReflectionUtils.get(Methods.of(controller, withAnnotation(RequestMapping.class)));
            mappingHandler(controller, methods);
        }
    }

    private void mappingHandler(final Class<?> controller, final Set<Method> methods) {
        final Object handler = getHandlerInstance(controller);

        for (final Method method : methods) {
            final List<HandlerKey> handlerKeys = createHandlerKeys(method);
            final HandlerExecution handlerExecution = new HandlerExecution(handler, method);
            mappingHandler(handlerKeys, handlerExecution);
        }
    }

    private static List<HandlerKey> createHandlerKeys(final Method method) {
        final RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);

        final RequestMethod[] requestMethods = requestMapping.method();
        final String url = requestMapping.value();

        if (notExistsMethods(requestMethods)) {
            return createHandlerKeys(url, RequestMethod.values());
        }

        return createHandlerKeys(url, requestMethods);
    }

    private static boolean notExistsMethods(final RequestMethod[] requestMethods) {
        return requestMethods.length == 0;
    }

    private static List<HandlerKey> createHandlerKeys(final String url, final RequestMethod[] requestMethods) {
        return Arrays.stream(requestMethods)
            .map(requestMethod -> new HandlerKey(url, requestMethod))
            .collect(Collectors.toList());
    }

    private void mappingHandler(final List<HandlerKey> handlerKeys, final HandlerExecution handlerExecution) {
        for (final HandlerKey handlerKey : handlerKeys) {
            handlerExecutions.put(handlerKey, handlerExecution);
        }
    }

    private static Object getHandlerInstance(final Class<?> controller) {
        try {
            return controller.getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new InvalidInstanceException(e);
        }
    }

    public HandlerExecution getHandler(HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        RequestMethod rm = RequestMethod.valueOf(request.getMethod().toUpperCase());
        return handlerExecutions.get(new HandlerKey(requestUri, rm));
    }
}
