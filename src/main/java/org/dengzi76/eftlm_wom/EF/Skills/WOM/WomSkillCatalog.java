package org.dengzi76.eftlm_wom.EF.Skills.WOM;

import java.util.List;
import net.EFTLM.EF.API.Event.MaidSkillBuildEvent;
import net.EFTLM.EF.Capability.MaidPatch;
import net.EFTLM.EF.Skill.MaidSkill;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import org.dengzi76.eftlm_wom.EF.Register.EFTLMWOM_TAB;
import org.dengzi76.eftlm_wom.EF.Skills.Passive.Meditation;
import yesman.epicfight.world.capabilities.item.CapabilityItem;

/** Complete registry inventory from the bundled WOM 2.0.171 WOMSkills.buildSkillEvent. */
public final class WomSkillCatalog {
    public enum Group { DODGE, GUARD, PASSIVE, MOVER, IDENTITY, WEAPON_INNATE, WEAPON_PASSIVE }

    public record Entry(String id, Group group, String weapon, float consumption, int maxStacks, String texture) {
        public ResourceLocation location() { return ResourceLocation.fromNamespaceAndPath("eftlm_wom", id); }
        public ResourceLocation icon() {
            String folder = switch (group) {
                case DODGE -> "dodge";
                case GUARD -> "guard";
                case WEAPON_PASSIVE, WEAPON_INNATE -> "weapon_innate";
                default -> group.name().toLowerCase(java.util.Locale.ROOT);
            };
            return ResourceLocation.fromNamespaceAndPath("wom", "textures/gui/skills/" + folder + "/" + texture + ".png");
        }
        public boolean matches(MaidPatch<?> patch) {
            if (weapon.isEmpty()) return true;
            var item = patch.getOriginal().getMainHandItem().getItem();
            var name = ForgeRegistries.ITEMS.getKey(item);
            if (name == null || !name.getNamespace().equals("wom")) return false;
            boolean matches = weapon.equals("staff") ? name.getPath().endsWith("_staff") : name.getPath().equals(weapon);
            if (!matches) return false;
            var style = patch.getHoldingItemCapability(net.minecraft.world.InteractionHand.MAIN_HAND).getStyle(patch);
            if (id.equals("ender_blast")) return style == CapabilityItem.Styles.ONE_HAND;
            if (id.equals("ender_fusion")) return style == CapabilityItem.Styles.TWO_HAND;
            return true;
        }
    }

    public static final List<Entry> ALL = List.of(
        new Entry("ender_step", Group.DODGE, "", 4F, 1, "ender_step"),
        new Entry("ender_obscuris", Group.DODGE, "", 7F, 1, "ender_obscuris"),
        new Entry("shadow_step", Group.DODGE, "", 7F, 1, "shadow_step"),
        new Entry("precise_roll", Group.DODGE, "", 3F, 1, "precise_roll"),
        new Entry("dodge_master", Group.DODGE, "", 4F, 1, "dodge_master"),
        new Entry("bull_charge", Group.DODGE, "", 8F, 1, "bull_charge"),
        new Entry("time_travel", Group.DODGE, "", 12F, 1, "time_travel"),
        new Entry("punishment_kick", Group.DODGE, "", 8F, 1, "punishment_kick"),
        new Entry("counter_attack", Group.GUARD, "", 0F, 1, "counter_attack"),
        new Entry("vengeful_parry", Group.GUARD, "", 0F, 1, "vengeful_parry"),
        new Entry("perfect_bulwark", Group.GUARD, "", 0F, 1, "perfect_bulwark"),
        new Entry("soul_protection", Group.GUARD, "", 0F, 1, "soul_protection"),
        new Entry("shulker_cloak", Group.GUARD, "", 0F, 1, "shulker_cloak"),
        new Entry("buster_parade", Group.GUARD, "", 0F, 1, "buster_parade"),
        new Entry("arrow_tenacity", Group.PASSIVE, "", 0F, 1, "arrow_tenacity"),
        new Entry("pain_anticipation", Group.PASSIVE, "", 0F, 1, "pain_anticipation"),
        new Entry("latent_retribution", Group.PASSIVE, "", 0F, 1, "latent_retribution"),
        new Entry("vampirize", Group.PASSIVE, "", 0F, 1, "vampirize"),
        new Entry("critical_knowledge", Group.PASSIVE, "", 0F, 1, "critical_knowledge"),
        new Entry("heart_shield", Group.PASSIVE, "", 0F, 1, "heart_shield"),
        new Entry("mindset", Group.PASSIVE, "", 0F, 1, "mindset"),
        new Entry("adrenaline", Group.PASSIVE, "", 0F, 1, "adrenaline"),
        new Entry("dancing_blade", Group.PASSIVE, "", 0F, 1, "dancing_blade"),
        new Entry("meditation", Group.PASSIVE, "", 0F, 1, "meditation"),
        new Entry("inner_growth", Group.PASSIVE, "", 0F, 1, "inner_growth"),
        new Entry("dopamine", Group.PASSIVE, "", 0F, 1, "dopamine"),
        new Entry("manipulator", Group.PASSIVE, "", 0F, 1, "manipulator"),
        new Entry("lethal_focus", Group.PASSIVE, "", 0F, 1, "lethal_focus"),
        new Entry("spider_techniques", Group.MOVER, "", 0F, 1, "spider_techniques"),
        new Entry("aqua_maneuvre", Group.MOVER, "", 0F, 1, "aqua_maneuvre"),
        new Entry("natural_sprinter", Group.MOVER, "", 0F, 1, "natural_sprinter"),
        new Entry("lunatic_vivacity", Group.IDENTITY, "", 0F, 1, "lunatic_vivacity"),
        new Entry("voodoo_magic", Group.IDENTITY, "", 0F, 1, "voodoo_magic"),
        new Entry("back_and_forth", Group.IDENTITY, "", 0F, 1, "back_and_forth"),
        new Entry("shooting_style", Group.IDENTITY, "", 0F, 1, "shooting_style"),
        new Entry("proximity_exploiter", Group.IDENTITY, "", 0F, 1, "proximity_exploiter"),
        new Entry("all_eyes_on_you", Group.IDENTITY, "", 0F, 1, "all_eyes_on_you"),
        new Entry("all_eyes_on_me", Group.IDENTITY, "", 0F, 1, "all_eyes_on_me"),
        new Entry("avatar_of_might", Group.IDENTITY, "", 0F, 1, "avatar_of_might"),
        new Entry("charybdis", Group.WEAPON_INNATE, "staff", 6F, 1, "charybdis"),
        new Entry("agony_plunge", Group.WEAPON_INNATE, "agony", 100F, 3, "agony_plunge"),
        new Entry("true_berserk", Group.WEAPON_INNATE, "tormented_mind", 200F, 1, "true_berserk"),
        new Entry("plunder_perdition", Group.WEAPON_INNATE, "ruine", 16F, 10, "plunder_perdition"),
        new Entry("sakura_state", Group.WEAPON_INNATE, "satsujin", 45F, 12, "sakura_state"),
        new Entry("ender_blast", Group.WEAPON_INNATE, "ender_blaster", 6F, 12, "ender_blast"),
        new Entry("ender_fusion", Group.WEAPON_INNATE, "ender_blaster", 12F, 24, "ender_fusion"),
        new Entry("regierung", Group.WEAPON_INNATE, "herrscher", 10F, 1, "regierung"),
        new Entry("lunar_eclipse", Group.WEAPON_INNATE, "moonless", 160F, 2, "lunar_eclipse"),
        new Entry("solar_arcano", Group.WEAPON_INNATE, "solar", 100F, 1, "solar_arcano"),
        new Entry("rechargement", Group.WEAPON_INNATE, "napoleon", 8F, 1, "rechargement"),
        new Entry("orbital_beam", Group.WEAPON_INNATE, "orbit", 64F, 4, "orbital_beam"),
        new Entry("flash_mutilation", Group.WEAPON_INNATE, "nova", 48F, 4, "flash_mutilation"),
        new Entry("unbreakable", Group.WEAPON_INNATE, "blackstar", 82F, 1, "unbreakable"),
        new Entry("satsujin_passive", Group.WEAPON_PASSIVE, "satsujin", 0F, 1, "sakura_state"),
        new Entry("ruine_passive", Group.WEAPON_PASSIVE, "ruine", 0F, 1, "plunder_perdition"),
        new Entry("herrscher_passive", Group.WEAPON_PASSIVE, "herrscher", 0F, 1, "regierung"),
        new Entry("torment_passive", Group.WEAPON_PASSIVE, "tormented_mind", 0F, 1, "true_berserk"),
        new Entry("lunar_echo_passive", Group.WEAPON_PASSIVE, "moonless", 0F, 1, "lunar_eclipse"),
        new Entry("solar_passive", Group.WEAPON_PASSIVE, "solar", 0F, 1, "solar_arcano"),
        new Entry("napoleon_passive", Group.WEAPON_PASSIVE, "napoleon", 0F, 1, "rechargement"),
        new Entry("evil_tachi_passive", Group.WEAPON_PASSIVE, "evil_tachi", 0F, 1, "flash_mutilation"),
        new Entry("unbreakable_passive", Group.WEAPON_PASSIVE, "blackstar", 0F, 1, "unbreakable")
    );

    private WomSkillCatalog() { }

    public static void register(MaidSkillBuildEvent event) {
        for (Entry entry : ALL) {
            var builder = MaidSkill.createBuilder().setCreativeTab(EFTLMWOM_TAB.Skill.get())
                    .setIcon(ResourceLocation.fromNamespaceAndPath("eftlm_wom", "textures/item/skillbook.png"));
            switch (entry.group()) {
                case DODGE -> event.build(entry.location(), b -> new WomDodgeSkill(b, entry), builder);
                case GUARD -> event.build(entry.location(), b -> new WomGuardSkill(b, entry), builder);
                case WEAPON_INNATE -> event.build(entry.location(), b -> new WomWeaponSkill(b, entry), builder);
                default -> {
                    if (entry.id().equals("meditation")) event.build(entry.location(), Meditation::new, builder);
                    else event.build(entry.location(), b -> new WomPassiveSkill(b, entry), builder);
                }
            }
        }
        org.apache.logging.log4j.LogManager.getLogger("eftlm_wom").info("Registered {} WOM maid skills", ALL.size());
    }
}

