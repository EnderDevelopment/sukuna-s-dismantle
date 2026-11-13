package com.enderdevelopment.plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

public final class Main extends JavaPlugin implements Listener, CommandExecutor {
    private static final double DAMAGE = 5.0;
    private static final double RANGE = 6.0;

    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private SliceItem sliceItem;

    @Override
    public void onEnable() {
        sliceItem = new SliceItem(this);
        Bukkit.getPluginManager().registerEvents(this, this);
        getCommand("slice").setExecutor(this);
    }

    @Override
    public void onDisable() {
        cooldowns.clear();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Only players can use this command.", NamedTextColor.RED));
            return true;
        }
        player.getInventory().addItem(sliceItem.create());
        player.sendMessage(Component.text("You received Slice.", NamedTextColor.GREEN));
        return true;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        if (!sliceItem.isSlice(event.getItem())) {
            return;
        }

        event.setCancelled(true);
        Player player = event.getPlayer();
        long now = System.currentTimeMillis();
        long readyAt = cooldowns.getOrDefault(player.getUniqueId(), 0L);
        if (now < readyAt) {
            long remainingSeconds = (readyAt - now + 999L) / 1000L;
            player.sendActionBar(Component.text("Slice is on cooldown: " + remainingSeconds + "s", NamedTextColor.RED));
            return;
        }

        cooldowns.put(player.getUniqueId(), now + 3000L);
        showSlash(player);
        hitTarget(player);
    }

    private void showSlash(Player player) {
        var world = player.getWorld();
        var start = player.getEyeLocation().add(player.getLocation().getDirection().normalize().multiply(0.8));
        Vector forward = player.getLocation().getDirection().normalize();
        Vector side = new Vector(-forward.getZ(), 0.0, forward.getX()).normalize();
        Vector up = new Vector(0.0, 1.0, 0.0);
        for (int index = 0; index < 13; index++) {
            double progress = index / 12.0;
            double offset = (progress - 0.5) * 2.4;
            var point = start.clone().add(side.clone().multiply(offset)).add(up.clone().multiply(Math.sin(progress * Math.PI) * 0.8));
            world.spawnParticle(Particle.DUST, point, 3, 0.04, 0.04, 0.04,
                    new Particle.DustOptions(Color.fromRGB(220, 240, 255), 1.2F));
        }
        world.playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.0F, 1.35F);
    }

    private void hitTarget(Player player) {
        var eye = player.getEyeLocation();
        Vector direction = eye.getDirection().normalize();
        RayTraceResult result = player.getWorld().rayTraceEntities(eye, direction, RANGE, 0.45,
                entity -> entity != player && entity.isValid() && !entity.isDead());
        if (result == null || result.getHitEntity() == null) {
            return;
        }
        if (!(result.getHitEntity() instanceof Damageable target)) {
            return;
        }
        target.damage(DAMAGE, player);
        target.getWorld().spawnParticle(Particle.CRIT, target.getLocation().add(0, 1, 0), 12, 0.25, 0.35, 0.25, 0.15);
    }

    private static final class SliceItem {
        private final Main plugin;

        private SliceItem(Main plugin) {
            this.plugin = plugin;
        }

        private ItemStack create() {
            ItemStack item = new ItemStack(Material.IRON_SWORD);
            ItemMeta meta = item.getItemMeta();
            meta.displayName(Component.text("Slice", NamedTextColor.AQUA));
            meta.setCustomModelData(7123);
            meta.getPersistentDataContainer().set(plugin.getSliceKey(), PersistentDataType.BYTE, (byte) 1);
            item.setItemMeta(meta);
            return item;
        }

        private boolean isSlice(ItemStack item) {
            if (item == null || item.getType() != Material.IRON_SWORD || !item.hasItemMeta()) {
                return false;
            }
            Byte marker = item.getItemMeta().getPersistentDataContainer().get(plugin.getSliceKey(), PersistentDataType.BYTE);
            return marker != null && marker == 1;
        }
    }

    private org.bukkit.NamespacedKey getSliceKey() {
        return new org.bukkit.NamespacedKey(this, "slice_item");
    }
}
