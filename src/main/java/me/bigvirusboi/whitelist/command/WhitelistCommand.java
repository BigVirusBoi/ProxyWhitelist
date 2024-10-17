package me.bigvirusboi.whitelist.command;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import me.bigvirusboi.whitelist.BuildConstants;
import me.bigvirusboi.whitelist.cache.CachedPlayer;
import me.bigvirusboi.whitelist.cache.PlayerCache;
import me.bigvirusboi.whitelist.manager.Whitelist;
import me.bigvirusboi.whitelist.util.Constants;
import me.bigvirusboi.whitelist.util.DurationParser;
import me.bigvirusboi.whitelist.util.Permissions;
import me.bigvirusboi.whitelist.util.TimeUtil;
import net.kyori.adventure.text.Component;

import java.util.ArrayList;
import java.util.List;

import static me.bigvirusboi.whitelist.util.Constants.CMD;

public class WhitelistCommand extends CoreCommand {
	private final ProxyServer proxy;
	private final Whitelist whitelist;
	private final PlayerCache cache;

	public WhitelistCommand(ProxyServer proxy, Whitelist whitelist, PlayerCache cache) {
		super(Constants.MAIN_COMMAND_NAME, Permissions.BASE, Constants.COMMAND_ALIASES);
		this.proxy = proxy;
		this.whitelist = whitelist;
		this.cache = cache;
	}

	@Override
	public void execute(Invocation invocation) {
		CommandSource source = invocation.source();
		String[] args = invocation.arguments();

		if (args.length == 0) {
			source.sendMessage(Component.text("§6§lCommand usage§7 (Proxy Whitelist)"));
			source.sendMessage(Component.text("§6 /" + CMD + "§e clear <all/expired>§8 - §7Clear the whitelist"));
			source.sendMessage(Component.text("§6 /" + CMD + "§e disable§8 - §7Disable the whitelist"));
			source.sendMessage(Component.text("§6 /" + CMD + "§e enable§8 - §7Enable the whitelist"));
			source.sendMessage(Component.text("§6 /" + CMD + "§e get <player>§8 - §7Get the whitelist status of a player"));
			source.sendMessage(Component.text("§6 /" + CMD + "§e kick§8 - §7Kick everyone who is not whitelisted"));
			source.sendMessage(Component.text("§6 /" + CMD + "§e list§8 - §7List all whitelisted players"));
			source.sendMessage(Component.text("§6 /" + CMD + "§e remove <player>§8 - §7Remove a player from the whitelist"));
			source.sendMessage(Component.text("§6 /" + CMD + "§e set <player> [duration]§8 - §7Add a player to the whitelist"));
			source.sendMessage(Component.newline().append(Component.text(
					"§7Running §6§lProxy Whitelist v" + BuildConstants.VERSION + "§7 by §e" + Constants.CREDITS)));
			return;
		}

		switch (args[0].toLowerCase()) {
			case "clear" -> handleClear(source, args);
			case "disable" -> handleDisable(source);
			case "enable" -> handleEnable(source);
			case "get" -> handleGet(source, args);
			case "kick" -> handleKick(source);
			case "list" -> handleList(source);
			case "remove" -> handleRemove(source, args);
			case "set" -> handleSet(source, args);
			default -> source.sendMessage(Component.text("§cInvalid subcommand, see /" + CMD + " for usage"));
		}
	}

	@Override
	public List<String> suggest(Invocation invocation) {
		String[] args = invocation.arguments();
		List<String> arguments = new ArrayList<>();

		if (args.length == 1) {
			arguments.addAll(List.of("clear", "disable", "enable", "get", "kick", "list", "remove", "set"));
		} else if (args.length == 2) {
			switch (args[0].toLowerCase()) {
				case "clear" -> arguments.addAll(List.of("all", "expired"));
				case "get", "set" -> arguments.addAll(proxy.getAllPlayers().stream().map(Player::getUsername).toList());
				case "remove" -> arguments.addAll(whitelist.getWhitelisted().keySet().stream().map(CachedPlayer::name).toList());
			}
		}

		return arguments;
	}

	private void handleClear(CommandSource source, String[] args) {
		if (args.length == 1) {
			source.sendMessage(Component.text("§cUsage: /" + CMD + " clear <all/expired>"));
			return;
		}

		switch (args[1].toLowerCase()) {
			case "all" -> {
				source.sendMessage(Component.text("§7Cleared the whitelist"));
				whitelist.clear(false);
			}
			case "expired" -> {
				source.sendMessage(Component.text("§7Cleared expired players from the whitelist"));
				whitelist.clear(true);
			}
			default -> source.sendMessage(Component.text("§cUsage: /" + CMD + " clear <all/expired>"));
		}
	}

	private void handleDisable(CommandSource source) {
		if (!whitelist.isEnabled()) {
			source.sendMessage(Component.text("§cWhitelist is already disabled"));
			return;
		}

		source.sendMessage(Component.text("§aWhitelist is now disabled"));
		whitelist.setEnabled(false);
		whitelist.save();
	}

	private void handleEnable(CommandSource source) {
		if (whitelist.isEnabled()) {
			source.sendMessage(Component.text("§cWhitelist is already enabled"));
			return;
		}

		source.sendMessage(Component.text("§aWhitelist is now enabled"));
		whitelist.setEnabled(true);
		whitelist.save();
	}

	private void handleGet(CommandSource source, String[] args) {
		if (args.length == 1) {
			source.sendMessage(Component.text("§cUsage: /" + CMD + " get <player>"));
			return;
		}

		CachedPlayer player = cache.getPlayer(args[1]);
		if (player == null) {
			source.sendMessage(Component.text("§cPlayer not found"));
			return;
		}
		if (!whitelist.contains(player)) {
			source.sendMessage(Component.text("§cPlayer is not whitelisted"));
			return;
		}

		source.sendMessage(Component.text("§7Whitelist status for §f%s§7 is §e%s".formatted(player.name(), whitelist.getDurationFormatted(player))));
	}

	private void handleKick(CommandSource source) {
		source.sendMessage(Component.text("§7Kicked all non whitelisted players"));
		whitelist.kickNotWhitelisted();
	}

	private void handleList(CommandSource source) {
		if (whitelist.isEmpty()) {
			source.sendMessage(Component.text("§7The whitelist is empty"));
			return;
		}

		source.sendMessage(Component.text("§6§lWhitelisted players §7(Proxy Whitelist)"));
		for (CachedPlayer player : whitelist.getWhitelisted().keySet()) {
			source.sendMessage(Component.text("§8 - §7%s §e%s".formatted(player.name(), whitelist.getDurationFormatted(player))));
		}
	}

	private void handleRemove(CommandSource source, String[] args) {
		if (args.length == 1) {
			source.sendMessage(Component.text("§cUsage: /" + CMD + " remove <player>"));
			return;
		}

		CachedPlayer player = cache.getPlayer(args[1]);
		if (player == null) {
			source.sendMessage(Component.text("§cPlayer not found"));
			return;
		}
		if (!whitelist.contains(player)) {
			source.sendMessage(Component.text("§cPlayer is not whitelisted"));
			return;
		}

		whitelist.remove(player);
		source.sendMessage(Component.text("§7Removed §f%s§7 from the whitelist!".formatted(player.name())));
	}

	private void handleSet(CommandSource source, String[] args) {
		if (args.length == 1) {
			source.sendMessage(Component.text("§cUsage: /" + CMD + " set <player> [duration]"));
			return;
		}

		CachedPlayer player = cache.getPlayer(args[1]);
		if (player == null) {
			source.sendMessage(Component.text("§cPlayer not found"));
			return;
		}

		DurationParser parser = DurationParser.permanent();
		if (args.length > 2) {
			parser.parse(args[2]);
		}
		if (!parser.isPermanent() && !parser.isValid()) {
			source.sendMessage(Component.text("§cInvalid duration, leave blank for permanent"));
			return;
		}
		if (parser.isPermanent()) {
			whitelist.set(player, -1);
			source.sendMessage(Component.text("§7Set whitelist of §f%s§7 to §2INFINITE§7!".formatted(player.name())));
			return;
		}

		long expire = parser.isPermanent() ? -1 : System.currentTimeMillis() + parser.getMillis();
		whitelist.set(player, expire);
		source.sendMessage(Component.text("§7Set whitelist of §f%s§7 to §a%s§7!".formatted(player.name(), TimeUtil.formatToSeconds(parser.getMillis()))));
	}
}
