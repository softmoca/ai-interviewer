package com.aiinterviewer.domain.user;

import com.aiinterviewer.common.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 서비스 사용자. MVP는 자체 로그인(Spring Security + JWT), 2차에 소셜 로그인 확장
 * (결정사항 D9). {@code provider}/{@code providerId}는 소셜 로그인용 예약 필드.
 */
@Getter
@Entity
@Table(name = "users") // 'user'는 일부 DB의 예약어라 회피
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    /** BCrypt 해시. 소셜 로그인 사용자는 null 가능. */
    @Column
    private String passwordHash;

    @Column(nullable = false)
    private String nickname;

    /** (2차) 소셜 로그인 프로바이더 (예: google, kakao) */
    private String provider;

    /** (2차) 프로바이더 내 사용자 식별자 */
    private String providerId;
}
