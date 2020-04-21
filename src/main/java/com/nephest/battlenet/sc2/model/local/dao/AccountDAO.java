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

import com.nephest.battlenet.sc2.model.local.Account;
import com.nephest.battlenet.sc2.model.util.PostgreSQLUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.convert.ConversionService;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.util.Collections;

@Repository
public class AccountDAO
{
    private static final String CREATE_QUERY = "INSERT INTO account "
        + "(battlenet_id, region, battle_tag, updated) "
        + "VALUES (:battlenetId, :region, :battleTag, :updated)";

    private static final String MERGE_QUERY = CREATE_QUERY
        + " "
        + "ON CONFLICT(region, battlenet_id) DO UPDATE SET "
        + "battle_tag=excluded.battle_tag, "
        + "updated=excluded.updated";

    private static final String REMOVE_EXPIRED_PRIVACY_QUERY =
        "DELETE FROM account WHERE updated < DATE_SUB(NOW(), INTERVAL 30 DAY)";

    private NamedParameterJdbcTemplate template;
    private ConversionService conversionService;
    private final PostgreSQLUtils postgreSQLUtils;

    @Autowired
    public AccountDAO
    (
        @Qualifier("sc2StatsNamedTemplate") NamedParameterJdbcTemplate template,
        @Qualifier("sc2StatsConversionService") ConversionService conversionService,
        PostgreSQLUtils postgreSQLUtils
    )
    {
        this.template = template;
        this.conversionService = conversionService;
        this.postgreSQLUtils = postgreSQLUtils;
    }

    public Account create(Account account)
    {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        MapSqlParameterSource params = createParameterSource(account);
        template.update(CREATE_QUERY, params, keyHolder, new String[]{"id"});
        account.setId(keyHolder.getKey().longValue());
        return account;
    }

    public Account merge(Account account)
    {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        MapSqlParameterSource params = createParameterSource(account);
        template.update(MERGE_QUERY, params, keyHolder, new String[]{"id"});
        account.setId(keyHolder.getKey().longValue());
        return account;
    }

    private MapSqlParameterSource createParameterSource(Account account)
    {
        return new MapSqlParameterSource()
            .addValue("battlenetId", account.getBattlenetId())
            .addValue("region", conversionService.convert(account.getRegion(), Integer.class))
            .addValue("battleTag", account.getBattleTag())
            .addValue("updated", account.getUpdated());
    }

    public void removeExpiredByPrivacy()
    {
        template.update(REMOVE_EXPIRED_PRIVACY_QUERY, Collections.emptyMap());
        postgreSQLUtils.vacuum();
    }

}

