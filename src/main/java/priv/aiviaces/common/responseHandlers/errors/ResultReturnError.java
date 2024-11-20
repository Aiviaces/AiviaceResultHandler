package priv.aiviaces.common.responseHandlers.errors;

import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;

@Getter
public class ResultReturnError extends RuntimeException {
    private final int code;

    public ResultReturnError(int code, String message) {
        super(message);
        this.code = code;
    }

    public ResultReturnError(int code, String message, HttpServletResponse response) {
        super(message);
        this.code=code;
        response.setStatus(code);
    }

    public ResultReturnError(String message, int code) {
        super(message);
        this.code = code;
    }

    public ResultReturnError(String message, int code, HttpServletResponse response) {
        super(message);
        this.code=code;
        response.setStatus(code);
    }

    public ResultReturnError(String message) {
        super(message);
        this.code = 500;
    }

}

