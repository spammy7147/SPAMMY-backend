package spammy.eve.domain.character;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import spammy.eve.client.EsiClient;
import spammy.eve.domain.auth.EsiAuthService;
import tools.jackson.databind.JsonNode;

import java.util.List;

import static spammy.eve.global.utils.JsonlUtils.getDouble;

@Slf4j
@Service
@RequiredArgsConstructor
public class EsiService {

    private final EsiClient esiClient;
    private final EsiAuthService esiAuthService;
    private final EsiSyncService esiSyncService;
    private final CharacterRepository characterRepository;


}
