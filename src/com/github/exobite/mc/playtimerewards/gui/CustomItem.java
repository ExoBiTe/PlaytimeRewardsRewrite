package com.github.exobite.mc.playtimerewards.gui;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.md_5.bungee.api.ChatColor;


public class CustomItem {

    /*
     *
     * Newer Version of customItem.
     * generates a new Itemstack on method call getItemStack(), doesn´t cache it anymore.
     *
     * Made for 1.13+, doesn´t use the data short anymore
     *
     * Also got many setters.
     *
     */

    private String Name;
    private List<String> Lore;
    private Material Mat;
    private int Amount;
    //private short data;
    private Map<Enchantment, Integer> Enchants = new HashMap<>();

    public CustomItem(Material Mat) {
        this.Name = Mat.name();
        this.Lore = null;
        this.Mat = Mat;
        this.Amount = 1;
        //this.data = 0;
    }

    public CustomItem(String Name, List<String> Lore, Material Mat, /*short Damage, */int Amount) {
        this.Name = Name;
        this.Lore = Lore;
        this.Mat = Mat;
        this.Amount = Amount;
        //this.data = Damage;
    }

    public CustomItem(ItemStack is) {
        Amount = is.getAmount();
        Mat = is.getType();
        //data = is.getDurability();
        Enchants = is.getEnchantments();
        Lore = is.getItemMeta().getLore();
    }

    public CustomItem addEnchantment(Enchantment e, int lv) {
        Enchants.put(e, lv);
        return this;
    }

    public CustomItem setDisplayName(String displayName) {
        this.Name = displayName;
        return this;
    }

    public CustomItem setLore(List<String> Lore) {
        this.Lore = Lore;
        return this;
    }

    public CustomItem setMaterial(Material mat) {
        this.Mat = mat;
        return this;
    }

    public ItemStack getItemStack() {
        ItemStack is = new ItemStack(Mat, Amount/*, data*/);
        ItemMeta im = is.getItemMeta();
        if(im==null) return is;
        if(Name!=null) im.setDisplayName(ChatColor.RESET.toString() + Name);
        if(Lore!=null) im.setLore(Lore);
        for(Enchantment e:Enchants.keySet()) {
            im.addEnchant(e, Enchants.get(e), true);
        }
        is.setItemMeta(im);
        return is;
    }
}
