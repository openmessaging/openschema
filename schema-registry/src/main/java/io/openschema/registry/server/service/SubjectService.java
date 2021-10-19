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

import io.openschema.registry.server.common.util.ConverterUtil;
import io.openschema.registry.server.common.util.JsonUtils;
import io.openschema.registry.server.domain.*;
import io.openschema.registry.server.exception.ExceptionEnum;
import io.openschema.registry.server.exception.OpenSchemaException;
import io.openschema.registry.server.repository.SchemaRepository;
import io.openschema.registry.server.repository.SubjectRepository;
import io.openschema.registry.server.response.SchemaIdResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import javax.annotation.Resource;

@Service
public class SubjectService {

    @Autowired
    SubjectRepository subjectRepository;

    @Autowired
    SchemaRepository schemaRepository;
    
    @Resource
    private ConverterUtil converterUtil;

    public List<String> getAllSubjects() {
        List<String> subjects;
        try {
            subjects = subjectRepository.getAllSubject();
        } catch (Exception e) {
            throw new OpenSchemaException(ExceptionEnum.StorageServiceException);
        }
        return subjects;
    }

    public List<Integer> getAllVersionsBySubject(String subject) {
        List<Integer> versions;
        try {
            versions = subjectRepository.getAllVersionsBySubject(subject);
        } catch (Exception e) {
            throw new OpenSchemaException(ExceptionEnum.StorageServiceException);
        }
        if (versions == null) {
            throw new OpenSchemaException(ExceptionEnum.SubjectNonExist);
        }
        return versions;
    }

    public List<Integer> deleteSubjectAndAllSchemaBySubject(String subject) {
        List<Integer> versions;
        Integer deletedSubject;
        try {
            deletedSubject = subjectRepository.deleteSubjectBySubject(subject);
        } catch (Exception e) {
            throw new OpenSchemaException(ExceptionEnum.StorageServiceException);
        }
        if (deletedSubject == 0) {
            throw new OpenSchemaException(ExceptionEnum.SubjectNonExist);
        }

        try {
            versions = schemaRepository.deleteSchemasBySubject(subject);
        } catch (Exception e) {
            throw new OpenSchemaException(ExceptionEnum.StorageServiceException);
        }
        return versions;
    }

    public Subject getSubjectByName(String subject) {
        Subject subjectObj;
        try {
            subjectObj = subjectRepository.getSubjectBySubject(subject);
        } catch (Exception e) {
            throw new OpenSchemaException(ExceptionEnum.StorageServiceException);
        }

        if (subjectObj == null) {
            throw new OpenSchemaException(ExceptionEnum.SubjectNonExist);
        }

        return subjectObj;
    }

    public SubjectWithSchema getSchemaBySubjectAndVersion(String subject, int version) {
        Schema schema = null;
        Subject subjectObj = null;
        try {
            subjectObj = subjectRepository.getSubjectBySubject(subject);
            schema = schemaRepository.getSchemaBySubjectAndVersion(subject, version);
        } catch (Exception e) {
            throw new OpenSchemaException(ExceptionEnum.StorageServiceException);
        }

        if (subjectObj == null) {
            throw new OpenSchemaException(ExceptionEnum.VersionNonExist);
        }

        if (schema == null) {
            throw new OpenSchemaException(ExceptionEnum.SubjectNonExist);
        }

        return new SubjectWithSchema(subjectObj, schema);
    }


    public SchemaIdResponse checkOrRegisterSchema(String subject, Schema schema) {
        Subject subjectObj = null;
        try {
            subjectObj = subjectRepository.getSubjectBySubject(subject);
        } catch (Exception e) {
            throw new OpenSchemaException(ExceptionEnum.StorageServiceException);
        }

        if (subjectObj == null) {
            throw new OpenSchemaException(ExceptionEnum.SubjectNonExist);
        }

        List<SchemaWithSubjectName> schemaWithNames = null;
        try {
            schemaWithNames = schemaRepository.getSchemaWithSubjectNamesBySubjectOrderByVersionDesc(subject);
        } catch (Exception e) {
            throw new OpenSchemaException(ExceptionEnum.StorageServiceException);
        }

        SchemaIdResponse schemaIdResponse = schemaWithNames
                .stream()
                .filter(schemaWithSubjectName
                        -> schema.getSerialization().equals(schemaWithSubjectName.getSerialization())                        
                        && schema.getSchemaDefinition().equals(schemaWithSubjectName.getSchemaType()))
                .findFirst()
                .map(schemaWithSubjectName
                        -> new SchemaIdResponse(String.valueOf(schemaWithSubjectName.getId())))
                .orElse(null);

        if (schemaIdResponse == null) {        	
        	try {
        		String schemaDefinitionStr = JsonUtils.toJson(schema.getSchemaDefinition());        		
        		int version = schemaWithNames.size();
        		if (version > 0) {
        			version = schemaWithNames.get(0).getVersion();
        		}
	        	SchemaWithSubjectName schemaWithSubjectName = SchemaWithSubjectName
	                    .builder()
	                    .name(schema.getName())
	                    .comment(schema.getComment())
	                    .serialization(schema.getSerialization())	                    
	                    .schemaDefinition(schemaDefinitionStr)
	                    .validator(schema.getValidator())
	                    .version(version + 1)
	                    .subject(subject)
	                    .build();	        
                SchemaWithSubjectName save = schemaRepository.save(schemaWithSubjectName);
                schemaIdResponse = new SchemaIdResponse(String.valueOf(save.getId()));
            } catch (Exception e) {
                throw new OpenSchemaException(ExceptionEnum.StorageServiceException);
            } 

        }
        return schemaIdResponse;
    }
    
    @Transactional
    public Subject updateSubjectIfDifferent(String subjectName, Subject subject) {        
    	long timestamp = converterUtil.timestamp();
    	String createdTime = converterUtil.toLocalTime(timestamp);        	
    	subject.setCreatedTime(createdTime);
    	subject.setLastModifiedTime(createdTime);
    	
    	Subject subjectObj = null;
        Subject returnSubject = null;        
        try {
            subjectObj = subjectRepository.getSubjectBySubject(subjectName);
        } catch (Exception e) {
            throw new OpenSchemaException(ExceptionEnum.StorageServiceException);
        }                      
    	if (subjectObj == null) {        
            returnSubject = subjectRepository.save(subject);
        } else {
            if (subjectObj.getSubject().equals(subject.getSubject())) {
            	subject.setCreatedTime(subjectObj.getCreatedTime());
            	returnSubject = subjectRepository.save(subject);
            } else {
                //todo
                // if compatibility is changed, the left schemas should satisfy compatibility
            }
        }
        return returnSubject;
    }

    public Integer deleteSchemaBySubjectAndVersion(String subject, int version) {
        Integer deletedVersion = null;
        // todo complete the logic here for compatibility check
        try {
            deletedVersion = schemaRepository.deleteSchemaBySubjectAndVersion(subject, version);
        } catch (Exception e) {
            throw new OpenSchemaException(ExceptionEnum.StorageServiceException);
        }
        if (deletedVersion == null) {
            throw new OpenSchemaException(ExceptionEnum.SchemaNonExist);
        }
        return deletedVersion;
    }

    public Compatibility getCompatibilityBySubject(String subject) {
        Subject subjectObj = null;
        try {
            subjectObj = subjectRepository.getSubjectBySubject(subject);
        } catch (Exception e) {
            throw new OpenSchemaException(ExceptionEnum.StorageServiceException);
        }

        if (subjectObj == null) {
            throw new OpenSchemaException(ExceptionEnum.SubjectNonExist);
        }

        return new Compatibility(subjectObj.getCompatibility());
    }
}
