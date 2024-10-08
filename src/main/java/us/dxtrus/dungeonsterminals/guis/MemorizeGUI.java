package us.dxtrus.dungeonsterminals.guis;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import us.dxtrus.dungeonsterminals.DungeonsTerminals;
import us.dxtrus.dungeonsterminals.config.Config;
import us.dxtrus.dungeonsterminals.models.Terminal;
import us.dxtrus.dungeonsterminals.models.TerminalType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class MemorizeGUI extends TerminalGUI {

    private final DungeonsTerminals plugin;
    private final List<Material> correctItems = new ArrayList<>();
    private final Map<Material, Boolean> guessed = new HashMap<>();
    private final List<BukkitTask> tasks = new ArrayList<>();
    private int incorrectGuesses = 0;

    public MemorizeGUI(Terminal terminal, Player player, DungeonsTerminals plugin) {
        super(terminal, player);
        this.plugin = plugin;
        chooseRandomItems();
        showRandomItems();
        tasks.add(Bukkit.getScheduler().runTaskLater(plugin, this::startGuessing, 20L * Config.getInstance().getTerminals().getMemorize().getPreviewSeconds()));
    }

    @Override
    public void onClose(InventoryCloseEvent e) {
        tasks.forEach(BukkitTask::cancel);
    }

    private void chooseRandomItems() {
        for (int i = 0; i < 3; i++) {
            Material[] materials = Material.values();
            Material material = materials[plugin.getRandom().nextInt(materials.length)];
            if (correctItems.contains(material) || material.isAir() || material == Material.SUGAR_CANE || !material.isItem()) {
                --i;
                continue;
            }
            correctItems.add(material);
        }
    }

    private Material randomMaterialNotInCorrect() {
        for (int i = 0; i < 2; i++) {
            Material[] materials = Material.values();
            Material material = materials[plugin.getRandom().nextInt(materials.length)];
            if (correctItems.contains(material) || material.isAir() || !material.isItem()) {
                --i;
                continue;
            }
            return material;
        }
        return Material.SUGAR_CANE;
    }

    private void showRandomItems() {
        setItem(11, new ItemStack(correctItems.get(0)));
        setItem(13, new ItemStack(correctItems.get(1)));
        setItem(15, new ItemStack(correctItems.get(2)));
    }

    private void startGuessing() {
        clearPreview();
        showGuessOptions();
    }

    private void clearPreview() {
        setItem(11, new ItemStack(Material.AIR));
        setItem(13, new ItemStack(Material.AIR));
        setItem(15, new ItemStack(Material.AIR));
    }

    private void showGuessOptions() {
        int guiSize = TerminalType.MEMORIZE.getGuiSize() - 1;
        int firstSlot = plugin.getRandom().nextInt(guiSize);
        int secondSlot;
        int thirdSlot;

        do {
            secondSlot = plugin.getRandom().nextInt(guiSize);
        } while (secondSlot == firstSlot);

        do {
            thirdSlot = plugin.getRandom().nextInt(guiSize);
        } while (thirdSlot == firstSlot || thirdSlot == secondSlot);

        setCorrectItem(firstSlot);
        setCorrectItem(secondSlot);
        setCorrectItem(thirdSlot);

        for (int i = 0; i < TerminalType.MEMORIZE.getGuiSize()-3; i++) {
            ItemStack itemStack = new ItemStack(randomMaterialNotInCorrect());
            addItem(itemStack, incorrect(itemStack));
        }
    }

    private Consumer<InventoryClickEvent> incorrect(ItemStack itemStack) {
        return e -> {
            incorrectGuesses++;
            setItem(e.getSlot(), new ItemStack(Material.RED_STAINED_GLASS_PANE));
            tasks.add(Bukkit.getScheduler().runTaskLater(plugin, () -> setItem(e.getSlot(), itemStack, incorrect(itemStack)), 15L));
            if (incorrectGuesses == 3) {
                failTerminal();
            }
        };
    }

    private void setCorrectItem(int firstSlot) {
        Material material = correctItems.removeFirst();
        setItem(firstSlot, new ItemStack(material), e -> {
            guessed.put(material, true);
            setItem(e.getSlot(), new ItemStack(Material.LIME_STAINED_GLASS_PANE));
            if (guessed.size() == 3) {
                completeTerminal();
            }
        });
    }
}
