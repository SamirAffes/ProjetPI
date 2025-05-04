package services;

import java.util.List;

public interface CRUD<T> {
    void ajouter(T t);
    void supprimer(T t);
    void modifier(T t);
    T afficher(int id);
    List<T> afficher_tout();
}
