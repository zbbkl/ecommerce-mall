package com.example.springboot.exception;

import com.example.springboot.common.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 全局异常处理器
 *
 * 状态码约定（修复：认证/鉴权失败此前一律返回 HTTP 200，网关与监控无法识别）：
 * - code=401 → HTTP 401（未登录 / token 失效）
 * - code=403 → HTTP 403（已登录但无权限）
 * - 其余业务异常 → HTTP 200 + body 里的业务 code（如 201 库存不足）
 */
@ControllerAdvice
public class GlobalException {

    private static final Logger log = LoggerFactory.getLogger(GlobalException.class);

    @ExceptionHandler(ServiceException.class)
    @ResponseBody
    public ResponseEntity<Result> serviceException(ServiceException e) {
        Result result = Result.error(e.getCode(), e.getMessage());
        if ("401".equals(e.getCode())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);
        }
        if ("403".equals(e.getCode())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(result);
        }
        return ResponseEntity.ok(result);
    }

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ResponseEntity<Result> globalException(Exception e) {
        // 上日志框架并保留堆栈（替代 printStackTrace，后续可接 traceId）
        log.error("未处理异常", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Result.error("500", "系统错误"));
    }

}
