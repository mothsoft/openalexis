OpenAlexis
================

OpenAlexis is a suite of web-based tools for text mining and predictive analytics.

OpenAlexis is licensed under the terms of the Apache License v2.0. See LICENSE for details.

Building
==============
This software is built with Maven. Issue 'mvn clean install -Dmaven.test.skip=true' at the root of your checkout. Unit tests generally work, but integration tests with dependence on other systems require some bootstrapping.

Installation
==============
* Complete installation of https://github.com/mothsoft/stanford-nlp-war
* Create file alexis.properties in the root package of a location accessible to servlet container's shared class loader (example: TOMCAT_HOME/shared/classes)
    * Override any settings from alexis-service-impl/src/main/resources/com/mothsoft/alexis/service/alexis.properties that differ from your environment
    * You will likely need to override filesystem properties (values starting /home/alexis) and Twitter integration settings
        * alexis.activemq.directory
        * alexis.models.directory
        * hibernate.search.default.indexBase
        * oauth.consumerKey, oauth.consumerSecret, oauth.accessToken, oath.accessTokenSecret
* Push alexis-ui-war/target/alexis.war to servlet container
* Push alexis-service-war/target/api.war to servlet container
* Create starter database by importing alexis-domain/alexis.ddl into MySQL
* Make sure data source jdbc/alexis is available to web apps (already set up for Tomcat in META-INF/context.xml)
* Sample user creation (admin/admin): 
    * insert into user(username, salt, is_admin) values ('admin', sha2(rand(), 256), 1);
    * update user set hashed_password = sha2(CONCAT('admin', '{', salt, '}'), 256) where username = 'admin';

Executing
=============
* Start servlet container on port 8080 (or modify following instructions accordingly)
* Navigate to http://localhost:8080/alexis
* Login as admin/admin
