# About

This application exports a user's list of followed manga on MangaDex.

# Requirements
* An internet connection
* A MangaDex account and [API Client credentials](https://api.mangadex.org/docs/02-authentication/personal-clients/)
* A MangaUpdates account (optional if not exporting to MangaUpdates)

# Installation
* Download and run the latest executable in the releases page.
* Alternatively, download the jar if you have Java 21+ installed.

# Building
* Download the git repository and unzip it if necessary.
* In a terminal, navigate to the repository directory and run
```bash
./gradlew build
 ```
* If you want to make an installer, run
```bash
./gradlew createExe
```