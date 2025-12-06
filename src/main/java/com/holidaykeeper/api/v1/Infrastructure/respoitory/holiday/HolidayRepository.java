package com.holidaykeeper.api.v1.Infrastructure.respoitory.holiday;

import com.holidaykeeper.api.v1.domain.Holiday;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface HolidayRepository extends JpaRepository<Holiday, UUID>, HolidayJdbcRepository, HolidayQuerydslRepository{
  @Query("""
    SELECT h
    FROM Holiday h
    JOIN FETCH h.country c
    WHERE c.code = :countryCode
      AND YEAR(h.date) = :year
      AND h.isDeleted = FALSE
  """)
  List<Holiday> findByCountryCodeAndYear(String countryCode, int year);
}
