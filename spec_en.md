# OpenSchema Specification


## 1. Abstract

This specification defines a vendor-neutral OpenSchema metadata and interaction modes, targeting the data schema domain.

## Table of Contents

- [Compatibility Mode](#compatibility-mode)
- [Content Types](#3-content-types)
- [Error Codes](#4-error-codes)
- [Schema Format](#5-schema-format)
  - [Metadata Information](#51-metadata-information)
  - [Schema Definition](#52-schema-definition)
- [Relationship Between Subjects, Topics and Schemas](#6-relationship-between-subjects-topics-and-schemas)
- [REST Interface Definition](#7-rest-interface-definition)
  - [Schema related APIs](#71-schema-related-apis)
  - [Subject related APIs](#72-subject-related-apis)
  - [Compatibility related APIs](#73-compatibility-related-apis)

## 2. Compatibility Mode

The compatibility mode defines the compatibility rules concerning which changes you’re allowed to make to the schema without breaking the consumers, and how to handle upgrades for the different types of schema changes

| Compatibility Settings | Description |
| ------------------- | ------------------------------------------------------------ |
| BACKWARD (default) | Consumers using the new schema can read data produced with the last schema.|
| BACKWARD_TRANSITIVE | Consumers using the new schema can read data sent by the producer using all previously registered schemas.|
| Forward | data produced with a new schema can be read by consumers using the last schema.|
| Forward_TRANSITIVE | data produced with a new schema can be read by consumers using all registered schemas.|
| FULL | The new schema is backward and forward compatible with the newly registered schema.|
| FULL_TRANSITIVE | The newly registered schema is backward and forward compatible with all previously registered schemas |
| NONE | Schema compatibility checks are disabled.|



## 3. Content Types

The OpenSchema REST service communicates using HTTP+JSON.

The request should specify the most specific format and version information via the HTTP Accept header, and MAY include several authentication settings:

> Accept:application/vnd.openschema.v1+json



## 4. Error Codes

The HTTP response of all requests is consistent with the HTTP standard. The detailed error code is determined by the returned JSON response. The format is as follows:

```json
{
    "errorCode": 422,
    "errorMessage": "schema info cannot be empty"
}
```



## 5. Schema Format

### 5.1 Metadata Information

The following metadata information describes Subject definition in OpenSchema specification.

|Parameter Name|Description|Example|
| ------------- | ------------------------ | ------------------------------- |
| tenant | Tenant | org/apache/rocketmq/mybank |
| namespace | Namespace | Cluster name, for example, rocketmq-cluster |
| subject | Subject name | For example, use the topic name as the subject name. |
| app | Application deployment unit of the service provider | |
| description | Description | Provided by the applicant |
| status | Subject status | For example, released or abandoned |
| compatibility | Compatibility setting | None, forward compatibility, backward compatibility, and full compatibility|
| coordinate | Maven coordinate | Maven coordinate of the JAR of the payload|
| createdTime    | the time when a subject was registered   |  2021-09-14T02:26:09.018    |
| lastModifiedTime    | the time when a subject was last updated     |  2021-09-15T02:26:09.018    |
| format | Enumeration of schema types: NONE, JSON, PB, AVRO, USER-DEFINED, Int, Long, String, and Map | If no schema is provided in a message, the schema type is NONE. You can also add a schema to the current message. For example, you can use PB to describe the format of the data transmitted by the RocketMQ.
| schema | Data format | Associated data format description. For details, see the following table. |

Example:

```json
{
    "tenant": "messaging/rocketmq",
    "namespace": "org.apache.rocketmq",
    "subject": "test-topic",
    "app": "rocketmq",
    "description": "rocketmq test subject",
    "status": "released",
    "compatibility": "NONE",
    "coordinate": "maven-group:package:1.0.0",
    "createdTime": "2021-09-14T02:26:09.018",
    "lastModifiedTime": "2021-09-15T02:26:09.018",
    "format": "AVRO",
    "schema": {}
}
```

### 5.2 Schema Definition

The Payload Schema is used to describe the payload data of a message.

|Parameter Name|Description|Example|
| ---------------- | ------------------------------------------------------------ | ------------------------------------------------------------ |
| name | Payload name, which can be null. For example, the payload of a message does not need a name. | |
| id | Globally unique identifier, which is used to identify the schema | |
| comment | Payload comment | |
| serialization | Serialization mode: Hissian, JSON, PB, AVRO, and user-defined | |
| schemaDefinition | Schema content, which is used to describe the data format. | NONE: none PB: PB description file AVRO: AVRO schema content USER-DEFINED: user-defined information Basic content type: none |
| validator | Object value validator | Validates the values of objects described in the schema.|
| version | Schema version | For example, the payload may change. In this case, the version is required to identify different schemas. |

Example:

```json
{
    "name": "rocketmq-user-topic",
    "id": "SLEd-3334-XSSFE-44983",
    "comment": "Rocketmq user information",
    "serialization": "",
    "schemaDefinition": [{
                "name": "id",
                "type": "string"
            },
            {
                "name": "age",
                "type": "short"
        }
    ],
   "validator": "a.groovy",
   "version": 1
}
```



## 6. Relationship Between Subjects, Topics and Schemas

### 6.1 Relationship Between Subjects and Topics

- Messaging system


Subject Name by default, the value is a topic name, which defines the format of the message body. The value can be extended by suffix ${topic}-${suffix}. For example, in Kafka, Kafka-Key is generally used to define the data format of the key in Kafka messages.

- Other systems


Subject Name can be defined and customized according to other system's requirement.

### 6.2 Relationship Between Subjects and Schemas

- A subject contains multiple versions of schemas, and the relationship is 1:N.

- Compatibility settings are defined at the subject level. Multiple versions of schemas evolve based on the settings.

- The globally unique ID is defined in a schema. You can find a unique schema based on the ID. The relationship is 1:1.

- Subject+version can also uniquely locate a schema.




## 7. REST Interface Definition

- **Common Parameter**

| | Parameter name | Parameter type | Required or not | Parameter description |
| ------------ | ------------- | -------- | -------- | -------- |
| Common request parameter | tenant | string | Optional | Tenant |
| | namespace | string | Optional | Namespace |
| Common Response parameter | errorCode | int | Required | Error code |
| | errorMessage | string | Required | Error message |

- **Version Rules**

By default, the schema version number increases in ascending order. You can use the latest version number to obtain the latest schema version. However, a new schema version may be registered right after the latest version number is retrieved.

For example, the following request is used to obtain the latest schema definition under test-value:

```sh
curl -X GET http://localhost:8081/subjects/test-value/versions/latest/schema
```





### 7.1 Schema related APIs

#### 7.1.1 Obtaining Schema Details by ID

- URL


GET /schemas/{string: id}

- Request parameters


|Parameter name|Parameter type|Required or not|Parameter description|
| -------- | -------- | -------- | ---------------- |
| id | string | Yes | Unique ID of a schema |

- Response parameters


|Parameter name|Parameter type|Required or not|Parameter description|
| -------- | -------- | -------- | -------------------- |
| name | string | No | schema name |
| id | string | Yes | Unique ID of a schema |
| comment | string | No | description for this schema |
| serialization | string | No | schema serialization mode: JSON, AVRO, etc |
| schemaDefinition | json | Yes | schema definition content |
| validator | string | No | schema value validator |
| version | string | Yes | schema version |

- Error code.

    401:

    40101 - Unauthorized Error

    404:

    40401: The corresponding schema information does not exist.

    500:

    50001 - Storage Service Error

- Sample request


```shell
curl -X GET http://localhost:8081/schema/20
```

- Response Example


```json
{
    "version": 1,
    "id": "20",
    "serialization": "PB",    
    "schemaDefinition": [{
            "name": "id",
            "type": "string"
        },
        {
            "name": "age",
            "type": "short"
        }
    ],
    "validator": "a.groovy",
    "comment": "user information"
}
```



#### 7.1.2 Obtaining the Subject name and Version Number Based on the ID

- URL


​ GET /schemas/{string: id}/subject

- Request parameters

|Parameter name|Parameter type|Required or not|Parameter description|
| -------- | -------- | -------- | ---------------- |
| id | string | Unique ID of a | schema |

- Response parameters


| Parameter Name| Parameter Type| Parameter Description|
| -------- | -------- | -------- |
| subject | string | Subject name |
| version | int | Version |

- Error code.

    401:

    40101 - Unauthorized Error

    404:

    40401: The corresponding schema information does not exist.

    500:

    50001 - Storage Service Error

- Sample request


```shell
curl -X GET http://localhost:8081/schemas/20/subject
```

- Response Example


```json
{"subject":"test-topic","version":1}
```



### 7.2 Subject related APIs

#### 7.2.1 Obtaining All Subjects

- URL


GET /subjects

- Request parameters

None

- Response parameters


| Parameter Name| Parameter Type| Parameter Description|
| -------- | --------- | --------------- |
| name | JsonArray | Subject name list |

- Error code.

    401:

    40101 - Unauthorized Error

    500:

    50001 - Storage Service Error

- Sample request


```shell
curl -X GET http://localhost:8081/subjects
```

- Response Example


```json
{ "name": ["subject1", "subject2"] }
```


#### 7.2.2 Obtaining Subject Definitions

- URL


GET /subjects/(string: subject)

- Request parameters

|Parameter name|Parameter type|Required or not|Parameter description|
| -------- | -------- | -------- | ----------- |
| subject | string | Required | Subject name |

- Response parameters


| Parameter Name| Parameter Type| Parameter Description|
| ------------- | -------- | ---------------------- |
| subject | string | Subject name |
| namespace | string | Namespace |
| tenant | string | Tenant |
| app | string | Application |
| compatibility | string | Compatibility setting |
| coordinate | string | coordinate |
| status | string | Status |
| description | string | description |
| createdTime    | string   |  the time when a subject was registered    |
| lastModifiedTime    | string     |  the time when a subject was last updated    |

- Error code.

  401:

  40101 - Unauthorized Error

  404:

  40401: The corresponding openschema information does not exist.

  500:

  50001 - Storage Service Error

- Sample request


```shell
curl -X GET http://localhost:8081/subjects/test-value
```

- Response Example


```json
{
    "subject": "test-topic",
    "namespace": "org.apache.rocketmq",
    "tenant": "messaging/rocketmq",
    "app": "rocketmq",
    "description": "JSON",
    "compatibility": "NONE",
    "createdTime": "2021-09-14T02:26:09.018",
    "lastModifiedTime": "2021-09-15T02:26:09.018"
}
```

#### 7.2.3 Obtaining All Versions of a Subject

- URL


GET /subjects/(string: subject)/versions

- Request parameters

|Parameter name|Parameter type|Required or not|Parameter description|
| -------- | -------- | -------- | ----------- |
| subject | string | Required | Subject name |

- Response parameters


| Parameter Name| Parameter Type| Parameter Description|
| -------- | -------- | -------- |
| version | int | Version number |

- Error code.

    401:

    40101 - Unauthorized Error

    404:

    40401: The corresponding openschema information does not exist.

    500:

    50001 - Storage Service Error

- Sample request


```shell
curl -X GET http://localhost:8081/subjects/test-value/versions
```

- Response Example


```json
{ "version": [1, 2, 3, 4] }
```

#### 7.2.4 Obtaining Schema Definitions Based on Subject and Schema Version

- URL


GET /subjects/(string: subject)/versions/(version: version)/schema

- Request parameters

|Parameter name|Parameter type|Required or not|Parameter description|
| -------- | -------- | -------- | ------------ |
| subject | string | Required | Subject name |
| version | int | Required | Schema version number |

- Response parameters


| Parameter Name| Parameter Type| Parameter Description|
| ------------- | -------- | ---------------------- |
| subject | string | Subject name |
| namespace | string | Namespace |
| tenant | string | Tenant |
| app | string | Application |
| compatibility | string | Compatibility setting |
| coordinate | string | coordinate |
| status | string | Status |
| description | string | description |
| createdTime    | string   |  the time when a subject was registered    |
| lastModifiedTime    | string     |  the time when a subject was last updated    |
| schema | JSON | Refer to the schema definition |

- Error code.

  401:

  40101 - Unauthorized Error

  404:

  40401: The corresponding openschema information does not exist.

  40402 - The version does not exist.

  500:

  50001 - Storage Service Error

- Sample request


```shell
curl -X GET http://localhost:8081/subjects/test-value/versions/1/schema
```

- Response Example


```json
{
    "subject": "test-topic",
    "namespace": "org.apache.rocketmq",
    "tenant": "messaging/rocketmq",
    "app": "rocketmq",
    "description": "rocketmq user information",    
    "compatibility": "NONE",
    "createdTime": "2021-09-14T02:26:09.018",
    "lastModifiedTime": "2021-09-15T02:26:09.018",
    "format": "AVRO",
    "schema": {
        "version": 1,
        "id": "20",
        "serialization": "PB",        
        "schemaDefinition": [
            {
                "name": "id",
                "type": "string"
            },
            {
                "name": "amount",
                "type": "double"
            }
        ],
        "validator": "a.groovy",
        "comment": "rocketmq user information"
    }
}
```


#### 7.2.5 Create or Modify a Subject

If the same subject does not exist, create a subject.

If yes, modify the related attributes of existing subject.

- URL


POST /subjects/(string: subject)/

- Request parameters

|Parameter name|Parameter type|Required or not|Parameter description|
| ------------- | -------- | -------- | ----------- |
| tenant | string | Required | Tenant |
| namespace | string | Required | Namespace |
| subject | string | Required | Subject name |
| app | string | | Home app |
| description | string | | description |
| status | string | Required | Status |
| compatibility | string | | Compatibility setting |
| coordinate | string | | Maven coordinate |

- Response parameters

| Parameter Name| Parameter Type| Parameter Description|
| ------------- | -------- | ----------- |
| tenant | string | Tenant |
| namespace | string | Namespace |
| subject | string | subject name |
| app | string | Home app |
| description | string | description |
| status | string | Status |
| compatibility | string | Compatibility policy |
| coordinate | string | Maven coordinate |
| createdTime    | string   |  the time when a subject was registered    |
| lastModifiedTime    | string   |  the time when a subject was last updated    |

- Error code.

  401:

  40101 - Unauthorized Error

  422:

  42201 - Incorrect format

  500:

  50001 - Storage Service Error

  50002 - Timeout

- Sample request


```shell
curl -X POST -H "Content-Type: application/vnd.openschema.v1+json" \
http://localhost:8081/subjects/test-value/ --data'
{
    "subject": "test-topic",
    "namespace": "org.apache.rocketmq",
    "tenant": "messaging/rocketmq",
    "app": "rocketmq",
    "description": "rocketmq user information",
    "compatibility": "NONE",
    "status": "deprecated"
}'
```

- Response Example


```json
{
    "subject": "test-topic",
    "namespace": "org.apache.rocketmq",
    "tenant": "messaging/rocketmq",
    "app": "rocketmq",
    "description": "rocketmq user information",
    "compatibility": "NONE",
    "createdTime": "2021-09-14T02:26:09.018",
    "lastModifiedTime": "2021-09-15T02:26:09.018",
    "status": "deprecated"
}
```

#### 7.2.6 Create and Register New Schemas

If the same definition already exists, the current schema ID is returned.

If no, check new schema against the subject's compatibility setting, create a new schema, and return the new ID.

- URL


POST /subjects/(string: subject)/versions

- Request parameters

|Parameter name|Parameter type|required or not|Parameter description|
| -------- | -------- | -------- | -------------- |
| subject | string | required | Subject name |
| schema | JSON | required | Refer to the schema definition |

- Response parameters


| Parameter Name| Parameter Type| Parameter Description|
| -------- | -------- | --------- |
| id | string | schema ID |

- Error code.

    401:

    40101 - Unauthorized Error

    409:

    40901 - Compatibility Error

    422:

    42201 - Incorrect format

    500:

    50001 - Storage Service Error

    50002 - Timeout

- Sample request


```shell
curl -X POST -H "Content-Type: application/vnd.openschema.v1+json" \
http://localhost:8081/subjects/test-value/versions --data'
{
    "serialization": "PB",    
    "schemaDefinition": [
        {
            "name": "id",
            "type": "string"
        },
        {
            "name": "amount",
            "type": "double"
        }
    ]
}'
```

- Response Example


```json
{"id":"10"}
```


#### 7.2.7 Delete the subject, compatibility settings along with all versions of schemas belong to this subject.

- URL


DELETE /subjects/(string: subject)

- Request parameters

|Parameter name|Parameter type|Required or not|Parameter description|
| -------- | -------- | -------- | ----------- |
| subject | string | Required | Subject name |

- Response parameters


| Parameter Name| Parameter Type| Parameter Description|
| -------- | -------- | -------- |
| version | int | Version number |

- Error code.

  401:

  40101 - Unauthorized Error

  404:

  40401: The corresponding openschema information does not exist.

  500:

  50001 - Storage Service Error

- Sample request


```shell
curl -X DELETE http://localhost:8081/subjects/test-value
```

- Response Example


```json
{ "version": [1, 2, 3, 4] }
```

#### 7.2.8 Delete a Specific Schema Version of a Subject

- URL


DELETE /subjects/(string: subject)/versions/(version: version)

- Request parameters

|Parameter name|Parameter type|Required or not|Parameter description|
| -------- | -------- | -------- | ----------- |
| subject | string | Required | Subject name |
| version | int | Required | Version number |

- Response parameters

| Parameter Name| Parameter Type| Parameter Description|
| -------- | -------- | -------- |
| version | int | Version number |

- Error code.

    401:

    40101 - Unauthorized Error

    404:

    40401: The subject does not exist.

    40402-The version does not exist.

    500:

    50001 - Storage Service Error

- Sample request


```shell
curl -X DELETE http://localhost:8081/subjects/test-value/versions/1
```

- Response Example


```json
{ "version": 1 }
```


### 7.3 Compatibility related APIs

#### 7.3.1 Testing if new schema is compatible against Compatibility Setting of this Subject

- URL


​ POST /compatibility/subjects/(string: subject)/versions/(version: version)

- Request parameters

|Parameter name|Parameter type|Required or not|Parameter description|
| -------- | -------- | -------- | ----------- |
| subject | string | Required | Subject name |
| version | int | Required | Version number |
| schema | json | Required | |

- Response parameters

| Parameter Name| Parameter Type| Parameter Description|
| ------------- | -------- | -------- |
| isCompatible | boolean | Compatible |

- Error code.

    401:

    40101 - Unauthorized Error

    404:

    40401: The subject does not exist.

    40402-The version does not exist.

    422: The format is incorrect.

    42201: Schema format error

    42202: The version format is incorrect.

    500:

    50001 - Storage Service Error

- Sample request


```shell
curl -X POST -H "Content-Type: application/vnd.openschema.v1+json" \
--data'{"schema": "{"type": "string"}"}'\
http://localhost:8081/compatibility/subjects/test-value/versions/latest
```

- Response Example


```json
{"isCompatible": true}
```



#### 7.3.2 Obtaining Compatibility Setting

- URL


GET /config/(string: subject)

- Request parameters

|Parameter name|Parameter type|Required or not|Parameter description|
| -------- | -------- | -------- | ----------- |
| subject | string | Required | Subject name |

- Response parameters

| Parameter Name| Parameter Type| Parameter Description|
| ------------- | -------- | -------- |
| Compatibility | string | Compatibility |

- Error code.

    401:

    40101 - Unauthorized Error

    404:

    40401: The subject does not exist.

    500:

    50001 - Storage Service Error

- Sample request


```shell
curl -X GET -H "Content-Type: application/vnd.openschema.v1+json" \
http://localhost:8081/config/test-value
```

- Response Example


```json
{"compatibility": "FULL"}
```



#### 7.3.3 Update Compatibility Setting

- URL


PUT /config/(string: subject)

- Request parameters

|Parameter name|Parameter type|Required or not|Parameter description|
| ------------- | -------- | -------- | ----------- |
| subject | string | Required | Subject name |
| compatibility | string | | Compatibility |

- Response parameters

| Parameter Name| Parameter Type| Parameter Description|
| ------------- | -------- | -------- |
| compatibility | string | compatibility |

- Error code.

    401:

    40101 - Unauthorized Error

    404:

    40401: The subject does not exist.

    422:

    42201 - Compatibility format error

    500:

    50001 - Storage Service Error

- Sample request


```shell
curl -X PUT -H "Content-Type: application/vnd.openschema.v1+json" \
--data '{"compatibility": "NONE"}'\
http://localhost:8081/config/test-value
```

- Response Example


```json
{"compatibility": "NONE"}
```