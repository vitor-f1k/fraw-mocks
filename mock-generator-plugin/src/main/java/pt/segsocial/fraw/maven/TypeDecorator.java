package pt.segsocial.fraw.maven;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

public abstract class TypeDecorator {

    private static Map<String, TypeDecorator> DECORATORS = new HashMap<>();

    static {
        new PrimitiveReturnDecorator(void.class);
        new PrimitiveReturnDecorator(boolean.class);
        new PrimitiveReturnDecorator(short.class);
        new PrimitiveReturnDecorator(int.class);
        new PrimitiveReturnDecorator(long.class);
        new PrimitiveReturnDecorator(float.class);
        new PrimitiveReturnDecorator(double.class);
        new ClassReturnDecorator();
    }

    protected static void register(String type, TypeDecorator decorator) {
        DECORATORS.put(type, decorator);
    }


    static TypeDecorator getDecorator(Class<?> clazz) {
        if(clazz.isPrimitive()) {
            return DECORATORS.get(clazz.getSimpleName());
        } else {
            return DECORATORS.get("class");
        }
    }

    private String type;

    protected TypeDecorator(String type) {
        this.type = type;
        register(this.type, this);
    }

    protected String getType() {
        return type;
    }

    protected abstract void decorate(PrintStream ps);

    public abstract void decorate(PrintStream ps, Class<?> returnType);

}
