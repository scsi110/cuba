/*
 * Copyright (c) 2008-2016 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.haulmont.restapi.controllers;

import com.haulmont.restapi.service.DatatypesControllerManager;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;

/**
 * Controller that is used for getting datatypes information.
 */
@RestController("cuba_DatatypesController")
@RequestMapping(value = "/v2/metadata/datatypes", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class DatatypesController {
    @Inject
    protected DatatypesControllerManager datatypesControllerManager;

    @RequestMapping(method = RequestMethod.GET)
    public String getDatatypes() {
        return datatypesControllerManager.getDatatypesJson();
    }
}