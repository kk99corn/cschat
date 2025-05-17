package com.kk.cschat.config.aop;

import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class ExecutionLoggerAspect {

    @Around("execution(* com.kk.cschat..*(..)) && !bean(mockOrderMapper)")
    public Object logExecutionInfo(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName();
        String args = Arrays.toString(joinPoint.getArgs());

        log.info("[START][{}] args = [{}]", methodName, args);

        long startTime = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        long endTime = System.currentTimeMillis();

        log.info("[END][{}] executionTime = {}ms", methodName, (endTime - startTime));
        return result;
    }
}
