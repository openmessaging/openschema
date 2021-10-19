/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.openschema.registry.server.service;

import io.openschema.registry.server.domain.Compatibility;
import io.openschema.registry.server.domain.Schema;
import io.openschema.registry.server.domain.Subject;
import io.openschema.registry.server.exception.ExceptionEnum;
import io.openschema.registry.server.exception.OpenSchemaException;
import io.openschema.registry.server.repository.SchemaRepository;
import io.openschema.registry.server.repository.SubjectRepository;
import io.openschema.registry.server.response.CompatibilityResultResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CompatibilityService {

    @Autowired
    SubjectRepository subjectRepository;

    @Autowired
    SchemaRepository schemaRepository;


    public CompatibilityResultResponse checkWhetherCompatible(String subject, Integer version, Schema schema){
        Subject subjectObj = null;
        Schema schemaObj = null;
        CompatibilityResultResponse response = null;
        try {
            subjectObj = subjectRepository.getSubjectBySubject(subject);
        }catch (Exception e){
            throw new OpenSchemaException(ExceptionEnum.StorageServiceException);
        }
        if(subjectObj == null){
            throw new OpenSchemaException(ExceptionEnum.SubjectNonExist);
        }

        try {
            schemaObj = schemaRepository.getSchemaBySubjectAndVersion(subject, version);
        }catch (Exception e){
            throw new OpenSchemaException(ExceptionEnum.StorageServiceException);
        }
        if(schemaObj == null){
            throw new OpenSchemaException(ExceptionEnum.SchemaNonExist);
        }
        // todo check the compatibility

        return response;
    }


    public Compatibility getCompatibilityBySubject(String subject){
        Subject subjectObj = null;
        try {
            subjectObj = subjectRepository.getSubjectBySubject(subject);
        }catch (Exception e){
            throw new OpenSchemaException(ExceptionEnum.StorageServiceException);
        }

        if(subjectObj==null){
            throw new OpenSchemaException(ExceptionEnum.SubjectNonExist);
        }

        return new Compatibility(subjectObj.getCompatibility());
    }

    public Compatibility updateCompatibilityBySubject(String subject, Compatibility compatibility){
        Subject subjectObj = null;
        try {
            subjectObj = subjectRepository.getSubjectBySubject(subject);
        }catch (Exception e){
            throw new OpenSchemaException(ExceptionEnum.StorageServiceException);
        }
        if(subjectObj == null){
            throw new OpenSchemaException(ExceptionEnum.SubjectNonExist);
        }
        subjectObj.setCompatibility(compatibility.getCompatibility());
        try {
            // todo check the compatibility among schemas of newly set compatibility
            subjectRepository.save(subjectObj);
        }catch (Exception e){
            throw new OpenSchemaException(ExceptionEnum.StorageServiceException);
        }
        return compatibility;
    }
}
