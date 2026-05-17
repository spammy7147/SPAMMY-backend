package spammy.eve.sde;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public interface BlueprintItemRepository extends JpaRepository<BlueprintItem, Long> {
    @Modifying
    @Query("DELETE FROM BlueprintItem bi WHERE bi.blueprint.blueprintTypeId IN :blueprintTypeId")
    void deleteByBlueprintTypeIdIn(Collection<Long> blueprintTypeId);
}