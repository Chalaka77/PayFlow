package com.b2b.paymentservice.outbox;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface OutboxEventRepository extends JpaRepository<OutboxEventEntity, UUID>
{
    List<OutboxEventEntity> findByStatusOrderByCreatedAtAsc(String status, Pageable pageable);
}
