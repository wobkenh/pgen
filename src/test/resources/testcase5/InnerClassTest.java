package testcase5;

public class InnerClassTest {
    public static class ExceptionBase extends Exception {
        public ExceptionBase(String message) {
            super(message);
        }
    }

    public static class ExceptionExtend extends ExceptionBase {

        private String code;

        public ExceptionExtend(String message, String code) {
            super(message);
            this.code = code;
        }

        public String getCode() {
            return code;
        }
    }
}
