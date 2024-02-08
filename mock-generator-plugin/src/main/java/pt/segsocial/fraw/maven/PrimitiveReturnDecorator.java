package pt.segsocial.fraw.maven;

import java.io.PrintStream;

final public class PrimitiveReturnDecorator extends ReturnDecorator {

    static {
        new PrimitiveReturnDecorator(void.class);
        new PrimitiveReturnDecorator(boolean.class);
        new PrimitiveReturnDecorator(short.class);
        new PrimitiveReturnDecorator(int.class);
        new PrimitiveReturnDecorator(long.class);
        new PrimitiveReturnDecorator(float.class);
        new PrimitiveReturnDecorator(double.class);
    }


    public PrimitiveReturnDecorator(Class<?> type) {
        super(type.getSimpleName());
    }

    @Override
    protected void decorate(PrintStream ps) {
        super.decorate(ps);
        if(getType().equals(void.class.getSimpleName()))
            ps.print("");
        else if(getType().equals(boolean.class.getSimpleName()))
            ps.print("false");
        else if(getType().equals(short.class.getSimpleName()))
            ps.print("0");
        else if(getType().equals(int.class.getSimpleName()))
            ps.print("0");
        else if(getType().equals(long.class.getSimpleName()))
            ps.print("0L");
        else if(getType().equals(float.class.getSimpleName()))
            ps.print("0.0f");
        else if(getType().equals(double.class.getSimpleName()))
            ps.print("0.0d");
        ps.println(";");
    }

    @Override
    public void decorate(PrintStream ps, Class<?> returnType) {
        decorate(ps);
    }

}
