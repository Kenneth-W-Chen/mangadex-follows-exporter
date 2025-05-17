# About

This application exports a user's MangaDex followed manga.

Currently, it exports the title and the links listed on the MangaDex page to separate files. Titles/links are separated by newlines.

Current plan is to build a simple GUI for this and then follow titles on MangaUpdates.

# Setup
This program expects 2 files (both located in the directory the executable is in, or the working directory of the project [depending on how you run this app]):
* `config.properties`
* `secrets.properties`

1. Follow the steps outlined [here](https://api.mangadex.org/docs/02-authentication/personal-clients/#registering-an-api-client) to get API client credentials.
2. In `secrets.properties`, add the following lines (replace with your info): 
```properties
username=<your_username>
password=<your_password>
client-id=<your_client_id>
client-secret=<your_client_secret>
```
3. In `config.properties`, add/remove the links you want saved. Syntax:
```properties
links=mu,nu,al,ap
```
*See [this link](https://api.mangadex.org/docs/3-enumerations/#manga-links-data) for the link naming definitions*