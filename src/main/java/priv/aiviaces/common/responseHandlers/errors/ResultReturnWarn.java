package priv.aiviaces.common.responseHandlers.errors;

import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;

@Getter
public class ResultReturnWarn extends RuntimeException {
    private final int code;

    public ResultReturnWarn(int code, String message) {
        super(message);
        this.code = code;
    }

    public ResultReturnWarn(int code, String message, HttpServletResponse response) {
        super(message);
        this.code = code;
        response.setStatus(code);
    }

    public ResultReturnWarn( String message,int code) {
        super(message);
        this.code = code;
    }

    public ResultReturnWarn(String message, int code, HttpServletResponse response) {
        super(message);
        this.code = code;
        response.setStatus(code);
    }

    public ResultReturnWarn(String message) {
        super(message);
        this.code = 400;
    }
}
