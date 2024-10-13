# AWS EC2 Instance Environment Setup

## Launch SSH EC2 connection
`ssh -i .ssh/mothernuk.pem ubuntu@kloudnuk.com`

# Configure power user account and hostname
```bash
groupadd kloudnuk
useradd -m -g kloudnuk -G sudo,adm,ubuntu vsmolinski -s /bin/bash
passwd vsmolinski
su vsmolinski

hostnamectl set-hostname kloudnuk
```

## Configure and enable the OS firewall
```bash
ufw default deny incoming
ufw default allow outgoing
ufw allow 51820/udp
ufw allow 22/tcp
ufw limit 22/tcp
ufw allow https
ufw enable
ufw reload
```

## Create keys
```bash
wg genkey | sudo tee $wgdir/knprivate.key
chmod go= $wgdir/knprivate.key
cat $wgdir/knprivate.key | sudo wg pubkey | sudo tee $wgdir/knpublic.key
```

## Create Initial Configuration file
```bash
cat > knwg0.conf <<EOF
[Interface]
Address = 10.0.0.1/8
SaveConfig = true
PostUp = iptables -A FORWARD -i wg0 -j ACCEPT; iptables -t nat -A POSTROUTING -o eth0 -j MASQUERADE; ip6tables -A FORWARD -i wg0 -j ACCEPT; ip6tables -t nat -A POSTROUTING -o eth0 -j MASQUERADE
PostDown = iptables -D FORWARD -i wg0 -j ACCEPT; iptables -t nat -D POSTROUTING -o eth0 -j MASQUERADE; ip6tables -D FORWARD -i wg0 -j ACCEPT; ip6tables -t nat -D POSTROUTING -o eth0 -j MASQUERADE
ListenPort = 51820
PrivateKey = $(cat $wgdir/knprivate.key)
DNS = 1.1.1.1

[Peer]
PublicKey = ME0OyTouVTv6yoF0BPeGXMPml2s1Fner7z1Z6wArfkE=
AllowedIPs = 10.0.0.0/8
Endpoint = 192.168.1.12:51820
PersistentKeepalive = 24
EOF
```

## Enable wireguard tunnel interface to run as a service
```bash
wg-quick up knwg0
systemctl enable wg-quick@knwg0
```

## Configure Application user account and environment
```bash
useradd -r -g kloudnuk kloudnuk
```


# Install Docker
Download the docker [pakages](https://download.docker.com/linux/ubuntu/dists/jammy/pool/stable/amd64/) into your local dev environment, and then send them to the EC2 instance via sftp.
```bash
sftp -i ~/.ssh/mothernuk.pem vsmolinski@kloudnuk.com <<EOF
cd /home/vsmolinski
put -r .
bye
EOF

```
1. Install the packages.
2. Enable the docker service to auto start.
3. Configure group docker group membership for dev user accounts.
```bash
dpkg -i containerd.io.deb \
        docker-buildx.deb \
        docker-ce-cli.deb \
        docker-ce.deb \
        docker-compose.deb

systemctl enable docker
systemctl start docker

groupadd docker
usermod -aG docker vsmolinski
```

### Install the Netdata container
```bash
docker run -d --name=netdata \
  --pid=host \
  --network=host \
  -v netdataconfig:/etc/netdata \
  -v netdatalib:/var/lib/netdata \
  -v netdatacache:/var/cache/netdata \
  -v /etc/passwd:/host/etc/passwd:ro \
  -v /etc/group:/host/etc/group:ro \
  -v /etc/localtime:/etc/localtime:ro \
  -v /proc:/host/proc:ro \
  -v /sys:/host/sys:ro \
  -v /etc/os-release:/host/etc/os-release:ro \
  -v /var/log:/host/var/log:ro \
  -v /var/run/docker.sock:/var/run/docker.sock:ro \
  --restart unless-stopped \
  --cap-add SYS_PTRACE \
  --cap-add SYS_ADMIN \
  --security-opt apparmor=unconfined \
  netdata/netdata
```
Verify the netdata image installed and the container is running.

# Certificate Management (Certbot)

1. Install certbot: `snap install --classic certbot`
2. Make symlink from snap install folder to the usr/bin folder... `ln -s /snap/bin/certbot /usr/bin/certbot`
3. Configure the certbot cloudflare plugin (godaddy doesn't work):

```bash
snap set certbot trust-plugin-with-root=ok
snap install certbot-dns-cloudflare
```

Create cloudflare configuration file with credentials, and request certificate. `sudo touch /etc/letsencrypt/cloudflare.ini`

```bash
certbot certonly \
--dns-cloudflare \
--dns-cloudflare-credentials /etc/letsencrypt/cloudflare.ini \
--dns-cloudflare-propagation-seconds 60 \
-d kloudnuk.com
```

```txt
Saving debug log to /var/log/letsencrypt/letsencrypt.log
Requesting a certificate for kloudnuk.com
Unsafe permissions on credentials configuration file: /etc/letsencrypt/cloudflare.ini
Waiting 60 seconds for DNS changes to propagate

Successfully received certificate.
Certificate is saved at: /etc/letsencrypt/live/kloudnuk.com/fullchain.pem
Key is saved at:         /etc/letsencrypt/live/kloudnuk.com/privkey.pem
This certificate expires on 2024-08-23.
These files will be updated when the certificate renews.
Certbot has set up a scheduled task to automatically renew this certificate in the background.
```

## Dockerize the web app on AWS EC2 instance

1. `docker login registry.gitlab.com`
2. Pull the images from the private container registry:
   ```bash
   docker pull registry.gitlab.com/kloudnuk/kloudnuk:0.0.1-stg
   docker pull registry.gitlab.com/kloudnuk/postgres:0.0.1-stg
   ```
3. Create the networks:
    ```bash
    docker network create --internal backend
    docker network create -d bridge frontend
    ```
4. Create the volumes and alias their mount paths:
    ```bash
    docker volume create kloudnuk
    docker volume create postgres
    export KNVOL=/var/lib/docker/volumes/kloudnuk/_data
    export KNPG=/var/lib/docker/volumes/postgres/_data
    ```
5. Tarball the local container volumes and send them to the EC2 instance.
    ```bash
    mkdir kn

    sudo tar -cvzf ./postgresvol.tar.gz \
    /var/snap/docker/common/ var-lib-docker/volumes/kloudnuk_postgres-vol/_data

    sudo tar -cvzf ./knvol.tar.gz \
    /var/snap/docker/common/var-lib-docker/volumes/kloudnuktest-vol/_data 

    sftp -i ~/.ssh/mothernuk.pem ubuntu@ec2-34-233-200-152.compute-1.amazonaws.com <<EOF
    cd /home/ubuntu/kn
    put postgresvol.tar.gz
    put knvol.tar.gz
    put test.env
    bye
    EOF
    ```

6. Extract tarball and copy over to volume's directory.
    ```bash
    cd kn

    tar -xvzf knvol.tar.gz

    sudo cp -r var/snap/docker/common/var-lib-docker/volumes/kloudnuktest-vol/_data/* $KNVOL

    tar -xzvf postgresvol.tar.gz

    sudo cp -r var/snap/docker/common/var-lib-docker/volumes/kloudnuk_postgres-vol/_data/* $KNPG
    ```

7. Run the postgresql docker container:
    ```bash
    docker run -it --name pg-stg --mount source=postgres,target=/var/lib/postgresql/data --network backend -p 5432:5432 registry.gitlab.com/kloudnuk/postgres:0.0.1-stg
    ```

8. Copy the server certificates into the server container's volume host mount-point.
    ```bash
    sudo cp /etc/letsencrypt/live/kloudnuk.com/fullchain.pem $KNVOL

    sudo cp /etc/letsencrypt/live/kloudnuk.com/privkey.pem $KNVOL
    ```

9. Run the web server docker container:
    ```bash
    docker run -it --name kn-stg --env-file test.env --mount source=kloudnuk,target=/nuk/ --network=backend --network=frontend -p 443:443 registry.gitlab.com/kloudnuk/kloudnuk:0.0.2-stg

    --On AWS EC2 instance--
    docker run -it --name kn-stg --env-file /home/ubuntu/kn/test.env --mount source=kloudnuk,target=/nuk/ --network=backend --network=frontend -p 443:443 registry.gitlab.com/kloudnuk/kloudnuk:0.0.2-stg
    ```
