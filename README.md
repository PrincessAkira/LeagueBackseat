# LeagueBackseat

## About

This is a TwitchBot that allows viewers to Backseat the streamer.

## How to launch the TwitchBot

- Install Java (17 preferred)
- Download the latest release from Releases
- Rename `config.json.example` to `config.json`
- Edit the `config.json` file to your liking
- Run the jar file with `java -jar LeagueBackseat.jar`
- Get roasted by your Viewers

## How to setup the project to help with the development
### IntelliJ IDEA
1. Open **IntelliJ IDEA**.
2. Select **Open**.
3. Locate the **LeagueBackseat** folder, select it and press **OK**.
4. Go to **File->Project Structure** and under **Project** select **JDK 17** as the current SDK.
5. The project should now have no issues when trying to build it.

## Commands
| Command | Description |
| --- | --- |
| `!add <timestamp> <message>` | Adds a message to the queue.|
| `!info` | Shows the message at the given timestamp.|
| `!next` | Enters next session file.|
| `!list <@user>` | Gets a number of how many Backseats the user has added in the current session.|
| `!searchall <IndexID>` | Searches current Session for the given IndexID.|
| `!searchuser <@user> <Keyword>` | Searches current Session for the given Keyword and User and returns every message with keyword from that user.|

## config.json explanation
| JSON key and value | Description |
| --- | --- | 
| `"prefix" : "prefixhere"` | Here you can set your prefix, for example if you wanna use ! or - at the beginning of the command.|
| `"token" : "tokenhere"` | Here you need to enter the Twitch Token, the token can be created [here](https://twitchtokengenerator.com/).|
| `"channel" : "channelhere"` | Here you need to enter the name of your Twitch channel that you are streaming on. <br> |
| `"admin" : "ur id here"` | Here you have to enter the ID of your Twitch account where you stream on. You can convert your Twitch Username to Twitch Channel ID [here](https://www.streamweasels.com/tools/convert-twitch-username-to-user-id/).|
| `"secret" : "secret here"` | To receive the client secret you need to follow through this [documentation](https://dev.twitch.tv/docs/authentication/register-app/) from Twitch.|
| `"clientid" : "client id here"` | To receive the client id you need to follow through this [documentation](https://dev.twitch.tv/docs/authentication/register-app/) from Twitch.|
| `"obsscene" : "name here"` | Here you will have to enter the name of your scene in OBS.|
| `"refreshtoken" : "refresh token here"` | Your refresh token will also be generated when you completed creating your token [here](https://twitchtokengenerator.com/).|

## TODO

- [X] Add a way for viewers to queue the messages from the textfile
- [X] Save in TxT and JSON to allow for the above
- [ ] GUI? (prob not)
- [X] Clear file on command
- [X] Make multiple files possible on the same day
- [X] Folder for each day

## Credits

- [this one guy there that helped me test](https://www.discordapp.com/users/294910497499774976)
- [DrPanger for the Idea](https://www.twitch.tv/drpanger)

<div align="center">

## Author

  <a href="https://www.discordapp.com/users/202740603790819328" >
   <img src="https://lanyard.kyrie25.me/api/202740603790819328?waveColor=8B8BFA&waveSpotifyColor=B48EF7&gradient=7E37F9-B48EF7-E568C4&imgStyle=square"  />
  </a>
</div>
