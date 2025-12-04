package com.holidaykeeper.api.v1.Infrastructure.respoitory.country;

import com.holidaykeeper.api.v1.domain.Country;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CountryRepository extends JpaRepository<Country, UUID>, CountryJdbcRepository{
}
