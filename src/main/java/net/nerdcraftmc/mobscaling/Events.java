package net.nerdcraftmc.mobscaling;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
// import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;

@Mod.EventBusSubscriber(modid = MobScaling.MOD_ID)
public class Events {
    static HashMap<EntityType<? extends Monster>, HashMap<ResourceKey<Biome>, Float>> changeAttributes = new HashMap<>();

    @SubscribeEvent
    public static void Attribute(final EntityJoinLevelEvent event) {
        // Get variables, including full NBT tag, distance from (0, 0), and current HP
        Entity entity = event.getEntity();
        // TODO? ((Monster) entity).getAttribute(Attributes.MAX_HEALTH).addPermanentModifier(new AttributeModifier("a", 1, AttributeModifier.Operation.MULTIPLY_TOTAL));
        if (entity instanceof Monster) {
            CompoundTag mainTag = entity.serializeNBT();
            int distance = Math.max(Math.abs(entity.getBlockX()), Math.abs(entity.getBlockZ()));
            float hp = mainTag.getFloat("Health");
            float interval = 1000;
            float changePercent = (distance / interval);

            // Add changed attributes to attribute list
            addAttribute(mainTag, entity, "minecraft:generic.max_health",                 hp,         hp, changePercent);
            addAttribute(mainTag, entity, "minecraft:generic.movement_speed",       0.23f, 0.05f, changePercent);
            addAttribute(mainTag, entity, "minecraft:generic.attack_damage",        4,     4, changePercent);
            addAttribute(mainTag, entity, "minecraft:generic.follow_range",         32,    32, changePercent);
            addAttribute(mainTag, entity, "minecraft:generic.armor",                0,     5, changePercent);
            addAttribute(mainTag, entity, "minecraft:generic.armor_toughness",      0,     2, changePercent);
            addAttribute(mainTag, entity, "minecraft:generic.attack_knockback",     0,     0.5f, changePercent);
            addAttribute(mainTag, entity, "minecraft:generic.knockback_resistance", 0,     0.1f, changePercent);
            mainTag.putFloat("Health", hp + hp * changePercent);

            // Write new NBT to entity
            entity.deserializeNBT(mainTag);
        }
    }
    // Change an attribute. Takes an attribute name (String) and a new value (Float). Returns a CompoundTag.
    static CompoundTag changeAttribute(String name, float value) {
        CompoundTag tag = new CompoundTag();
        tag.putFloat("Base", value);
        tag.putString("Name", name);
        return tag;
    }

    static void biomeMap(EntityType<? extends Monster> monster, ResourceKey<Biome> biome, Float value) {
        HashMap<ResourceKey<Biome>, Float> map;
        if (!changeAttributes.containsKey(monster)) {
            map = new HashMap<>();
        }
        else {
            map = changeAttributes.get(monster);
        }
        map.put(biome, value);
        changeAttributes.put(monster, map);
    }
    static void addAttribute(CompoundTag mainTag, Entity entity, String attribute, float initial, float step, float change) {

        // Get attribute list from main tag
        // pTagType is the tag type of the items inside the Attributes list, which in this case is CompoundTag (10)
        ListTag attributesListTag = mainTag.getList("Attributes", 10);
        attributesListTag.addTag(attributesListTag.size(), changeAttribute(attribute, initial + step * change));

        // Add attribute list back into main tag
        mainTag.put("Attributes", attributesListTag);
    }
    @SubscribeEvent
    public static void entityLeave(EntityLeaveLevelEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof Monster) {
            CompoundTag mainTag = entity.serializeNBT();
            float hp = mainTag.getFloat("Health");
            mainTag.putFloat("Health", (float) ((Monster) entity).getAttributeBaseValue(Attributes.MAX_HEALTH));
            entity.deserializeNBT(mainTag);
        }
    }
}
