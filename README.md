# CC Peripherals (Fabric 1.20.1)

Adds two peripherals for CC: Tweaked, plus a creative tab ("CC Peripherals") holding both blocks.

Requires: Minecraft 1.20.1, Fabric Loader 0.15+, Fabric API, CC: Tweaked 1.120.x.

## Chat Box  (peripheral type `chat_box`)
| Method | Description |
|---|---|
| `sendMessage(message [, prefix])` | Message to all players. Prefix defaults to "ChatBox", `""` for none. |
| `sendMessageToPlayer(message, player [, prefix])` | Message to one online player. |
| `getPlayers()` | Names of online players. |

Send methods return `true`, or `false, reason` (cooldown 0.5 s, empty/too long message, player offline).

## Weather Sensor  (peripheral type `weather_sensor`)
`getWeather()` ("clear"/"rain"/"thunder"), `isRaining()`, `isThundering()`, `isClear()`,
`isRainingHere()` (open sky + rain biome), `getRainLevel()`, `getThunderLevel()`,
`hasWeather()`, `getRainTime()`, `getThunderTime()`, `getClearTime()` (ticks).

## Example
```lua
local chat = peripheral.find("chat_box")
local sensor = peripheral.find("weather_sensor")
local last
while true do
  local w = sensor.getWeather()
  if w ~= last then chat.sendMessage("Weather is now: " .. w, "Weather") last = w end
  sleep(5)
end
```

## Build
1. Run `gradle wrapper --gradle-version 8.5` once (or copy a wrapper from the Fabric example mod), JDK 17.
2. `./gradlew build` -> `build/libs/cc-peripherals-1.0.0.jar`; `./gradlew runClient` to test.

Recipes: Chat Box = stone ring + note block + redstone; Weather Sensor = stone ring + lightning rod + redstone.
