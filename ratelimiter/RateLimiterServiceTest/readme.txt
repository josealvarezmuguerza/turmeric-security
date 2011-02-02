This Is RL Integration test

The following is required
Database: jdbc:mysql://localhost/turmericdb
username: turmeric
password: turmeric

NOTE: This has a script located in src/test/sql/MysqlScript.sql, this Script will drop and create tables you my want to backup your database or change it.

To change it; edit the ff:
pom.xml its in plugin "sql-maven-plugin"
src/main/resources/META-INF/persistence.xml its in hibernate configuration


the running test is
src/test/resources/RateLimiterServiceV1-wsdl-soapui-project.xml

to view policies used, and you need soapui for it
src/test/resources/PolicyServiceV1-wsdl-soapui-project.xml

