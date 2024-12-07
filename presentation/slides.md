---
# You can also start simply with 'default'
theme: default
# random image from a curated Unsplash collection by Anthony
# like them? see https://unsplash.com/collections/94734566/slidev
background: https://cover.sli.dev
# some information about your slides (markdown enabled)
title: DAI - PW02
# apply unocss classes to the current slide
class: text-center
# https://sli.dev/features/drawing
drawings:
  persist: false
# slide transition: https://sli.dev/guide/animations.html#slide-transitions
transition: slide-left
# enable MDC Syntax: https://sli.dev/features/mdc
mdc: true
# take snapshot for each slide in the overview
overviewSnapshots: true
---

# DAI - PW02

A simple file transfer application written in JAVA

---

# Project management

<div class="flex items-center justify-between">
  <div>
    <ul>
      <li>
        GitHub Projects
        <ul>
          <li>Issues</li>
          <li>Pull request</li>
          <li>Review</li>
        </ul>
      </li>
      <li>
        Git flow
        <ul>
          <li>main</li>
          <li>develop</li>
          <li>features</li>
          <li>fix/hotfix/bugfix</li>
        </ul>
      </li>
    </ul>
  </div>
  <div class="flex flex-col gap-10 basis-1/2">
    <img src="/assets/github-project.png"  alt="project" width="full"/>
    <img src="/assets/graph.png"  alt="branches" width="full"/>
  </div>
</div>

---

# Features

<div class="flex items-center justify-between">
  <div>
    <ul>
      <li>Upload and download files on a server</li>
      <li>Multiple connections at the same time</li>
      <li>Docker image on the GitHub Container Registry</li>
      <li>Maven package on the GitHub Package Registry</li>
      <li>Automatic builds using Github Actions</li>
    </ul>
  </div>
  <div class="flex flex-col space-y-10">
    <img src="https://upload.wikimedia.org/wikipedia/commons/9/91/Octicons-mark-github.svg"  alt="drawing" width="100"/>
    <img src="https://upload.wikimedia.org/wikipedia/commons/4/4e/Docker_%28container_engine%29_logo.svg"  alt="drawing" width="300"/>
    <img src="https://upload.wikimedia.org/wikipedia/commons/5/52/Apache_Maven_logo.svg"  alt="drawing" width="300"/>
  </div>
</div>

---

# Protocol

<div class="flex items-center justify-between">
  <div>
    <ul>
      <li>
        LIST &lt;REMOTE_PATH&gt;
        <ul>
          <li class="list-none">&lt;STATUS_CODE&gt;</li>
          <li class="list-none">foldera/:folderb/:filea:file</li>
        </ul>
      </li>
      <li>
        GET &lt;REMOTE_PATH&gt;
        <ul>
          <li class="list-none">&lt;STATUS_CODE&gt;</li>
          <li class="list-none">&lt;FILE_SIZE&gt;</li>
          <li class="list-none">&lt;DATA&gt;</li>
        </ul>
      </li>
      <li>
        PUT &lt;LOCAL_PATH&gt; &lt;FILE_SIZE&gt;
        <ul>
          <li class="list-none">&lt;STATUS_CODE&gt;</li>
        </ul>
      </li>
      <li>
        DELETE &lt;REMOTE_PATH&gt;
        <ul>
          <li class="list-none">&lt;STATUS_CODE&gt;</li>
        </ul>
      </li>
    </ul>
  </div>
  <div class="flex flex-col space-y-10">
    <table class="table-auto">
      <thead>
        <tr>
          <th>CONSTANT NAME</th>
          <th>INTEGER VALUE</th>
          <th>MEANING</th>
        </tr>
      </thead>
      <tbody>
        <tr>
          <td>EACCES</td>
          <td>13</td>
          <td>Permission denied</td>
        </tr>
        <tr>
          <td>EFBIG</td>
          <td>27</td>
          <td>File too large</td>
        </tr>
        <tr>
          <td>ENOENT</td>
          <td>2</td>
          <td>No such file or directory</td>
        </tr>
        <tr>
          <td>EINVAL</td>
          <td>22</td>
          <td>Invalid argument</td>
        </tr>
        <tr>
          <td>EISDIR</td>
          <td>21</td>
          <td>Is a directory</td>
        </tr>
        <tr>
          <td>ENOTDIR</td>
          <td>20</td>
          <td>Not a directory</td>
        </tr>
      </tbody>
    </table>
  </div>
</div>

---

# Protocol

<div class="flex items-center justify-between">
  <div>
    <img class="object-fill h-full w-80" src="/assets/get.svg" alt="PUT action"/>
  </div>
  <div class="flex flex-col space-y-10">
    <img class="object-fill h-full w-80" src="/assets/put.svg" alt="PUT action"/>
  </div>
</div>

---

# Binary and text on one socket

<div class="flex items-start justify-between pt-8">
  <div class="flex flex-col gap-10">
    <h3>Why</h3>
    <ul>
      <li>A single connection</li>
      <li>Support for text/binary files</li>
    </ul>
  </div>
  <div class="flex flex-col gap-10">
    <h3>Issues</h3>
    <ul>
      <li>Need to share the input buffer</li>
      <li>Can't use a marker for binary data</li>
      <li>Hard to debug</li>
      <li>Cannot use the provided UTF8 support</li>
    </ul>
  </div>
</div>

<!--
  Single connection: no need to manage two sockets at once as well as a map of
    tokens to retrieve or send a file.

  Buffer: The first version used BufferedWriter/Reader and later on we added
    BufferedInput/OutputStream to handle the binary data which led to a lot of
    debugging since the buffer would eat the data but not always. We had to
    switch to a DataInputStream which still provided a buffer while being usable
    for both text and binary data

  UTF8 support: DataInputStream has both writeUTF and readUTF methods but those
    implementations use the first two bytes to store the length of the string.
    This would have been good to know before we started since that would have
    solved our marker issue early on but with the current implementation there
    would be a lot to change in both the application and the protocol.
-->

---

# Concurrency

<div class="flex items-center justify-between space-x-10">
  <div class="flex flex-col space-y-20">
```java
public void launch() {
  try (ServerSocket ServerSocket
        = new ServerSocket(port, backlog, iaddress);
      ExecutorService executor
        = Executors.newFixedThreadPool(n);) {
    while (!serverSocket.isClosed()) {
      Socket clientSocket = serverSocket.accept();
      executor.submit(new ClientHandler(clientSocket, this));
    }
}

static class ClientHandler implements Runnable {
public void run() {/_ handle request _/}
}

```

  </div>
  <div>

```java
public class ServerParser extends ConnectionParser {
  ConcurrentHashMap<Path, ReentrantLock> fileLocks
      = new ConcurrentHashMap<>();

  private ReentrantLock getLockForFile(Path filename) {
    return fileLocks.computeIfAbsent(
      filename, k -> new ReentrantLock()
    );
  }

  private void lockFile(Path filename) {
    getLockForFile(filename).lock();
  }

  private void unlockFile(Path filename) {
    getLockForFile(filename).unlock();
  }

  private void list(Path path) throws IOException {
    lockFile(full_path);
    unlockFile(full_path);
  }
```

  </div>
</div>

---

# Docker

<div class="flex items-stretch justify-between gap-4">
  <div>
    <span class="text-lg">
      server
    </span>

```bash
docker run --rm                   \
  -p 127.0.0.1:1234:1234          \
  -v "./server-data:/data"        \
  ghcr.io/thynkon/dai-pw-02:latest\
  --mode server                   \
  --address 0.0.0.0               \
  --root-dir /data                \
  --connections 2
```

  </div>
  <div>
  <span class="text-lg">
    client
  </span>

```bash
docker run --rm                   \
  -v "./client-data:/data"        \
  ghcr.io/thynkon/dai-pw-02:latest\
  --mode client                   \
  --address host.docker.internal  \
  --root-dir /data                \
```

  </div>
  <div>RIGHT</div>
</div>

---

# Github actions

<div class="flex items-stretch justify-between gap-4">
  <div>
      <img src="/assets/github-workflows-1.png" alt="github workflows configurations" width="500"/>
    </div>
    <div class="flex flex-col gap-10">
      <div>
        <img src="/assets/github-workflows-3.png" alt="github workflows configurations" width="400"/>
      </div>
      <div class="flex justify-center">
        <img src="/assets/github-workflows-2.png" alt="github workflows configurations" width="200"/>
      </div>
    </div>
</div>

---

# Demo

<p style="opacity:1">
<a href="https://asciinema.org/a/mlgXHZX7MviE3ArVzCsYEnHHo" target="_blank"><img src="https://asciinema.org/a/mlgXHZX7MviE3ArVzCsYEnHHo.svg" /></a>
</p>

---

# What could be improved
