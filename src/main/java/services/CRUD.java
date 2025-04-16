package services;

import java.util.List;

public interface CRUD<T> {
    public void ajouter(T t);
    public void supprimer(T t);
    public void modifier(T t);
    public T afficher(int id);
    public List<T> afficher_tout();
}
