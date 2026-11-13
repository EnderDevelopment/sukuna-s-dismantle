# Minecraft Plugin: Sukuna's Dismantle

Sukuna's Dismantle is a Paper Minecraft plugin that adds a custom combat item, right click ability, slash particles, entity ray tracing, damage, cooldown handling, custom item identification, and permission controlled item distribution.

## Install

1. Build the plugin with Java 21 and Maven:

   ```text
   mvn package
   ```

2. Copy `target/plugin.jar` to the `plugins` folder of a Paper 1.21.11 server.

3. Start or restart the server.

4. Give yourself permission to use the item if needed:

   ```text
   /lp user <player> permission set slice.use true
   ```

5. The plugin uses custom model data value `7123`. Add a compatible resource pack if you want to provide a custom inventory appearance.

## Use

1. Join the server with the `slice.use` permission.
2. Run:

   ```text
   /slice
   ```

3. Hold the received item and right click into the air or at a block.
4. The plugin displays a slash particle effect and plays a sweeping attack sound.
5. The ability ray traces up to 6 blocks in the direction the player is looking.
6. The first valid damageable entity hit takes 5 damage.
7. The ability has a 3 second cooldown. The remaining cooldown appears in the action bar when the ability is used too early.

The item is identified internally as an Iron Sword named “Slice” and uses persistent item data. The plugin source does not include a resource pack or custom model file.

---

More Minecraft plugin projects by EnderDevelopment: https://github.com/EnderDevelopment

Project page: https://dash.enderdevelopment.com/community/312b3e47-670c-4afe-a609-018cc3bd12bb?utm_source=github&utm_medium=readme&utm_campaign=auto_publish&utm_content=312b3e47-670c-4afe-a609-018cc3bd12bb
