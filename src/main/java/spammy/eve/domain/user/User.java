package spammy.eve.domain.user;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;
import spammy.eve.domain.character.Character;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    @BatchSize(size = 20)
    @Builder.Default
    private List<Character> characters = new ArrayList<>();
}
