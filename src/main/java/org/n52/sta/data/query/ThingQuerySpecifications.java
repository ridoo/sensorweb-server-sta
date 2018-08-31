/*
 * Copyright (C) 2012-2018 52°North Initiative for Geospatial Open Source
 * Software GmbH
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 as published
 * by the Free Software Foundation.
 *
 * If the program is linked with libraries which are licensed under one of
 * the following licenses, the combination of the program with the linked
 * library is not considered a "derivative work" of the program:
 *
 *     - Apache License, version 2.0
 *     - Apache Software License, version 1.0
 *     - GNU Lesser General Public License, version 3
 *     - Mozilla Public License, versions 1.0, 1.1 and 2.0
 *     - Common Development and Distribution License (CDDL), version 1.0
 *
 * Therefore the distribution of the program linked with libraries licensed
 * under the aforementioned licenses, is permitted by the copyright holders
 * if the distribution is compliant with both the GNU General Public
 * License version 2 and the aforementioned licenses.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 */
package org.n52.sta.data.query;

import java.util.Optional;

import org.n52.series.db.beans.sta.LocationEntity;
import org.n52.series.db.beans.sta.QLocationEntity;
import org.n52.series.db.beans.sta.QThingEntity;
import org.n52.series.db.beans.sta.ThingEntity;
import org.n52.series.db.old.dao.DbQuery;
import org.n52.series.db.query.OfferingQuerySpecifications;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 *
 */
public class ThingQuerySpecifications extends EntityQuerySpecifications {

    final QThingEntity qthing = QThingEntity.thingEntity;
    
    public JPQLQuery<ThingEntity> toSubquery(final BooleanExpression filter) {
        return JPAExpressions.selectFrom(qthing)
                             .where(filter);
    }
    
    public <T> BooleanExpression selectFrom(JPQLQuery<T> subquery) {
        return qthing.id.in(subquery.select(qthing.id));
    }
    
    public BooleanExpression matchesId(Long id) {
        return qthing.id.eq(id);
    }
    

}