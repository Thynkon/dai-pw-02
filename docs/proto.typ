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
The default port is 1234.

Thee protocol has three kinds of messages:

- `Actions` which are encoded in UTF-8 and use the following pattern
  `<ACTION> <ARG>\n` where `\n` is used as a delimiter.
- `Statuses` which are encoded in UTF-8 and use the following pattern `<CODE> \n`
  where `\n` is used as a delimiter
- `Datas` which is the binary content of a transferred file delimited by an end of
  transmission character `EOT`.

The initial connection must be established by the client.

Once the server accepts the connection, the client can sens `Actions` to interact
with files on the server.

#task[
  If there is enough time, add an authentication step in the connection process
]

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

#task[
  If there is enough time, add a move action
]

The `Status` values use the values defined by the c standard library in #link("https://man7.org/linux/man-pages/man3/errno.3.html")[errno.h]

When an invalid message is received, the server should answer with `ENOTSUP` and
flush its buffer.

== Section 3 - Messages

=== List files

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

On error, only the error code is sent. The code matches one of:
- EACCES
- ENOENT

=== Get file

The client sends a get request to the server to download a file.

==== Request

```txt
GET <PATH>
```

==== Response

```txt
<CODE>
```
```bin
<DATA>
```

On a successful request, the server answers with the code `0`, followed by the
content of the file in binary form.

On error, only the error code is sent. The code matches one of:
- EACCES
- ENOENT
- EISDIR


=== Put file

The client sends a put request to the server to upload a file or create a directory.

==== Request

```txt
PUT <PATH>
```

The first part of the request provides the path to the file or directory on the
server. A trailing `/` indicates that a directory should be created.

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

On error, the code matches one of:
- EACCES
- EFBIG
- EISDIR
- ENOENT

=== Delete file

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

On error, the code matches one of:
- EACCES
- ENOENT
- EINVAL

== Section 4 - Examples

=== List

#align(center,[
  #image("./diagrams/list.svg" )
])

=== Get

#align(center,[
  #image("./diagrams/get.svg" )
])

=== Put

#align(center,[
  #image("./diagrams/put.svg" )
])

=== Delete

#align(center,[
  #image("./diagrams/delete.svg" )
])

