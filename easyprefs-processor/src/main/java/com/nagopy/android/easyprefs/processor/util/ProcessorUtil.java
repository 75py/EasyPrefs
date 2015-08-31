package com.nagopy.android.easyprefs.processor.util;

import com.nagopy.android.easyprefs.SingleSelectionItem;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.SimpleElementVisitor7;
import javax.lang.model.util.SimpleTypeVisitor7;

public class ProcessorUtil {

    private ProcessorUtil() {
        throw new AssertionError();
    }

    public static Optional<Element> toElement(TypeMirror typeMirror) {
        Class<?> cls = typeMirror.getClass();
        try {
            Method asElement = cls.getDeclaredMethod("asElement");
            return Optional.of((Element) asElement.invoke(typeMirror));
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return Optional.empty(); // Not found
    }

    private static Set<String> getInterfaces(final Element element) {
        final Set<String> interfaces = new HashSet<>();
        if (element != null) element.accept(new SimpleElementVisitor7<Void, Void>() {
            @Override
            public Void visitType(final TypeElement e, final Void p) {
                interfaces.addAll(getInterfaces(e.getInterfaces()));
                TypeMirror superClass = e.getSuperclass();
                if (superClass.getKind().equals(TypeKind.NONE)) {
                    return null;
                }
                return superClass.accept(new SimpleTypeVisitor7<Void, Void>() {
                    @Override
                    public Void visitDeclared(final DeclaredType t, final Void p) {
                        interfaces.addAll(getInterfaces(t.asElement()));
                        return null;
                    }
                }, null);
            }
        }, null);
        return interfaces;
    }

    private static Set<String> getInterfaces(final List<? extends TypeMirror> typeMirrors) {
        Set<String> interfaces = new HashSet<>();
        for (final TypeMirror typeMirror : typeMirrors) {
            interfaces.add(typeMirror.toString());
        }
        return interfaces;
    }

    public static boolean isImplements(final Element element, final String interfaceFullName) {
        final Set<String> interfaces = getInterfaces(element);
        return interfaces.contains(interfaceFullName);
    }
}
