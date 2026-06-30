package com.deoksan.share.audit;

import com.deoksan.share.security.AlertService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
public class AuditService {

    private final AuditEntryRepository repository;
    private final Optional<AlertService> alertService;

    @Autowired
    public AuditService(AuditEntryRepository repository,
                        @Autowired(required = false) AlertService alertService) {
        this.repository = repository;
        this.alertService = Optional.ofNullable(alertService);
    }

    // 원본 트랜잭션과 분리 — 본 로직 실패해도 감사 로그는 저장
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(AuditCommand command) {
        try {
            repository.save(AuditEntry.builder()
                    .userId(command.userId())
                    .action(command.action())
                    .result(command.result())
                    .ip(command.ip())
                    .detail(command.detail())
                    .build());
        } catch (Exception e) {
            log.error("[Audit] DB 저장 실패 action={} userId={}: {}",
                    command.action(), command.userId(), e.getMessage());
            alertService.ifPresent(alert ->
                    alert.alertAuditFailure(command.action(), command.userId(), e.getMessage()));
        }
    }
}
