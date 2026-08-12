# File Management System

This is a backend project for a file management system.

## Features

This project provides hierarchical folder organization and role-based file sharing and retrieval.

Users can create their own folder and file hierarchies. Each user's hierarchy is independent and only affects their own file organization; it does not affect other users. Users can share files with other users, but folders cannot be shared.

There are three types of file permissions: **OWNER**, **WRITE**, and **READ**, with the following allowed operations:

|Permission|OWNER|WRITE|READ|
|---|---|---|---|
|Read|v|v|v|
|Rename|v|v|x|
|Delete|v|v|x|
|Share|v|x|x|
|Revoke other's access|v|x|x|
|Remove own access|v|v|v|

The project uses **MinIO** as the file storage platform. File upload and download operations use MinIO presigned URLs.

Files are stored using the following path:

```text
{main-bucket}/users/{userId}/{fileUuid}.{extension}
```

## Technologies
The project is built with:
* Java 26
* Spring Boot 4.1.0
* PostgreSQL
* MinIO

## Configuration
To run the project, first create a PostgreSQL database and a MinIO instance. Then configure the database and MinIO credentials in `application.yaml`:

```yaml
spring:
  datasource:
    url:
    username:
    password:

minio:
  endpoint:
  access-key:
  secret-key:
  bucket-name:
```

## Testing the APIs
The registration function has not been implemented yet. For testing purposes, add a user with a username and a BCrypt-encrypted password to the database.

To test the APIs, open the Swagger UI:

```text
http://localhost:8080/swagger-ui/index.html
```

Use the login API to generate a JWT token, then click the Authorize button in the upper-right corner of Swagger UI and paste the token there.
