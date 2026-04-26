package spammy.eve.character.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import spammy.eve.character.repository.CharacterRepository;
import spammy.eve.client.EsiClient;
import spammy.eve.auth.EsiAuthService;

@Slf4j
@Service
@RequiredArgsConstructor
public class EsiService {

    private final EsiClient esiClient;
    private final EsiAuthService esiAuthService;
    private final EsiSyncService esiSyncService;
    private final CharacterRepository characterRepository;


}
