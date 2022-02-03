# Signum Node (previously Burstcoin Reference Software)
[![Build BRS](https://github.com/burst-apps-team/burstcoin/actions/workflows/build.yml/badge.svg)](https://github.com/burst-apps-team/burstcoin/actions/workflows/build.yml)
[![GPLv3](https://img.shields.io/badge/license-GPLv3-blue.svg)](LICENSE.txt)
[![Get Support at https://discord.gg/ms6eagX](https://img.shields.io/badge/join-discord-blue.svg)](https://discord.gg/ms6eagX)

The world's first HDD-mined cryptocurrency using an energy efficient
and fair Proof-of-Commitment (PoC+) consensus algorithm.

The two supported database backends are:

- H2 (embedded, recommended)
- MariaDB (advanced users)

> Requirements: [Docker installed](https://docs.docker.com/engine/install/)

# Running on Docker
Running the Signum node on docker is a great way to provide easy updates upon new releases, but requires some knowledge of how to use docker.
These containers are built with a script that will automatically download the latest version of Phoenix and install it during startup. 
Each time you launch the container, it should have the latest Phoenix interface.

## H2 Image via docker-compose
The easiest way is to use docker-compose to launch the image consistently every time.

There are two choices of database driver: `h2` and `mariadb`. The H2 driver is built into the node and will be completely self-contained.
It is not slower than mariadb and works just as well, but does not require a mariadb RDMS installed.
To use the H2 docker image, in a folder called 'signum-node', create a file called `docker-compose.yml` and fill it with:

> As a template you can use the docker-compose.yml file which is in this repository

```yml
version: "3.8"
services:
  signum:
    image: signumnetwork/node:latest-h2
    init: true
    stop_grace_period: 2m
    deploy:
      replicas: 1
    restart: always
    ports:
      - "8123:8123"
      - "8125:8125"
    volumes:
      - "./conf:/conf"
      - "./db:/db"
```

The h2 image will create 2 docker volumes by default: a `db` volume and a `conf` volume, though these will show up in `docker volume ls` as a pair of UUIDs. 
By default the node will use all default settings. The `volumes` section in the docker-compose.yml file above tells docker to map the internal '/conf' folder to an external folder on the host's hard drive instead of using a docker volumne. This allows node configuration files to be customized.

Create a `conf` folder inside the 'signum-node' folder and drop your `brs.properties` file into that conf folder. Make any desired changes to the config file.

Finally, run `docker-compose up -d` from inside the 'signum-node' folder. The node will begin starting up. You can monitor it with `docker logs signum-node_signum_1`. The name is generated from the folder name, following by the name of the service in the docker-compose file, then a number indicating which replica instance it is. Docker-compose can launch multiple replicas if software properly supports it, but the Signum node is not designed with this feature of docker in mind, so this docker-compose file specificies only a single replica.

The website should now be available at the server's host name on port 8125.

## Starting manually in the console

Alternatively, you may want to run the container manually in the command line. Then you need to link/mount your local `conf` and `db` folders to the container and forward the ports.
The command may look like this:

```bash
docker run -d -p "8123:8123" -p "8125:8125" -v "/home/me/crypto/signum-node/db:/db" -v "/home/me/crypto/signum-node/conf:/conf" signumnetwork/node:latest-h2
```

> using `docker ps` will list the running container 

Be sure that the `db` and `conf` folder exist under the give path. You do not need to put any files there, they will be created on the first startup.
Within the `conf` folder you'll see three file created:

- logging-default.properties - only if you know how logging conf works
- node-default.properties - better not touch it
- node.properties - this is your custom node configuration file...mess it up here


## Mariadb Image
Running the Mariadb image is similar to the H2 image, except it does not create a database docker volume. You must also provide the configuration in `brs.properties` to point it to a pre-established mariadb instance.

*The mariadb image is provided as an alternative, but is not officially supported in docker form due to its more complex setup.*

If running mariadb in its own docker container, add the service directly to the docker-compose file used for h2 so both containers launch simultaneously. If hosting it on the docker host or somewhere else, no modification to the docker-compose is necessary, but there may be additional options needed to allow the node to communicate with the mariadb outside of its container network.

# Configuring Dockerhub Releases on Github
The github actions workflow that releases docker images to dockerhub is set up to do so automatically whenever there is a 'release' created via the Github interface, or when the workflow is manually triggered. Some configuration is required to succesfully run this workflow.

*Note: This workflow only supports dockerhub.*

1. Set up a [docker account][docker-signup]
2. Open your github repository settings and navigate to the 'Environments' tab on the left
3. Create a new evironment named "dockerhub-publish"
4. Open that environment up and at the bottom of the page click the 'Add Secret' button.
5. Add 3 separate secrets:
    a. `DOCKERHUB_USERNAME` - set the value to the username you created in step 1
    b. `DOCKERHUB_PASSWORD` - set the value to the password you created in step 1
    c. `DOCKERHUB_REPO` - set the value to the desired docker repository path (e.g. `signumnetwork/node`)
6. The file `.github/workflows/dockerhub.yml` (provided) must exist in the github repo's default branch. This is usually `main` or, for legacy repos, `master`. Obviously, the file must also contain all the proper instructions.

With all of that in place, Github Actions should automatically build and deploy a new pair of docker images each time a release is made, and each time the workflow is manually run. It will create the following tags (where `<version>` is replaced by the most recent 'release' version.):

* `<version>-h2`
* `<version>-maria`
* `latest-h2`
* `latest-maria`

To manually deploy images, click on Github Actions, choose the 'Publish Docker Image' workflow, the click the 'Run Workflow' button, and choose a branch, then click 'Run'.


[docker-signup]: https://hub.docker.com/signup "Docker Signup"
