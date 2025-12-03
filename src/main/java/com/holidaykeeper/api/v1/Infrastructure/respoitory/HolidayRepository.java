package com.holidaykeeper.api.v1.Infrastructure.respoitory;

import com.holidaykeeper.api.v1.domain.Country;
import com.holidaykeeper.api.v1.domain.Holiday;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HolidayRepository extends JpaRepository<Holiday, UUID> {
}
