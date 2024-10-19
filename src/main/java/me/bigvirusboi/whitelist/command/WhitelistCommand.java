package me.bigvirusboi.whitelist.command;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import me.bigvirusboi.whitelist.BuildConstants;
import me.bigvirusboi.whitelist.cache.CachedPlayer;
import me.bigvirusboi.whitelist.cache.PlayerCache;
import me.bigvirusboi.whitelist.manager.Whitelist;
import me.bigvirusboi.whitelist.util.Constants;
import me.bigvirusboi.whitelist.util.time.DurationParser;
import me.bigvirusboi.whitelist.util.Permissions;
import me.bigvirusboi.whitelist.util.time.TimeUtil;
import net.kyori.adventure.text.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
			int entries = whitelist.getWhitelisted().size();

			source.sendMessage(Component.text(""));
			source.sendMessage(Component.text("§7Running §6§lProxy Whitelist §8(v" + BuildConstants.VERSION + ")§7 by §e" + Constants.CREDITS));
			source.sendMessage(Component.text("§7Whitelist is %s§7 with §e%s %s"
					.formatted(whitelist.isEnabled() ? "§aactive" : "§cnot active", entries, entries == 1 ? "entry" : "entries")));
			source.sendMessage(Component.text(""));
			source.sendMessage(Component.text("§7To view subcommands, use §e/" + CMD + " help"));
			source.sendMessage(Component.text(""));
			return;
		}

		switch (args[0].toLowerCase()) {
			case "help" -> handleHelp(source);
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
		CommandSource source = invocation.source();
		String[] args = invocation.arguments();
		List<String> arguments = new ArrayList<>();

		if (args.length == 1) {
			arguments.add("help");
			if (source.hasPermission(Permissions.CLEAR)) arguments.add("clear");
			if (source.hasPermission(Permissions.DISABLE)) arguments.add("disable");
			if (source.hasPermission(Permissions.ENABLE)) arguments.add("enable");
			if (source.hasPermission(Permissions.GET)) arguments.add("get");
			if (source.hasPermission(Permissions.KICK)) arguments.add("kick");
			if (source.hasPermission(Permissions.LIST)) arguments.add("list");
			if (source.hasPermission(Permissions.REMOVE)) arguments.add("remove");
			if (source.hasPermission(Permissions.SET)) arguments.add("set");
		} else if (args.length == 2) {
			Map<String, List<String>> permittedArguments = new HashMap<>();
			switch (args[0].toLowerCase()) {
				case "clear" -> permittedArguments.put(Permissions.CLEAR, List.of("all", "expired"));
				case "get" -> permittedArguments.put(Permissions.GET, whitelist.getWhitelisted().keySet().stream().map(CachedPlayer::name).toList());
				case "set" -> permittedArguments.put(Permissions.SET, proxy.getAllPlayers().stream().map(Player::getUsername).toList());
				case "remove" -> permittedArguments.put(Permissions.REMOVE, whitelist.getWhitelisted().keySet().stream().map(CachedPlayer::name).toList());
			}

			for (String permission : permittedArguments.keySet()) {
				if (source.hasPermission(permission)) arguments.addAll(permittedArguments.get(permission));
			}
		}

		return arguments;
	}

	private void handleHelp(CommandSource source) {
		source.sendMessage(Component.text("§6§lCommand usage"));
		source.sendMessage(Component.text("§6 /" + CMD + "§e clear <all/expired>§8 - §7Clear the whitelist"));
		source.sendMessage(Component.text("§6 /" + CMD + "§e disable§8 - §7Disable the whitelist"));
		source.sendMessage(Component.text("§6 /" + CMD + "§e enable§8 - §7Enable the whitelist"));
		source.sendMessage(Component.text("§6 /" + CMD + "§e get <player>§8 - §7Get the whitelist status of a player"));
		source.sendMessage(Component.text("§6 /" + CMD + "§e help§8 - §7View the command help"));
		source.sendMessage(Component.text("§6 /" + CMD + "§e kick§8 - §7Kick everyone who is not whitelisted"));
		source.sendMessage(Component.text("§6 /" + CMD + "§e list§8 - §7List all whitelisted players"));
		source.sendMessage(Component.text("§6 /" + CMD + "§e remove <player>§8 - §7Remove a player from the whitelist"));
		source.sendMessage(Component.text("§6 /" + CMD + "§e set <player> [duration]§8 - §7Add a player to the whitelist"));
	}

	private void handleClear(CommandSource source, String[] args) {
		if (!source.hasPermission(Permissions.CLEAR)) {
			sendPermission(source);
			return;
		}
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
		if (!source.hasPermission(Permissions.DISABLE)) {
			sendPermission(source);
			return;
		}
		if (!whitelist.isEnabled()) {
			source.sendMessage(Component.text("§cWhitelist is already disabled"));
			return;
		}

		source.sendMessage(Component.text("§aWhitelist is now disabled"));
		whitelist.setEnabled(false);
		whitelist.save();
	}

	private void handleEnable(CommandSource source) {
		if (!source.hasPermission(Permissions.ENABLE)) {
			sendPermission(source);
			return;
		}
		if (whitelist.isEnabled()) {
			source.sendMessage(Component.text("§cWhitelist is already enabled"));
			return;
		}

		source.sendMessage(Component.text("§aWhitelist is now enabled"));
		whitelist.setEnabled(true);
		whitelist.save();
	}

	private void handleGet(CommandSource source, String[] args) {
		if (!source.hasPermission(Permissions.GET)) {
			sendPermission(source);
			return;
		}
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
		if (!source.hasPermission(Permissions.KICK)) {
			sendPermission(source);
			return;
		}

		source.sendMessage(Component.text("§7Kicked all non-whitelisted players"));
		whitelist.kickNotWhitelisted();
	}

	private void handleList(CommandSource source) {
		if (!source.hasPermission(Permissions.LIST)) {
			sendPermission(source);
			return;
		}
		if (whitelist.isEmpty()) {
			source.sendMessage(Component.text("§7The whitelist is empty"));
			return;
		}

		source.sendMessage(Component.text("§6§lWhitelisted players"));
		for (CachedPlayer player : whitelist.getWhitelisted().keySet()) {
			source.sendMessage(Component.text("§8 - §7%s §e%s".formatted(player.name(), whitelist.getDurationFormatted(player))));
		}
	}

	private void handleRemove(CommandSource source, String[] args) {
		if (!source.hasPermission(Permissions.REMOVE)) {
			sendPermission(source);
			return;
		}
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
		if (!source.hasPermission(Permissions.SET)) {
			sendPermission(source);
			return;
		}
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
		source.sendMessage(Component.text("§7Set whitelist of §f%s§7 to §a%s§7!".formatted(player.name(), TimeUtil.toFormattedString(parser.getMillis()))));
	}

	private void sendPermission(CommandSource source) {
		source.sendMessage(Component.text("§cYou do not have permission to use this command"));
	}
}
