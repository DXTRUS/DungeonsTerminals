package us.dxtrus.dungeonsterminals.guis;

import org.bukkit.entity.Player;
import us.dxtrus.commons.cooldowns.Cooldown;
import us.dxtrus.commons.gui.FastInv;
import us.dxtrus.commons.utils.StringUtils;
import us.dxtrus.dungeonsterminals.api.TerminalCompleteEvent;
import us.dxtrus.dungeonsterminals.models.Terminal;

public abstract class TerminalGUI extends FastInv {
    protected final Player player;
    protected final Terminal terminal;

    public TerminalGUI(Terminal terminal, Player player) {
        super(terminal.getType().getGuiSize(), terminal.getType().getFriendlyName());
        this.player = player;
        this.terminal = terminal;
    }

    protected void completeTerminal() {
        getInventory().close();
        player.sendMessage(StringUtils.modernMessage("&aTerminal Complete!"));
        new TerminalCompleteEvent(player, terminal).callEvent();
    }


    protected void failTerminal() {
        getInventory().close();
        player.sendMessage(StringUtils.modernMessage("&cTerminal Failed!"));
        Cooldown.local("terminal_fail", player.getUniqueId(), 600L).start();
    }
}
