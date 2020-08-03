# StudentSystem

A simple grade/assignment manager written in Java.

Details:
 - Uses JWTs for authentication
 - Spark framework
 - MySQL database
 - Complete with docker-compose
 
Environment variables:
 - `SIGNING_KEY`
    - Signing key used for JWTs
 - `ENFORCE_PERMISSIONS`
    - If present, server will only permit teachers to modify their own courses
 - `USE_HTTPS`
    - If present, cookies will be secure
 - `MYSQL_URI`
    - JDBC connection uri
 - `MYSQL_USER`
    - MySQL user to connect with
 - `MYSQL_PASSWORD`
    - MySQL password to connect with
 - `USER_ID` (docker-compose only)
    - User ID to run web server and database as
 - `BIND_PORT` (docker-compose only)
    - Port to bind web server to
 - `MYSQL_VOLUME` (docker-compose only)
    - Directory to mount the MySQL database volume to
