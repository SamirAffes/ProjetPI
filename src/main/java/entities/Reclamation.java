package entities;

import java.util.*;

import entities.ENUMS.EtatReclamation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Data
@ToString
public class Reclamation {
private int id;
private String type;
private String description;
private Date date;
private EtatReclamation etat;
private int user_id;
private int chauffeur_id;
private int organisme_id;

}