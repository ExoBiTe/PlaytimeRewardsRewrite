package com.github.exobite.mc.playtimerewards.utils;

import com.github.exobite.mc.playtimerewards.main.PluginMaster;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExoDebugTools implements Listener {

    private static final String usage = ChatColor.RED+"#exodebug <arg>"
            + "\ninfo: Shows the ExoAPI-Info"
            + "\nplaceholders: Shows all available Placeholders"
            + "\nmsg: Opens the Messages Interface"
            + "\ngm: Opens the GUIManagerOLD Interface"
            + "\nregex: Opens the Regex Pattern Matcher"
            + "\nreflect: Opens the Reflection Interface";
    private static final String usageMsg = ChatColor.RED+"#exodebug msg <arg>"
            + "\nlistValues:            Lists all registered Messages"
            + "\ngetMsg <msg> <args>:   Sends the given Message with args"
            + "\nreload:                Reloads all Messages from the File";
    private static final String usageRegex = ChatColor.RED+"#exodebug regex <arg>"
            + "\nregexTest <pattern> <testString>: Tests the specified Pattern against the String and returns the Match"
            + "\ncheckTimeArg <timeString>: Check if the String is a compatible Time-Param, and outputs a confirmation";
    private static final String usageGm = ChatColor.RED+"#exodebug gm <arg>"
            + "\nopenGui <Modifier> <guiName>: Opens the specified GUI"
            + "\nlistMods: Lists all currently registered GUI Modifiers"
            + "\nlistGuis: Lists all registered guiNames"
            + "\nlistFunctions: Lists all registered GuiFunctions";
    private static final String usageReflection = ChatColor.RED+"#exodebug reflect <arg>"
            + "\nlistFields <className>: Lists all known fields of the class"
            + "\nlistMethods <className>: Lists all known Methods of the class"
            + "\nstoreInstance <className> <fieldName>: Stores the Content of the Field as Instance"
            + "\ngetFieldValue <className> <fieldName>: Displays info for the Field"
            + "\nsetFieldValue <className> <fieldName> <value>: Sets the Value of the specified field"
            + "\ninvokemethod <className> <methodName>: Invokes the selected Method"
            + "\ntoggle <somerandom3rdArg>: Toggles whether the plugin packageprefix gets added automatically or not";

    private static JavaPlugin main;
    private static ExoDebugTools inst;

    private static final Map<UUID, Object> storedInst = new HashMap<>();
    private static final Map<UUID, Boolean> insertPackagePrefix = new HashMap<>();

    private static final Map<String, PlaceholderRunnable> placeHolders = new HashMap<>();

    public static void registerDebugTools(JavaPlugin main) {
        ExoDebugTools.main = main;
        if(inst!=null) {
            return;
        }
        if(!new File(main.getDataFolder()+File.separator+"ExoTools.jar").exists()) {
            PluginMaster.sendConsoleMessage(Level.WARNING,
                    "Registering the Debug Tools isn´t allowed, contact the Plugin Developer for further help.");
            return;
        }
        inst = new ExoDebugTools();
        main.getServer().getPluginManager().registerEvents(inst, main);
        PluginMaster.sendConsoleMessage(Level.WARNING,
                "Caution! Exo Debug Tools are running by Plugin "+main.getDescription().getName()+"!\nAll OPs got Permissions to Access internal Plugin Data!\n"
                + "Make sure they know what they're doing!");

        //Initialize the Placeholders
        placeHolders.put("%UUID%", new PlaceholderRunnable("Gets replaced with the Players UUID") {

            @Override
            public String run(Player p) {
                return p.getUniqueId().toString();
            }

        });

        placeHolders.put("%PNAME%", new PlaceholderRunnable("Gets replaced with the Players Name") {

            @Override
            public String run(Player p) {
                return p.getName();
            }

        });

    }

    private ExoDebugTools(){/* Empty Constructor */}

    private static void sendSyncMessage(final Player p, final String msg) {
        new BukkitRunnable(){
            @Override
            public void run() {
                p.sendMessage(msg);
            }
        }.runTask(main);
    }

    @EventHandler
    private void onChatAsync(@NotNull AsyncPlayerChatEvent e) {
        Player p = e.getPlayer();
        if(e.getMessage().toLowerCase().startsWith("#exodebug") && p.isOp()) {
            e.setCancelled(true);
            String msg = e.getMessage();
            for(String s:placeHolders.keySet()){
                msg = msg.replaceAll(s, placeHolders.get(s).run(p));
            }
            String[] args = msg.split(" ");
            if(args.length > 1) {
                switch (args[1].toLowerCase()) {
                    case "info" -> infoCommand(p);
                    case "placeholders" -> placeholderCommand(p);
                    case "msg" -> msgCommand(p, args);
                    case "gm" -> guimanagerCommand(p, args);
                    case "regex" -> regexCommand(p, args);
                    case "reflect" -> reflectionCmd(p, args);
                    default -> sendSyncMessage(p, usage);
                }
            } else {
                sendSyncMessage(p, usage);
            }
        }

    }

    @EventHandler
    private void onLeave(@NotNull PlayerQuitEvent e){
        if(!e.getPlayer().isOp()) return;
        storedInst.remove(e.getPlayer().getUniqueId());
        insertPackagePrefix.remove(e.getPlayer().getUniqueId());
    }

    private void infoCommand(Player p) {
        String sb = ChatColor.DARK_AQUA + "##############################\n" +
                ChatColor.GOLD + "Running " +
                ChatColor.AQUA + main.getDescription().getName() +
                ChatColor.GOLD + " on Version " +
                ChatColor.AQUA + main.getDescription().getVersion() +
                "\n" + ChatColor.DARK_AQUA + "##############################";
        sendSyncMessage(p, sb);
    }

    private void placeholderCommand(Player p) {
        StringBuilder sb = new StringBuilder("Found ").append(placeHolders.size()).append(" Placeholders: ");
        for(String s:placeHolders.keySet()) {
            sb.append("\n").append(s).append(" : ").append(placeHolders.get(s).getDescription());
        }
        sendSyncMessage(p, sb.toString());
    }

    private void msgCommand(@NotNull Player p, String @NotNull [] args) {
        if(args.length>2){
            switch (args[2].toLowerCase()) {
                case "listvalues" -> { //msg values
                    Set<String> msgs = Lang.getInstance().getRegisteredMessages();
                    StringBuilder sb = new StringBuilder(ChatColor.GOLD.toString()).append("Listing all registered(").append(msgs.size()).append(") msg values:");
                    for (String m: msgs) {
                        sb.append("\n").append(ChatColor.GOLD).append(m.toString()).append(": ").append(ChatColor.AQUA).append(Lang.getInstance().getRawMessage(m))
                                .append(ChatColor.AQUA).append(" Args: ").append(Lang.getInstance().getParamAmount(m));
                    }
                    sendSyncMessage(p, sb.toString());
                }
                case "getmsg" -> { //msg get
                    if (args.length > 3) {
                        if(!Lang.getInstance().exists(args[3])) {
                            sendSyncMessage(p, "Found no Message " + args[3]);
                            return;
                        }
                        String[] argsToSend = new String[args.length - 4];
                        if (args.length > 4) {
                            System.arraycopy(args, 4, argsToSend, 0, args.length - 4);
                        }
                        sendSyncMessage(p, Lang.getInstance().getMessageWithArgs(args[3], argsToSend));
                    } else {
                        sendSyncMessage(p, usageMsg);
                    }
                }
                case "reload" -> {
                    Lang.getInstance().reloadAsync();
                    sendSyncMessage(p, "Scheduled a reload on an Async Thread");
                }
                default -> sendSyncMessage(p, usageMsg);
            }
        }else {
            sendSyncMessage(p, usageMsg);
        }
    }

    private void regexCommand(@NotNull Player p, String @NotNull [] args){
        if(args.length < 3) {
            sendSyncMessage(p, usageRegex);
            return;
        }

        switch (args[2]) {
            case "regexTest" -> {
                if (args.length < 5) {
                    sendSyncMessage(p, usageRegex);
                    return;
                }
                Pattern pat = Pattern.compile(args[3]);
                Matcher match = pat.matcher(args[4]);
                sendSyncMessage(p, "Pattern: " + pat.pattern() + "\nMsg: " + args[4]);
                if (match.find()) {
                    sendSyncMessage(p, "Result:\n" + match.group());
                } else {
                    sendSyncMessage(p, "No Matches found.");
                }
            }
            case "checkTimeArg" -> {
                if (args.length < 4) {
                    sendSyncMessage(p, usageRegex);
                    return;
                }
                long ms = Utils.convertTimeStringToMS(args[3]);
                String msg = Utils.convertTimeMsToString(ms);
                sendSyncMessage(p, "Millis: " + ms + "\nCalculated Times:\n" + msg);
            }
            default -> sendSyncMessage(p, usageRegex);
        }

    }

    private void guimanagerCommand(@NotNull Player p, String @NotNull [] args) {
        sendSyncMessage(p, "GUIManager doesnt support this right now.");
    }

    private void reflectionCmd(@NotNull Player p, String @NotNull [] args) {
        if(args.length>3) {
            if(!insertPackagePrefix.containsKey(p.getUniqueId())) {
                insertPackagePrefix.put(p.getUniqueId(), true);
            }
            switch(args[2].toLowerCase()) {
                case "listfields" -> {
                    try {
                        Class clazz = getClass(p.getUniqueId(), args[3]);
                        Field[] fields = clazz.getDeclaredFields();
                        StringBuilder sb = new StringBuilder("Found ");
                        sb.append(fields.length);
                        sb.append(" Fields:\n");
                        for(Field f:fields) {
                            sb.append("\n").append(Modifier.toString(f.getModifiers())).append(" ");
                            sb.append(f.getName()).append(" - ").append(f.getType().getName());
                        }
                        sendSyncMessage(p, sb.toString());
                    } catch (ClassNotFoundException e) {
                        sendSyncMessage(p, "Couldnt find the Class.");
                        e.printStackTrace();
                    }
                }
                case "listmethods" -> {
                    try {
                        Class clazz = getClass(p.getUniqueId(), args[3]);
                        Method[] methods = clazz.getDeclaredMethods();
                        StringBuilder sb = new StringBuilder("Found ");
                        sb.append(methods.length);
                        sb.append(" Methods:\n");
                        for(Method m:methods) {
                            sb.append("\n\n").append(m.getName()).append(" :\n");
                            sb.append("    Modifiers: ").append(Modifier.toString(m.getModifiers()));
                            sb.append("\n    Params: ");
                            if(m.getParameterCount() > 0){
                                for(Class param:m.getParameterTypes()) {
                                    sb.append("\n        ").append(param.getName());
                                }
                            }else{
                                sb.append("\n        none");
                            }
                            sb.append("\n    Returns ").append(m.getReturnType().getName());
                        }
                        sendSyncMessage(p, sb.toString());
                    } catch (ClassNotFoundException e) {
                        sendSyncMessage(p, "Couldnt find the class.");
                    }
                }
                case "storeinstance" ->  {
                    if(args.length>4){
                        boolean hasInstStored = storedInst.containsKey(p.getUniqueId());
                        try {
                            Class clazz = getClass(p.getUniqueId(), args[3]);
                            Object inst = null;

                            //First try if it is a Field
                            try {
                                Field f = clazz.getDeclaredField(args[4]);
                                boolean isStatic = Modifier.toString(f.getModifiers()).toLowerCase().contains("static");
                                if(!isStatic && !hasInstStored) {
                                    throw new NonStaticWithoutInstanceException();
                                }
                                f.setAccessible(true);
                                inst = f.get(isStatic ? null : storedInst.get(p.getUniqueId()));
                                if(inst==null) {
                                    sendSyncMessage(p, "The Field returned null.");
                                    return;
                                }
                            }catch(NoSuchFieldException ignored){
                            }

                            if(inst==null){
                                try {
                                    //If inst is still null it seems like it wasnt a field
                                    Method m = clazz.getMethod(args[4]);
                                    boolean isStatic = Modifier.toString(m.getModifiers()).toLowerCase().contains("static");
                                    if(!isStatic && !hasInstStored) {
                                        throw new NonStaticWithoutInstanceException();
                                    }
                                    m.setAccessible(true);
                                    inst = m.invoke(isStatic ? null : storedInst.get(p.getUniqueId()));
                                    if(inst==null) {
                                        sendSyncMessage(p, "The Method returned null.");
                                        return;
                                    }
                                }catch(NoSuchMethodException ignored) {
                                }
                            }

                            //If inst is still null here, there wasn't a either a field or a method
                            if(inst==null) {
                                sendSyncMessage(p, "Couldn't find either a field nor a method.");
                            }else{
                                sendSyncMessage(p, "Stored the Instance: \n"+inst.toString());
                                storedInst.put(p.getUniqueId(), inst);
                            }


                        } catch (ClassNotFoundException e) {
                            sendSyncMessage(p, "Couldnt find the class.");
                        } catch (IllegalAccessException e) {
                            sendSyncMessage(p, "Couldnt access the Data.");
                        } catch (NonStaticWithoutInstanceException e) {
                            sendSyncMessage(p, "Can´t access non-static fields without a stored instance!");
                        } catch (InvocationTargetException e) {
                            e.printStackTrace();
                        }
                    }
                }
                case "getfieldvalue" -> {
                    if(args.length>4){
                        boolean hasInstStored = storedInst.containsKey(p.getUniqueId());
                        try {
                            Class clazz = getClass(p.getUniqueId(), args[3]);
                            Object toPrint = getFieldObject(clazz,  args[4], hasInstStored ? storedInst.get(p.getUniqueId()) : null);
                            if(toPrint==null){
                                sendSyncMessage(p, "The Field contains no Data! (null)");
                            }else{
                                sendSyncMessage(p, toPrint.toString());
                            }
                        } catch (ClassNotFoundException e) {
                            sendSyncMessage(p, "Couldnt find the class.");
                        } catch (NoSuchFieldException e) {
                            sendSyncMessage(p, "Couldnt find the field.");
                            e.printStackTrace();
                        } catch (IllegalAccessException e) {
                            sendSyncMessage(p, "Couldnt access the field.");
                        } catch (NonStaticWithoutInstanceException e) {
                            sendSyncMessage(p, "Can´t access non-static fields without a stored instance!");
                        }
                    }
                }
                case "invokemethod" ->  {
                    if(args.length>4){
                        boolean hasInstStored = storedInst.containsKey(p.getUniqueId());
                        try {
                            Class clazz = getClass(p.getUniqueId(), args[3]);
                            Method m = clazz.getDeclaredMethod(args[4]);
                            /*
                            Compiler may Produce the following message, caused by the Line before this comment:
                            "unchecked call to getDeclaredMethod(java.lang.String,java.lang.Class<?>...)
                            as a member of the raw type java.lang.Class"
                             */
                            if(m.getParameterCount() == 0){
                                boolean isStatic = Modifier.toString(m.getModifiers()).contains("static");
                                if(!isStatic && !hasInstStored){
                                    sendSyncMessage(p, "Can´t invoke non-static Methods without a stored instance!");
                                }else{
                                    m.setAccessible(true);
                                    try {
                                        Object o = m.invoke(isStatic ? null : storedInst.get(p.getUniqueId()));
                                        if(m.getReturnType().getName().contains("void") || o == null) {
                                            sendSyncMessage(p, "Invoked the Method succesfully!");
                                        }else{
                                            sendSyncMessage(p, "Invoked the Method, it returned:\n" + o.toString());
                                        }
                                    } catch (InvocationTargetException | IllegalAccessException e) {
                                        e.printStackTrace();
                                    } catch(Exception e){
                                        StringWriter sw = new StringWriter();
                                        PrintWriter pw = new PrintWriter(sw);
                                        e.printStackTrace(pw);
                                        String stackTrace = sw.toString();
                                        pw.close();
                                        try {
                                            sw.close();
                                        } catch (IOException ex) {
                                            ex.printStackTrace();
                                        }
                                        sendSyncMessage(p, "An Exception occured while invoking the Method:\n" + stackTrace);
                                    }
                                }
                            }else{
                                sendSyncMessage(p, "Can only call Methods without Parameters.");
                            }

                        } catch (ClassNotFoundException e) {
                            sendSyncMessage(p, "Couldnt find the class.");
                        } catch (NoSuchMethodException e) {
                            sendSyncMessage(p, "Couldnt find the method.");
                        }
                    }
                }

                case "toggle" -> {
                    boolean val = !insertPackagePrefix.get(p.getUniqueId());
                    insertPackagePrefix.put(p.getUniqueId(), val);
                    sendSyncMessage(p, val ? "Now adds package names itself" : "No longer adds package names");
                }

                default -> sendSyncMessage(p, usageReflection);
            }

        }else{
            sendSyncMessage(p, usageReflection);
        }

    }


    @NotNull
    private static Class getClass(UUID id, String name) throws ClassNotFoundException {
        String classname = insertPackagePrefix.get(id) ? ("com.github.exobite.mc.playtimerewards." + name) : name;
        return Class.forName(classname);
    }


    private static Object getFieldObject(@NotNull Class clazz, String name, Object instance) throws
            ClassNotFoundException, NoSuchFieldException, IllegalAccessException, NonStaticWithoutInstanceException{

        Field f = clazz.getDeclaredField(name);
        boolean isStatic = Modifier.toString(f.getModifiers()).toLowerCase().contains("static");
        if(!isStatic && instance==null) {
            throw new NonStaticWithoutInstanceException();
        }
        f.setAccessible(true);
        return f.get(isStatic ? null : instance);
    }

    private static class NonStaticWithoutInstanceException extends Exception {

        NonStaticWithoutInstanceException(){}

    }

    private static abstract class PlaceholderRunnable {
        private final String desc;

        public PlaceholderRunnable(final String desc) {
            this.desc = desc;
        }

        public String getDescription(){
            return desc;
        }

        public abstract String run(final Player p);

    }

}
