package services;

import java.sql.SQLException;
import java.util.List;

public interface CRUD<T> {
    public void ajouter(T t) throws SQLException;
    public void supprimer(T t) throws SQLException;
    public void modifier(T t) throws SQLException;
    public T afficher(int id) throws SQLException;
    public List<T> afficher_tout() throws SQLException;
}
