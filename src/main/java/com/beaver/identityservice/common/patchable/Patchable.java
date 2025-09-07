package com.beaver.identityservice.common.patchable;

import com.beaver.identityservice.common.annotations.PatchableField;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public interface Patchable {

    Map<Class<?>, Set<String>> PATCHABLE_CACHE = new ConcurrentHashMap<>();

    default <D> void patchFrom(D dto) {
        if (dto == null) return;

        BeanWrapper src = new BeanWrapperImpl(dto);
        BeanWrapper dst = new BeanWrapperImpl(this);

        Set<String> allowed = PATCHABLE_CACHE.computeIfAbsent(
                this.getClass(), Patchable::resolveAllowedFields
        );

        for (PropertyDescriptor pd : dst.getPropertyDescriptors()) {
            String name = pd.getName();
            if (!allowed.contains(name)) continue;
            if (!src.isReadableProperty(name) || !dst.isWritableProperty(name)) continue;

            Object val = src.getPropertyValue(name);
            if (val != null) {
                dst.setPropertyValue(name, val);
            }
        }
    }

    private static Set<String> resolveAllowedFields(Class<?> type) {
        Set<String> names = new HashSet<>();
        for (Class<?> c = type; c != null && c != Object.class; c = c.getSuperclass()) {
            for (Field f : c.getDeclaredFields()) {
                if (!f.isAnnotationPresent(PatchableField.class)) continue;

                // never allow identifiers/audit/version implicitly
                if (f.isAnnotationPresent(OneToOne.class) ||
                        f.isAnnotationPresent(OneToMany.class) ||
                        f.isAnnotationPresent(ManyToOne.class) ||
                        f.isAnnotationPresent(ManyToMany.class)) {
                    continue;
                }
                String n = f.getName();
                if (!Set.of("id", "createdAt", "updatedAt", "version").contains(n)) {
                    names.add(n);
                }
            }
        }
        return Collections.unmodifiableSet(names);
    }
}

