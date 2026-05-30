package com.goldbyte.citieye_server.userauth.demomodel;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "demo_auth", schema = "auth")
public class DemoAuthEntity {
    @Id
    @Column(name = "nin", nullable = false)
    private Integer id;

    @Column(name = "fullname", nullable = false)
    private String fullname;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "phone_number", nullable = false)
    private Integer phoneNumber;

    @Column(name = "username", nullable = false)
    private String username;

    @Column(name = "password", nullable = false, length = 30)
    private String password;

}