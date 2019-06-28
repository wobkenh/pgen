package testcase1;

public class TestClass extends TestAbstract implements TestInterface {
    @Override
    public void testAbstract(String test) {
        System.out.println("test");
    }

    @Override
    public int test() {
        return 0;
    }
}
