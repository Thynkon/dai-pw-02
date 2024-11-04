#import "@preview/ilm:1.2.1": *
#import "@preview/gentle-clues:1.0.0": *
#import "@preview/chronos:0.1.0"

#show: ilm.with(
  title: [TBD],
  author: "Kenan Augsburger & Mário Ferreira",
  date: datetime.today(),
)

= Title

#task[
  add context
]


== Section 1 - Overview

#task[
  add overview
]

== Section 2 - Transport protocol

#task[
  add transport protocol specification
]

== Section 3 - Messages

#task[
  specify each messages with

  - Description
  - Request
  - Response
]

== Section 4 - Examples

#task[
  add examples with sequence diagrams
]

=== Functional example

#chronos.diagram({
  import chronos: *
  _par("Client")
  _par("Server")

  _seq("Client", "Server", comment: "Establish connection")
  _seq("Server", "Server", comment: "Do something")
  _seq("Server", "Client", comment: "Connection established")
})

