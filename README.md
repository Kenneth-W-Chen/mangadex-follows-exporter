# About

This application exports a user's MangaDex followed manga.

# Requirements
* An internet connection
* A MangaDex account and [API Client credentials](https://api.mangadex.org/docs/02-authentication/personal-clients/)

# Installation
* Download and run the latest executable in the releases page.
* Alternatively, download the jar if you have Java 21+ installed.

# to-do
* Import stuff to MangaUpdates
  * The ID that MangaDex provides for MangaUpdates is not usable by MangaUpdates' API. MangaUpdates has a different ID.
  * Can call a GET with the link md gives to fetch the actual webpage and then parse the html for the rss feed link for the actual ID. Might use this for titles that fail
* Export chapter read status
  * Will probably be a relational database scheme. Not sure if I'm going to add sql or just keep it in a csv/txt. Will be a separate csv from the titles though 