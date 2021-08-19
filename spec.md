<H2>一、综述

本篇提供了 OpenSchema 的相关元数据以及交互方式定义。




## 二、兼容性设计 

| 兼容性设置          | 说明                                                         |
| ------------------- | ------------------------------------------------------------ |
| BACKWARD(默认)      | Consumer使用新Schema可以读取由使用最新Schema的Producer发送的数据。 |
| BACKWARD_TRANSITIVE | Consumer可以使用新Schema读取由Producer使用所有以前注册过的Schema发送的数据。 |
| FORWARD             | Consumer使用最近的Schema可以读取由使用最新Schema的Producer发送的数据。 |
| FORWARD_TRANSITIVE  | Consumer可以使用所有注册过的Schema读取由Producer使用最新的Schema发送的数据。 |
| FULL                | 新Schema与最新注册的Schema向前和向后兼容。                   |
| FULL_TRANSITIVE     | 新Schema向前和向后兼容所有以前注册的Schema                   |
| NONE                | 禁用模式兼容性检查。                                         |



##  三、Content-Types 

OpenSchema REST服务器通过使用http+json的方式进行通信。

请求应通过HTTP Accept标头指定最具体的格式和版本信息，此外可以包括多个加权首选项：

> Accept:application/vnd.openschema.v1+json



##  四、ErrorCode 

所有的请求的HTTP返回保持跟HTTP标准统一，其中细化的错误码由返回的json字符串来决定，格式：

```json
{
	"error_code": 422,
	"error_message": "schema info cannot be empty"
}
```



##  五、Schema格式 

### 5.1 元信息 

元信息称之为 subject 。

| 元信息        | 含义                     | 示例                            |
| ------------- | ------------------------ | ------------------------------- |
| tenant        | 租户                     | org/apache/rocketmq/mybank      |
| namespace     | 命名空间                 | 集群名称, 如 rocketmq- cluster  |
| subject       | 元数据的名称             | 比如使用Topic名称作为元数据名称 |
| app           | 服务提供方的应用部署单元 |                                 |
| description   | 描述信息                 | 由申请人提供                    |
| status        | 元数据状态               | 比如已发布、已废弃等            |
| compatibility | 兼容性策略               | 无、向前兼容、向后兼容、全兼容  |
| coordinate    | Maven坐标                | 消息Payload的JAR的Maven坐标     |
| schema        | 数据格式                 | 关联的数据格式描述，详见下表    |

### 5.2 Schema定义 

Payload Schema用于描述消息的Payload数据。

| 元信息           | 含义                                                         | 实例                                                         |
| ---------------- | ------------------------------------------------------------ | ------------------------------------------------------------ |
| name             | payload名称，可空（比如消息的payload不需要名字）             |                                                              |
| id               | 全局唯一标识，用于确定该schema                               |                                                              |
| comment          | payload注释说明                                              |                                                              |
| serialization    | 序列化方式：hissian、json、pb、avro、user-defined            |                                                              |
| schemaType       | schema类型的枚举：NONE、JSON、PB、AVRO、USER-DEFINED、Int、Long、String、Map | 当消息都是没有提供Schema的，所以Schema类型都是NONE。 我们也可以给当前的消息加上Schema，比如用PB来描述RocketMQ 传输的数据的格式 |
| schemaDefinition | schema具体的内容，以一种方式来描述数据格式                   | NONE：无 PB：给出PB描述文件 AVRO：给出AVRO Schema内容 USER-DEFINED：给出用户自定义的信息 基础内容类型：无 |
| validator        | 值校验器                                                     | 对Schema描述的对象的值进行校验                               |
| version          | schema的版本信息                                             | 以消息为例，Payload可能会变，这个时候需要版本来标识区别不同的Schema |

示例：

```json
{
	"subject": "test-topic",
	"namespace": "org.apache.rocketmq",
	"tenant": "messaging/rocketmq",
	"app": "rocketmq",
	"description": "rocketmq user infomation",
	"compatibility": "NONE",
	"validator": "a.groovy",
	"comment": "Rocketmq user infomation",
	"schemaType": "AVRO",
	"schemaDefinition": [{
			"name": "id",
			"type": "string"
		},
		{
			"name": "age",
			"type": "short"
		}
	]
}
```



##  六、Subject与Topic的对应关系 

###  6.1 Subject和Topic的关系 

- 消息系统


Subject名称默认对于Topic名称，用于定义消息体的格式。可以通过后缀的方式进行扩展，${topic}-${suffix}，比如，在kafka中，一般使用Kafka-Key来用来定义kafka消息的中的key的数据格式。

- ﻿其他系统


由使用方自己负责解释。

###  6.2 Subject 和schema的关系 

- 一个subject包含多个版本的schema，是1:N的关系。

- subject层级提供兼容性设置，多个schema按照该设置演进。

- 全局唯一标识定义在schema中，由该id可以找到唯一的schema，是1:1的关系。

- subject+version同样可以唯一定位到一个schema。




##  七、REST 接口定义 

- **公共参数**

|              | 参数名称      | 参数类型 | 是否必选 | 参数说明 |
| ------------ | ------------- | -------- | -------- | -------- |
| 请求公共参数 | tenant        | string   | 非必选   | 租户     |
|              | namespace     | string   | 非必选   | 命名空间 |
| 返回公共参数 | error_code    | int      | 必选     | 错误码   |
|              | error_message | string   | 必选     | 错误解释 |

- **版本规则**

Schema的版本号默认采用递增的方式增加，可以使用latest替代版本号来获取最新版本的schema，但是latest在获取的那一刻可能会有新版本的schema产生。

例如，通过以下请求获取test-value下最新版本的schema定义：

```sh
curl -X GET http://localhost:8081/subjects/test-value/versions/latest/schema
```





### 7.1 Schema相关接口 

#### 7.1.1 根据ID获取schema详细信息

- URL


​	GET /schemas/ids/{string: id}

- 请求参数


| 参数名称 | 参数类型 | 是否必选 | 参数说明         |
| -------- | -------- | -------- | ---------------- |
| id       | string   | 是       | schema的唯一标识 |

- 响应参数


| 参数名称 | 参数类型 | 是否必选 | 参数说明             |
| -------- | -------- | -------- | -------------------- |
| schema   | JSON     | 否       | 返回具体的schema定义 |

- 错误码

  401：

  ​	40101 - 未授权错误

  404：

  ​	40401 - schema信息不存在

  500： 

  ​	50001 - 存储服务错误

- 请求示例


```shell
curl -X GET http://localhost:8081/schema/ids/1
```

- 响应示例


```json
{
	"version": 1,
	"id": "20",
	"serialization": "PB",
	"schemaType": "AVRO",
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



#### 7.1.2 根据ID获取所属的subject以及对应的版本号 

- URL


​	GET /schemas/ids/{string: id}/subjects

- 请求参数

| 参数名称 | 参数类型 | 是否必选 | 参数说明         |
| -------- | -------- | -------- | ---------------- |
| id       | string   | 是       | schema的唯一标识 |

- 响应参数


| 参数名称 | 参数类型 | 参数说明 |
| -------- | -------- | -------- |
| subject  | string   | 主题名称 |
| version  | int      | 版本     |

- 错误码

  401：

  ​	40101 - 未授权错误

  404：

  ​	40401 - schema信息不存在

  500： 

  ​	50001 - 存储服务错误

- 请求示例


```shell
curl -X GET http://localhost:8081/schemas/ids/1/subjects
```

- 响应示例


```json
[{"subject":"test-topic","version":1}]
```

 

### 7.2 Subject 相关接口 

#### 7.2.1 获取所有的subject 

- URL


​	GET /subjects

- 请求参数

  无

- 响应参数


| 参数名称 | 参数类型  | 参数说明        |
| -------- | --------- | --------------- |
| name     | JsonArray | subject名称列表 |

- 错误码

  401：

  ​	40101 - 未授权错误

  500： 

  ​	50001 - 存储服务错误

- 请求示例


```shell
curl -X GET http://localhost:8081/subjects
```

- 响应示例


```json
["subject1", "subject2"]
```

 

####  7.2.2 获取对应subject的所有版本 

- URL


​	GET /subjects/(string: subject)/versions

- 请求参数

| 参数名称 | 参数类型 | 是否必选 | 参数说明    |
| -------- | -------- | -------- | ----------- |
| subject  | string   | 必选     | subject名称 |

- 响应参数


| 参数名称 | 参数类型 | 参数说明 |
| -------- | -------- | -------- |
| version  | int      | 版本号   |

- 错误码

  401：

  ​	40101 - 未授权错误

  404：

  ​	40401 - subject信息不存在

  500： 

  ​	50001 - 存储服务错误

- 请求示例


```shell
curl -X GET http://localhost:8081/subjects/test-value/versions
```

- 响应示例


```json
[ 1, 2, 3, 4]
```

 

####  7.2.3 删除subject以及其对应所有版本的schema

- URL


​	DELETE /subjects/(string: subject)

- 请求参数

| 参数名称 | 参数类型 | 是否必选 | 参数说明    |
| -------- | -------- | -------- | ----------- |
| subject  | string   | 必选     | subject名称 |

- 响应参数


| 参数名称 | 参数类型 | 参数说明 |
| -------- | -------- | -------- |
| version  | int      | 版本号   |

- 错误码

  401：

  ​	40101 - 未授权错误

  404：

  ​	40401 - subject信息不存在

  500： 

  ​	50001 - 存储服务错误

- 请求示例


```shell
curl -X DELETE http://localhost:8081/subjects/test-value
```

- 响应示例


```json
[ 1, 2, 3, 4]
```

 

#### 7.2.4 获取subject定义 

- URL


​	GET /subjects/(string: subject)

- 请求参数

| 参数名称 | 参数类型 | 是否必选 | 参数说明    |
| -------- | -------- | -------- | ----------- |
| subject  | string   | 必选     | subject名称 |

- 响应参数


| 参数名称      | 参数类型 | 参数说明               |
| ------------- | -------- | ---------------------- |
| subject       | string   | subject名称subject名称 |
| namespace     | string   | 命名空间               |
| tenant        | string   | 租户                   |
| app           | string   | 所属应用               |
| compatibility | string   | 兼容性设置             |
| coordinate    | string   | 坐标                   |
| status        | string   | 状态                   |
| description   | string   | 描述                   |

- 错误码

  401：

  ​	40101 - 未授权错误

  404：

  ​	40401 - subject信息不存在

  500： 

  ​	50001 - 存储服务错误

- 请求示例


```shell
curl -X GET http://localhost:8081/subjects/test-value
```

- 响应示例


```json
{
	"subject": "test-topic",
	"namespace": "org.apache.rocketmq",
	"tenant": "messaging/rocketmq",
	"app": "rocketmq",
	"description": "JSON",
	"compatibility": "NONE"
}
```



####  7.2.5 根据subject以及schema版本获取schema定义 

- URL


​	GET /subjects/(string: subject)/versions/(version: version)/schema

- 请求参数

| 参数名称 | 参数类型 | 是否必选 | 参数说明     |
| -------- | -------- | -------- | ------------ |
| subject  | string   | 必选     | subject名称  |
| version  | int      | 必选     | schema版本号 |

- 响应参数


| 参数名称      | 参数类型 | 参数说明               |
| ------------- | -------- | ---------------------- |
| subject       | string   | subject名称subject名称 |
| namespace     | string   | 命名空间               |
| tenant        | string   | 租户                   |
| app           | string   | 所属应用               |
| compatibility | string   | 兼容性设置             |
| coordinate    | string   | 坐标                   |
| status        | string   | 状态                   |
| description   | string   | 描述                   |
| schema        | JSON     | schema的具体信息        |


- 错误码

  401：

  ​	40101 - 未授权错误

  404：

  ​	40401 - subject信息不存在

  ​	40402 - version不存在

  500： 

  ​	50001 - 存储服务错误

- 请求示例


```shell
curl -X GET http://localhost:8081/subjects/test-value/versions/1/schema
```

- 响应示例


```json
{
	"subject": "test-topic",
	"namespace": "org.apache.rocketmq",
	"tenant": "messaging/rocketmq",
	"app": "rocketmq",
	"description": "rocketmq user information",
	"compatibility": "NONE",
	"schema": {
		"version": 1,
		"id": "20",
		"serialization": "PB",
		"schemaType": "AVRO",
		"schemaDefinition": [{
			"name": "id",
			"type": "string"
		}, {
			"name": "amount",
			"type": "double"
		}],
		"validator": "a.groovy",
		"comment": "rocketmq user information"
	}
}
```



####  7.2.6 检查、注册Schema 

如果已有相同定义，则直接返回原有的id。

如果无相同定义，则检查兼容性设置，创建新的schema，返回新的id。

- URL


​	POST /subjects/(string: subject)/versions

- 请求参数

| 参数名称 | 参数类型 | 是否必选 | 参数说明       |
| -------- | -------- | -------- | -------------- |
| subject  | string   | 必选     | subject名称    |
| schema   | Json     | 必选     | 参考schema定义 |

- 响应参数


| 参数名称 | 参数类型 | 参数说明  |
| -------- | -------- | --------- |
| id       | string   | schema ID |

- 错误码

  401：

  ​	40101 - 未授权错误

  409:

  ​	40901 - 兼容性错误

  422:

  ​	42201 - 格式错误

  500： 

  ​	50001 - 存储服务错误

  ​	50002 - 超时

- 请求示例


```shell
curl -X POST -H "Content-Type: application/vnd.openschema.v1+json" \
http://localhost:8081/subjects/test-value/versions --data '
{
	"serialization": "PB",
	"schemaType": "AVRO",
	"schemaDefinition": [{
		"name": "id",
		"type": "string"
	}, {
		"name": "amount",
		"type": "double"
	}]
}'
```

- 响应示例


```json
{id":"10"}
```



####  7.2.7 新增、修改subject 

如果不存在相关的subject，则新增subject。

如果存在，则修改相关属性。

- URL


​	POST /subjects/(string: subject)/

- 请求参数

| 参数名称      | 参数类型 | 是否必选 | 参数说明    |
| ------------- | -------- | -------- | ----------- |
| tenant        | string   | 必选     | 租户        |
| namespace     | string   | 必选     | 命名空间    |
| subject       | string   | 必选     | subject名称 |
| app           | string   |          | 所属app     |
| description   | string   |          | 描述        |
| status        | string   | 必选     | 状态        |
| compatibility | string   |          | 兼容性策略  |
| coordinate    | string   |          | Maven坐标   |

- 响应参数

| 参数名称      | 参数类型 | 参数说明    |
| ------------- | -------- | ----------- |
| tenant        | string   | 租户        |
| namespace     | string   | 命名空间    |
| subject       | string   | subject名称 |
| app           | string   | 所属app     |
| description   | string   | 描述        |
| status        | string   | 状态        |
| compatibility | string   | 兼容性策略  |
| coordinate    | string   | Maven坐标   |

- 错误码

  401：

  ​	40101 - 未授权错误

  409：
   
   40901 - 兼容性错误

  422:

  ​	42201 - 格式错误

  500： 

  ​	50001 - 存储服务错误

  ​	50002 - 超时

- 请求示例


```shell
curl -X POST -H "Content-Type: application/vnd.openschema.v1+json" \
http://localhost:8081/subjects/test-value/ --data '
{
	"subject": "test-topic",
	"namespace": "org.apache.rocketmq",
	"tenant": "messaging/rocketmq",
	"app": "rocketmq",
	"description": "rocketmq user information",
	"compatibility": "NONE",
	"status": "deprecated"
}
'
```

- 响应示例


```json
{
	"subject": "test-topic",
	"namespace": "org.apache.rocketmq",
	"tenant": "messaging/rocketmq",
	"app": "rocketmq",
	"description": "rocketmq user information",
	"compatibility": "NONE",
	"status": "deprecated"
}
```



####  7.2.8 删除指定subject指定版本的schema 

- URL


​	DELETE /subjects/(string: subject)/versions/(version: version)

- 请求参数

| 参数名称 | 参数类型 | 是否必选 | 参数说明    |
| -------- | -------- | -------- | ----------- |
| subject  | string   | 必选     | subject名称 |
| version  | int      | 必选     | 版本号      |

- 响应参数

| 参数名称 | 参数类型 | 参数说明 |
| -------- | -------- | -------- |
| version  | int      | 版本号   |

- 错误码

  401：

  ​	40101 - 未授权错误

  404:

  ​	40401 - subject信息不存在

  ​	40402 - version信息不存在

  409：
   
   40901 - 兼容性错误

  500： 

  ​	50001 - 存储服务错误

- 请求示例


```shell
curl -X DELETE http://localhost:8081/subjects/test-value/versions/1
```

- 响应示例


```json
1
```



### 7.3 兼容性相关接口 

####  7.3.1 测试是否兼容 

- URL


​	POST /compatibility/subjects/(string: subject)/versions/(version: version)

- 请求参数

| 参数名称 | 参数类型 | 是否必选 | 参数说明    |
| -------- | -------- | -------- | ----------- |
| subject  | string   | 必选     | subject名称 |
| version  | int      | 必选     | 版本号      |
| schema   | json     | 必选     |             |

- 响应参数

| 参数名称      | 参数类型 | 参数说明 |
| ------------- | -------- | -------- |
| is_compatible | boolean  | 是否兼容 |

- 错误码

  401：

  ​	40101 - 未授权错误

  404:

  ​	40401 - subject信息不存在

  ​	40402 - version信息不存在

  422:格式错误

  ​	42201:  schema格式错误

  ​	42202：版本格式错误

  500： 

  ​	50001 - 存储服务错误

- 请求示例


```shell
curl -X POST -H "Content-Type: application/vnd.openschema.v1+json" \
--data'{"schema": "{"type": "string"}"}' \
http://localhost:8081/compatibility/subjects/test-value/versions/latest
```

- 响应示例


```json
{"is_compatible": true}
```



####  7.3.2 兼容性配置获取 

- URL


​	GET /config/(string: subject)

- 请求参数

| 参数名称 | 参数类型 | 是否必选 | 参数说明    |
| -------- | -------- | -------- | ----------- |
| subject  | string   | 必选     | subject名称 |

- 响应参数

| 参数名称      | 参数类型 | 参数说明 |
| ------------- | -------- | -------- |
| compatibility | string   | 是否兼容 |

- 错误码

  401：

  ​	40101 - 未授权错误

  404:

  ​	40401 - subject信息不存在

  500： 

  ​	50001 - 存储服务错误

- 请求示例


```shell
curl -X GET -H "Content-Type: application/vnd.openschema.v1+json" \
http://localhost:8081/config/test-value
```

- 响应示例


```json
{"compatibility": "FULL"}
```



####  7.3.3 兼容性配置更新 

- URL


​	PUT /config/(string: subject)

- 请求参数

| 参数名称      | 参数类型 | 是否必选 | 参数说明    |
| ------------- | -------- | -------- | ----------- |
| subject       | string   | 必选     | subject名称 |
| compatibility | string   |          | 兼容性      |

- 响应参数

| 参数名称      | 参数类型 | 参数说明 |
| ------------- | -------- | -------- |
| compatibility | string   | 兼容性   |

- 错误码

  401：

  ​	40101 - 未授权错误

  404:

  ​	40401 - subject信息不存在

  409：

   40901 - 兼容性错误

  422:

  ​	42201 - compatibility格式错误

  500： 

  ​	50001 - 存储服务错误

- 请求示例


```shell
curl -X PUT -H "Content-Type: application/vnd.openschema.v1+json" \
--data'{"compatibility": "NONE"}' \
http://localhost:8081/config/test-value
```

- 响应示例


```json
{"compatibility": "NONE"}
```
