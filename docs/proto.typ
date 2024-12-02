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

#task[
  add context
]


== Section 1 - Overview

The SimpFTP (Simple File Transfer Protocol) is a communication protocol that
allows a client to interact with files on a server.

== Section 2 - Transport protocol

The SimpFT protocol is a text based protocol. It uses TCP to ensure reliability.
The default port is `1234`.

Thee protocol has three kinds of messages:

- `Actions` which are encoded in UTF-8 and use the following pattern
  `<ACTION> <ARG>\n` where `\n` is used as a delimiter.
- `Statuses` which are encoded in UTF-8 and use the following pattern `<CODE> \n`
  where `\n` is used as a delimiter
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
indicate success.

When an invalid message is received, the server should answer with `ENOTSUP`.

When the status represents an error, the server terminates the connection by
sending `<CODE>\4` where `\4` is the `EOT` (End Of Transmission) character.

== Section 3 - Messages

Even though you will find in the examples bellow the name of the actions in uppercase, the server accepts them in any form (upper, lower, mix of both, etc...).

The valid messages are:

- `LIST` - List the contents of directories
- `GET` - Downloads a file
- `PUT` - Create a new file
- `DELETE` - Delete a file

=== LIST

The client sends a list request to the server to show the list of files and
folders at the specified path.

==== Request

```txt
LIST <PATH>
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
- `ENOENT`
- `ENOTDIR`

=== GET

The client sends a get request to the server to download a file.

#warning(title: "Downloading directories")[
Notice that directories can not be downloaded. If you want to download the content of a directory you have to
to list its contents to fetch the name of the files and then download them.
]

==== Request

```txt
GET <REMOTE_PATH> <LOCAL_PATH>
```

- `REMOTE_PATH`: The path of the file to be downloaded
- `LOCAL_PATH`: The path where the downloaded content should be stored.


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
content in binary form.

On error, only the error code is sent. `<CODE>` matches one of:
- `EACCES`
- `ENOENT`
- `EISDIR`


=== PUT

The client sends a put request to the server to upload a file or create a directory.

==== Request

```txt
PUT <PATH> <FILE_SIZE>
```

The first part of the request provides the path to the file or directory on the
server. A trailing `/` indicates that a directory should be created and no size
should be included.

```bin
<DATA>
```

If the path doesn't end with a `/`, the rest of the request contains the file
content in binary.

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

=== DELETE

The client sends a delete request to the server to delete a file.

==== Request

```txt
DELETE <PATH>
```

Where path is the path to the file or directory to delete.

If the path points to a directory, the whole directory is removed recursively.

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

== Section 4 - Examples

=== LIST

#align(center,[
  #image("./diagrams/list.svg" )
])

=== GET

#align(center,[
  #image("./diagrams/get.svg" )
])

=== PUT

#align(center,[
  #image("./diagrams/put.svg" )
])

=== DELETE

#align(center,[
  #image("./diagrams/delete.svg" )
])

