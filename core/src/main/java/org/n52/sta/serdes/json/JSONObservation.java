/*
 * Copyright (C) 2018-2020 52°North Initiative for Geospatial Open Source
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

package org.n52.sta.serdes.json;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.joda.time.DateTime;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.geojson.GeoJsonReader;
import org.n52.series.db.beans.parameter.ParameterEntity;
import org.n52.series.db.beans.parameter.ParameterJsonEntity;
import org.n52.series.db.beans.sta.ObservationEntity;
import org.n52.shetland.ogc.gml.time.Time;
import org.n52.shetland.ogc.gml.time.TimeInstant;
import org.n52.shetland.ogc.gml.time.TimePeriod;
import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;

@SuppressWarnings("VisibilityModifier")
@SuppressFBWarnings({"NM_FIELD_NAMING_CONVENTION", "UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD"})
public class JSONObservation extends JSONBase.JSONwithIdTime<ObservationEntity> implements AbstractJSONEntity {

    // JSON Properties. Matched by Annotation or variable name
    public String phenomenonTime;
    public String resultTime;
    public String result;
    public Object resultQuality;
    public String validTime;
    public ArrayNode parameters;

    @JsonManagedReference
    public JSONFeatureOfInterest FeatureOfInterest;
    @JsonManagedReference
    public JSONDatastream Datastream;

    private final String NAME = "name";
    private final String VALUE = "value";

    public JSONObservation() {
        self = new ObservationEntity();
    }

    @Override
    public ObservationEntity toEntity(JSONBase.EntityType type) {
        switch (type) {
        case FULL:
            parseReferencedFrom();
            Assert.notNull(result, INVALID_INLINE_ENTITY_MISSING + "result");
            return createPostEntity();
        case PATCH:
            parseReferencedFrom();
            return createPatchEntity();
        case REFERENCE:
            Assert.isNull(phenomenonTime, INVALID_REFERENCED_ENTITY);
            Assert.isNull(resultTime, INVALID_REFERENCED_ENTITY);
            Assert.isNull(result, INVALID_REFERENCED_ENTITY);
            Assert.isNull(resultTime, INVALID_REFERENCED_ENTITY);
            Assert.isNull(resultQuality, INVALID_REFERENCED_ENTITY);
            Assert.isNull(parameters, INVALID_REFERENCED_ENTITY);

            self.setIdentifier(identifier);
            self.setStaIdentifier(identifier);
            return self;
        default:
            return null;
        }
    }

    @Override
    protected void parseReferencedFrom() {
        if (referencedFromType != null) {
            switch (referencedFromType) {
            case "Datastreams":
                Assert.isNull(Datastream, INVALID_DUPLICATE_REFERENCE);
                this.Datastream = new JSONDatastream();
                this.Datastream.identifier = referencedFromID;
                return;
            case "FeaturesOfInterest":
                Assert.isNull(FeatureOfInterest, INVALID_DUPLICATE_REFERENCE);
                this.FeatureOfInterest = new JSONFeatureOfInterest();
                this.FeatureOfInterest.identifier = referencedFromID;
                return;
            default:
                throw new IllegalArgumentException(INVALID_BACKREFERENCE);
            }
        }
    }

    private ObservationEntity createPatchEntity() {
        self.setIdentifier(identifier);
        self.setStaIdentifier(identifier);

        // parameters
        storeParameters(parameters);

        // phenomenonTime
        if (phenomenonTime != null) {
            Time time = parseTime(phenomenonTime);
            if (time instanceof TimeInstant) {
                self.setSamplingTimeStart(((TimeInstant) time).getValue().toDate());
                self.setSamplingTimeEnd(((TimeInstant) time).getValue().toDate());
            } else if (time instanceof TimePeriod) {
                self.setSamplingTimeStart(((TimePeriod) time).getStart().toDate());
                self.setSamplingTimeEnd(((TimePeriod) time).getEnd().toDate());
            }
        }

        // Set resultTime only when supplied
        if (resultTime != null) {
            // resultTime
            self.setResultTime(((TimeInstant) parseTime(resultTime)).getValue().toDate());
        }

        // validTime
        if (validTime != null) {
            Time time = parseTime(validTime);
            if (time instanceof TimeInstant) {
                self.setValidTimeStart(((TimeInstant) time).getValue().toDate());
                self.setValidTimeEnd(((TimeInstant) time).getValue().toDate());
            } else if (time instanceof TimePeriod) {
                self.setValidTimeStart(((TimePeriod) time).getStart().toDate());
                self.setValidTimeEnd(((TimePeriod) time).getEnd().toDate());
            }
        }

        self.setValue(result);

        // Link to Datastream
        if (Datastream != null) {
            self.setDatastream(Datastream.toEntity(JSONBase.EntityType.REFERENCE));
        }

        // Link to FOI
        if (FeatureOfInterest != null) {
            self.setFeature(FeatureOfInterest.toEntity(JSONBase.EntityType.REFERENCE));
        }

        return self;
    }

    private ObservationEntity createPostEntity() {
        self.setIdentifier(identifier);
        self.setStaIdentifier(identifier);

        // phenomenonTime
        if (phenomenonTime != null) {
            Time time = parseTime(phenomenonTime);
            if (time instanceof TimeInstant) {
                self.setSamplingTimeStart(((TimeInstant) time).getValue().toDate());
                self.setSamplingTimeEnd(((TimeInstant) time).getValue().toDate());
            } else if (time instanceof TimePeriod) {
                self.setSamplingTimeStart(((TimePeriod) time).getStart().toDate());
                self.setSamplingTimeEnd(((TimePeriod) time).getEnd().toDate());
            }
        } else {
            // Use time of POST Request as fallback
            Date date = DateTime.now().toDate();
            self.setSamplingTimeStart(date);
            self.setSamplingTimeEnd(date);
        }

        // Set resultTime only when supplied
        if (resultTime != null) {
            // resultTime
            self.setResultTime(((TimeInstant) parseTime(resultTime)).getValue().toDate());
        }

        // validTime
        if (validTime != null) {
            Time time = parseTime(validTime);
            if (time instanceof TimeInstant) {
                self.setValidTimeStart(((TimeInstant) time).getValue().toDate());
                self.setValidTimeEnd(((TimeInstant) time).getValue().toDate());
            } else if (time instanceof TimePeriod) {
                self.setValidTimeStart(((TimePeriod) time).getStart().toDate());
                self.setValidTimeEnd(((TimePeriod) time).getEnd().toDate());
            }
        }

        // parameters
        storeParameters(parameters);

        // result
        self.setValue(result);

        // Link to Datastream
        if (Datastream != null) {
            self.setDatastream(
                    Datastream.toEntity(JSONBase.EntityType.FULL, JSONBase.EntityType.REFERENCE));
        } else if (backReference instanceof JSONDatastream) {
            self.setDatastream(((JSONDatastream) backReference).getEntity());
        } else {
            Assert.notNull(null, INVALID_INLINE_ENTITY_MISSING + "Datastream");
        }

        // Link to FOI
        if (FeatureOfInterest != null) {
            self.setFeature(
                    FeatureOfInterest.toEntity(JSONBase.EntityType.FULL, JSONBase.EntityType.REFERENCE));
        } else if (backReference instanceof JSONFeatureOfInterest) {
            self.setFeature(((JSONFeatureOfInterest) backReference).getEntity());
        }

        return self;
    }

    protected void storeParameters(ArrayNode parameters) {
        // parameters
        if (parameters != null) {
            HashSet<ParameterEntity<?>> parameterJsonEntities = new HashSet<>();
            for (JsonNode param : parameters) {
                // Check that structure is correct
                Assert.isTrue(param.has(NAME));
                Assert.isTrue(param.has(VALUE));
                Assert.isTrue(!param.has(2));

                String paramName = param.get(NAME).asText();
                JsonNode jsonNode = param.get(VALUE);

                ParameterJsonEntity parameterEntity = new ParameterJsonEntity();
                parameterEntity.setName(paramName);
                parameterEntity.setValue(jsonNode.toString());
                parameterJsonEntities.add(parameterEntity);
            }
            self.setParameters(parameterJsonEntities);
        }
    }

    public JSONObservation parseParameters(Map<String, String> propertyMapping) {
        if (parameters != null) {
            for (Map.Entry<String, String> mapping : propertyMapping.entrySet()) {
                for (JsonNode param : parameters) {
                    String paramName = param.get(NAME).asText();
                    if (paramName.equals(mapping.getValue())) {
                        JsonNode jsonNode = param.get(VALUE);
                        switch (mapping.getKey()) {
                        case "samplingGeometry":
                            // Add as samplingGeometry to enable interoperability with SOS
                            GeometryFactory factory =
                                    new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING), 4326);
                            GeoJsonReader reader = new GeoJsonReader(factory);
                            try {
                                self.setSamplingGeometry(reader.read(jsonNode.toString()));
                            } catch (ParseException e) {
                                Assert.notNull(null, "Could not parse" + e.getMessage());
                            }
                            break;
                        case "verticalFrom":
                            // Add as verticalTo to enable interoperability with SOS
                            self.setVerticalTo(BigDecimal.valueOf(jsonNode.asDouble()));
                            break;
                        case "verticalTo":
                            // Add as verticalTo to enable interoperability with SOS
                            self.setVerticalFrom(BigDecimal.valueOf(jsonNode.asDouble()));
                            break;
                        case "verticalFromTo":
                            // Add as verticalTo to enable interoperability with SOS
                            self.setVerticalTo(BigDecimal.valueOf(jsonNode.asDouble()));
                            self.setVerticalFrom(BigDecimal.valueOf(jsonNode.asDouble()));
                            break;
                        default:
                            throw new RuntimeException("Unable to parse Parameters!");
                        }
                    }
                }
            }
        }
        return this;
    }
}
