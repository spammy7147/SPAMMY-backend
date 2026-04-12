package spammy.eve.domain.sde;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

}