package pt.segsocial.iies.fraw;

import org.junit.Before;
import org.junit.Test;
import pt.segsocial.iies.fraw.annotations.mock.Mocking;
import pt.segsocial.iies.fraw.api.DummyService;
import pt.segsocial.iies.fraw.api.IIMock_DummyService;
import pt.segsocial.iies.fraw.mock.mapper.AbstractCustomMock;

public class MockTest {

    private DummyService service;

    @Before
    public void setUp() {
        new CustomMock();
        this.service = new IIMock_DummyService();
    }

    @Test
    public void testCustomMock() throws Exception {

        System.out.println(service.printStringWithOneParam("Batatas"));


    }


    @Test
    public void testDefaultMock() throws Exception {

        System.out.println(service.printStringWithOneParam("Batatas"));

    }

    public static class CustomMock extends AbstractCustomMock {

        @Mocking(mockedClass = DummyService.class)
        public String printStringWithOneParam(String arg) {
            return "Got string from custom Mock! " + arg;
        }

        @Override
        public <T> T callback(Object... args) throws Throwable {
            return null;
        }
    }



}
