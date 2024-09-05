package priv.aiviaces.common.responseHandlers.errors;

import lombok.Getter;

@Getter
public class ResultReturnError extends RuntimeException {
    private final int code;

    public ResultReturnError(int code, String message) {
        super(message);
        this.code = code;
    }

    public ResultReturnError(String message, int code) {
        super(message);
        this.code = 500;
    }

}

