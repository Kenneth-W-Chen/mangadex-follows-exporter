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
MD_USERNAME=<your_username>
MD_PASSWORD=<your_password>
MD_API_CLIENT_ID=<your_client_id>
MD_API_CLIENT_SECRET=<your_client_secret>
```
3. Update `config.properties`. Syntax:
```properties
EXPORT=txt,csv,MangaUpdates
FETCH_LIMIT=100
INITIAL_OFFSET=0
LINKS=Amazon,AniList,Anime-Planet,Book Walker,CDJapan,eBookJapan,Kitsu,MangaUpdates,MyAnimeList,NovelUpdates,Official English,Raws
LOCALE_PREFERENCE=en,ja-ro,ja,ko-ro,ko,zh-ro

```
An [example file](https://github.com/Kenneth-W-Chen/mangadex-follows-exporter/blob/main/config.properties) is provided in this repository. It saves all of the links.

*See [this link](https://api.mangadex.org/docs/3-enumerations/#manga-links-data) for the link naming definitions.*
