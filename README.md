# Moera Naming Server

Read more about Moera at https://moera.org

Learn more about Moera naming servers: http://moera.org/overview/naming.html

Bugs and feature requests: https://github.com/MoeraOrg/moera-issues/issues

How to setup a complete Moera Development Environment: http://moera.org/development/setup/index.html

Installation instructions:

1. As prerequisites you need to have Java 11+ and PostgreSQL 9.6+
   installed. In all major Linux distributions you can install them from
   the main package repository.
2. You need to have [moera-commons][1] installed.
3. Create a PostgreSQL user `<username>` with password `<password>` and
   an empty database `<dbname>` owned by this user (see detailed
   instructions here:
   http://moera.org/development/setup/create-db.html).
4. Go to the source directory.
5. Create `src/main/resources/application-dev.yml` with the following
   content:
   
   ```yaml
   spring:
     datasource:
       url: jdbc:postgresql:<dbname>?characterEncoding=UTF-8
       username: <username>
       password: <password>
   ```
6. By default, the server runs on port 8080. If you want it to run on a
   different port, add these lines to the file above:
    
   ```yaml
   server:
     port: <port number>
   ```
7. Execute `./run` script.

[1]: https://github.com/MoeraOrg/moera-commons
