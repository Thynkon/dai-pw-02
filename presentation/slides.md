---
# You can also start simply with 'default'
theme: default
# random image from a curated Unsplash collection by Anthony
# like them? see https://unsplash.com/collections/94734566/slidev
background: https://cover.sli.dev
# some information about your slides (markdown enabled)
title: DAI - PW01
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

A compression tool written in modern JAVA

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
          <li>master</li>
          <li>features</li>
          <li>fix/hotfix</li>
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
      <li>Compression / Extraction</li>
      <li>tar files</li>
      <li>LZW</li>
      <li>RLE</li>
      <li>Docker</li>
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

# Compression algorithms

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
```dockerfile
FROM eclipse-temurin:21-jdk AS builder
WORKDIR /app
# Download build dependencies
COPY mvnw pom.xml dependency-reduced-pom.xml ./
COPY .mvn ./.mvn
RUN chmod +x ./mvnw && ./mvnw install
# Copy project and build
COPY src ./src
RUN ./mvnw package
FROM eclipse-temurin:21-jre AS prod
WORKDIR /app
COPY --from=builder /app/target/dai-lab-01-1.0.jar /app/app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
CMD ["--help"]
```

```bash
docker build -t compress-tool .
docker run --rm -v ".:/data" compress-tool:latest \
  -c -a RLE -o /data/output.tar.rle /data/input
```

  </div>
  <div>
    <span class="text-lg">
      Github actions
    </span>
```yaml
name: Publish package to GitHub Packages
on:
  release:
    types: [created]
jobs:
  publish:
    runs-on: ubuntu-latest
    permissions:
      contents: read
      packages: write
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          java-version: "21"
          distribution: "temurin"
      - name: Publish package
        run: ./mvnw deploy
        env:
          GITHUB_TOKEN: ${{ secrets.AUTH_TOKEN }}
```
  </div>
</div>

---

# What could be improved

- Handle binary files
- CI/CD pipeline
- Implement commands to modify archive file - update or delete tar entries
- Publication on the maven repository
