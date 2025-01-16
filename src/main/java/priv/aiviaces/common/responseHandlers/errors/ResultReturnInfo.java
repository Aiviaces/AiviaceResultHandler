package priv.aiviaces.common.responseHandlers.errors;

import lombok.Data;
import lombok.EqualsAndHashCode;
import priv.aiviaces.common.responseHandlers.entitys.Result;

import java.util.Objects;

/**
 * 当需要返回一个信息时，可以抛出该异常，直接返回结果；
 * 也可以一层层返回Result对象
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ResultReturnInfo extends RuntimeException {

    private static String thorwMessage = "directly return response.";

    Result<?> result;

    public ResultReturnInfo() {
        super(thorwMessage);
        this.result = Result.info(null, null);
    }

    public ResultReturnInfo(String message) {
        this();
        this.result.setCode(200);
        this.result.setMessage(message);
    }

    public ResultReturnInfo(int code, String message) {
        this();
        this.result.setCode(code);
        this.result.setMessage(message);
    }

    public ResultReturnInfo(Result<?> result) {
        this();
        this.result = Objects.requireNonNullElseGet(result, Result::new);
    }


}
