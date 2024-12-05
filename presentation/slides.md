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

# DAI - PW01

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
      <li>Multiple clients in concurrency</li>
      <li>Docker image on the GitHub Container Registry</li>
      <li>Maven package on the GitHub Package Registry</li>
      <li>Automatic builds using Github Actions</li>
      <li>Automatic protocol documentation compilation</li>
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

---

# Compression algorithms

<!-- TODO: Remove this slide -->

<div class="flex items-stretch justify-between">
  <div class="basis-1/2 h-full">
    <span class="text-lg">
      RLE
    </span>
    <ul>
      <li>Low memory usage</li>
      <li>Fast and simple</li>
      <img src="/assets/RLE-v1.svg" alt="RLE drawing" width="300"/>
      <li>Derivative implementation</li>
      <img src="/assets/RLE-v2.svg" alt="RLE drawing" width="300"/>
    </ul>
  </div>
  <div class="basis-1/2">
    <span class="text-lg">
      LZW
    </span>
    <ul>
      <li>Memory-intensive algorithm</li>
      <li>Dictionnaries are built during runtime</li>
    </ul>
    <img src="https://www.eecs.yorku.ca/course_archive/2019-20/F/2030/labs/lab4/fig1c.png"  alt="drawing" width="300"/>
  </div>
</div>

---

# Docker and Github Actions

<div class="flex items-stretch justify-between gap-4">
  <div>
    <span class="text-lg">
      Docker
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

```bash
docker run --rm                   \
  -v "./client-data:/data"        \
  ghcr.io/thynkon/dai-pw-02:latest\
  --mode client                   \
  --address host.docker.internal  \
  --root-dir /data                \
```

  </div>
  <div>
    <span class="text-lg">
      Github actions
    </span>
    <img src="/assets/github-workflows.png" alt="github workflows configurations" width="300"/>
  </div>
</div>

---

# What could be improved
