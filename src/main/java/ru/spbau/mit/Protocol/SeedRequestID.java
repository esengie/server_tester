package ru.spbau.mit.Protocol;

public enum SeedRequestID {
    STAT(1),
    GET(2),
    ERROR(123);

    private int value;

    SeedRequestID(int value) {
        this.value = value;
    }

    public int getValue(){
        return value;
    }

    public static SeedRequestID fromInt(int b) {
        switch (b) {
            case 1:
                return STAT;
            case 2:
                return GET;
            default:
                return ERROR;
        }
    }
}
