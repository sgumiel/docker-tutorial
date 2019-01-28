# Docker-tutorial

En este tutorial vamos a ejecutar una aplicación que consta de una base de dates de usuarios y un back con el que consultar estos usuarios y dar de alta nuevos usuarios.

* En una primera aproximación la base de datos la levantaremos en un contenedor docker y la aplicación back la ejecutaremos en local.
* En una segunda aproximación tanto la base de datos como la aplicación las levantaremos en un contenedor docker cada una (2 contenedores).
* En una tercera aproximación levantaremos la base de datos y la aplicación con docker-compose.
* En una cuarta aproximación levantaremos la base de datos en kubernetes y la aplicación en local
* En una quinta aproximación levantaremos tanto la base de datos como el back en kubernetes.

> Para la base de datos utilizaremos una imagen de la base de datos [MariaDB](https://mariadb.org/download/)

> Todas las rutas en los comandos docker deben ser absolutas. En el tutorial las pondremos relativas al path del proyecto.

## PRIMERA APROXIMACIÓN
(Base de datos en contenedor docker y aplicación en local)

Para ejecutar la base de datos en un contenedor, lo primero que tenemos que hacer es crear nuestra propia imagen de MariaDB.
Esto lo hacemos porque queremos que al arrancar la base de datos, ya existe un esquema, tablas, etc.
Nos colocamos en la carpeta db y ejecutamos el siguiente comandos.
```shell
docker build -t dummydb .
```
> Con este comando creamos nuestra propia imagen de MariaDB con el tag 'dummydb'.

> Para entender como se crea el esquema, tablas, etc mirar la documentacion de MariaDB

Una vez tenemos creada nuestra imagen de la base de datos, levantaremos la base de datos en un contenedor docker.
```shell
docker run -d --name dummydb -e MYSQL_ROOT_PASSWORD=mariadb -p 3306:3306 dummydb:latest
```

> Para entender el comando run de docker, ver la documentacion [docker run](https://docs.docker.com/engine/reference/commandline/run/)

> Una breve explicación de los parámetros:
* -d -> Ejecutar en detach mode (Ejecutar en background)
* --name -> Dar un nombre al contenedor
* -e -> Pasar al contenedor una variable en entorno (En este caso la variable es 'MYSQL_ROOT_PASSWORD' y el valor 'mariadb')
* -p -> Indicar que puerto expone el contenedor

Para arrancar la aplicación en local, el código está preparado para que los archivos de configuración de la aplicación
estén externamente. Esto es así porque en el caso de que un dato de configuración cambiase, no sería necesario volver
a crear la imagen de la aplicación. Si para esta  primera aproximación te sientes mas cómodo con los archivos de
configuración dentro del proyecto, basta con copiar los archivos de la carpeta config/local en la carpeta resources del proyecto.

Si deseas mantener la configuración fuera del proyecto, al arrancar la aplicación debes configurar 2 variables en la VM.

* -Dspring.config.location=config/local/
* -Dlogging.config=config/local/logback.xml

Ahora vamos a jugar un poco con la aplicación a ver si funciona correctamente. Utilizaremos [Postman](https://www.getpostman.com/) para realizar las peticiones a la aplicación.

* Hacemos una petición GET a http://localhost:8080/users y nos debería devolver 3 usuarios.
* Hacemos una petición POST a http://localhost:8080/users { name: "name", surname: "surname" }
* Hacemos una petición GET a http://localhost:8080/users y nos debería devolver 4 usuarios.

Vemos que todo funciona bien. Sería interesante que si la base de datos se detuviera, los datos siguieran siendo los mismos ¿verdad?. Vamos a parar y eliminar el contenedor de
la base de datos y a volver a crearlos a ver que ocurre.

Para parar y borrar el contenedor de la base de datos ejecutamos los siguientes comandos
``` shell
docker stop dummydb
docker rm dummydb
```

Volvemos a arrancar la base de datos de la misma forma que anteriormente
```shell
docker run -d --name dummydb -e MYSQL_ROOT_PASSWORD=mariadb -p 3306:3306 dummydb:latest
```

* hacemos una petición GET a http://localhost:8080/users y nos debería devolver 3 usuarios.

¿Qué esta pasando? ¿Por qué me devuelve 3 usuarios en lugar de los 4 de antes ya que había creado uno?

Esto es debido a que al arrancar la base de datos no se le ha dicho ningún volumen, por lo que cada vez que se crea el contenedor, es como si se reiniciase la base de datos
a su estado inicial que marca el archivo db/schema/dummy-db.sql

Para que los datos se puedan persistir y no se pierdan con las paradas y borrados de contenedores de la base de datos vamos a crear un volumen (manejado por docker) y al
arrancar el contendor de la base de datos le diremos que utilice ese volumen

Paramos y borramos el actual contenedor de base de datos
``` shell
docker stop dummydb
docker rm dummydb
```

Para crear el volumen ejecutaremos
```shell
docker volume create dummydbvol
```

Creamos el contenedor de base de datos utilizando el volumen que acabamos de crear
```shell
docker run -d --name dummydb -e MYSQL_ROOT_PASSWORD=mariadb -p 3306:3306 -v dummydbvol:/var/lib/mysql dummydb:latest
```

> Le pasamos un nuevo parámetro al comando run. El parámetro -v indica que volumen utilizar (en este caso 'dummydb') y donde montarlo dentro del contenedor (en este caso '/var/lib/mysql')

* Hacemos una petición GET a http://localhost:8080/users y nos debería devolver 3 usuarios.
* Hacemos una petición POST a http://localhost:8080/users { name: "name", surname: "surname" }
* Hacemos una petición GET a http://localhost:8080/users y nos debería devolver 4 usuarios.

Al igual que antes. La novedad ahora es que al parar y borrar el contenedor. Lo volvemos a crear y montamos el mismo volumen, y la base de datos está en el mismo estado en el que estaba
antes de parar y borrar el anterior contenedor

Paramos y borramos el actual contenedor de base de datos
``` shell
docker stop dummydb
docker rm dummydb
```

Volvemos a crear el contenedor de base de datos con el volumen
``` shell
docker run -d --name dummydb -e MYSQL_ROOT_PASSWORD=mariadb -p 3306:3306 -v dummydbvol:/var/lib/mysql dummydb:latest
```

* Hacemos una petición GET a http://localhost:8080/users y nos debería devolver 4 usuarios.

## SEGUNDA APROXIMACIÓN
(Base de datos y aplicación en contenedores docker)

Si hemos elegido antes la opción de que los archivos de configuración estuvieran **dentro** de la aplicación, en este punto borraremos estos archivos y continuaremos con la configuración externa.

Vamos a crear una imagen de la aplicación back. Para ello, nos colocamos en el directorio raíz del proyecto java dummy-back (donde se encuentra el fichero Dockerfile) y ejecutamos el comando
```shell
docker build -t dummyback .
```
> Hemos creado una imagen con el tag 'dummyback'

Arrancamos un contenedor con la aplicación back.
```shell
docker run -d --name dummyback -p 8080:8080 --link dummydb:dummydb -v config/docker:/config dummyback
```
> * El parámetro --link sirve para que el contenedor de la aplicación pueda usar la red 'dummydb' que se crea por defecto cuando arrancamos el contenedor de la base de datos.
* El parámetro -v lo usamos para montar el volumen para la configuración de la aplicación.

* Hacemos una petición GET a http://localhost:8080/users y nos debería devolver 4 usuarios.

## TERCERA APROXIMACIÓN
(Usaremos docker-compose para ejecutar los contenedores de base de datos y aplicación)

Paramos y borramos los contenedores de base de datos y la aplicación
```shell
docker stop dummydb
docker rm dummydb
docker stop dummyback
docker rm dummyback
```

Configuramos el fichero docker-compose.yml en funcion de nuestras rutas
* La línea 15 tiene que apuntar a nuestra carpeta schema del repositorio
* La Línea 35 tiene que apuntar a nuestra carpeta config/docker del repositorio

Nos colocamos en la carpeta docker-compose y ejecutamos el siguiente comando
```shell
docker-compose up
```
> Para ejecutar docker-compose en background ejecutarlo con el parámetro -d

Para parar los contenedores ejecutar
```shell
docker-compose down
```

## CUARTA APROXIMACIÓN
(Levantaremos la base de datos en kubernetes y la aplicación en local)

Lo primero que tenemos que hacer para empezar a usar kubernetes es hacer que docker apunte al registry de kubernetes, para eso lanzamos el siguiente comandos
```shell
eval $(minikube docker-env)
```

Para comprobar si se ja ejecutado correctamente, lazamos docker images. Deberiamos de ver imágenes de kubernetes.
Construimos una nueva imagen de la base de datos y la tagueamos con la version 1.0.0. Nos colocamos en la carpeta docker-tutorial/db y lanzamos
```shell
docker build -t mariadb-users:1.0.0 .
```

Comprobamos que se haya creado con el comando
```shell
docker images
```

Para desplegar la base de datos en kubernetes utilizaremos los ficheros de despliegue. Nos movemos a la carpeta kubernetes-tutorial y lanzamos
```shell
kubectl create -f mariadb-users.yaml
kubectl create -f mariadb-users-svc.yaml
kubectl port-forward pods/mariadb-users-74c88cffb-rjvtd 3306:3306 &
```
> Para saber el identificador del pod dentro de kubernetes ejecutar kubectl get pods

* Hacemos una petición GET a http://localhost:8080/users y nos debería devolver 3 usuarios.
* Hacemos una petición POST a http://localhost:8080/users { name: "name", surname: "surname" }
* Hacemos una petición GET a http://localhost:8080/users y nos debería devolver 4 usuarios.

Borramos el servicio y el pod de base de datos con los comandos
```shell
kubectl delete -f mariadb-users-svc.yaml
kubectl delete -f mariadb-users.yaml
```

Matamos también el proceso del port-forward (la única forma que se, debe de haber alguna con el comando kubectl)
```shell
netstat -putonl | grep 3306
kill -9 proceso_id
```

Paramos también la aplicación back, y volvemos a levantarlo todo como antes.

* Hacemos una petición GET a http://localhost:8080/users y nos debería devolver 3 usuarios.

Esto ocurre porque no hemos usado ningún volumen persistente para la base de datos en kubernetes.

## QUINTA APROXIMACIÓN
(Levantaremos la base de datos y la aplicación en kubernetes)

Creamos la imagen de la aplicación con el tag 1.0.0
```shell
docker build -t dummyback:1.0.0 .
```
