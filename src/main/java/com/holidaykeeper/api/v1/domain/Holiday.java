package com.holidaykeeper.api.v1.domain;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
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

  @Column(name = "local_name", nullable = false, length = 100)
  private String localName;

  @Column(name = "name", nullable = false, length = 100)
  private String name;

  @Column(name = "fixed", nullable = false)
  private Boolean fixed;

  @Column(name = "global", nullable = false)
  private Boolean global;

  @ElementCollection
  @CollectionTable(
      name = "counties",
      joinColumns = @JoinColumn(name = "holiday_id")
  )
  @Column(name = "counties")
  private List<String> counties = new ArrayList<>();

  @Column(name = "launch_year")
  private Integer launchYear;

  @ElementCollection
  @CollectionTable(
      name = "types",
      joinColumns = @JoinColumn(name = "holiday_id")
  )
  @Column(name = "types")
  private List<HolidayType> types = new ArrayList<>();

  @Builder
  private Holiday(
      Country country,
      LocalDate date,
      String localName,
      String name,
      Boolean fixed,
      Boolean global,
      List<String> counties,
      Integer launchYear,
      List<HolidayType> types
  ) {
    this.country = country;
    this.date = date;
    this.localName = localName;
    this.name = name;
    this.fixed = fixed;
    this.global = global;
    this.counties = counties;
    this.launchYear = launchYear;
    this.types = types;
  }

}
