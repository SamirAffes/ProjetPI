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
public class Organisation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String nom;
    private String adresse;
    private String telephone;
    private String email;
    private String siteWeb;
    private Date dateCreation;
    private String logo;
    private OrgType type;
    private int nombreConducteurs;
    private int tailleFlotte;
    @ElementCollection
    private List<Integer> conducteurs;
    @ElementCollection
    private List<Integer> vehicules;


}

