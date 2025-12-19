
package com.vivekanand.manager.finance;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FinanceService {
    private final FinancialRecordRepository repo;

    public FinanceService(FinancialRecordRepository r) {
        repo = r;
    }

    public FinancialRecord add(FinancialRecord r) {
        return repo.save(r);
    }

    public List<FinancialRecord> listByEvent(Long eventId) {
        return repo.findByEventId(eventId);
    }

    public BigDecimal calculateBalance(Long eventId) {
        return repo.findByEventId(eventId).stream().map(r -> switch (r.getType()) {
            case BUDGET, INCOME -> r.getAmount();
            case EXPENSE -> r.getAmount().negate();
        }).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Map<FinancialCategory, BigDecimal> totalsByCategory(Long eventId) {
        return repo.findByEventId(eventId).stream().collect(Collectors.groupingBy(FinancialRecord::getCategory, Collectors.mapping(FinancialRecord::getAmount, Collectors.reducing(BigDecimal.ZERO, (a, b) -> a.add(b)))));
    }
}
