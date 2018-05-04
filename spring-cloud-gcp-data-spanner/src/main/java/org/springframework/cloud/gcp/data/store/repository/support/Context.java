package org.springframework.cloud.gcp.data.store.repository.support;

import com.google.cloud.datastore.PathElement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

public class Context implements AutoCloseable {

    private static ThreadLocal<Deque<PathElement>> localAncestorsStack = new ThreadLocal<Deque<PathElement>>() {
        @Override
        protected Deque<PathElement> initialValue() {
            return new LinkedList<PathElement>();
        }
    };

    private int count;

    public Context(int count) {
        this.count = count;
    }

    public static Context with(PathElement ancestor, PathElement... other) {
        List<PathElement> ancestors = new ArrayList<>(other.length + 1);
        ancestors.add(ancestor);
        ancestors.addAll(Arrays.asList(other));
        return with(ancestors);
    }

    public static Context with(Iterable<PathElement> ancestors) {
        Deque<PathElement> ancestorsStack = getAncestors();
        int count = 0;
        for (PathElement ancestor : ancestors) {
            ancestorsStack.addLast(ancestor);
            count++;
        }
        return new Context(count);
    }

    @Override
    public void close() {
        Deque<PathElement> ancestors = getAncestors();
        for (int i = 0; i < this.count; i++) {
            ancestors.removeLast();
        }
    }

    public static Deque<PathElement> getAncestors() {
        return localAncestorsStack.get();
    }
}
