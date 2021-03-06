/*
 * Copyright (c) 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

package jdk.tools.jaotc.collect;

import jdk.tools.jaotc.LoadedClass;

import java.util.ArrayList;
import java.util.List;

public final class ClassSearch {
    private final List<SourceProvider> providers = new ArrayList<>();

    public void addProvider(SourceProvider provider) {
        providers.add(provider);
    }

    public List<LoadedClass> search(List<SearchFor> search, SearchPath searchPath) {
        List<LoadedClass> loaded = new ArrayList<>();

        List<ClassSource> sources = new ArrayList<>();

        for (SearchFor entry : search) {
            sources.add(findSource(entry, searchPath));
        }

        for (ClassSource source : sources) {
            source.eachClass((name, loader) -> loaded.add(loadClass(name, loader)));
        }

        return loaded;
    }

    private static LoadedClass loadClass(String name, ClassLoader loader) {
        try {
            Class<?> clzz = loader.loadClass(name);
            return new LoadedClass(name, clzz);
        } catch (ClassNotFoundException e) {
            throw new InternalError("Failed to load with: " + loader, e);
        }
    }

    private ClassSource findSource(SearchFor searchFor, SearchPath searchPath) {
        ClassSource found = null;

        for (SourceProvider provider : providers) {
            if (!searchFor.isUnknown() && !provider.supports(searchFor.getType())) {
                continue;
            }

            ClassSource source = provider.findSource(searchFor.getName(), searchPath);
            if (source != null) {
                if (found != null) {
                    throw new InternalError("Multiple possible sources: " + source + " and: " + found);
                }
                found = source;
            }
        }

        if (found == null) {
            throw new InternalError("Failed to find " + searchFor.getType() + " file: " + searchFor.getName());
        }
        return found;
    }

    public static List<SearchFor> makeList(String type, String argument) {
        List<SearchFor> list = new ArrayList<>();
        String[] elements = argument.split(":");
        for (String element : elements) {
            list.add(new SearchFor(element, type));
        }
        return list;
    }
}
