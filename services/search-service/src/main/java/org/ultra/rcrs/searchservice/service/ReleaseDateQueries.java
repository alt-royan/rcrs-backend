package org.ultra.rcrs.searchservice.service;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.json.JsonData;

import java.time.LocalDate;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Albums are indexed with a real {@code releaseDate} date field rather than a free-text
 * year, so a query like "2019" can no longer be matched by the text analyzer. Instead it is
 * translated into a range covering that whole calendar year.
 */
final class ReleaseDateQueries {

    private static final Pattern YEAR = Pattern.compile("\\b(1[89]\\d{2}|20\\d{2}|21\\d{2})\\b");

    private ReleaseDateQueries() {
    }

    /**
     * Builds a range query over {@code releaseDate} spanning the calendar year mentioned in
     * the search text, or empty when the text contains no plausible year.
     */
    static Optional<Query> forYearIn(String queryText) {
        if (queryText == null) {
            return Optional.empty();
        }
        var matcher = YEAR.matcher(queryText);
        if (!matcher.find()) {
            return Optional.empty();
        }
        int year = Integer.parseInt(matcher.group(1));
        LocalDate from = LocalDate.of(year, 1, 1);
        LocalDate to = from.withDayOfYear(from.lengthOfYear());

        return Optional.of(Query.of(q -> q.range(r -> r.date(d -> d
                .field("releaseDate")
                .gte(from.toString())
                .lte(to.toString())
                .format("yyyy-MM-dd")))));
    }
}
