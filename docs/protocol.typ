#import "@preview/ilm:1.3.0": *
#import "@preview/gentle-clues:1.0.0": *
#import "@preview/chronos:0.1.0"
#import "@preview/codly:1.0.0": *

#show: ilm.with(
  title: [SimpFT protocol],
  author: "Kenan Augsburger & Mário Ferreira",
  date: datetime.today(),
)
#show: codly-init.with()

#codly(
  languages: (
    bin: (
      name: "binary",
      icon: text(font: "JetBrainsMono NFP", " "),
      color: rgb("#f43f5e")
    ),
    txt: (
      name: "text",
      icon: text(font: "JetBrainsMono NFP", "󰦨 "),
      color: rgb("#64748b")
    ),
  )
)

#show raw: set text(font: "JetBrainsMono NFP")

#show link: underline

= SimpFTP

== Section 1 - Overview

The SimpFTP (Simple File Transfer Protocol) is a communication protocol that
allows a client to interact with files on a server.

== Section 2 - Transport protocol

The SimpFT protocol is a text based protocol. It uses TCP to ensure reliability.
The default port is `1234`.

Thee protocol has three kinds of messages:

- `Actions` which are encoded in UTF-8 and use the following pattern
  `<ACTION> <ARG>\n` where `\n` is used as a delimiter.
- `Statuses` which are encoded in UTF-8 and use the following pattern `<CODE><EOT>`
  where `EOT` (`0x04` character in ASCII table) is used as a delimiter
- `Datas` which is the binary content of a transferred file delimited by an end of
  transmission character `EOT`.

The initial connection must be established by the client.

Once the server accepts the connection, the client can send `Actions` to interact
with files on the server.

When an `Action` is used to transfer a file from the server to the client, the
server response should be a `Status` followed by the `Data` of the file if there
is no error.

When an `Action` is used to transfer a file from the client to the server, the
`Data` should follow right away and the server responds with a status once the
file is sent.

The client can do the following actions:
- List the files and folders
- Get a file from the server
- Store a file on the server
- Delete a file from the server

The `Status` values use the values defined by the c standard library in
#link("https://man7.org/linux/man-pages/man3/errno.3.html")[errno.h] or `0` to
indicate success. In other words, a integer value where `0` means OK, and any other value means 
that there was an error.

When an invalid message is received, the server should answer with `ENOTSUP`.

When the status represents an error, the server terminates the connection by
sending `<CODE>\x04` asfsfd `\x04` is the `EOT` (End Of Transmission) character.

#pagebreak()

== Section 3 - Messages

Even though you will find in the examples bellow the name of the actions in uppercase, the server accepts them in any form (upper, lower, mix of both, etc...).

=== Valid messages
The valid messages are:

- `LIST` - List the contents of directories
- `GET` - Downloads a file
- `PUT` - Create a new file
- `DELETE` - Delete a file

=== Status codes

Bellow is the list of all the status codes the server might return. We use the constant's names
instead of the integer values in the description of each message because they come from the *errno.h* file
and they are used as a standard in multiple programming languages.

#table(
  columns: (auto, auto, auto),
  inset: 10pt,
  align: horizon,
  table.header(
    [*CONSTANT NAME*], [*INTEGER VALUE*], [*MEANING*],
  ),
  [], [`0`], [OK],
  [`EACCES`], [`13`], [Permission denied],
  [`ENOENT`], [`2`], [No such file or directory],
  [`ENOTDIR`], [`20`], [Not a directory],
  [`EINVAL`], [`22`], [Invalid argument],
)

#pagebreak()

=== LIST

The client sends a list request to the server to show the list of files and
folders at the specified path.

==== Request

```txt
LIST <REMOTE_PATH>
```

If the path is empty, the working directory of the server will be used.

==== Response

```txt
<CODE>
```

```txt
foldera/:folderb/:filea:fileb
```

On a successful request, the server answers with the code `0`, followed by a
colon separated list of files and folders. Each folders have a trailing `/`
appended to them.

On error, only the error code is sent. `<CODE>` matches one of:
- `EACCES`
- `EFBIG`
- `EINVAL`
- `EISDIR`
- `ENOENT`
- `ENOTDIR`

#pagebreak()

=== GET

The client sends a get request to the server to download a file.

#warning(title: "Downloading directories")[
Notice that directories can not be downloaded. If you want to download the content of a directory you have to
to list its contents to fetch the name of the files and then download them.
]

==== Request

```txt
GET <REMOTE_PATH>
```

- `REMOTE_PATH`: The path of the file to be downloaded


==== Response

```txt
<CODE>
```

```txt
<FILE_SIZE>
```

```bin
<DATA>
```

On a successful request, the server answers with the code `0`, followed by the size (a non-negative integer value) of the file as well as its
content in binary form. All the 3 connections are delimited by the `EOT` character.

On error, only the error code is sent. `<CODE>` matches one of:
- `EACCES`
- `ENOENT`
- `EISDIR`
- `EINVAL`


#pagebreak()

=== PUT

The client sends a put request to the server to upload a file or create a directory.

#warning()[The size of the files to be uploaded is limited to 500MB.]

==== Request

```txt
PUT <LOCAL_PATH> <FILE_SIZE>
```

- `REMOTE_PATH`: The path of the file to be uploaded
- `FILE_SIZE`: The path of the file to be uploaded

The first part of the request provides the path to the file or directory on the
server. A trailing `/` indicates that a directory should be created and no size
should be included. The server reuses the path provided by the client to create
it.

```bin
<DATA>
```

If the path doesn't end with a `/`, the rest of the request contains the file
content in binary.

#warning()[Notice that the file is sent in two requests, the first one to create the file and the second one to send its content.]

==== Response

```txt
<CODE>
```

On a successful request, the server answers with the code `0` indicating that
the file or directory was created successfully.

On error, only the error code is sent. `<CODE>` matches one of:
- `EACCES`
- `EFBIG`
- `EISDIR`
- `ENOENT`
- `EINVAL`

#warning()[If the `<REMOTE_PATH>` contains directories that do not exist, the server creates them recursively.]

#pagebreak()
=== DELETE

The client sends a delete request to the server to delete a file.

==== Request

```txt
DELETE <REMOTE_PATH>
```

- `REMOTE_PATH`: The path of the file or directory to delete. If the path points to a directory, the whole directory is removed recursively.

==== Response

```txt
<CODE>
```

On a successful request, the server answers with the code `0` indicating that the
file or folder was removed successfully.

On error, only the error code is sent. `<CODE>` matches one of:
- `EACCES`
- `ENOENT`
- `EINVAL`

#pagebreak()

== Section 4 - Examples

=== LIST


#grid(
  columns: (auto, auto),
  gutter: 20pt,
  [
==== OK
#image("./diagrams/img/list.png", width: 100%)
],[
==== ERROR
#image("./diagrams/img/list_error.png", width: 100%)
])

=== GET

#grid(
  columns: (auto, auto),
  gutter: 20pt,
  [
==== OK
#image("./diagrams/img/get.png", width: 100%)
],[
==== ERROR
#image("./diagrams/img/get_error.png", width: 100% )
])

=== PUT

#grid(
  columns: (auto, auto),
  gutter: 20pt,
  [
==== OK
  #image("./diagrams/img/put.png", width: 100% )
],[
==== ERROR
  #image("./diagrams/img/put_error.png", width: 100% )
])


=== DELETE
#grid(
  columns: (auto, auto),
  gutter: 20pt,
  [
==== OK
#image("./diagrams/img/delete.png", width: 100%)
],[
==== ERROR
#image("./diagrams/img/delete_error.png", width: 100%)
])
