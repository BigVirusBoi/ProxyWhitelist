package me.bigvirusboi.whitelist.command;

import com.google.common.collect.ImmutableList;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import lombok.Getter;
import me.bigvirusboi.whitelist.util.Util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Getter
public abstract class CoreCommand {
	private final String name;
	private final String permission;
	private final String[] aliases;

	private final SimpleCommand command;

	public CoreCommand(String name, String permission, String... aliases) {
		this.name = name;
		this.permission = permission;
		this.aliases = aliases;

		this.command = new SimpleCommand() {
			@Override
			public void execute(Invocation invocation) {
				CoreCommand.this.execute(CoreCommand.Invocation.of(invocation, false));
			}

			@Override
			public List<String> suggest(Invocation velocityInvocation) {
				CoreCommand.Invocation invocation = CoreCommand.Invocation.of(velocityInvocation, true);
				String[] args = invocation.arguments();

				List<String> arguments = new ArrayList<>(CoreCommand.this.suggest(invocation));
				List<String> matches = new ArrayList<>();

				Util.copyPartialMatches(args[args.length - 1], arguments, matches);
				Collections.sort(matches);

				return matches;
			}

			@Override
			public CompletableFuture<List<String>> suggestAsync(Invocation invocation) {
				return CompletableFuture.completedFuture(this.suggest(invocation));
			}

			@Override
			public boolean hasPermission(Invocation invocation) {
				if (permission == null) return SimpleCommand.super.hasPermission(invocation);
				return invocation.source().hasPermission(permission);
			}
		};
	}

	public abstract void execute(Invocation invocation);

	public List<String> suggest(Invocation invocation) {
		return ImmutableList.of();
	}

	public interface Invocation {
		CommandSource source();

		String[] arguments();

		static Invocation of(SimpleCommand.Invocation invocation, boolean tab) {
			return new Invocation() {
				@Override
				public CommandSource source() {
					return invocation.source();
				}

				@Override
				public String[] arguments() {
					String[] args = invocation.arguments();
					if (tab) if (args.length == 0) args = new String[]{""};
					return args;
				}

			};
		}
	}
}
