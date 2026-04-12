package spammy.eve.domain.dashboard;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import spammy.eve.domain.character.Character;
import spammy.eve.domain.user.User;
import spammy.eve.domain.character.CharacterRepository;
import spammy.eve.domain.user.UserRepository;
import spammy.eve.domain.character.EsiService;
import spammy.eve.global.auth.JwtService;
import tools.jackson.databind.JsonNode;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DashboardController {

    private final EsiService esiService;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final CharacterRepository characterRepository;

    private Long getUserId(HttpServletRequest request) {
        if (request.getCookies() == null) throw new RuntimeException("로그인 필요");
        return Arrays.stream(request.getCookies())
                .filter(c -> c.getName().equals("auth_token"))
                .findFirst()
                .map(Cookie::getValue)
                .map(jwtService::getUserId)
                .orElseThrow(() -> new RuntimeException("로그인 필요"));
    }

    private Character getMainCharacter(HttpServletRequest request) {
        Long userId = getUserId(request);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User 없음"));
        return characterRepository.findByUserAndMainTrue(user)
                .orElseThrow(() -> new RuntimeException("메인 캐릭터 없음"));
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMe(HttpServletRequest request) {
        if (request.getCookies() == null) return ResponseEntity.status(401).build();
        try {
            Long userId = getUserId(request);
            User user = userRepository.findById(userId).orElseThrow();
            Character main = characterRepository.findByUserAndMainTrue(user).orElse(null);

            return ResponseEntity.ok(Map.of(
                    "userId", userId,
                    "characterId", main != null ? main.getCharacterId() : null,
                    "characterName", main != null ? main.getCharacterName() : null,
                    "portraitUrl", main != null ? main.getPortraitUrl() : null
            ));
        } catch (Exception e) {
            return ResponseEntity.status(401).build();
        }
    }

    @GetMapping("/summary")
    public ResponseEntity<?> getSummary(HttpServletRequest request) {
        Long userId = getUserId(request);
        User user = userRepository.findById(userId).orElseThrow();
        List<Character> chars = characterRepository.findByUser(user);

        double totalBalance = chars.stream()
                .mapToDouble(c -> c.getBalance() != null ? c.getBalance() : 0.0)
                .sum();

        List<Map<String, Object>> charDtos = chars.stream().map(c -> {
            Map<String, Object> m = new java.util.LinkedHashMap<>();
            m.put("characterId", c.getCharacterId());
            m.put("characterName", c.getCharacterName());
            m.put("portraitUrl", c.getPortraitUrl());
            m.put("corporationId", c.getCorporationId());
            m.put("corporationName", c.getCorporationName());
            m.put("allianceId", c.getAllianceId());
            m.put("allianceName", c.getAllianceName());
            m.put("balance", c.getBalance());
            m.put("omegaExpiresAt", c.getOmegaExpiresAt());
            m.put("lastSyncedAt", c.getLastSyncedAt());
            m.put("isMain", c.isMain());
            return m;
        }).toList();

        return ResponseEntity.ok(Map.of(
                "totalBalance", totalBalance,
                "characters", charDtos
        ));
    }

    @GetMapping("/wallet/balance")
    public ResponseEntity<?> getWalletBalance(HttpServletRequest request) {
        Long characterId = getMainCharacter(request).getCharacterId();
        double balance = esiService.getWalletBalance(characterId);
        return ResponseEntity.ok(Map.of("balance", balance));
    }

    @GetMapping("/wallet/transactions")
    public ResponseEntity<?> getWalletTransactions(HttpServletRequest request) {
        Long characterId = getMainCharacter(request).getCharacterId();
        JsonNode transactions = esiService.getWalletTransactions(characterId);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/assets")
    public ResponseEntity<?> getAssets(HttpServletRequest request) {
        Long characterId = getMainCharacter(request).getCharacterId();
        JsonNode assets = esiService.getAssets(characterId);
        return ResponseEntity.ok(assets);
    }

    @GetMapping("/wallet/journal")
    public ResponseEntity<?> getWalletJournal(HttpServletRequest request) {
        Long characterId = getMainCharacter(request).getCharacterId();
        JsonNode journal = esiService.getWalletJournal(characterId);
        return ResponseEntity.ok(journal);
    }
}
