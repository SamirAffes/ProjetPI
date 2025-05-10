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
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    
    @Column(unique = true, nullable = false)
    private String username;
    
    @Column(nullable = false)
    private String password;
    
    private String fullName;
    
    @Column(unique = true)
    private String email;
    
    private String phoneNumber;
    
    private String address;
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date registrationDate;
    
    private String profilePicture;
    
    // User role can be used for permissions
    private UserRole role;
    
    // This field is required by the database schema
    @Column(nullable = true)
    private Integer organisationId;
}
