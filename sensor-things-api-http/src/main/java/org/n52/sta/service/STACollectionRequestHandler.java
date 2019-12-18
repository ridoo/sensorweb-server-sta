/*
 * Copyright (C) 2018-2019 52°North Initiative for Geospatial Open Source
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
package org.n52.sta.service;

import org.n52.sta.serdes.model.ElementWithQueryOptions;
import org.n52.sta.data.service.EntityServiceRepository;
import org.n52.sta.exception.STACRUDException;
import org.n52.sta.exception.STAInvalidUrlException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RequestMapping("/v2")
@RestController
public class STACollectionRequestHandler extends STARequestUtils {

    private final EntityServiceRepository serviceRepository;
    private final String mappingPrefix = "**/";
    private final int rootUrlLength;

    public STACollectionRequestHandler(@Value("${server.rootUrl}") String rootUrl,
                                       EntityServiceRepository serviceRepository) {
        rootUrlLength = rootUrl.length();
        this.serviceRepository = serviceRepository;
    }

    /**
     * Matches all requests on Collections referenced directly
     * e.g. /Datastreams
     *
     * @param collectionName name of the collection. Automatically set by Spring via @PathVariable
     * @param queryOptions   query options. Automatically set by Spring via @RequestParam
     */
    @GetMapping(
            value = "/{collectionName:" + COLLECTION_REGEX + "}",
            produces = "application/json"
    )
    public List<ElementWithQueryOptions> readCollectionDirect(@PathVariable String collectionName,
                                                              @RequestParam Map<String, String> queryOptions)
            throws STACRUDException {
        return serviceRepository
                .getEntityService(collectionName)
                .getEntityCollection(createQueryOptions(queryOptions));
    }

    /**
     * Matches all requests on Entities not referenced directly via id but via referenced entity.
     * e.g. /Datastreams(52)/Thing
     *
     * @param entity  composite of entity and referenced entity. Automatically set by Spring via @PathVariable
     * @param request full request
     * @return JSON String representing Entity
     */
    @GetMapping(
            value = {mappingPrefix + COLLECTION_IDENTIFIED_BY_THING_PATH,
                     mappingPrefix + COLLECTION_IDENTIFIED_BY_LOCATION_PATH,
                     mappingPrefix + COLLECTION_IDENTIFIED_BY_OBSERVED_PROPERTY_PATH,
                     mappingPrefix + COLLECTION_IDENTIFIED_BY_FEATURE_OF_INTEREST_PATH,
                     mappingPrefix + COLLECTION_IDENTIFIED_BY_SENSOR_PATH,
                     mappingPrefix + COLLECTION_IDENTIFIED_BY_DATASTREAM_PATH
            },
            produces = "application/json"
    )
    public Object readRelatedCollection(@PathVariable String entity,
                                        @PathVariable String target,
                                        @RequestParam Map<String, String> queryOptions,
                                        HttpServletRequest request) throws STACRUDException, STAInvalidUrlException {

        // TODO(specki): check if something needs to be cut from the front like rootUrl
        // TODO(specki): short-circuit if url is only one element as spring already validated that when the path matched
        // TODO(specki): Error serialization for nice output

        validateURL(request.getRequestURL(), serviceRepository, rootUrlLength);

        String sourceType = entity.split("\\(")[0];
        String sourceId = entity.split("\\(")[1].replace(")", "");

        return serviceRepository.getEntityService(target)
                .getEntityCollectionByRelatedEntity(sourceId, sourceType, createQueryOptions(queryOptions));
    }
}