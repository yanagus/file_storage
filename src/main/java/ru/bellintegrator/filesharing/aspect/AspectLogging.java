package ru.bellintegrator.filesharing.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.bellintegrator.filesharing.exception.NotFoundException;

/**
 * Класс для логирования
 */
@Aspect
@Service
public class AspectLogging {

    private final Logger log = LoggerFactory.getLogger(AspectLogging.class);

    /**
     * Набор точек соединения - методы класса FileServiceImpl
     */
    @Pointcut("execution(* ru.bellintegrator.filesharing.service.FileServiceImpl.*(..))")
    public void selectAllMethodsAvaliable() {

    }

    /**
     * Логирует процесс выполнения метода
     *
     * @param joinPoint интерфейс для доступа к точке соединения
     * @return объект, возвращаемый методом - точкой соединения
     * @throws Throwable
     */
    @Around("selectAllMethodsAvaliable()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        log.info("Started invocation method: {}", joinPoint.getSignature().toShortString());

        Object returnValue = joinPoint.proceed();

        if (returnValue != null) {
            log.debug("The method: {} returns: {}", joinPoint.getSignature().toShortString(), returnValue);
        }

        log.info("Ended invocation method: {}", joinPoint.getSignature().toShortString());

        return returnValue;
    }

    /**
     * Логирует выброшенные исключения NotFoundException
     *
     * @param e исключение
     */
    @AfterThrowing(pointcut = "selectAllMethodsAvaliable()", throwing = "e")
    public void inCaseOfExceptionThrowAdvice(NotFoundException e) {
        log.debug("Throwing exception: {}", e.toString());
    }
}
