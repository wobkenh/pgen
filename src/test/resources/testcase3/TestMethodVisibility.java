package testcase3;

import java.time.LocalDate;

public class TestMethodVisibility {

    private int testPrivate() {
        return 42;
    }

    protected String testProtected(String test) {
        return test + "42";
    }

    LocalDate testPackage(String test, LocalDate test2) {
        return LocalDate.of(2042, 1, 1);
    }

    public Boolean testPublic(String test, LocalDate test2, boolean test3) {
        return true;
    }
}
