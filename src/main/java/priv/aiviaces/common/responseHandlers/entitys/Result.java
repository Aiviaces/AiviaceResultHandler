package priv.aiviaces.common.responseHandlers.entitys;

import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Result<T> {
    private int code;  // 状态码
    private String message;  // 消息
    private T data;  // 数据
    private String type;  // 新增字段，用于标识响应类型

    // 构造方法
    /**
     * 成功响应
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(200, "Success", data, "SUCCESS");
    }

    public static <T> Result<T> success(String message, T data) {
        return new Result<>(200, message, data, "SUCCESS");
    }

    /**
     * 信息响应
     */
    public static <T> Result<T> info(String message, T data) {
        return new Result<>(200, message, data, "INFO");
    }

    /**
     * 警告响应
     */
    public static <T> Result<T> warn(int code, String message, T data) {
        return new Result<>(code, message, data, "WARN");
    }

    /**
     * 错误响应
     */
    public static <T> Result<T> error(int code, String message) {
        return new Result<>(code, message, null, "ERROR");
    }

}
