# Moera Naming Server

## Resources

* Live network: https://web.moera.org
* Read more about Moera at https://moera.org
* Learn more about Moera naming servers: http://moera.org/overview/naming.html
* Bugs and feature requests: https://github.com/MoeraOrg/moera-issues/issues
* How to set up a complete Moera Development Environment:
  http://moera.org/development/development-environment.html

## Installation instructions

1. As prerequisites, you need to have Java 17+ and PostgreSQL 9.6+
   installed. In all major Linux distributions, you can install them from
   the main package repository.
2. Create a PostgreSQL user `<username>` with password `<password>` and
   an empty database `<dbname>` owned by this user (see [detailed instructions][1]).
3. Go to the source directory.
4. Create `application-dev.yml` file with the following content:
   
   ```yaml
   spring:
     datasource:
       url: jdbc:postgresql:<dbname>?characterEncoding=UTF-8
       username: <username>
       password: <password>
     flyway:
       user: <username>
       password: <password>
   ```
5. By default, the server runs on port 8080. If you want it to run on a
   different port, add these lines to the file above:
    
   ```yaml
   server:
     port: <port number>
   ```
6. Execute `./run` script.

[1]: https://moera.org/administration/installation/create-db.html
