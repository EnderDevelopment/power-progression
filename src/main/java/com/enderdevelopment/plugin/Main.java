package com.enderdevelopment.plugin;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.entity.Player;
import org.bukkit.entity.Entity;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.CommandExecutor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public final class Main extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        this.getCommand("power").setExecutor(new PowerCommand());
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        int currentPower = getPlayerPower(player);
        setPlayerPower(player, currentPower - 1);
        updatePlayerAttributes(player);
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer != null) {
            int currentPower = getPlayerPower(killer);
            setPlayerPower(killer, currentPower + 1);
            updatePlayerAttributes(killer);
        }
    }

    private int getPlayerPower(Player player) {
        return player.getPersistentDataContainer().getOrDefault(
            new org.bukkit.NamespacedKey(this, "power"),
            org.bukkit.persistence.PersistentDataType.INTEGER,
            5
        );
    }

    private void setPlayerPower(Player player, int power) {
        player.getPersistentDataContainer().set(
            new org.bukkit.NamespacedKey(this, "power"),
            org.bukkit.persistence.PersistentDataType.INTEGER,
            Math.max(1, Math.min(10, power))
        );
    }

    private void updatePlayerAttributes(Player player) {
        int power = getPlayerPower(player);
        AttributeInstance speed = player.getAttribute(Attribute.valueOf("GENERIC_MOVEMENT_SPEED"));
        AttributeInstance health = player.getAttribute(Attribute.valueOf("GENERIC_MAX_HEALTH"));
        AttributeInstance attackDamage = player.getAttribute(Attribute.valueOf("GENERIC_ATTACK_DAMAGE"));

        if (speed != null) {
            speed.setBaseValue(0.1 + (power * 0.01));
        }
        if (health != null) {
            health.setBaseValue(20 + (power * 2));
        }
        if (attackDamage != null) {
            attackDamage.setBaseValue(1 + (power * 0.5));
        }

        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, power - 1, false, false));
    }

    private class PowerCommand implements CommandExecutor {
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                int power = getPlayerPower(player);
                player.sendMessage(Component.text("Your current power level is: ").color(NamedTextColor.GOLD).append(Component.text(power).color(NamedTextColor.RED)));
            } else {
                sender.sendMessage("This command can only be used by players.");
            }
            return true;
        }
    }
}