package com.github.exobite.mc.playtimerewards.gui;

import com.github.exobite.mc.playtimerewards.utils.Message;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.*;

public class GUIManagerOLD implements Listener {

    private static final Map<Inventory, GUI> Guis = new HashMap<>();
    private static final Map<String, guiFunction> Functions = new HashMap<>();
    private static final Map<String, HolderInstanceInfo> Modifiers = new HashMap<>();
    private static final Map<UUID, guiState> playerStates = new HashMap<>();
    private static final String egf_Version = "1.3";
    private static JavaPlugin main;
    private static GUIManagerOLD inst;

    private GUIManagerOLD() {}

    public static void registerGUIManager(JavaPlugin main, CodeExec codeExec) {
        GUIManagerOLD.main = main;
        inst = new GUIManagerOLD();
        codeExec.setParam(inst).execCode();	//Registers all given Modifiers
        registerDefaultModifiers();
        mainFunctions();
        main.getServer().getPluginManager().registerEvents(inst, main);
        initializePreGuis(true);
    }

    @EventHandler
    public void InvClick(InventoryClickEvent e){
        Inventory inv = e.getClickedInventory();
        if(inv == e.getWhoClicked().getInventory() && GUIManagerOLD.Guis.containsKey(e.getWhoClicked().getOpenInventory().getTopInventory())) {
            //Check for clicks in own inventory
            if(e.getClick() == ClickType.DOUBLE_CLICK || e.getClick() == ClickType.SHIFT_LEFT || e.getClick() == ClickType.UNKNOWN ||
                    e.getClick() == ClickType.SHIFT_RIGHT) {
                e.setCancelled(true);
            }
        }
        if(!GUIManagerOLD.Guis.containsKey(inv)) return;
        GUI g = GUIManagerOLD.Guis.get(inv);
        executeFunction(e, g.getInvClick(e.getSlot(), e.getClick()));
    }

    @EventHandler
    public void InvClose(InventoryCloseEvent e){
        Inventory inv = e.getInventory();
        if(!GUIManagerOLD.Guis.containsKey(inv)) return;
        GUI g = GUIManagerOLD.Guis.get(inv);
        boolean close = g.canClose;
        if(playerStates.containsKey(e.getPlayer().getUniqueId())) {
            if(playerStates.get(e.getPlayer().getUniqueId()) == guiState.CAN_CLOSE) {
                close = true;
            }
        }
        if(!close){
            new BukkitRunnable(){

                @Override
                public void run() {
                    g.openInventory((Player)e.getPlayer());
                }

            }.runTaskLater(main, 1);
            return;
        }
        playerStates.remove(e.getPlayer().getUniqueId());
    }

    @EventHandler
    public void playerQuit(PlayerQuitEvent e) {
        playerStates.remove(e.getPlayer().getUniqueId());
    }

    //arg[0] = String funcName
    //arg[1] = boolean cancel

    private void executeFunction(InventoryClickEvent e, GuiSlot gs){
        if(!(e.getWhoClicked() instanceof Player)) return;
        String function = gs.funcName;
        e.setCancelled(gs.cancel);
        if(function.equals("empty")){
            return;
        }
        Object[] args = gs.args;
        //Execute Function
        if(GUIManagerOLD.Functions.containsKey(function)){
            guiFunction gf = GUIManagerOLD.Functions.get(function);
            //Check for the right types, if specified
            if(gf.types!=null){
                for(int i=0;i<args.length;i++){
                    if(gf.types[i] != args[i].getClass()){
                        //if(PluginMaster.dat().conf_debugMode) System.out.println("i"+i+", "+gf.types[i]+" != "+args[i].getClass());
                        //PluginMaster.getUtils().printMessageData(Lang.ERR_GUIFUNC_ARGS, new String[]{function});
                        System.out.println(Message.ERR_GUI_WRONG_ARGS.getMessage(new String[] {function}));
                        return;
                    }
                }
            }
            if(args.length>0) {
                System.out.println("GS "+gs.getArgs().length+" "+gs.getArgs()[0]);
            }
            gf.exec(e, args);
        }
    }

    public static GUI createGui(String title, int size, boolean canClose){
        GUI g = inst.new GUI(title, size, canClose);
        GUIManagerOLD.Guis.put(g.inv, g);
        return g;
    }

    private enum guiState {
        OPEN_GUI, NO_GUI, CAN_CLOSE
    }

    public class GUI {

        private Inventory inv;
        private Map<String, GuiSlot> functions = new HashMap<>();
        private boolean canClose;
        private String initialName;

        private GUI(String title, int size, boolean canClose){
            this.canClose = canClose;
            inv = Bukkit.createInventory(null, size, title);
        }

        public void setItemDummy(CustomItem ci, int slot) {
            if(ci==null) {
                ci = new CustomItem(Material.AIR);
            }
            inv.setItem(slot, ci.getItemStack());
            boolean f = false;
            for(String s:functions.keySet()) {
                if(s.toLowerCase().contains(slot+"")) {
                    f = true;
                    break;
                }
            }
            if(!f) {
                functions.put(slot+".LEFT", new GuiSlot("empty").setCancel(true));
            }
        }

        public void changeMaterial(int slot, Material m) {
            if(slot>inv.getSize()-1 || slot<0) return;
            ItemStack is = inv.getItem(slot);
            if(is==null) return;
            is.setType(m);
            inv.setItem(slot, is);
        }

        public GuiSlot getGuiSlot(int slot, ClickType ct) {
            String n = slot+"."+ct.toString();
            return functions.getOrDefault(n, null);
        }

        public void setGuiSlot(GuiSlot gs, int slot) {
            functions.put(slot+"."+gs.ct.toString(), gs);
        }

        public void setItemFunc(CustomItem ci, int slot, String funcName, boolean cancel, ClickType ct, Object[] args) {
            //System.out.println("Assigning function "+funcName+" to slot "+slot+ "with Type "+ct);
            if(slot>inv.getSize()-1 || slot<0) return;
            GuiSlot g = new GuiSlot(funcName).setCancel(cancel).setClickType(ct).setArgs(args);
            if(ci==null) {
                ci = new CustomItem(Material.AIR);
            }
            inv.setItem(slot, ci.getItemStack());
            functions.put(slot+"."+ct, g);
        }

        public void copyGuiSlot(int src, int target) {
            if(src==target) return;
            if(src > inv.getSize()-1 || src < 0 || target > inv.getSize()-1 || target < 0) return;
            Map<String, GuiSlot> temp = new HashMap<>();
            for(String g:functions.keySet()) {
                if(g.toLowerCase().contains(src+".")) {
                    String name = g.replace(src+".", target+".");
                    GuiSlot sr = functions.get(g);
                    GuiSlot copy = new GuiSlot(sr.funcName);
                    copy.args = sr.getArgs().clone();
                    copy.cancel = sr.cancel;
                    copy.ct = sr.ct;
                    temp.put(name, copy);
                }
            }
            for(String s:temp.keySet()) {
                functions.put(s, temp.get(s));
            }
        }

        public void setItemFunc(CustomItem ci, int slot, Object[] args){
            if(args.length<2) return;
            if(!(args[0] instanceof String Name) || !(args[1] instanceof Boolean)) {
                return;
            }
            boolean cancel = (boolean) args[1];
            Object[] d;
            if(args.length-2>0) {
                d = new Object[args.length-2];
                System.arraycopy(args, 2, d, 0, args.length - 2);
            }else {
                d = null;
            }
            setItemFunc(ci, slot, Name, cancel, null, d);
        }

        public GuiSlot getInvClick(int slot, ClickType ct){
            if(slot<0 || slot>inv.getSize()-1 || !functions.containsKey(slot+"."+ct)) {
                return new GuiSlot("empty").setCancel(true);
            }
            return functions.get(slot+"."+ct);
        }

        public void clearSlots(int from, int to, boolean deleteGuiSlots) {
            for(int i=from;i<to;i++) {
                inv.setItem(i, null);
                if(deleteGuiSlots) clearGuiSlot(i);
            }
        }

        public void clearGuiSlot(int slot) {
            for(ClickType ct:ClickType.values()) {
                functions.remove(slot+"."+ct);
            }
        }

        public int searchFunction(String funcName) {
            for(String f:functions.keySet()) {
                GuiSlot gs = functions.get(f);
                if(gs.funcName.equals(funcName)) {
                    return Integer.parseInt(f.split("\\.")[0]);
                }
            }
            return -1;
        }

        public GuiSlot searchGuislot(String funcName) {
            for(String f:functions.keySet()) {
                GuiSlot gs = functions.get(f);
                if(gs.funcName.equals(funcName)) {
                    return gs;
                }
            }
            return null;
        }

        public int getEmptySlots() {
            int amount = 0;
            for(ItemStack is:inv.getContents()) {
                if(is!=null) amount++;
            }
            return amount;
        }

        public void openInventory(Player p){
            playerStates.put(p.getUniqueId(), guiState.CAN_CLOSE);
            p.openInventory(inv);
            playerStates.put(p.getUniqueId(), guiState.OPEN_GUI);
        }

        public void clear(){
            inv.clear();
            functions.clear();
        }

        public int getSize() {
            return inv.getSize();
        }

        public void delete() {
            for(HumanEntity he:inv.getViewers()) {
                he.closeInventory();
            }
            GUIManagerOLD.Guis.remove(inv);
            inv = null;
            functions = null;
        }

    }

    public class GuiSlot {

        private String funcName;
        private ClickType ct = ClickType.LEFT;
        private boolean cancel = true;
        private Object[] args;

        private GuiSlot(String funcName) {
            this.funcName = funcName;
        }

        public GuiSlot setCancel(boolean cancel) {
            this.cancel = cancel;
            return this;
        }

        public GuiSlot setFunction(String funcName) {
            this.funcName = funcName;
            return this;
        }

        public GuiSlot setArgs(Object[] args) {
            this.args = args;
            return this;
        }

        public GuiSlot setClickType(ClickType ct) {
            this.ct = ct;
            return this;
        }

        public Object[] getArgs() {
            return args;
        }

        public GuiSlot getCopy() {
            GuiSlot gs = new GuiSlot(funcName);
            gs.setCancel(cancel);
            gs.setArgs(args);
            gs.setClickType(ct);
            return gs;
        }
    }

    public abstract class HolderInstanceInfo {

        protected boolean playerParams = false;

        public HolderInstanceInfo(String ModName) {
            addModifier(ModName, this);
        }

        private Map<String, FileConfiguration> srcData = new HashMap<>();

        protected abstract guiHolder getInstance(Object source);

        public void addSourceData(String Name, FileConfiguration fc) {
            srcData.put(Name, fc);
        }
    }

    public abstract class guiFunction{

        private Class<?>[] types;

        public guiFunction(String fName){
            GUIManagerOLD.Functions.put(fName, this);
        }

        public guiFunction setTypes(Class<?> ... args){
            types = args;
            return this;
        }

        protected abstract void exec(InventoryClickEvent e, Object[] args);

    }

    private static void addModifier(String name, HolderInstanceInfo hi) {
        GUIManagerOLD.Modifiers.put(name, hi);
    }

    private static void registerDefaultModifiers() {
        //Default Modifiers go here.
    }

    public static void addFunction() {}

    private static void mainFunctions(){
        inst.new guiFunction("openInventory"){

            @Override
            protected void exec(InventoryClickEvent e, Object[] args) {
                Player p = (Player)e.getWhoClicked();


                if ((!(args[0] instanceof String)) || (!(args[1] instanceof String))) {
                    System.out.println(Message.ERR_GUI_WRONG_ARGS.getMessage("openInventory"));
                    return;
                }
                String mod = (String)args[2];
                String Name = (String)args[3];
                openInventory(mod, Name, p);
            }

        }.setTypes(String.class, String.class);
        inst.new guiFunction("openGUI") {
            protected void exec(InventoryClickEvent e, Object[] args) {
                Player p = (Player)e.getWhoClicked();
                if ((!(args[0] instanceof String)) || (!(args[1] instanceof String)) || (!(args[2] instanceof Boolean))) {
                    System.out.println(Message.ERR_GUI_WRONG_ARGS.getMessage("openGUI"));
                    return;
                }
                String mod = (String)args[0];
                String Name = (String)args[1];
                boolean repop = (Boolean) args[2];
                if(repop){
                    guiHolder gh = getModifier(mod).getInstance(p.getUniqueId());
                    GUI g = gh.getGuis().get(Name);
                    Map<String, String> placeholders = gh.getPlaceholders();
                    if (g == null){
                        g = initGuiFromMap(getModifier(mod).srcData.get(Name), placeholders);
                    }
                    String GuiIdent = "";
                    for (String ident : gh.getGuis().keySet()) {
                        if (gh.getGuis().get(ident) == g) GuiIdent = ident;
                    }
                    if (GuiIdent.equals("")) return;
                    g.delete();
                    g = initGuiFromMap(getModifier(mod).srcData.get(g.initialName), placeholders);
                    gh.addGui(GuiIdent, g);
                    g.openInventory(p);
                } else {
                    openInventory(mod, Name, p);
                }
            }
        }.setTypes(String.class, String.class, Boolean.class);
        inst.new guiFunction("closeInventory") {

            @Override
            protected void exec(InventoryClickEvent e, Object[] args) {
                closeInventory(Bukkit.getPlayer(e.getWhoClicked().getUniqueId()));
            }
        };
    }

    public static void closeInventory(Player p) {
        if(!playerStates.containsKey(p.getUniqueId())) return;
        if(!Guis.containsKey(p.getOpenInventory().getTopInventory())) return;
        playerStates.put(p.getUniqueId(), guiState.CAN_CLOSE);
        p.closeInventory();
    }

    public static boolean openInventory(String Mod, String Name, Player p) {
        if (getModifier(Mod).getInstance(p.getUniqueId()) == null) return false;
        Map<String, GUI> guis = getModifier(Mod).getInstance(p.getUniqueId()).getGuis();
        if (guis == null) return false;
        if (!guis.containsKey(Name)) {
            System.out.println(Message.ERR_GUI_NOT_FOUND.getMessage(Mod, Name));
            return false;
        }
        guis.get(Name).openInventory(p);
        return true;
    }

    public static void reloadPreGuis() {
        for(String s: GUIManagerOLD.Modifiers.keySet()) {
            GUIManagerOLD.Modifiers.get(s).srcData = new HashMap<>();
        }
        initializePreGuis(false);
    }

    private static void initializePreGuis(boolean sync) {
        File[] files = new File(main.getDataFolder() + File.separator + "guis").listFiles();
        if (files == null) return;
        if (files.length == 0) return;
        CodeExec ce = new CodeExec(){

            @Override
            public Object execCode() {
                for (File f : files) {
                    FileConfiguration data = YamlConfiguration.loadConfiguration(f);
                    if (!data.contains("GUI")) return null;
                    if (!data.contains("GUI.egf_v")) return null;
                    if(!data.getString("GUI.egf_v").equalsIgnoreCase(egf_Version)) return null;			//Checks for the right Version
                    data.set("GUI.egf_v", null);														//Removes the Version from the cached-data
                    String name = data.getConfigurationSection("GUI").getKeys(false).iterator().next();
                    String Mod = data.getString("GUI." + name + ".GuiType", "NONE");
                    getModifier(Mod).addSourceData(name, data);
                }
                return null;
            }

        };
        if(sync) {
            //System.out.println("sync");
            ce.execCode();
        }else {
            //System.out.println("async");
            new BukkitRunnable() {

                @Override
                public void run() {
                    ce.execCode();
                }
            }.runTaskAsynchronously(main);
        }
    }

    public static GUI initPreGuiWithoutList(String guiName, String modifier, guiHolder gh) {
        if(!getModifier(modifier).srcData.containsKey(guiName)) {
            for(String k:getModifier(modifier).srcData.keySet()) {
                System.out.println(k);
            }
            throw new NullPointerException();
        }
        return initGuiFromMap(getModifier(modifier).srcData.get(guiName), gh.getPlaceholders());
    }

    public static GUI initPreGui(String guiName, String modifier, guiHolder gh) {
        GUI g = initPreGuiWithoutList(guiName, modifier, gh);
        gh.addGui(guiName, g);
        return g;
    }

    private static GUI initGuiFromMap(FileConfiguration fc, Map<String, String> ph) {
        System.out.println("Found "+fc.getConfigurationSection("GUI").getKeys(false).size()+" false Keys!");
        String px = fc.getConfigurationSection("GUI").getKeys(false).iterator().next();
        px = "GUI." + px + ".";
        System.out.println(px);
        int slots = fc.getInt(px + "Slots", 9);
        String displayName = convert(fc.getString(px + "DisplayName", px.replace("GUI.", "")), ph);
        boolean close = !fc.getBoolean(px + "DenyClose");
        GUI g = createGui(displayName, slots, close);
        g.initialName = fc.getConfigurationSection("GUI").getKeys(false).iterator().next();
        for (String k : fc.getConfigurationSection(px + "Items").getKeys(false)) {
            String pr = px + "Items." + k + ".";
            Material m = Material.matchMaterial(ChatColor.stripColor(convert(fc.getString(pr + "Material", "GRASS"), ph)));
            String itemName = convert(fc.getString(pr + "DisplayName", m.name()), ph);
            int Amount = fc.getInt(pr + "Amount", 1);
            //short Damage = (short)fc.getInt(pr + "Damage", 0);
            List<String> Lore = fc.getStringList(pr + "Lore");
            for (String loreRow : Lore) {
                System.out.println(loreRow);
                Lore.set(Lore.indexOf(loreRow), convert(ChatColor.RESET+ loreRow, ph));
            }
            CustomItem ci = new CustomItem(itemName, Lore, m, Amount);
            if (fc.get(pr + "Enchantments") != null) {
                for (String ench : fc.getConfigurationSection(pr + "Enchantments").getKeys(false)) {
                    Enchantment e = Enchantment.getByKey(new NamespacedKey(main, ench));
                    if (e != null) {
                        ci.addEnchantment(e, fc.getInt(pr + "Enchantments." + ench, 0));
                    }
                }
            }
            int slot = Integer.parseInt(k.toLowerCase().replace("slot", ""));
            if(fc.getConfigurationSection(pr+"Actions")!=null) {
                for(String f:fc.getConfigurationSection(pr+"Actions").getKeys(false)) {
                    String pt = pr + "Actions." + f+ ".";
                    String[] ctarr = f.split("\\.");
                    String ctype = ctarr[ctarr.length-1];
                    ClickType ct = ClickType.valueOf(ctype);
                    if(ct==ClickType.UNKNOWN) {
                        System.out.println("Unknown ClickType: "+ctype);
                        continue;
                    }
                    String funcName = fc.getString(pt + "FunctionName", "empty");
                    boolean cancel = fc.getBoolean(pt + "CancelClick");
                    List<String> funcArgs = fc.getStringList(pt + "FunctionArgs");
                    Object[] o = new Object[funcArgs.size()];
                    for (int i = 0; i < funcArgs.size(); i++) {
                        String arg = convert(funcArgs.get(i), ph);
                        o[i] = arg;
                    }
                    g.setItemFunc(ci, slot, funcName, cancel, ct, o);
                }
            }
            if(fc.getConfigurationSection(pr+"OptionalChecks")!=null) {
                for(String f:fc.getConfigurationSection(pr+"OptionalChecks").getKeys(false)) {
                    StringBuilder pt = new StringBuilder(pr).append("OptionalChecks.");


                }
            }

        }
        return g;
    }

    private static String convert(String s, Map<String, String> ph) {
        s = ChatColor.translateAlternateColorCodes('&', ChatColor.stripColor(s));
        if (ph == null) {
            System.out.println("ph is null");
            return s;
        }
        for (String key : ph.keySet()) {
            if (s.contains(key)) s = s.replace(key, ph.get(key));
        }
        if(s.contains("_LANG:")) {
            //System.out.println(s+" contains _LANG:");
            //Inserts Messages from the lang file
            Message m = null;
            try {
                m = Message.valueOf(ChatColor.stripColor(s.replace("_LANG:", "")));
            }catch(IllegalArgumentException ignored){
            }
            if(m==null) {
                System.out.println("Unknown Message: "+s);
            }else {
                s = s.replace("_LANG:"+ m, m.getMessage());
            }
        }
        return s;
    }

    public static HolderInstanceInfo getModifier(String Mod) {
        if (!GUIManagerOLD.Modifiers.containsKey(Mod)) {
            System.out.println(Message.ERR_GUI_MOD_NOT_FOUND.getMessage(Mod));
            throw new NullPointerException();
        }
        return GUIManagerOLD.Modifiers.get(Mod);
    }

    //Debug stuff

    public static List<String> getModifiers(){
        return new ArrayList<>(GUIManagerOLD.Modifiers.keySet());
    }

    public static List<String> getGuis(){
        List<String> r = new ArrayList<>();
        for(Inventory v: GUIManagerOLD.Guis.keySet()) {
            GUI g = GUIManagerOLD.Guis.get(v);
            r.add(g.initialName);
        }
        return r;
    }

    public static List<String> getFunctions(){
        return new ArrayList<>(Functions.keySet());
    }

    public static GUIManagerOLD getInstance() {
        return inst;
    }

}
