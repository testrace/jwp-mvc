package core.mvc.tobe;

import static org.reflections.ReflectionUtils.Methods;
import static org.reflections.util.ReflectionUtilsPredicates.withAnnotation;

import com.google.common.collect.Maps;
import core.annotation.web.Controller;
import core.annotation.web.RequestMapping;
import core.annotation.web.RequestMethod;
import java.lang.reflect.Field;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AnnotationHandlerMapping {

    private static final Logger logger = LoggerFactory.getLogger(AnnotationHandlerMapping.class);

    private Object[] basePackage;

    private Map<HandlerKey, HandlerExecution> handlerExecutions = Maps.newHashMap();

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
            final List<HandlerKey> handlerKeys = createHandlerKey(method);
            final HandlerExecution handlerExecution = new HandlerExecution(handler, method);
            mappingHandler(handlerKeys, handlerExecution);
        }
    }

    private void mappingHandler(final List<HandlerKey> handlerKeys, final HandlerExecution handlerExecution) {
        for (final HandlerKey handlerKey : handlerKeys) {
            handlerExecutions.put(handlerKey, handlerExecution);
        }
    }

    private static List<HandlerKey> createHandlerKey(final Method method) {
        final RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);

        final String value = requestMapping.value();

        final RequestMethod requestMethod = requestMapping.method();
        if (requestMethod == null) {
            return Arrays.stream(RequestMethod.values())
                .map(it -> new HandlerKey(requestMapping.value(), it))
                .collect(Collectors.toList());
        }

        return List.of(new HandlerKey(requestMapping.value(), requestMethod));
    }

    private static Object getHandlerInstance(final Class<?> controller) {
        try {
            return controller.getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public HandlerExecution getHandler(HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        RequestMethod rm = RequestMethod.valueOf(request.getMethod().toUpperCase());
        return handlerExecutions.get(new HandlerKey(requestUri, rm));
    }
}
