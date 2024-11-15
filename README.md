# SimpFT - A Simple File Transfer application

<a name="readme-top"></a>

<details>
  <summary>Table of Contents</summary>
  <ol>
    <li>
        <a href="#built-with">Built With</a>
    </li>
    <li>
      <a href="#getting-started">Getting Started</a>
      <ul>
        <li><a href="#documentation-and-protocol">Documentation and protocol</a></li>
        <li>
          <a href="#prerequisites">Prerequisites</a>
          <ul>
            <li><a href="#java-21">Java 21</a></li>
            <li><a href="#docker-setup">Docker setup</a></li>
            <li><a href="#github-actions">GitHub actions</a></li>
          </ul>
        </li>
            <li><a href="#development">Development</a></li>
        <li>
          <a href="#usage">Usage</a>
          <ul>
            <li><a href="#with-java">With java</a></li>
            <li>
              <a href="#with-docker">With docker</a>
              <ul>
                <li><a href="#building-the-image">Building the image</a></li>
                <li>
                  <a href="#running-the-image">Running the image</a>
                  <ul>
                    <li><a href="#server">Server</a></li>
                    <li><a href="#client">Client</a></li>
                  </ul>
                </li>
              </ul>
            </li>
          </ul>
        </li>
      </ul>
    </li>
    <li><a href="#contributing">Contributions</a></li>
    <li><a href="#license">License</a></li>
    <li><a href="#contacts">Contacts</a></li>
  </ol>
</details>

## Built With

- [Java 21 temurin][java]
- [Maven][maven]
- [Docker][docker]
- [Picocli][picocli]

<p align="right">(<a href="#readme-top">back to top</a>)</p>

<!-- GETTING STARTED -->

## Getting Started

SimpFT is a simple file transfer application to upload and download files on a
server.

You can also find the Protocol definition as a [pdf](./docs/proto.pdf) or [typst](./docs/proto.typ)

### Documentation and protocol

You will find all the information you need to use this application in this readme.

You can find the protocol definition as a [pdf](./docs/proto.pdf) or [typst](./docs/proto.typ) if you need to edit
the it. All the documentation regarding the usage of typst for the documentation
can be found under [docs/readme.md](./docs/README.md)

The CLI application is self documented and provides all the info you need for its
arguments using

```bash
java -jar simpft.jar --help
```

The code documentation is written with standard javadoc

### Prerequisites

#### Java 21

- [asdf][asdf]

  ```sh
  # Install the plugin if needed
  asdf plugin add java
  # Install
  asdf install java latest:temurin-21
  ```

- Mac (homebrew)

  ```zsh
  brew tap homebrew/cask-versions
  brew install --cask temurin@21
  ```

- Windows (winget)

  ```ps
  winget install EclipseAdoptium.Temurin.21.JDK
  ```

#### Docker setup

The application can be used with docker.

To install and use docker, follow the [official documentation](https://docs.docker.com/engine/install/)

#### Github actions

If you want to test the `Github actions` on your machine, you can use [act](https://github.com/nektos/act).

Before you launch any workflow, make sure you have created a repository secret named `AUTH_TOKEN`.

Then, create a file named `.secrets` which should contain the following:

```env
AUTH_TOKEN=<YOUR_AUTH_TOKEN>
```

Finally, launch the publish workflow (which publishes the mvn package to Github repository) with the following command:

```sh
act --secret-file .secrets
```

### Development

Use the maven wrapper to install dependencies, build and package the project.

```sh
# install the dependencies
./mvnw clean install
# build
./mvnw package
# run
java -jar target/<filename>.jar --help
```

### Usage

#### With java

Once you have all the prerequisites, including the application archive, you can
simply run the application like this:

```bash
# Run as server
java -jar simpft.jar --mode server --port 1234 --address localhost --connections 2
# Run client
java -jar simpft.jar --mode client --port 1234 --address localhost
```

The default values are:

- Address: localhost
- Port: 1234
- Connections: 2

#### With docker

SimpFT can be run directly using java or through a docker container.

The same image can be used for the server and client.

> ![TODO]
> Publish to GitHub Container Registry

##### Building the image

You can build the container by cloning the repository and using:

```bash
docker build . -t dai-lab-02:latest
```

Or with the [compose.yml][compose] file provided

```bash
docker compose build
```

When using compose, you can modify the file directly or override the default
parameters by providing the following environment variables.

- SERVER_ADDRESS: the address to expose (default: 127.0.0.1)
- SERVER_PORT: the port to map on the host (default: 1234)
- MAX_CONNECTIONS: the number of connections to handle in parallel (default: 2)

##### Running the image

The container simply runs the jar file with the provided arguments so the
following lines do the same thing

```bash
docker run --rm -v "./data:/data" dai-lab-02:latest --help
java -jar simpft-1.0.jar --help
```

###### Server

You can run the server either manually using

```bash
docker run --rm -p 127.0.0.1:1234:1234 -v "./server_data:/data" dai-lab-02:latest --mode server
```

Or with the [compose.yml][compose]

```bash
docker compose up -d
```

###### Client

When using the client, you should run the container manually.

```bash
docker run --rm -v "./client_data:/data" dai-lab-02:latest --mode client -a <server-address>
```

<!-- CONTRIBUTING -->

## Contributing

Contributions are what make the open source community such an amazing place to learn, inspire, and create. Any contributions you make are **greatly appreciated**.

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

<p align="right">(<a href="#readme-top">back to top</a>)</p>

<!-- LICENSE -->

## License

Distributed under the MIT License. See `LICENSE` for more information.

<p align="right">(<a href="#readme-top">back to top</a>)</p>

<!-- CONTACT -->

## Contacts

- [Thynkon](https://github.com/Thynkon)
- [Mondotosz](https://github.com/Mondotosz)

<p align="right">(<a href="#readme-top">back to top</a>)</p>

<!-- MARKDOWN LINKS & IMAGES -->
<!-- https://www.markdownguide.org/basic-syntax/#reference-style-links -->

[java]: https://adoptium.net/temurin/releases/
[maven]: https://maven.apache.org/
[docker]: https://www.docker.com/
[picocli]: https://picocli.info/
[asdf]: https://asdf-vm.com/
[compose]: ./compose.yml
