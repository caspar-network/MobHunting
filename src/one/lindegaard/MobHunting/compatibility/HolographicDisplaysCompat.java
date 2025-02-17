package one.lindegaard.MobHunting.compatibility;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;

import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import one.lindegaard.MobHunting.MobHunting;

public class HolographicDisplaysCompat {

	private static Plugin mPlugin;
	private static boolean supported = false;

	// https://dev.bukkit.org/projects/holographic-displays

	public HolographicDisplaysCompat() {
		if (!isEnabledInConfig()) {
			Bukkit.getConsoleSender().sendMessage(ChatColor.GOLD + "[MobHunting] " + ChatColor.RESET
					+ "Compatibility with Holographic Displays is disabled in config.yml");
		} else {
			mPlugin = Bukkit.getPluginManager().getPlugin(CompatPlugin.HolographicDisplays.getName());

			Bukkit.getConsoleSender()
					.sendMessage(ChatColor.GOLD + "[MobHunting] " + ChatColor.RESET
							+ "Enabling compatibility with Holographic Displays ("
							+ mPlugin.getDescription().getVersion() + ").");
			supported = true;
		}
	}

	// **************************************************************************
	// OTHER
	// **************************************************************************

	public Plugin getHologramsPlugin() {
		return mPlugin;
	}

	public static boolean isSupported() {
		return supported;
	}

	public static boolean isEnabledInConfig() {
		return MobHunting.getInstance().getConfigManager().enableIntegrationHolographicDisplays;
	}

	public static HologramsAPI getHologramsAPI() {
		return ((HologramsAPI) mPlugin);
	}

}
