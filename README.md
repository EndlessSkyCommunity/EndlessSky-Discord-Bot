# EndlessSky-Discord-Bot
[![Build Status](https://travis-ci.org/EndlessSkyCommunity/EndlessSky-Discord-Bot.svg?branch=master)](https://travis-ci.org/MCOfficer/EndlessSky-Discord-Bot)
[![license](https://img.shields.io/aur/license/yaourt.svg)](https://github.com/MCOfficer/EndlessSky-Discord-Bot/tree/master/LICENSE)

Meet James, the Discord Bot made specifically for the Endless Sky Server.

## Installation
### Requirements
- A JDK (Java 11 or better)
- gradle on Linux, on Windows use depr_gradlew.bat instead
### Setup
1. Clone or fork this repository
2. Using the Discord API, make a new Bot and save token in `james.properties` (follow [this guide](https://github.com/DV8FromTheWorld/JDA/wiki/3%29-Getting-Started) until "2. Setup JDA Project" to obtain a token)
3. Acquire a GitHub API Key and save it in `james.properties`
5. Start James by executing `gradle run` or `depr_gradlew run`

## Features
- Displays portions of the Endless Sky data files (Ships & their variants, Outfits, Sprites/Thumbnails, Missions)
- Links to PRs, commits and issues of the Endless Sky repository
- Links to [various](http://endless-sky.7vn.io/) [online](http://bunker.tejat.net/endless-ships/) [resources](https://endlesssky.mcofficer.me/ship_gallery/)
- Basic Moderation Commands (purge and move messages, timeout bad boys/girls)
- Several tools for Plugin Creators, such as various Templates & Infos about the swizzles used by the game
- Some fun commands (random dogs and cats etc.)
- Full Music Player functionality using [lavaplayer](https://github.com/sedmelluq/lavaplayer)

## Credit
Original Creator: @Wrzlprnft

Original Maintainer / Hoster: @Nechochwen-D

Current Maintainer / Hoster: @MCOfficer

Contributors / PR Bots:
 - @tehhowch
 - @warp-core

24/7 Development Support: @MinnDevelopment and the JDA Discord Server
