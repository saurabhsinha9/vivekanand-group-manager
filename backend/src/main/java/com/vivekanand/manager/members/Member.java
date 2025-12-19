
package com.vivekanand.manager.members;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Entity
@Table(name = "members")
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotBlank
    private String fullName;
    private String phone;
    private String address;
    @Column(unique = true)
    private String email;
    private boolean active = true;
    private Long userId;
    private Boolean notifyEmail = true;
    private Boolean notifyWhatsapp = true;

}
