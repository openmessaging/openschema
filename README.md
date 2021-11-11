# OpenSchema
This project propose a specification for data schema when exchanging the message and event in more and more modern cloud-native applications. Any vendor could provide a serving layer base on this spec, designing a RESTful interface for storing and retrieving such as Avro®, JSON Schema, and Protobuf3 schemas. Implementations like that already exist, such as Schema Registry, you could store a versioned history of all schemas based on a specified subject name strategy, provides multiple compatibility settings and allows evolution of schemas according to the configured compatibility settings and expanded support for these schema types. 

Nowadays, it is still in the rapid development process. We released the preview version 1.0.0-preview. The schema registry of Spring and Confluent can be regarded as an implementation of it. Currently, communities such as Apache Eventmesh, Apache RocketMQ are actively adapting it. If you have any improvment suggestion, please do not hesitate to issue us.

## Releases
[1.0.0-preview](https://github.com/openmessaging/openschema/blob/master/spec_en.md)

## Quick Start

[SchemaRegistry](https://github.com/openmessaging/schemaregistry) is the reference implementation for OpenSchema, that you can rely on in your project. We would like to hear any feedback from the community.
