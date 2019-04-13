package dev.tinyz.excel2json.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class AnnotationUtils {

    private AnnotationUtils() {
        //  no-op
    }

    public static List<Method> findMethodByAnnotation(Class annotation, Class clazz, boolean declared) {
        return findElementByAnnotation(annotation, clazz, (clz) -> declared ? clz.getDeclaredMethods() : clz.getMethods());
    }

    public static List<Field> findFieldByAnnotation(final Class annotation, final Class clazz, final boolean declared) {
        return findElementByAnnotation(annotation, clazz, (clz) -> declared ? clz.getDeclaredFields() : clz.getFields());
    }

    @SuppressWarnings("unchecked")
    public static <T extends AnnotatedElement> List<T> findElementByAnnotation(Class annotation, Class clazz, Function<Class, T[]> func) {
        List<T> elements = new ArrayList<>();
        if (clazz == Object.class)
            return elements;
        elements.addAll(Arrays.stream(func.apply(clazz))
                .filter(field -> field.getAnnotation(annotation) != null)
                .collect(Collectors.toList()));
        elements.addAll(findElementByAnnotation(annotation, clazz.getSuperclass(), func));
        return elements;
    }

    public static <A extends Annotation> A findAnnotation(final Class<A> targetAnnotation, final Class<?> annotatedType) {
        A foundAnnotation = annotatedType.getAnnotation(targetAnnotation);
        if (foundAnnotation == null) {
            for (Annotation annotation : annotatedType.getAnnotations()) {
                Class<? extends Annotation> annotationType = annotation.annotationType();
                if (annotationType.isAnnotationPresent(targetAnnotation)) {
                    foundAnnotation = annotationType.getAnnotation(targetAnnotation);
                    break;
                }
            }
        }
        return foundAnnotation;
    }

    public static boolean isAnnotationPresent(final Class<? extends Annotation> targetAnnotation, final Class<?> annotatedType) {
        return findAnnotation(targetAnnotation, annotatedType) != null;
    }

    public static List<Method> getMethodsAnnotatedWith(final Class<? extends Annotation> annotation, final Object object, boolean isOnlyPublic) {
        Method[] methods = isOnlyPublic
                ? object.getClass().getMethods()
                : object.getClass().getDeclaredMethods();
        List<Method> annotatedMethods = new ArrayList<>();
        for (Method method : methods) {
            if (method.isAnnotationPresent(annotation)) {
                annotatedMethods.add(method);
            }
        }
        return annotatedMethods;
    }

}
