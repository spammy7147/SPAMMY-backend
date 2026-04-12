// ContractItemRepository.java
package spammy.eve.domain.contract;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ContractItemRepository extends JpaRepository<ContractItem, Long> {
    List<ContractItem> findByContractContractId(Long contractId);
}