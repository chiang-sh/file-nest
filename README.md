# File Management System

This is a backend project for a file management system.

## Features

This project provides hierarchical folder organization and role-based file sharing and retrieval.

There are three types of file permissions: **OWNER**, **WRITE**, and **READ**, with the following allowed operations:

|Permission|OWNER|WRITE|READ|
|---|---|---|---|
|Read|v|v|v|
|Rename|v|v|x|
|Delete|v|v|x|
|Share|v|x|x|
|Revoke other's access|v|x|x|
|Remove own access|v|v|v|

Folder records are user-specific, so each user's folder hierarchy is independent and does not affect other users. Each uploaded file has a single file record, regardless of how many users it is shared with. Files are displayed in each user's file list based on the permissions assigned to that user in the permission table.

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
http://{host}:{port}/swagger-ui/index.html
```

Use the login API to generate a JWT token, then click the Authorize button in the upper-right corner of Swagger UI and paste the token there.
