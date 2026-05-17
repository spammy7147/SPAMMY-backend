package spammy.eve.sde;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public interface BlueprintRepository extends JpaRepository<Blueprint, Long> {
    void deleteByBlueprintTypeIdIn(Collection<Long> blueprintTypeIds);
}