package testcase4;

public enum TestEnum {
    A("TestA"), B("TestB");

    private final String test;

    TestEnum(String test) {
        this.test = test;
    }

    public String getTest() {
        return test;
    }
}
