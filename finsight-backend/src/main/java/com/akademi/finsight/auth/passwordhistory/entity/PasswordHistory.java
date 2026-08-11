package com.akademi.finsight.auth.passwordhistory.entity;

import com.akademi.finsight.common.entity.BaseEntity;
import com.akademi.finsight.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/** Kullanicinin gecmis sifre hash'leri; ayni sifrenin kisa sure icinde tekrar kullanilmasini engeller. */
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
@Entity
@Table(name = "password_history")
public class PasswordHistory extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;
}
