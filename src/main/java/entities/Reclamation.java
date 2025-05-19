package entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class Reclamation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User user;

    private String title;
    private String description;
    private LocalDateTime creationDate;
    private LocalDateTime responseDate;
    private String response;

    @Enumerated(EnumType.STRING)
    private ReclamationType type;

    @Enumerated(EnumType.STRING)
    private ReclamationStatus status;

    public enum ReclamationStatus {
        PENDING,
        IN_PROGRESS,
        RESOLVED,
        REJECTED
    }

    public enum ReclamationType {
        TECHNICAL,
        BILLING,
        SERVICE,
        OTHER
    }
}
