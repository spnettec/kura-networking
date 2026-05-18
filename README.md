# kura-networking
Eclipse Kura™ Networking addon

Bundle list: 

* org.eclipse.kura.core.net
* org.eclipse.kura.net.admin.firewall
* org.eclipse.kura.nm
* org.eclipse.kura.linux.net
* org.eclipse.kura.net.configuration
* org.eclipse.kura.rest.network.configuration.provider
* org.eclipse.kura.linux.systemd.provider
* org.eclipse.kura.network.threat.manager
* org.eclipse.kura.rest.network.status.provider

## Installing networking vs firewall-only

`kura-networking` and `kura-firewall-only` are mutually exclusive — both
control files declare `Conflicts:` against the other. Install one of them on
top of the other to swap; the previous package is removed automatically and
the event is recorded in `/var/log/kura-install.log`, e.g.:

```
2026-05-18T12:02:43+07:00 [kura-firewall-only preinst] kura-networking was removed seconds ago — apt Conflicts swap.
```

**Always use `apt install ./<pkg>.deb`**, not `dpkg -i`:

| Command | Behavior |
| --- | --- |
| `sudo apt install ./kura-firewall-only_*.deb` | apt resolves `Conflicts:` and removes the previously installed package automatically. The new package's preinst runs and writes the swap entry to `/var/log/kura-install.log`. |
| `sudo dpkg -i kura-firewall-only_*.deb` | dpkg refuses with `dpkg: regarding ...: kura-firewall-only conflicts with kura-networking`. preinst is **not** executed and no swap log is written. Recover with `sudo apt -f install` or by first purging the other package by hand. |

