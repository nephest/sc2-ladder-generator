# About
This is the source code of the https://www.nephest.com/sc2 website
## Disclamier
I use this project to learn the java web development process. This project is not production-ready.
## Required application properties
```
blizzard.api.key = base64 encoded client_id:client_secret
ds.jndi.url = DataSource is expected to be available via JNDI
```
## Constraints
The project uses some features of:
* Java 11
* MySQL extensions

## Task configuration
[Cron class](src/main/java/com/nephest/battlenet/sc2/config/Cron.java) contains all scheduled tasks.
