package entities;


import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class Vehicule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vehiculeId")
    private int id;

    private int organisationId;
    private int conducteurId;       
    private String marque;
    private String modele;
    private String immatriculation;
    private int capacite;
    private VehiculeType type;
    private Date dateAjout;
    private Date dateFabrication;

    private VehiculeStatut statut;
    private String photo;


}
