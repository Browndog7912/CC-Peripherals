# CC Peripherals (Fabric 1.20.4)

Adds nine peripheral blocks for CC: Tweaked, pocket computer upgrades for two of them, and a creative tab ("CC Peripherals") holding the blocks.

Requires: Minecraft 1.20.4, Fabric Loader 0.15+, Fabric API, CC: Tweaked 1.110.2 (1.20.4 Fabric).

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

## Environment Sensor  (peripheral type `environment_sensor`)
`getDimension()` (e.g. "minecraft:overworld"), `getBiome()` (e.g. "minecraft:plains"),
`getPosition()` ({x,y,z}), `getX()`, `getY()`, `getZ()`, `getChunkPosition()` ({x,z}),
`isSlimeChunk()` (Overworld only; false elsewhere. Swamps can also spawn slimes regardless of chunk).

## Player Sensor  (peripheral type `player_sensor`)
Events (the first argument after the event name is the peripheral's name, as with `monitor_touch`):

| Event | Arguments | When |
|---|---|---|
| `player_click` | name, username, uuid, face | A player right-clicks the block (`face` is e.g. "north", "up"). |
| `player_touch` | name, username, uuid | A player touches the block. Fires once per contact. |
| `player_join` | name, username, uuid | A player joins the world. |
| `player_leave` | name, username, uuid | A player leaves the world. |

Methods: `getPlayers()` (online usernames), `isPlayerOnline(username)`.

Touch works from the block's sides; standing on top of it does not count. Join/leave events are delivered to
every Player Sensor that has a computer attached and is in a loaded chunk.

```lua
local sensor = peripheral.find("player_sensor")
while true do
  local event, name, user = os.pullEvent()
  if event == "player_click" or event == "player_touch" then print(user, "used", name)
  elseif event == "player_join" then print(user, "joined")
  elseif event == "player_leave" then print(user, "left") end
end
```

## NFC Reader and NFC Cards  (peripheral type `nfc_reader`)
NFC cards come in six colours: red, green, yellow, purple, blue and white. Craft one from paper, redstone,
an iron nugget and the matching dye (shapeless). Each card gets a unique id and can hold up to 1024 characters.

Right-click the reader with a card to insert it, and right-click it with an empty hand to take the card back.
Breaking the reader gives the card back. The reader's top shows a green light while a card is inside.

| Method | Description |
|---|---|
| `hasCard()` | true if a card is inserted. |
| `getCardColor()` | "red", "green", "yellow", "purple", "blue" or "white" (nil if no card). |
| `getCardId()` | The card's unique id (nil if no card). |
| `read()` | The stored string (nil if blank), or nil and a reason if there is no card. |
| `write(data)` | Stores a string on the card, replacing what was there. true, or false and a reason. |
| `clear()` | Erases the data (the card keeps its id). |
| `eject()` | Pops the card out above the reader. |
| `getMaxLength()` | 1024. |

Events: `nfc_card_inserted` and `nfc_card_removed`, each with (name, color, id).

```lua
local reader = peripheral.find("nfc_reader")
while true do
  local _, name, color, id = os.pullEvent("nfc_card_inserted")
  local data = reader.read()
  if data == "door:open" then print("Access granted for", color, "card", id)
  else reader.write(textutils.serialiseJSON({owner = "Steve", level = 2})) end
end
```
Cards are not copy-protected: anyone with a reader can write any data to a card, so check the card id as well
as the data if you use cards for access control.

## NFC Scanner  (peripheral type `nfc_scanner`)
A keycard-style scanner. Right-click it holding an NFC card (don't sneak): the card is read, the scanner flashes
green, and attached computers get an event. The card **stays in your inventory** and the scanner can only read,
never write. Anything that isn't an NFC card is ignored.

Event: `nfc_scan` with (name, color, id, data, username, uuid). `data` is nil if the card is blank.
`getLastScan()` returns the latest scan as a table, in case your program missed the event.

```lua
while true do
  local _, name, color, id, data, user = os.pullEvent("nfc_scan")
  if color == "green" and data == "door:open" then
    redstone.setOutput("back", true) sleep(2) redstone.setOutput("back", false)
  end
end
```
Anyone can write data to cards with an NFC Reader, so for real security check the card `id` against a list you
trust instead of relying on `data` alone.

## Computer Light  (peripheral type `light`)
A lamp that computers switch on and off. Methods: `setLit(bool)`, `isLit()`, `toggle()` (returns the new state),
`setBrightness(0-15)`, `getBrightness()`. It is dark and gives no light until a computer turns it on.

## Computer Button  (peripheral type `button`)
Right-click it and attached computers get `button_click` with (name, username, uuid). The button stays pressed
in for half a second, and clicks during that time are ignored. `isPressed()` reports whether it is pressed in.

```lua
local lamp = peripheral.find("light")
while true do
  os.pullEvent("button_click")
  lamp.toggle()
end
```

## Redstone Port  (peripheral type `redstone_port`)
Sends and receives redstone on all six sides of the block, independently. Sides are absolute directions:
`top`, `bottom`, `north`, `south`, `east`, `west` (`up` and `down` also work).

| Method | Description |
|---|---|
| `setOutput(side, bool)` | Signal out of a side: fully on (15) or off. |
| `setAnalogOutput(side, 0-15)` | Signal out of a side at a set strength. |
| `getOutput(side)` / `getAnalogOutput(side)` | What the side is currently sending out. |
| `getInput(side)` / `getAnalogInput(side)` | The signal coming in on that side. |
| `getSides()` | The six side names. |

Event: `redstone_input` with (name, side, power) when the signal coming in on a side changes.
Outputs are saved with the block. The block glows red while any side is sending a signal.

```lua
local port = peripheral.find("redstone_port")
while true do
  os.pullEvent("redstone_input")
  port.setOutput("north", port.getInput("south"))   -- relay a signal straight through
end
```

## Computer Casing
A plain decorative block with no behaviour, for building computer rooms. Craft 2 from 8 iron ingots around a redstone.

## Computer Metal
Another plain decorative block with no behaviour. It looks like metal but has stone sounds. Craft 4 from 4 iron
ingots and 5 stone in a checkerboard.

## Pocket computers
Craft a pocket computer together with a **Chat Box** or **Weather Sensor** item (pocket computer in the grid with
the block directly above it, same as the wireless modem). The peripheral is then on the "back" side:
```lua
local chat = peripheral.find("chat_box")        -- or "weather_sensor"
chat.sendMessage("Hello from my pocket!")
```
A pocket weather sensor reports `isRainingHere()` for wherever the player is standing.

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
1. Run `gradle wrapper --gradle-version 8.7` once (or copy a wrapper from the Fabric example mod), JDK 17.
2. `./gradlew build` -> `build/libs/cc-peripherals-1.0.0.jar`; `./gradlew runClient` to test.

Recipes (stone ring + redstone below + centre item): Chat Box = note block; Weather Sensor = lightning rod; Environment Sensor = compass; Player Sensor = observer; NFC Reader = comparator; NFC Scanner = tripwire hook; Computer Light = redstone lamp; Computer Button = stone button; Redstone Port = repeater.
