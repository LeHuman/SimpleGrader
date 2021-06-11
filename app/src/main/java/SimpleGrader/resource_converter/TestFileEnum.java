package SimpleGrader.resource_converter;

public enum TestFileEnum {
    QUESTIONS(0), ANSWERS(1);

    int index;
    boolean checked = false;

    TestFileEnum(int index) {
        this.index = index;
    }

    public int getIndex() {
        checked = true;
        return this.index;
    }

    private void resetCheck() {
        this.checked = false;
    }

    public boolean hasChecked() {
        return this.checked;
    }

    public static boolean isChecked() throws UncheckedValuesException {
        boolean check = true;
        for (TestFileEnum c : TestFileEnum.values()) {
            check = check && c.hasChecked();
        }
        if (!check)
            throw new UncheckedValuesException();
        return check;
    }

    public static void reset() {
        for (TestFileEnum c : TestFileEnum.values()) {
            c.resetCheck();
        }
    }

    public static boolean contains(String test) {
        for (TestFileEnum c : TestFileEnum.values()) {
            if (c.name().equals(test)) {
                return true;
            }
        }
        return false;
    }
}