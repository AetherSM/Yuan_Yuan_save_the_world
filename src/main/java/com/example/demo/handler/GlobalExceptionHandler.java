package com.example.demo.handler;

import com.example.demo.expection.NoPermissionException;
import com.example.demo.expection.NotExistException;
import com.example.demo.expection.TokenNullException;
import com.example.demo.pojo.result.Result;
import io.jsonwebtoken.JwtException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler({IllegalArgumentException.class})
    public Result<Void> handleIllegalArgument(IllegalArgumentException e) {
        return Result.error(e.getMessage());
    }

    @ExceptionHandler({NotExistException.class})
    public Result<Void> handleNotExist(NotExistException e) {
        return Result.error(e.getMessage());
    }

    @ExceptionHandler({NoPermissionException.class})
    public ResponseEntity<Result<Void>> handleNoPermission(NoPermissionException e) {
        return new ResponseEntity<>(Result.error(e.getMessage()), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler({TokenNullException.class, JwtException.class})
    public ResponseEntity<Result<Void>> handleAuth(Exception e) {
        return new ResponseEntity<>(Result.error(e.getMessage()), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handleOther(Exception e) {
        return new ResponseEntity<>(Result.error("系统错误：" + e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
