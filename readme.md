# PlaytimeRewards

[![Spiget Downloads](https://img.shields.io/spiget/downloads/100231?label=Downloads)](https://www.spigotmc.org/resources/100231/)
[![Spiget Rating](https://img.shields.io/spiget/stars/100231?color=8132a8&label=Rating)](https://www.spigotmc.org/resources/100231/)
[![Spiget Size](https://img.shields.io/spiget/download-size/100231?label=Size)](https://www.spigotmc.org/resources/100231/)

[![Tested Versions](https://img.shields.io/spiget/tested-versions/100231?color=ffee33&label=Tested%20Versions)](https://www.spigotmc.org/resources/100231/) 
[![bStats Servers](https://img.shields.io/bstats/servers/14369?label=Current%20Servers)](https://bstats.org/plugin/bukkit/PlaytimeRewards_Rec/14369#servers)
[![bStats Players](https://img.shields.io/bstats/players/14369?label=Current%20Players)](https://bstats.org/plugin/bukkit/PlaytimeRewards_Rec/14369#players)

A small Spigot Plugin that keeps track of each User's Playtime and Rewards them.
Contains Support for PlaceholderAPI 

## Configuration Files

### Config.yml
In the [config.yml](src/main/resources/config.yml) are Options for the whole Plugin defined.
There are also some Values not Visible by default, if you need them you can just paste them into your 
Config.yml and the Plugin recognizes them on the next reload.
<details>
<summary>Hidden Values</summary>
(Note that the Default Values are shown)

```yml
# If set to 'false', opt-out of bStats Data collection
AllowMetrics: true

# The Color Character for the lang.yml File
ColorCode: '§'
```
</details>

### Rewards.yml
The [rewards.yml](src/main/resources/rewards.yml) contains the Data for all Rewards 
players should get awarded with.
Many Variables are optional from the Default rewards.yml, refer to the Comments of the Variables.

### Lang.yml
The last configuration File - the lang.yml - manages all texts sent from the Plugin.

(This File gets generated from the Program-Code itself, so there's no default File Link.
But you may take a look at [this File](src/main/com/github/exobite/mc/playtimerewards/utils/Msg.java) 
if you want to see where the Values are coming from).

Every Message is editable by the User. Also every Minecraft Color-Code gets translated if 
you want to have Colored Messages.

Some Messages use Variables - they contain Placeholders in form of `%[*]`. 
These Placeholders get replaced with the Variable's Value at Runtime.
It is important to include the same Amount of Placeholders in your edited Messages, 
otherwise the Plugin will use the default Values for these "faulty" Messages.

#### Hex Colors
##### Single Color
You may also use Hex Colors and Hex Color Ranges in your Messages.
If you want a Message to use a Hex Color until the end of the Message (or until another
color is specified) you may use this Syntax: `SOME_MSG: §(hexcolor) Your Message!`
Here's an example:
```yml
# The Return Message from the /playtime Command
CMD_SUC_PT_OWN: |-
  §(fc03ad)Your Playtime is %[0]d %[1]h %[2]m %[3]s
  Your Sessiontime is %[4]d %[5]h %[6]m %[7]s
```
translates into ![Pink-Color](https://i.imgur.com/08PUAQ9.png)

##### Color Range
If you want to specify a Range between two Hex values, you may us this Syntax:
`SOME_MSG: §(hexcolor1-) Some Message! §(-hexcolor2)`
Another example:
```yml
# The Return Message from the /playtime Command
CMD_SUC_PT_OWN: |-
  §(031cfc-)Your Playtime is %[0]d %[1]h %[2]m %[3]s
  Your Sessiontime is %[4]d %[5]h %[6]m %[7]s§(-fc03ad)
```
translates into ![Color-Range](https://i.imgur.com/HbDeH0A.png)

## Commands
Command Parameters:

`[arg]` -> Optional Parameter

`<arg>` -> Necessary Parameter

### /Playtime 
(alias /pt)

Display the current Play- and Sessiontime of the Commandexecutor.

Permission needed: 
```
playtimerewards.cmd.playtime.own  - Permission to see own Playtime
```

Messages used:
```
'CMD_SUC_PT_OWN': Displays the own Playtime.
    -> Parameters:  0-3: The Total Playtime in Days to Seconds,
                    4-7: The current Session Time in Days to Seconds.
                    
'CMD_ERR_NO_PERMISSION': The "No Permission" Text from Commands.
```

#### /Playtime [player] 
(alias /pt [player])

Displays the current Play- and Sessiontime of the specified User.
Players with the additional 'Offline' Permission can also see the Playtime from a Offline Player

Permissions needed: 
```
playtimerewards.cmd.playtime.others - Permission to see others Playtime
playtimerewards.cmd.playtime.others.offline - Permission to see Offlineplayers Playtime
```

Messages used:
```
'CMD_SUC_PT_OTHER': Displays the specified Players Playtime.
    -> Parameters:  0 & 5: The specified Playername
                    1-4: The Total Playtime in Days to Seconds,
                    6-9: The current Session Time in Days to Seconds.
                    
'CMD_SUC_PT_OTHER_OFFLINE': Displays the specified Offlineplayers Playtime
    -> Parameters:  0: The specified Playername
                    1-4: The total Playtime in Days to Seconds
                    
'CMD_ERR_NO_PERMISSION': The "No Permission" Text from Commands.
                    
'CMD_ERR_PLAYER_NOT_FOUND': The Error Message, when a specified Player couldn't be found.
    -> Parameters:    0: The specified Player Name.
    
'CMD_ERR_TOO_MANY_REQUESTS': The Error Message when a user exceeds his Request Limit (1 Request / 10 s)
```

### /PlaytimeTop 
(alias /pttop)

Display the x (amount specified in config.yml File) Players with the biggest Playtime.

Permissions needed:
```
playtimerewards.cmd.playtimetop - Access to the Command
```

Messages used:
```
'CMD_SUC_PTTOP_HEADER': The first Row(s) of the returned Text
    -> Parameters:	0: The Amount of listed Players
    
'CMD_SUC_PTTOP_ENTRY': This Message gets printed once for Each Data-Entry
    -> Parameters:	0: The No# on the Leaderboard
                        1: The Players Name
                        2-5: The Playtime in Days to Seconds
                        
'CMD_ERR_NO_PERMISSION': The "No Permission" Text from Commands.
```

### /PlaytimeRewards <list|reload> 
(alias /ptr <list|reload>)

#### /PlaytimeRewards list 
(alias /ptr list)

This Command lists all known Rewards.

Permissions needed:
```
playtimerewards.cmd.playtimerewards.list - Access to the Command
```

Messages used:
```
'CMD_SUC_PTR_LIST_HEADER': The first Row(s) of the returned Text
        -> Parameters:	0: The Amount of listed Rewards
		
'CMD_SUC_PTR_LIST_ENTRY': This Message gets printed once for Each Data-Entry
        -> Parameters:	0: The Internal Name of the Reward
                        1: The DisplayName of the Reward
                        2: The CountType of the Reward

'CMD_ERR_NO_PERMISSION': The "No Permission" Text from Commands.
```

#### /PlaytimeRewards reload
(alias /ptr reload)

This Command reloads all configuration data from the files
e.g. it reloads the rewards.yml, the lang.yml and the config.yml.

Permissions needed:
```
playtimerewards.cmd.playtimerewards.reload - Access to the Command
```

Messages used:
```					
'CMD_SUC_PTR_RELOAD_SUCCESS': The returned Message from the Command

'CMD_ERR_NO_PERMISSION': The "No Permission" Text from Commands.
```

## Permissions

Permissions for Commands are listed beneath the Command the Permission belongs to.
These are all Permissions the Plugin contains (copied from [plugin.yml](src/main/resources/plugin.yml)):
```yml
  playtimerewards.*:
    description: Grants all Permissions, excluding playtimerewards.afk.ignore
    default: op
    children:
      playtimerewards.notifyOnUpdate: true
      playtimerewards.cmd.playtime.other: true
      playtimerewards.cmd.playtimetop: true
      playtimerewards.cmd.playtimerewards.*: true

  playtimerewards.notifyOnUpdate:
    description: Sends Update Notification upon Login
    default: op

  playtimerewards.cmd.playtime.own:
    description: Grants access to the /playtime command (can only see own playtime)

  playtimerewards.cmd.playtime.other:
    description: Grants access to the /playtime command (including seeing others playtime)
    default: op
    children:
      playtimerewards.cmd.playtime.own: true

  playtimerewards.cmd.playtime.other.offline:
    description: Grants access to the /playtime command (including seeing others playtime, even from offline players)
    default: op
    children:
      playtimerewards.cmd.playtime.other: true

  playtimerewards.cmd.playtimetop:
    description: Grants access to the /playtimetop command

  playtimerewards.cmd.playtimerewards.*:
    description: Grants full access to the /playtimerewards command
    default: op
    children:
      playtimerewards.cmd.playtimerewards.list: true
      playtimerewards.cmd.playtimerewards.editreward: true
      playtimerewards.cmd.playtimerewards.reload: true

  playtimerewards.cmd.playtimerewards.list:
    description: Grants access to the /playtimerewards list command

  playtimerewards.cmd.playtimerewards.editreward:
    description: Grants access to the /playtimerewards editReward command

  playtimerewards.cmd.playtimerewards.reload:
    description: Grants access to the /playtimerewards reload command

  playtimerewards.afk.ignore:
    description: Get never flagged as AFK with this Permission
```


## External Dependencies

### Soft Dependencies
The following Dependencies aren't needed to run the Plugin, but enhance its functionality.

#### PlaceholderAPI
This Plugin hooks into [PlaceholderAPI](https://github.com/PlaceholderAPI/PlaceholderAPI).

It adds the following Placeholders to PlaceholderAPI:

`%ptr_playtime%` -> Returns the Playtime for the Player, the message format is defined as `EXT_PAPI_TIME_FORMAT`

`%ptr_sessiontime%` -> Returns the Sessiontime  for the Player, the message format is defined as `EXT_PAPI_TIME_FORMAT`

You may also use other Placeholders in your lang.yml File when PlaytimeRewards is hooked into
PlaceholderAPI by just typing the Placeholder in the lang.yml File.


