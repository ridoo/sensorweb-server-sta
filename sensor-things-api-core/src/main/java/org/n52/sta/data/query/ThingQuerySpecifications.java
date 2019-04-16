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

import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;

import org.apache.olingo.server.api.uri.queryoption.expression.BinaryOperatorKind;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitException;
import org.n52.series.db.beans.DescribableEntity;
import org.n52.series.db.beans.PlatformEntity;
import org.n52.series.db.beans.sta.DatastreamEntity;
import org.n52.series.db.beans.sta.HistoricalLocationEntity;
import org.n52.series.db.beans.sta.LocationEntity;
import org.springframework.data.jpa.domain.Specification;

/**
 * @author <a href="mailto:j.speckamp@52north.org">Jan Speckamp</a>
 *
 */
public class ThingQuerySpecifications extends EntityQuerySpecifications<PlatformEntity> {
    
    public Specification<PlatformEntity> withRelatedLocation(Long locationId) {
        return (root, query, builder) -> {
            final Join<PlatformEntity, LocationEntity> join =
                    root.join(PlatformEntity.PROPERTY_LOCATIONS, JoinType.INNER);
            return builder.equal(join.get(DescribableEntity.PROPERTY_ID), locationId);
        };
    }

    public Specification<PlatformEntity>  withRelatedHistoricalLocation(Long historicalId) {
        return (root, query, builder) -> {
            final Join<PlatformEntity, HistoricalLocationEntity> join =
                    root.join(PlatformEntity.PROPERTY_HISTORICAL_LOCATIONS, JoinType.INNER);
            return builder.equal(join.get(DescribableEntity.PROPERTY_ID), historicalId);
        };
    }
    
    public Specification<PlatformEntity> withRelatedDatastream(Long datastreamId) {
        return (root, query, builder) -> {
            final Join<PlatformEntity, DatastreamEntity> join =
                    root.join(PlatformEntity.PROPERTY_DATASTREAMS, JoinType.INNER);
            return builder.equal(join.get(DescribableEntity.PROPERTY_ID), datastreamId);
        };
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.n52.sta.data.query.EntityQuerySpecifications#getIdSubqueryWithFilter(com.querydsl.core.types.dsl.
     * BooleanExpression)
     */
    @Override
    public JPQLQuery<Long> getIdSubqueryWithFilter(Expression<Boolean> filter) {
        return this.toSubquery(qPlatform, qPlatform.id, filter);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.n52.sta.data.query.EntityQuerySpecifications#getFilterForProperty(java.lang.String,
     * java.lang.Object, org.apache.olingo.server.api.uri.queryoption.expression.BinaryOperatorKind)
     */
    @Override
    public Object getFilterForProperty(String propertyName,
                                       Object propertyValue,
                                       BinaryOperatorKind operator,
                                       boolean switched)
            throws ExpressionVisitException {
        if (propertyName.equals("Datastreams") || propertyName.equals("Locations")
                || propertyName.equals("HistoricalLocations")) {
            return handleRelatedPropertyFilter(propertyName, (JPQLQuery<Long>) propertyValue);
        } else if (propertyName.equals("id")) {
            return handleDirectNumberPropertyFilter(qPlatform.id, propertyValue, operator, switched);
        } else {
            return handleDirectPropertyFilter(propertyName, propertyValue, operator, switched);
        }
    }

    private BooleanExpression handleRelatedPropertyFilter(String propertyName, JPQLQuery<Long> propertyValue)
            throws ExpressionVisitException {
        switch(propertyName) {
        case "Datastreams": {
            return qPlatform.datastreamEntities.any().id.eqAny(propertyValue);
        }
        case "Locations": {
            return qPlatform.locationEntities.any().id.eqAny(propertyValue);
        }
        case "HistoricalLocations": {
            return qPlatform.historicalLocationEntities.any().id.eqAny(propertyValue);
        }
        default:
            throw new ExpressionVisitException("Filtering by Related Properties with cardinality >1 is currently not supported!");
        }
    }

    private Object handleDirectPropertyFilter(String propertyName,
                                              Object propertyValue,
                                              BinaryOperatorKind operator,
                                              boolean switched)
            throws ExpressionVisitException {
        switch (propertyName) {
        case "name":
            return handleDirectStringPropertyFilter(qPlatform.name, propertyValue, operator, switched);
        case "description":
            return handleDirectStringPropertyFilter(qPlatform.description, propertyValue, operator, switched);
        case "properties":
            // TODO 
//            qPlatform.parameters.any().name.eq("properties")
            return handleDirectStringPropertyFilter(qPlatform.properties, propertyValue, operator, switched);
        default:
            throw new ExpressionVisitException("Error getting filter for Property: \"" + propertyName
                    + "\". No such property in Entity.");
        }
    }
}
