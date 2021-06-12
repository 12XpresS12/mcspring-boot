package dev.alangomes.springspigot.util;

import dev.alangomes.springspigot.util.scheduler.SchedulerService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.bukkit.Server;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_SINGLETON;

@Aspect
@Component
@Scope(SCOPE_SINGLETON)
class UtilAspect {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(UtilAspect.class);
    @Autowired
    private SchedulerService schedulerService;

    @Autowired
    private Server server;

    @Order(0)
    @Around("within(@(@dev.alangomes.springspigot.util.Synchronize *) *) " +
            "|| execution(@(@dev.alangomes.springspigot.util.Synchronize *) * *(..)) " +
            "|| @within(dev.alangomes.springspigot.util.Synchronize)" +
            "|| execution(@dev.alangomes.springspigot.util.Synchronize * *(..))")
    public Object synchronizeCall(ProceedingJoinPoint joinPoint) throws Throwable {
        if (server.isPrimaryThread()) {
            return joinPoint.proceed();
        }
        schedulerService.scheduleSyncDelayedTask(() -> {
            try {
                joinPoint.proceed();
            } catch (Throwable throwable) {
                log.error("Error in synchronous task", throwable);
            }
        }, 0);
        return null;
    }

}
