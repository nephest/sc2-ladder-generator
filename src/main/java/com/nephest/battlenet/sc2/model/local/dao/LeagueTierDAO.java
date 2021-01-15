// Copyright (C) 2020 Oleksandr Masniuk and contributors
// SPDX-License-Identifier: AGPL-3.0-or-later

package com.nephest.battlenet.sc2.model.local.dao;

import com.nephest.battlenet.sc2.model.BaseLeagueTier;
import com.nephest.battlenet.sc2.model.local.LeagueTier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.convert.ConversionService;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class LeagueTierDAO
{

    public static final String STD_SELECT =
        "league_tier.id AS \"league_tier.id\","
        + "league_tier.league_id AS \"league_tier.league_id\","
        + "league_tier.type AS \"league_tier.type\","
        + "league_tier.min_rating AS \"league_tier.min_rating\","
        + "league_tier.max_rating AS \"league_tier.max_rating\" ";

    private static final String CREATE_QUERY = "INSERT INTO league_tier "
        + "(league_id, type, min_rating, max_rating) "
        + "VALUES (:leagueId, :type, :minRating, :maxRating)";

    private static final String MERGE_QUERY = CREATE_QUERY
        + " "
        + "ON CONFLICT(league_id, type) DO UPDATE SET "
        + "min_rating=excluded.min_rating, "
        + "max_rating=excluded.max_rating";

    private final NamedParameterJdbcTemplate template;
    private final ConversionService conversionService;

    private static RowMapper<LeagueTier> STD_ROW_MAPPER;
    private static ResultSetExtractor<LeagueTier> STD_EXTRACTOR;

    @Autowired
    public LeagueTierDAO
    (
        @Qualifier("sc2StatsNamedTemplate") NamedParameterJdbcTemplate template,
        @Qualifier("sc2StatsConversionService") ConversionService conversionService
    )
    {
        this.template = template;
        this.conversionService = conversionService;
        initMappers(conversionService);
    }

    private static void initMappers(ConversionService conversionService)
    {
        if(STD_ROW_MAPPER == null) STD_ROW_MAPPER = (rs, i)-> new LeagueTier
        (
            rs.getLong("league_tier.id"),
            rs.getLong("league_tier.league_id"),
            conversionService.convert(rs.getInt("league_tier.type"), BaseLeagueTier.LeagueTierType.class),
            rs.getInt("league_tier.min_rating"),
            rs.getInt("league_tier.max_rating")
        );
        if(STD_EXTRACTOR == null) STD_EXTRACTOR = (rs)->
        {
            if(!rs.next()) return null;
            return getStdRowMapper().mapRow(rs, 0);
        };
    }

    public static RowMapper<LeagueTier> getStdRowMapper()
    {
        return STD_ROW_MAPPER;
    }

    public static ResultSetExtractor<LeagueTier> getStdExtractor()
    {
        return STD_EXTRACTOR;
    }

    public LeagueTier create(LeagueTier tier)
    {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        MapSqlParameterSource params = createParameterSource(tier);
        template.update(CREATE_QUERY, params, keyHolder, new String[]{"id"});
        tier.setId(keyHolder.getKey().longValue());
        return tier;
    }

    public LeagueTier merge(LeagueTier tier)
    {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        MapSqlParameterSource params = createParameterSource(tier);
        template.update(MERGE_QUERY, params, keyHolder, new String[]{"id"});
        tier.setId(keyHolder.getKey().longValue());
        return tier;
    }

    private MapSqlParameterSource createParameterSource(LeagueTier tier)
    {
        return new MapSqlParameterSource()
            .addValue("leagueId", tier.getLeagueId())
            .addValue("type", conversionService.convert(tier.getType(), Integer.class))
            .addValue("minRating", tier.getMinRating())
            .addValue("maxRating", tier.getMaxRating());
    }

}
