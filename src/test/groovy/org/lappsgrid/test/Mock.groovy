package org.lappsgrid.test

import org.lappsgrid.gate.ampq.rest.util.Counter

/**
 * Mockito seems to have problems mocking Groovy classes in Spring Boot test cases.
 * Since Groovy is already pretty good at mocking we will just use Groovy's ability to use a HashMap of Closures to
 * provide mock instances.
 * <p>
 * Suppose we have the class:
 * <pre>
 * class Worker {
 *     String work(String input) { ... }
 * }
 * </pre>
 * We can provide a mock for it with:
 * <pre>
 * Mock m = new Mock(work: { "hello world" })
 * Worker w = m.methods as Worker
 * assert "hello world" == w.work()
 * assert m.called("work", 1)
 * </pre>
 */
class Mock {

    static Map<Class, Mock> instances = [:]

    // Implementations of the mock methods.
    Map<String,Closure> methods = [:]

    // Count the number of times each method is invoked.
    Map<String, Counter> methodCalls = [:]

    Mock() { }
    Mock(Map<String,Closure> map) {
        map.each { k,v ->
            methods.put(k,wrap(k,v))
        }
    }

    static Object Create(Class target, Map<String,Closure> methods)  {
        Mock mock = new Mock(methods)
        instances[target] = mock
        return mock.methods.asType(target)
    }

    static Object Create(Map<String,Closure> methods, Class target) {
        Mock mock = new Mock(methods)
        instances[target] = mock
        return mock.methods.asType(target)
    }

    static int called(Class theClass, String method) {
        Mock mock = instances[theClass]
        if (mock == null) {
            return 0
        }
        return mock.called(method)
    }

    void mock(String key, Closure f) {
        methods.put(key, wrap(key,f))
    }

    int called(String name) {
        Counter counter = methodCalls.get(name)
        if (counter == null) {
            return 0
        }
        return counter.count
    }

    boolean called(String name, int times) {
        Counter counter = methodCalls.get(name)
        if (counter == null) {
            return  0 == times
        }
        return times == counter.count
    }

    private Closure wrap(String name, Closure method) {
        return { Object... args ->
            record(name)
//            method.call(args)
            if (args.size() == 0) {
                method()
            }
            else if (args.size() == 1) {
                method(args[0])
            }
            else if (args.size() == 2) {
                method(args[0], args[1])
            }
            else if (args.size() == 3) {
                method(args[0], args[1], args[2])
            }
            else {
                throw new UnsupportedOperationException("Can not mock a method with more than three parameters.")
            }
        }
    }

    /** The Groovy magic that allows us to "cast" a HashMap to a class instance. */
    def asType(Class theClass) {
        return methods.asType(theClass)
    }

    /** Record that the method named <code>key</code> has been called. */
    private void record(String key) {
        Counter counter = methodCalls.get(key)
        if (counter == null) {
            counter = new Counter()
            methodCalls.put(key, counter)
        }
        counter.next()
    }
}