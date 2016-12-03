package ru.spbau.mit.Protocol;

public enum ServerRequestID {
    LIST(1),
    UPLOAD(2),
    SOURCES(3),
    UPDATE(4),
    ERROR(123);

    private int value;

    ServerRequestID(int value) {
        this.value = value;
    }

    public int getValue(){
        return value;
    }

    public static ServerRequestID fromInt(int b) {
        switch (b) {
            case 1:
                return LIST;
            case 2:
                return UPLOAD;
            case 3:
                return SOURCES;
            case 4:
                return UPDATE;
            default:
                return ERROR;
        }
    }
}
