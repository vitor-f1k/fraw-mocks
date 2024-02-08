package pt.segsocial.iies.fraw.mock.mapper;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class MockRegistry {

    private static Map<String, MockExecutionInfo> MOCK_CANDIDATES = new HashMap<>();


    protected static void register(Class<?> mockedClazz, Method method, Object mock) {
        String mockId = buildMockId(mockedClazz, fetchMethodSignature(method));
        if(!MOCK_CANDIDATES.containsKey(mockId)) {
            MockExecutionInfo info = new MockExecutionInfo();
            info.mock = mock;
            info.method = method;
            MOCK_CANDIDATES.put(mockId, info);
        }
    }

    public static <T> T execute(Object mock, String methodName, Class<?>[] params, Object ... args) {
        try {
            Method method = mock.getClass().getMethod(methodName, params);
            return (T)method.invoke(mock, args);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static Object getMock(String mockId) {
        return MOCK_CANDIDATES.get(mockId);
    }

    public static String fetchMethodSignature(Method method) {
        return fetchMethodSignature(method.getReturnType(), method.getName(), method.getParameterTypes());
    }

    public static Object fetchMock(Class<?> clazz, String methodSig) {
        MockExecutionInfo mockExecutionInfo = (MockExecutionInfo) MockRegistry.getMock(MockRegistry.buildMockId(clazz, methodSig));
        return mockExecutionInfo != null ? mockExecutionInfo.mock : null;
    }

    private static String buildMockId(Class<?> clazz, String methodSig) {
        return clazz.getName() + "_" + methodSig;
    }

    private static String fetchMethodSignature(Class<?> returnType, String methodName, Class<?>[] params) {
        StringBuilder sb = new StringBuilder();

        sb.append(getType(returnType));
        sb.append(" ").append(methodName).append("(");
        int paramCount = 0;
        for(Class<?> param : params) {
            if(paramCount > 0)
                sb.append(", ");
            sb.append(getType(param));
            sb.append(" ").append("_arg_").append(paramCount);
            paramCount += 1;
        }
        sb.append(")");
        return sb.toString();
    }

    private static String getType(Class<?> type) {
        if(type.isArray()) {
            Class<?> componentType = type.getComponentType();
            return getType(componentType) + "[]";
        } else {
            return type.getName();
        }
    }

    private static class MockExecutionInfo {
        Object mock;
        Method method;
    }





}
