# docker-tutorial
PRIMERA APROXIMACION (contenedor base de datos aplicacion back en local)

- crear imagen de mariadb con la base de datos preestablecida con un esquema
* Colocarse en el directorio db y ejecutar el siguiente comando
 docker build -t dummydb .
 
 - arrancar la imagen que acabamos de crear
docker run -d --name dummydb -e MYSQL_ROOT_PASSWORD=mariadb -p 3306:3306 dummydb:latest

- configuramos el dummy back para que tire contra esa base de datos
* configuramos el logback y el application.properties para que tiren de la carpeta config
* la variable local-config apunta al directorio config/local del repositorio
-Dspring.config.location=${local-config}/
-Dlogging.config=${local-config}/logback.xml

- hacemos una peticion GET a http://localhost:8080/users y nos deberia devolver 3 usuarios.
- hacemos una peticion POST a http://localhost:8080/users { name: "name", surname: "surname" }
- hacemos una peticion GET a http://localhost:8080/users y nos deberia devolver 4 usuarios.

- paramos y borramos el contenedor de base de datos
docker stop dummydb
docker rm dummydb

- volvemos a arrancar el contenedor de base de datos
docker run -d --name dummydb -e MYSQL_ROOT_PASSWORD=mariadb -p 3306:3306 -d dummydb:latest

- hacemos una peticion GET a http://localhost:8080/users y nos deberia devolver 3 usuarios.
¿Por qué aparecen 3 y no 4?  Porque al arrancar la base de datos no le estamos dando un 'volumen' por lo que los datos se pierden cuando el contenedor se borra

- borramos el contenedor de base de datos
docker stop dummydb
docker rm dummydb

- creamos un volumen (manejado por docker) para la base de datos
docker volume create dummydbvol

- creamos contenedor de base de datos con este volumen
docker run -d --name dummydb -e MYSQL_ROOT_PASSWORD=mariadb -p 3306:3306 -v dummydbvol:/var/lib/mysql dummydb:latest


- hacemos una peticion GET a http://localhost:8080/users y nos deberia devolver 3 usuarios.
- hacemos una peticion POST a http://localhost:8080/users { name: "name", surname: "surname" }
- hacemos una peticion GET a http://localhost:8080/users y nos deberia devolver 4 usuarios.

- paramos y borramos el contenedor de base de datos
docker stop dummydb
docker rm dummydb

- volvemos creamos contenedor de base de datos con este volumen
docker run -d --name dummydb -e MYSQL_ROOT_PASSWORD=mariadb -p 3306:3306 -v dummydbvol:/var/lib/mysql dummydb:latest

- hacemos una peticion GET a http://localhost:8080/users y nos deberia devolver 4 usuarios.

SEGUNDA APROXIMACION (contenedor base de datos y contenedor aplicacion back)

- creamos imagen de la aplicacion back
* Colocarse en el directorio donde se encuentra en arcivo Dockerfile (de la aplicacion, en este caso en el raiz del proyecto)
docker build -t dummyback .

- arrancamos el contenedor de la aplicacion back
* Colocarse en el directorio config del repositorio
docker run -d --name dummyback -p 8080:8080 --link dummydb:dummydb -v $(pwd)/docker:/config dummyback

- ejecutar el siguiente commando hasta que se vea que la aplicacion ha arrancado
docker logs dummyback

- hacemos una peticion GET a http://localhost:8080/users y nos deberia devolver 4 usuarios.

TERCERA APROXIMACION (levantar contenedor base de datos y back con docker-compose)

- paramos y borramos el contenedor de base de datos y la aplicacion
docker stop dummydb
docker rm dummydb
docker stop dummyback
docker rm dummyback

- configuramos el fichero docker-compose.yml en funcion de nuestras rutas
* la linea 15 tiene que apuntar a nuestra carpeta schema del repositorio
* la linea 35 tiene que apuntar a nuestra carpeta config/docker del repositorio

- arrancamos los contenedores con docker-compose
* Colocarse en el directorio docker-compose del repositorio
docker-compose up
(tambien se puede arrancar con docker-compose up -d, pero no veremos arrancar el back)

- paramos los contenedores
docker-compose down



