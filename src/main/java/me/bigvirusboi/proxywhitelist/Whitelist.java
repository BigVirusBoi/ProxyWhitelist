package me.bigvirusboi.proxywhitelist;

import com.google.inject.Inject;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.Plugin;
import org.slf4j.Logger;

@Plugin(
		id = "whitelist",
		name = "Whitelist",
		version = BuildConstants.VERSION,
		description = "Adds an advanced whitelist to Velocity",
		authors = {"BigVirusBoi"},
		dependencies = {
				@Dependency(id = "beanlib")
		}
)
public class Whitelist {

	@Inject
	private Logger logger;

	@Subscribe
	public void onProxyInitialization(ProxyInitializeEvent event) {
	}
}
