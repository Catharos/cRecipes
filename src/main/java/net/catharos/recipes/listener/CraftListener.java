package net.catharos.recipes.listener;

import net.catharos.recipes.cRecipes;
import net.catharos.recipes.crafting.CustomRecipe;
import net.catharos.recipes.util.InventoryUtil;
import net.catharos.recipes.util.TextUtil;

import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;

public class CraftListener implements Listener {
	protected cRecipes plugin;

	public CraftListener( cRecipes plugin ) {
		this.plugin = plugin;
		Bukkit.getServer().getPluginManager().registerEvents( this, plugin );
	}

	@EventHandler
	public void c( CraftItemEvent event ) {
		if (event.isCancelled()) event.setCancelled( true );

		ItemStack result = event.getInventory().getResult();
		if (result == null) return;

		CustomRecipe cr = plugin.getRecipe( result.getTypeId(), result.getData().getData() );

		if (cr != null && event.getRecipe().equals(cr.getRecipe())) {
			event.setCurrentItem( cr.getItem() );

			if (event.isShiftClick()) event.setCancelled( true );

			String perm = cr.getPermission();
			HumanEntity entity = event.getWhoClicked();

			if (!(entity instanceof Player)) return;
			Player player = (Player) entity;

			if (!perm.isEmpty() && !entity.isOp() && !entity.hasPermission( perm )) {
				event.setCancelled( true );

				String msg = cr.getNoPermissionMessage();
				if (msg == null || msg.isEmpty()) msg = plugin.getConfig().getString( "messages.no-permissions" );

				if (msg != null && !msg.isEmpty()) player.sendMessage( TextUtil.parseColors( msg ) );

				return;
			}

			// Experience
			if (cr.getXPNeeded() > 0) {
				int xp = cr.getXPNeeded();
				int pxp = player.getTotalExperience();

				if (pxp < xp) {
					event.setCancelled( true );

					String msg = cr.getNotEnoughXpMessage();
					if (msg == null || msg.isEmpty()) msg = plugin.getConfig().getString( "messages.not-enough-xp" );

					if (msg != null && !msg.isEmpty()) player.sendMessage( TextUtil.parseArguments( msg, xp, pxp ) );
				} else if (cr.subtractXp()) {
					player.setTotalExperience( pxp - xp );
				}
			}

			if (cr.getXPGiven() > 0) player.setExp( player.getExp() + cr.getXPGiven() );

			// Levels
			if (cr.getLvlNeeded() > 0) {
				int lvl = cr.getLvlNeeded();

				if (player.getLevel() < lvl) {
					event.setCancelled( true );

					String msg = cr.getNotEnoughLvlMessage();
					if (msg == null || msg.isEmpty()) msg = plugin.getConfig().getString( "messages.not-enough-lvl" );

					if (msg != null && !msg.isEmpty()) player.sendMessage( TextUtil.parseArguments( msg, lvl, player.getLevel() ) );
				} else if (cr.subtractLvl()) {
					player.setLevel( player.getLevel() - lvl );
				}
			}

			if (cr.getLvlGiven() > 0) {
				player.setLevel( player.getLevel() + cr.getLvlGiven() );
			}

			if (!cr.getExtraDrops().isEmpty()) {
				InventoryUtil.givePlayer( player, cr.getExtraDrops() );
			}

			if (!cr.getSuccessMessage().isEmpty()) {
				player.sendMessage( TextUtil.parseColors( cr.getSuccessMessage() ) );
			}
		}
	}

}
