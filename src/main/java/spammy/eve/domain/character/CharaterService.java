package spammy.eve.domain.character;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import spammy.eve.client.EsiClient;

import java.time.LocalDate;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CharaterService {

    private final EsiClient esiClient;
    private final CharacterRepository characterRepository;

    public void updateOmega() {

    }

    public void getCharacterPublicInfo() {
        esiClient.get()
    }

    public void extendOmega(Map<String, String> body, Character character) {
        String dateStr = body.get("omegaExpiresAt");
        if (dateStr == null) {
            character.updateOmegaExpiresAt(null);
        } else {
            character.updateOmegaExpiresAt(LocalDate.parse(dateStr));
        }
        characterRepository.save(character);
    }
}
