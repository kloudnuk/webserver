# kloudnuk

A web service to make building data easy to access on the cloud. Kloudnuk achieves this by:

- Providing a process to securely enroll hardware development boards like Raspberry Pi.
- Deploying communication and management software directly into the device.
- Providing over-the-air software and security updates to the device programatically.
- Providing REST APIs for cloud data access, and remote device management.

## Publish image to Docker registry

```bash
docker login registry.gitlab.com
docker build --no-cache -t registry.gitlab.com/kloudnuk/kloudnuk .
docker push registry.gitlab.com/kloudnuk/kloudnuk
```

or if you want to specify a tag (other tan latest) use:

```bash
docker build --no-cache -t registry.gitlab.com/kloudnuk/kloudnuk:staging-ubuntu .
docker push registry.gitlab.com/kloudnuk/kloudnuk:staging-ubuntu 
```

### Run Docker container locally

```bash
docker run -it --name kn-test \
--mount source=kloudnuktest-vol,target=/nuk/ \
-p 8000:8000 registry.gitlab.com/# kloudnuk

A web service to make building data easy to access on the cloud. Kloudnuk achieves this by:

- Providing a process to securely enroll hardware development boards like Raspberry Pi.
- Deploying communication and management software directly into the device.
- Providing over-the-air software and security updates to the device programatically.
- Providing REST APIs for cloud data access, and remote device management.

## Publish image to Docker registry

```bash
docker login registry.gitlab.com
docker build --no-cache -t registry.gitlab.com/kloudnuk/kloudnuk .
docker push registry.gitlab.com/kloudnuk/kloudnuk
```

or if you want to specify a tag (other tan latest) use:

```bash
docker build --no-cache -t registry.gitlab.com/kloudnuk/kloudnuk:wb-0.1.1 .
docker push registry.gitlab.com/kloudnuk/kloudnuk:wb-0.1.1 
```

### Run Docker container locally

```bash
docker run -it --name kn-test \
--mount source=kloudnuktest-vol,target=/nuk/ \
-p 8000:8000 registry.gitlab.com/kloudnuk/kloudnuk:latest
```

or

```bash
docker run -it --name kn-stg \
--env-file /home/vctor/Documents/gitlab/kloudnuk/src/main/resources/test.env \
--mount source=kloudnuktest-vol,target=/nuk/ \
-p 8000:8000 registry.gitlab.com/kloudnuk/kloudnuk:latest
```

> Connect to a common network so the web server can query the database container. `docker network connect kloudnuk_default kn-staging`. Note there are three external files required by the application; the *application.properties*, *createdbuser.sh* and *createdbusercert.sh*, which must be in the corresponding container folders (*/nuk/application.properties*, */nuk/mongodb_scripts/sh*). Since the container specifies a named volume *kloudnuktest-vol*, one can copy the three files and corresponding sub-folder directly to the host's named volume directory as shown below:

```bash
mkdir /var/snap/docker/common/var-lib-docker/volumes/kloudnuktest-vol/_data/mongodb_scripts
cp application.properties /var/snap/docker/common/var-lib-docker/volumes/kloudnuktest-vol/_data
cp createdbuser.sh createdbusercert.sh /var/snap/docker/common/var-lib-docker/volumes/kloudnuktest-vol/_data/mongodb_scripts/
```

### Publish an initialized version of the Postgresql database
Don't forget to also copy the volume over the the AWS instance.
```bash
docker commit -a 'Victor Smolinski' -m 'init image' 6ba4dc6e3d01 registry.gitlab.com/kloudnuk/postgres:0.0.1-stg
docker push registry.gitlab.com/kloudnuk/postgres:0.0.1-stg
```

## Publish device scripts to AWS S3

TODO

## Publish device scripts over SFTP

### [ From the development environment ]

```bash
sftp nuk@192.168.1.2 <<EOF
cd /nuk/
put -R /home/vsmolinski/Documents/gitlab/kloudnuk/src/main/resources/device_scripts/
bye
EOF
```

### [ From the development board ]

```bash
cd device_scripts
sudo chmod +x *.sh
./install.sh
```

### MongoDb Configuration

Project: **Kloudnuk-Test**

Database User: `test.kloudnuk`

Connection String: `mongodb+srv://cluster0.dz20ogp.mongodb.net/?authSource=%24external&authMechanism=MONGODB-X509&retryWrites=true&w=majority&appName=Cluster0`

Create the `application.properties` files under `/nuk/` 
```
ds.ownerid=
ds.orgid=
ds.privatekey=
ds.publickey=
ds.host=https://cloud.mongodb.com
ds.uri=/api/atlas/v2/
ds.connectionstring=
```

### MongoDb Logs Collection Regular Expression Examples:

- All the logs for the day of July 29th 2024: `{"timestamp": {$regex: /2024-07-29T\d{2}:\d{2}:\d{2}\+\d{4}/}}`
- All the logs that happened in one minute: `{"timestamp": {$regex: /2024-07-29T16:33:\d{2}\+\d{4}/}}`
- All the logs from 6PM to 8PM inclusive: `{"timestamp": {$regex: /2024-07-29T1[8-9]|2[0-2]:\d{2}:\d{2}\+\d{4}/}}`

## S3 Setup

Create the following folders and files:
1. `~/.aws/config`
    ```
        [default]
        region = us-east-1
        output = json
    ```
2. `~/.aws/credentials`
    ```
        [default]
        aws_access_key_id = 
        aws_secret_access_key = 
    ```loudnuk/kloudnuk:latest
```

or

```bash
docker run -it --name kn-stg \
--env-file /home/vctor/Documents/gitlab/kloudnuk/src/main/resources/test.env \
--mount source=kloudnuktest-vol,target=/nuk/ \
-p 8000:8000 registry.gitlab.com/kloudnuk/kloudnuk:latest
```

> Connect to a common network so the web server can query the database container. `docker network connect kloudnuk_default kn-staging`. Note there are three external files required by the application; the *application.properties*, *createdbuser.sh* and *createdbusercert.sh*, which must be in the corresponding container folders (*/nuk/application.properties*, */nuk/mongodb_scripts/sh*). Since the container specifies a named volume *kloudnuktest-vol*, one can copy the three files and corresponding sub-folder directly to the host's named volume directory as shown below:

```bash
mkdir /var/snap/docker/common/var-lib-docker/volumes/kloudnuktest-vol/_data/mongodb_scripts
cp application.properties /var/snap/docker/common/var-lib-docker/volumes/kloudnuktest-vol/_data
cp createdbuser.sh createdbusercert.sh /var/snap/docker/common/var-lib-docker/volumes/kloudnuktest-vol/_data/mongodb_scripts/
```

### Publish an initialized version of the Postgresql database
Don't forget to also copy the volume over the the AWS instance.
```bash
docker commit -a 'Victor Smolinski' -m 'init image' 6ba4dc6e3d01 registry.gitlab.com/kloudnuk/postgres:0.0.1-stg
docker push registry.gitlab.com/kloudnuk/postgres:0.0.1-stg
```

## Publish device scripts to AWS S3

TODO

## Publish device scripts over SFTP

### [ From the development environment ]

```bash
sftp nuk@192.168.1.2 <<EOF
cd /nuk/
put -R /home/vsmolinski/Documents/gitlab/kloudnuk/src/main/resources/device_scripts/
bye
EOF
```

### [ From the development board ]

```bash
cd device_scripts
sudo chmod +x *.sh
./install.sh
```

### MongoDb Configuration

Project: **Kloudnuk-Test**

Database User: `test.kloudnuk`

Connection String: `mongodb+srv://cluster0.dz20ogp.mongodb.net/?authSource=%24external&authMechanism=MONGODB-X509&retryWrites=true&w=majority&appName=Cluster0`

Create the `application.properties` files under `/nuk/` 
```
ds.ownerid=
ds.orgid=
ds.privatekey=
ds.publickey=
ds.host=https://cloud.mongodb.com
ds.uri=/api/atlas/v2/
ds.connectionstring=
```

### MongoDb Logs Collection Regular Expression Examples:

- All the logs for the day of July 29th 2024: `{"timestamp": {$regex: /2024-07-29T\d{2}:\d{2}:\d{2}\+\d{4}/}}`
- All the logs that happened in one minute: `{"timestamp": {$regex: /2024-07-29T16:33:\d{2}\+\d{4}/}}`
- All the logs from 6PM to 8PM inclusive: `{"timestamp": {$regex: /2024-07-29T1[8-9]|2[0-2]:\d{2}:\d{2}\+\d{4}/}}`

## S3 Setup

Create the following folders and files:
1. `~/.aws/config`
    ```
        [default]
        region = us-east-1
        output = json
    ```
2. `~/.aws/credentials`
    ```
        [default]
        aws_access_key_id = 
        aws_secret_access_key = 
    ```