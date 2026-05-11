package spammy.eve.global.aop;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EsiMetadataRepository extends JpaRepository<EsiMetadata, String> {
}
