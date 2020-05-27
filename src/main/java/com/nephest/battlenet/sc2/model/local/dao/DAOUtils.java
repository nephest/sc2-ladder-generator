// Copyright (C) 2020 Oleksandr Masniuk and contributors
// SPDX-License-Identifier: AGPL-3.0-or-later

package com.nephest.battlenet.sc2.model.local.dao;

import org.springframework.jdbc.core.ResultSetExtractor;

public final class DAOUtils
{
    private DAOUtils(){}

    public static final ResultSetExtractor<Long> LONG_EXTRACTOR =
    (rs)->
    {
        rs.next();
        long val = rs.getLong(1);
        return rs.wasNull() ? null : val;
    };

    public static final ResultSetExtractor<Integer> INT_EXTRACTOR =
    (rs)->
    {
        rs.next();
        int val = rs.getInt(1);
        return rs.wasNull() ? null : val;
    };

}
