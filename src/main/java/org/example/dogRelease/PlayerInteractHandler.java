package org.example.dogRelease;

import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.PistonMoveReaction;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.damage.DamageSource;
import org.bukkit.entity.*;
import org.bukkit.entity.memory.MemoryKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.loot.LootTable;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class PlayerInteractHandler implements Listener {

    public Main main;

    public PlayerInteractHandler(Main main) {
        this.main = main;
    }

    @EventHandler
    public void OnPlayerInteract(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();

        if(!(event.getRightClicked() instanceof Wolf wolf)) {
            return;
        }

        if (!wolf.isTamed()) {
            return;
        }

        if (wolf.getOwner() == null) {
            return;
        }

        if (!(wolf.getOwner().getUniqueId().equals(player.getUniqueId()))) {
            return;
        }

        if (!isReleaseItem(player.getInventory().getItemInMainHand())) {
            return;
        }

        Wolf newWolf = (Wolf) wolf.getWorld().spawnEntity(wolf.getLocation(), EntityType.WOLF);
        newWolf.setVariant(wolf.getVariant());
        wolf.remove();

        player.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, newWolf.getLocation(), 20, 0.5, 0.5, 0.5);

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            onlinePlayer.playSound(newWolf.getLocation(), Sound.ENTITY_WOLF_HOWL, 1.0f, 1.0f);
        }

        event.setCancelled(true);
    }

    private boolean isReleaseItem(ItemStack item) {
        FileConfiguration config = main.getConfig();

        Material expectedType = Material.matchMaterial(config.getString("release-item.type", "STICK"));

        if (item.getType() != expectedType) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();
        String expectedName = config.getString("release-item.name", "");

        main.getLogger().info("expected name: " + !expectedName.isEmpty());
        if (expectedName.isEmpty()) {
            if (meta.hasDisplayName()) {
                main.getLogger().info("return false name");
                return false;
            }
        } else {
            if (meta == null) {
                return  false;
            }

            String translatedName = ChatColor.translateAlternateColorCodes('&', expectedName);

            if (!meta.hasDisplayName()) {
                return false;
            }

            if (!meta.getDisplayName().equals(translatedName)) {
                return false;
            }
        }

        List<String> expectedLore = config.getStringList("release-item.lore");
        main.getLogger().info("expected lore: " + !expectedLore.isEmpty());
        if (expectedLore.isEmpty()) {
            if (meta.getLore() != null) {
                main.getLogger().info("return false lore");
                return false;
            }
        } else {
            if (meta == null) {
                return  false;
            }

            List<String> itemLore = meta.getLore();

            if (itemLore == null) {
                return false;
            }

            if (itemLore.size() != expectedLore.size()) {
                return false;
            }

            for (int i = 0; i < expectedLore.size(); i++) {
                String expectedLine = ChatColor.translateAlternateColorCodes('&', expectedLore.get(i));

                if (!expectedLine.equals(itemLore.get(i))) {
                    return false;
                }
            }
        }

        return true;
    }
}
