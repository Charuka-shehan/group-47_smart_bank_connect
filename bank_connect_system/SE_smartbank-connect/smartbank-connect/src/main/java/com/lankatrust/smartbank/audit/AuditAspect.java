package com.lankatrust.smartbank.audit;

import com.lankatrust.smartbank.service.AuditLogService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class AuditAspect {

    private static final Logger log = LoggerFactory.getLogger(AuditAspect.class);
    private final AuditLogService auditLogService;

    public AuditAspect(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    private String getActorEmail() {
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            return SecurityContextHolder.getContext().getAuthentication().getName();
        }
        return "SYSTEM";
    }

    // Account Service Interceptions
    @Pointcut("execution(* com.lankatrust.smartbank.service.AccountService.submitNewAccount(..))")
    public void accountSubmit() {}

    @Pointcut("execution(* com.lankatrust.smartbank.service.AccountService.approveAccount(..))")
    public void accountApprove() {}

    @Pointcut("execution(* com.lankatrust.smartbank.service.AccountService.freezeAccount(..))")
    public void accountFreeze() {}

    @Pointcut("execution(* com.lankatrust.smartbank.service.AccountService.unfreezeAccount(..))")
    public void accountUnfreeze() {}

    @Pointcut("execution(* com.lankatrust.smartbank.service.AccountService.approveClosure(..))")
    public void accountClose() {}

    // Loan Service Interceptions
    @Pointcut("execution(* com.lankatrust.smartbank.service.LoanService.approve(..))")
    public void loanApprove() {}

    @AfterReturning("accountSubmit()")
    public void logAccountSubmit(JoinPoint jp) {
        Object[] args = jp.getArgs();
        String customerIdStr = args[0] != null ? args[0].toString() : "N/A";
        String type = args[1] != null ? args[1].toString() : "N/A";
        auditLogService.log(getActorEmail(), "ACCOUNT_SUBMITTED", "Account", null,
                "Requested " + type + " account for customer ID: " + customerIdStr);
    }

    @AfterReturning("accountApprove()")
    public void logAccountApprove(JoinPoint jp) {
        Object[] args = jp.getArgs();
        String accountIdStr = args[0] != null ? args[0].toString() : "N/A";
        auditLogService.log(getActorEmail(), "ACCOUNT_APPROVED", "Account", null,
                "Approved account ID: " + accountIdStr);
    }

    @AfterReturning("accountFreeze()")
    public void logAccountFreeze(JoinPoint jp) {
        Object[] args = jp.getArgs();
        String accountIdStr = args[0] != null ? args[0].toString() : "N/A";
        auditLogService.log(getActorEmail(), "ACCOUNT_FROZEN", "Account", null,
                "Froze account ID: " + accountIdStr);
    }

    @AfterReturning("accountUnfreeze()")
    public void logAccountUnfreeze(JoinPoint jp) {
        Object[] args = jp.getArgs();
        String accountIdStr = args[0] != null ? args[0].toString() : "N/A";
        auditLogService.log(getActorEmail(), "ACCOUNT_UNFROZEN", "Account", null,
                "Unfroze account ID: " + accountIdStr);
    }

    @AfterReturning("accountClose()")
    public void logAccountClose(JoinPoint jp) {
        Object[] args = jp.getArgs();
        String accountIdStr = args[0] != null ? args[0].toString() : "N/A";
        auditLogService.log(getActorEmail(), "ACCOUNT_CLOSED", "Account", null,
                "Approved closure for account ID: " + accountIdStr);
    }

    @AfterReturning("loanApprove()")
    public void logLoanApprove(JoinPoint jp) {
        Object[] args = jp.getArgs();
        String loanIdStr = args[0] != null ? args[0].toString() : "N/A";
        auditLogService.log(getActorEmail(), "LOAN_APPROVED", "Loan", null,
                "Approved loan application ID: " + loanIdStr);
    }
}
