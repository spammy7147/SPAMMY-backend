package spammy.eve.sde;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BlueprintItemRepository extends JpaRepository<BlueprintItem, Long> {

}