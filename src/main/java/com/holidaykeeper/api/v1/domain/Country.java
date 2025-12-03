package com.holidaykeeper.api.v1.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.sql.Types;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Country extends BaseEntity {

  @Id
  @UuidGenerator
  @JdbcTypeCode(Types.VARCHAR)
  @Column(name = "country_id", length = 36, nullable = false, unique = true)
  private UUID id;

  @Column(name = "country_code", length = 2, nullable = false)
  private String code;

  @Column(name = "country_name", length = 100, nullable = false)
  private String name;

  @Builder
  private Country(String code, String name) {
    this.code = code;
    this.name = name;
  }

  public static Country of(String code, String name) {
    return Country.builder()
        .code(code)
        .name(name)
        .build();
  }

}
