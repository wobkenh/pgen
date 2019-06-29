package testcase2;

import java.time.LocalDate;

public class TestFieldVisibility {
    private int testPrivate = 42;
    protected String testProtected = "42";
    LocalDate testPackage = LocalDate.of(2042, 1, 1);
    public Boolean testPublic = true;
}
