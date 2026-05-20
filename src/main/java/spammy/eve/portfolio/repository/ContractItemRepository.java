// ContractItemRepository.java
package spammy.eve.portfolio.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import spammy.eve.portfolio.domain.ContractItem;

import java.util.List;

public interface ContractItemRepository extends JpaRepository<ContractItem, Long> {
    List<ContractItem> findByContractContractId(Long contractId);
}