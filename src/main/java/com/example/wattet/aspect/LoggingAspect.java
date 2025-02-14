package com.example.wattet.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {

    private static final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);

    @Before("execution(* com.example.wattet.service.*.*(..))")
    public void logBefore(JoinPoint joinPoint) {
        logger.info("Executing: {}", joinPoint.getSignature().getName());
    }

    @AfterThrowing(pointcut = "execution(* com.example.wattet.service.*.*(..))", throwing = "ex")
    public void logAfterThrowing(JoinPoint joinPoint, Throwable ex) {
        logger.error("Exception in {}: {}", joinPoint.getSignature().getName(), ex.getMessage());
    }
}