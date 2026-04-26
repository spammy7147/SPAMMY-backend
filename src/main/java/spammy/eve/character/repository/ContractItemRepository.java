// ContractItemRepository.java
package spammy.eve.character.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import spammy.eve.character.domain.ContractItem;

import java.util.List;

public interface ContractItemRepository extends JpaRepository<ContractItem, Long> {
    List<ContractItem> findByContractContractId(Long contractId);
}