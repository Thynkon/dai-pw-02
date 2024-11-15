# Documentation

The Protocol documentation is written in [typst](https://github.com/typst/typst) which is
compiled to pdf when pushed to the main branch

To run locally, just compile the file using the typst compiler

```bash
typst c proto.typ
```

Or use the preview extension in your favorite editor

- neovim: [typst-preview.nvim](https://github.com/niuiic/typst-preview.nvim)
- vscode: [tinymist](https://marketplace.visualstudio.com/items?itemName=myriad-dreamin.tinymist)


## Docker

SimpFT can be run directly using java or through a docker container.

The same image can be used for the server and client.

### Building

You can build the container by cloning the repository and using:

```bash
docker build . -t dai-lab-02:latest
```

Or with the [compose.yml][compose] file provided

```bash
docker compose build
```

### Running

The container simply runs the jar file with the provided arguments so the
following lines do the same thing

```bash
docker run --rm -v "./data:/data" dai-lab-02:latest --help
java -jar simpft-1.0.jar --help
```

#### Server

You can run the server either manually using

```bash
docker run --rm -p 127.0.0.1:8020:8020 -v "./server_data:/data" dai-lab-02:latest --mode server --port 8020 --root-dir /data
```

Or with the [compose.yml][compose]

```bash
docker compose up -d
```

#### Client

When using the client, you should run the container manually.

```bash
docker run --rm -v "./client_data:/data" dai-lab-02:latest --mode client --host 127.0.0.1 --port 8020 --root-dir /data
```

[compose]: ../compose.yml
