/*-
 * =========================LICENSE_START=========================
 * SC2 Ladder Generator
 * %%
 * Copyright (C) 2020 Oleksandr Masniuk
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * =========================LICENSE_END=========================
 */
package com.nephest.battlenet.sc2.model.local.dao;

import com.nephest.battlenet.sc2.model.local.Team;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.convert.ConversionService;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class TeamDAO
{
    private static final String CREATE_QUERY = "INSERT INTO team "
        + "("
            + "division_id, battlenet_id, "
            + "season, region, league_type, queue_type, team_type, tier_type, "
            + "rating, points, wins, losses, ties"
        + ") "
        + "VALUES ("
            + ":divisionId, :battlenetId, "
            + ":season, :region, :leagueType, :queueType, :teamType, :tierType, "
            + ":rating, :points, :wins, :losses, :ties"
        + ")";

    private static final String MERGE_QUERY = CREATE_QUERY
        + " "
        + "ON CONFLICT(region, battlenet_id) DO UPDATE SET "
        + "division_id=excluded.division_id, "
        + "season=excluded.season, "
        + "league_type=excluded.league_type, "
        + "queue_type=excluded.queue_type, "
        + "team_type=excluded.team_type, "
        + "tier_type=excluded.tier_type, "
        + "rating=excluded.rating, "
        + "points=excluded.points, "
        + "wins=excluded.wins, "
        + "losses=excluded.losses, "
        + "ties=excluded.ties";


    private NamedParameterJdbcTemplate template;
    private ConversionService conversionService;

    @Autowired
    public TeamDAO
    (
        @Qualifier("sc2StatsNamedTemplate") NamedParameterJdbcTemplate template,
        @Qualifier("sc2StatsConversionService") ConversionService conversionService
    )
    {
        this.template = template;
        this.conversionService = conversionService;
    }

    public Team create(Team team)
    {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        MapSqlParameterSource params = createParameterSource(team);
        template.update(CREATE_QUERY, params, keyHolder, new String[]{"id"});
        team.setId(keyHolder.getKey().longValue());
        return team;
    }

    public Team merge(Team team)
    {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        MapSqlParameterSource params = createParameterSource(team);
        template.update(MERGE_QUERY, params, keyHolder, new String[]{"id"});
        team.setId(keyHolder.getKey().longValue());
        return team;
    }

    private MapSqlParameterSource createParameterSource(Team team)
    {
        return new MapSqlParameterSource()
            .addValue("divisionId", team.getDivisionId())
            .addValue("battlenetId", team.getBattlenetId())
            .addValue("season", team.getSeason())
            .addValue("region", conversionService.convert(team.getRegion(), Integer.class))
            .addValue("leagueType", conversionService.convert(team.getLeagueType(), Integer.class))
            .addValue("queueType", conversionService.convert(team.getQueueType(), Integer.class))
            .addValue("teamType", conversionService.convert(team.getTeamType(), Integer.class))
            .addValue("tierType", conversionService.convert(team.getTierType(), Integer.class))
            .addValue("rating", team.getRating())
            .addValue("points", team.getPoints())
            .addValue("wins", team.getWins())
            .addValue("losses", team.getLosses())
            .addValue("ties", team.getTies());
    }

}
