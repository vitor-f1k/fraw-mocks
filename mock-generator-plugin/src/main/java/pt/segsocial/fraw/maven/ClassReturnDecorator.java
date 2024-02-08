package pt.segsocial.fraw.maven;

import java.io.PrintStream;

public class ClassReturnDecorator extends ReturnDecorator {

    static {
        new ClassReturnDecorator();
    }

    public ClassReturnDecorator() {
        super("class");
    }

    protected void decorate(PrintStream ps) {
        super.decorate(ps);
        ps.println("null;");
    }

    @Override
    public void decorate(PrintStream ps, Class<?> returnType) {
        if(returnType.isArray()) {
            super.decorate(ps);
            Class<?> componentType = returnType.getComponentType();
            ps.print("new ");
            ps.print(componentType.getName());
            ps.println("[0];");
        } else {
            decorate(ps);
        }
    }


}
