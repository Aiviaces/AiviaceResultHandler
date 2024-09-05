package priv.aiviaces.common.responseHandlers.errors;

import lombok.Getter;

@Getter
public class ResultReturnWarn extends RuntimeException {
    private final int code;

    public ResultReturnWarn(int code, String message) {
        super(message);
        this.code = code;
    }

    public ResultReturnWarn(String message) {
        super(message);
        this.code = 400;
    }
}
