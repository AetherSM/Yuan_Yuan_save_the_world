package com.example.demo.interceptor;

import com.example.demo.constant.JwtClaimsConstant;
import com.example.demo.context.BaseContext;
import com.example.demo.expection.TokenNullException;
import com.example.demo.properties.JwtProperties;
import com.example.demo.utils.JwtUtil;
import com.example.demo.pojo.entity.UserEntity;
import com.example.demo.service.UserService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * JWT拦截器
 */
@Component
@Slf4j
public class JwtInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtProperties jwtProperties;

    @Autowired
    private UserService userService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        /*获取到当前线程的id*/
        long id = Thread.currentThread().getId();
        log.info("当前线程id为{}", id);

        //HandlerMethod是封装控制器中具体的方法信息,这里就是检查是不是Controller方法
        if (!(handler instanceof HandlerMethod)) {
            //当前拦截到的不是动态方法，直接放行
            return true;
        }

        /*获得请求头中的token信息*/
        String token = request.getHeader(jwtProperties.getAdminTokenName());
        log.info("请求令牌: {}", token);
        /*检查是否携带令牌*/
        if (token == null || token.isEmpty()) {
            log.info("携带令牌为空");
            throw new TokenNullException("携带令牌为空");
        }

        try {
            log.info("开始解析令牌...");
            Claims claims = JwtUtil.parseJWT(jwtProperties.getAdminSecretKey(), token);
            log.info("获得到令牌信息{}", claims);

            Object idObj = claims.get(JwtClaimsConstant.EMP_ID);
            if (idObj == null) {
                idObj = claims.get(JwtClaimsConstant.USER_ID);
            }
            Long userId = idObj == null ? null : Long.valueOf(idObj.toString());
            if (userId == null) {
                throw new JwtException("token缺少用户ID");
            }
            BaseContext.setCurrentId(userId);
            request.setAttribute("userId", userId);
            String username = (String) claims.get(JwtClaimsConstant.USERNAME);
            request.setAttribute("username", username);

            // 检查管理员权限
            if (request.getRequestURI().startsWith("/admin")) {
                UserEntity user = userService.findById(userId);
                if (user == null || user.getUserType() != 0) {
                    response.setStatus(403); // Forbidden
                    return false;
                }
            }

            log.info("令牌校验通过，userId={}", userId);
            return true;
        } catch (Exception e) {
            log.info("令牌解析失败: {}", e.getMessage());
            throw new JwtException("令牌无效或已过期");
        }

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        BaseContext.remove();
    }
}

