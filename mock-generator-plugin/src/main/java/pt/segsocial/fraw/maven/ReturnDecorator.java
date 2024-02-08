package pt.segsocial.fraw.maven;

import java.io.PrintStream;

public abstract class ReturnDecorator extends TypeDecorator {
    public ReturnDecorator(String type) {
        super(type);
    }

    protected void decorate(PrintStream ps) {
        ps.print("\t\treturn ");
    }

}
