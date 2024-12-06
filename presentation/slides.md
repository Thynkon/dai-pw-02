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

---


# Binary vs text files

<div class="flex items-center justify-between">
  <div>LEFT</div>
  <div class="flex flex-col space-y-10">RIGHT</div>
</div>

---

# Concurrency

<div class="flex items-center justify-between">
  <div>LEFT</div>
  <div class="flex flex-col space-y-10">RIGHT</div>
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

# What could be improved
