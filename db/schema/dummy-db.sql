DROP DATABASE IF EXISTS dummydb;
CREATE DATABASE dummydb;
USE dummydb;

DROP TABLE IF EXISTS users;

CREATE TABLE users (
  id INT NOT NULL AUTO_INCREMENT,
  name VARCHAR(20) NOT NULL,
  surname VARCHAR(20) NOT NULL,
  PRIMARY KEY (id)
 );
 
INSERT INTO users(name,surname)VALUES('david','perez');
INSERT INTO users(name,surname)VALUES('jorge','fernandez');
INSERT INTO users(name,surname)VALUES('patricia','ramirez');