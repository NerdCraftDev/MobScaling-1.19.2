package net.nerdcraftmc.mobscaling;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.util.HashMap;

@Mod.EventBusSubscriber(modid = MobScaling.MOD_ID)
public class Events {
    static HashMap<EntityType<? extends Monster>, HashMap<ResourceKey<Biome>, Float>> changeAttributes = new HashMap<>();

    public Events() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        hashMap(EntityType.ZOMBIE, Biomes.DESERT, 0.5f);
    }

    @SubscribeEvent
    public static void Attribute(final EntityJoinLevelEvent event) {
        // Get main tag
        Entity entity = event.getEntity();
        CompoundTag mainTag = entity.serializeNBT();

        // Get attribute list from main tag
        // pTagType is the tag type of the items inside the Attributes list, which in this case is CompoundTag (10)
        ListTag attributesListTag = mainTag.getList("Attributes",10);

        // Add changed attribute to attribute list
        attributesListTag.addTag(attributesListTag.size(), changeAttribute("minecraft:generic.max_health", 40.0F));
        attributesListTag.addTag(attributesListTag.size(), changeAttribute("minecraft:generic.movement_speed", 0.5F));

        // Add attribute list back into main tag
        mainTag.put("Attributes", attributesListTag);

        // Write new NBT to entity
        entity.deserializeNBT(mainTag);
    }
    // Change an attribute. Takes an attribute name (String) and a new value (float). Returns a CompoundTag.
    static CompoundTag changeAttribute(String name, float value) {
        CompoundTag tag = new CompoundTag();
        tag.putFloat("Base", value);
        tag.putString("Name", name);
        return tag;
    }

    static void hashMap(EntityType<? extends Monster> monster, ResourceKey<Biome> biome, Float value) {
        HashMap<ResourceKey<Biome>, Float> map = new HashMap<>();
        map.put(biome, value);
        changeAttributes.put(monster, map);
    }
}
