package entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class Conducteur {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private int organisationId;
    private int vehiculeId;
    private String nom;
    private String prenom;
    private String cin;
    private String adresse;
    private String telephone;
    private String email;
    private Date dateNaissance;
    private Date dateEmbauche;
    private String numeroPermis;
    @ElementCollection
    private List<String> typePermis;
    private String photo;
    private String statut;
}
