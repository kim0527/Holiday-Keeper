package com.holidaykeeper.api.v1.domain;

import com.holidaykeeper.api.v1.application.util.JsonUtil;
import com.holidaykeeper.api.v1.domain.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.sql.Types;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
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
public class Holiday extends BaseEntity {

  @Id
  @UuidGenerator
  @JdbcTypeCode(Types.VARCHAR)
  @Column(name = "holiday_id", length = 36, nullable = false, unique = true)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "country_id", nullable = false)
  private Country country;

  @Column(name = "date", nullable = false)
  private LocalDate date;

  @Column(name = "local_name", nullable = false)
  private String localName;

  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "fixed", nullable = false)
  private Boolean fixed;

  @Column(name = "global", nullable = false)
  private Boolean global;

  @Column(name = "counties_json", columnDefinition = "TEXT")
  private String countiesJson;

  @Column(name = "launch_year")
  private Integer launchYear;

  @Column(name = "types_json", columnDefinition = "TEXT")
  private String typesJson;

  @Builder
  private Holiday(
      Country country,
      LocalDate date,
      String localName,
      String name,
      Boolean fixed,
      Boolean global,
      String countiesJson,
      Integer launchYear,
      String typesJson
  ) {
    this.country = country;
    this.date = date;
    this.localName = localName;
    this.name = name;
    this.fixed = fixed;
    this.global = global;
    this.countiesJson = countiesJson;
    this.launchYear = launchYear;
    this.typesJson = typesJson;
  }

  public boolean hasChanges(
      LocalDate date,
      String localName,
      String name,
      Boolean fixed,
      Boolean global,
      List<String> counties,
      Integer launchYear,
      List<String> types
  ) {
    return !Objects.equals(this.date, date)
        || !Objects.equals(this.localName, localName)
        || !Objects.equals(this.name, name)
        || !Objects.equals(this.fixed, fixed)
        || !Objects.equals(this.global, global)
        || !Objects.equals(this.countiesJson, JsonUtil.toJson(counties))
        || !Objects.equals(this.launchYear, launchYear)
        || !Objects.equals(this.typesJson, JsonUtil.toJson(types));
  }

}
