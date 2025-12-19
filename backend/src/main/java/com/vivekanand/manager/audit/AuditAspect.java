
package com.vivekanand.manager.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.JoinPoint;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class AuditAspect {
    private final AuditLogRepository repo;
    private final ObjectMapper mapper = new ObjectMapper();

    public AuditAspect(AuditLogRepository r) {
        this.repo = r;
    }

    @AfterReturning(value = "@annotation(auditable)", returning = "retVal")
    public void after(JoinPoint jp, Auditable auditable, Object retVal) {
        String username = (String) (SecurityContextHolder.getContext().getAuthentication() != null ? SecurityContextHolder.getContext().getAuthentication().getPrincipal() : "anonymous");
        AuditLog log = new AuditLog();
        log.setUsername(username);
        log.setAction(auditable.action());
        log.setEntity(auditable.entity());
        try {
            log.setDetails(mapper.writeValueAsString(retVal));
        } catch (Exception e) {
            log.setDetails("n/a");
        }
        repo.save(log);
    }
}
