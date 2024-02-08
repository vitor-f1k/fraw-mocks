package pt.segsocial.iies.fraw.api;


import pt.segsocial.iies.fraw.annotations.MockMe;

import java.util.List;

@MockMe
public interface DummyService {

    boolean printBoolean();

    float printFloat();

    short printShort();

    int printInt();

    void print_void();

    Void print_Void();

    String printString();

    String printStringWithOneParam(String arg);

    String printStringWithTwoParam(String arg1, Long arg2);

    String[] printStringArray();

    int[] printIntArray();

    //String[][] printDoubleArray(); // TODO

    List<String> printListOfStrings();

    void printThrowsException() throws IllegalAccessException, IllegalArgumentException;

}
