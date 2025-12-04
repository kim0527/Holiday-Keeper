package com.holidaykeeper.api.v1.Infrastructure.respoitory.holiday;

import com.holidaykeeper.api.v1.domain.Holiday;
import com.holidaykeeper.api.v1.domain.QCountry;
import com.holidaykeeper.api.v1.domain.QHoliday;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class HolidayQuerydslRepositoryImpl implements HolidayQuerydslRepository {
  private final JPAQueryFactory jpaQueryFactory;
  private final QCountry country = QCountry.country;
  private final QHoliday holiday = QHoliday.holiday;

  public final Page<Holiday> searchHoliday(
      Optional<Integer> year,
      Optional<String> countryCode,
      Optional<String> holidayType,
      Pageable pageable
  ) {
    BooleanBuilder where = new BooleanBuilder();

    year.ifPresent(y -> where.and(holiday.date.year().eq(y)));
    countryCode.ifPresent(code -> where.and(country.code.eq(code)));
    holidayType.ifPresent(type -> where.and(holiday.typesJson.contains("\"" + type + "\"")));

    Long total = jpaQueryFactory
        .select(holiday.count())
        .from(holiday)
        .leftJoin(holiday.country, country)
        .where(where)
        .fetchOne();

    List<Holiday> results = jpaQueryFactory
        .selectFrom(holiday)
        .leftJoin(holiday.country, country).fetchJoin()
        .where(where)
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .orderBy(getOrder(pageable))
        .fetch();

    return new PageImpl<>(results, pageable, total != null ? total : 0L);
  }

  private OrderSpecifier<?> getOrder(Pageable pageable) {
    if (pageable.getSort().isEmpty()) {
      return holiday.date.desc();
    }

    Sort.Order order = pageable.getSort().iterator().next();
    PathBuilder<Holiday> pathBuilder = new PathBuilder<>(
        holiday.getType(),
        holiday.getMetadata()
    );

    return new OrderSpecifier(
        order.isAscending() ? Order.ASC : Order.DESC,
        pathBuilder.get(order.getProperty())
    );
  }
}
