---
layout: default
title: Installation
parent: Server Owners
nav_order: 1
---

# Installation
{: .no_toc }

## Table of contents
{: .no_toc .text-delta }

1. TOC
{:toc}

---

## As a Spigot plugin

1. Download the latest `EzSkills-<version>.jar` from [GitHub Releases](https://github.com/ez-plugins/EzSkills/releases) or build from source.
2. Place the jar in your server's `plugins/` directory.
3. Start (or restart) the server.
4. Default configuration files are generated automatically under `plugins/EzSkills/`.

---

## Building from source

```bash
git clone https://github.com/ez-plugins/EzSkills.git
cd EzSkills
mvn package -DskipTests
```

The shaded jar is produced at `ezskills-plugin/target/EzSkills-<version>.jar`.


{: .note }
**For developers:** Maven / Gradle dependency setup is covered in [Developer Docs → Getting Started](getting_started).
