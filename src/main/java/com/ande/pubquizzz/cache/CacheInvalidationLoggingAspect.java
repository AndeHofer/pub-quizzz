package com.ande.pubquizzz.cache;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class CacheInvalidationLoggingAspect {

    @AfterReturning("@annotation(com.ande.pubquizzz.cache.InvalidateAllAppCaches)")
    public void logAutomaticInvalidation(JoinPoint joinPoint) {
        log.info("Cache invalidated: all caches (trigger={})", buildTrigger(joinPoint));
    }

    String buildTrigger(JoinPoint joinPoint) {
        if (joinPoint.getSignature() instanceof MethodSignature signature) {
            return signature.getDeclaringType().getSimpleName() + "." + signature.getMethod().getName();
        }
        return joinPoint.getSignature().toShortString();
    }
}
