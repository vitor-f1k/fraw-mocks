package pt.segsocial.iies.fraw.mock.mapper;

import pt.segsocial.iies.fraw.annotations.Mocking;

import java.lang.reflect.Method;

public abstract class AbstractCustomMock {

    protected AbstractCustomMock() {
        Class<?> clazz = this.getClass();
        for(Method m : clazz.getDeclaredMethods()) {
            if(m.isAnnotationPresent(Mocking.class)) {
                System.out.println("mocking method: " + m);
                Mocking a = m.getAnnotation(Mocking.class);
                a.mockedClass();
                try {
                    MockRegistry.register(a.mockedClass(), this.getClass().getMethod(m.getName(), m.getParameterTypes()), this);
                } catch (NoSuchMethodException e) {
                    throw new RuntimeException(e.getMessage(), e);
                }
            }
        }
    }

    public abstract <T> T callback(Object ... args) throws Throwable;


}
