package org.dengzi76.eftlm_wom.gametest;

import com.github.tartaricacid.touhoulittlemaid.init.InitEntities;
import net.EFTLM.EF.API.Event.MaidHurtTargetEvent;
import net.EFTLM.EF.API.Event.MaidSkillInitEvent;
import net.EFTLM.EF.Capability.MaidPatch;
import net.EFTLM.EF.Item.MaidSkillBookItem;
import net.EFTLM.EF.Skill.MaidSkillManager;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import reascer.wom.gameasset.animations.weapons.AnimsRuine;
import reascer.wom.world.item.WOMItems;
import yesman.epicfight.api.animation.property.AnimationEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.registries.ForgeRegistries;
import net.EFTLM.TLM.Task.FightModeTask;
import org.dengzi76.eftlm_wom.EF.Skills.WOM.WomMaidSkill;
import org.dengzi76.eftlm_wom.EF.Skills.WOM.WomSkillCatalog;
import org.dengzi76.eftlm_wom.EF.Skills.WOM.WomWeaponSkill;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import org.dengzi76.eftlm_wom.EF.Event.WomMaidSkillEvents;
import reascer.wom.gameasset.WOMAnimations;

@GameTestHolder("eftlm_wom")
@PrefixGameTestTemplate(false)
public final class WomSkillGameTests {
    @GameTest(template = "skill_test", timeoutTicks = 40)
    public static void defenseSkillsAnticipateNearbyEnemies(GameTestHelper helper) {
        for (var entry : WomSkillCatalog.ALL) {
            if (entry.group() != WomSkillCatalog.Group.DODGE && entry.group() != WomSkillCatalog.Group.GUARD) continue;
            if (entry.id().equals("soul_protection") || entry.id().equals("shulker_cloak")) continue;
            var maid = helper.spawnWithNoFreeWill(InitEntities.MAID.get(), 1, 2, 1);
            var target = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, 3, 2, 3);
            maid.setNoGravity(true);
            target.setNoGravity(true);
            target.setInvulnerable(true);
            maid.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, new ItemStack(Items.IRON_SWORD));
            maid.setTask(new FightModeTask());
            maid.setTarget(target);
            maid.getBrain().setMemory(net.minecraft.world.entity.ai.memory.MemoryModuleType.ATTACK_TARGET, target);
            MaidPatch<?> patch = EpicFightCapabilities.getEntityPatch(maid, MaidPatch.class);
            patch.setStamina(patch.getMaxStamina());
            var skill = MaidSkillManager.getSkillFor(entry.location());
            patch.addLearnedSkill(entry.location());
            helper.runAfterDelay(3, () -> helper.assertTrue(patch.getDataValue(skill, WomMaidSkill.COOLDOWN) > 0,
                    "Defense did not anticipate nearby hostile target: " + entry.id()));
        }
        helper.runAfterDelay(5, helper::succeed);
    }

    @GameTest(template = "skill_test", timeoutTicks = 40)
    public static void boostedRandomPassivesUseLearnedEventDispatch(GameTestHelper helper) {
        var maid = helper.spawnWithNoFreeWill(InitEntities.MAID.get(), 1, 2, 1);
        var target = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, 3, 2, 3);
        maid.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,
                new ItemStack(ForgeRegistries.ITEMS.getValue(ResourceLocation.fromNamespaceAndPath("wom", "evil_tachi"))));
        maid.setTask(new FightModeTask());
        MaidPatch<?> patch = EpicFightCapabilities.getEntityPatch(maid, MaidPatch.class);
        patch.addLearnedSkill(ResourceLocation.fromNamespaceAndPath("eftlm_wom", "critical_knowledge"));
        patch.addLearnedSkill(ResourceLocation.fromNamespaceAndPath("eftlm_wom", "evil_tachi_passive"));
        maid.getRandom().setSeed(20261006L);
        int criticals = 0;
        int withers = 0;
        for (int i = 0; i < 1000; i++) {
            target.removeEffect(net.minecraft.world.effect.MobEffects.WITHER);
            var hit = new LivingHurtEvent(target, maid.damageSources().mobAttack(maid), 10);
            net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(hit);
            if (hit.getAmount() > 10) criticals++;
            if (target.hasEffect(net.minecraft.world.effect.MobEffects.WITHER)) withers++;
        }
        helper.assertTrue(criticals >= 300 && criticals <= 500, "Unexpected boosted critical rate: " + criticals);
        helper.assertTrue(withers >= 300 && withers <= 500, "Unexpected boosted Wither rate: " + withers);
        helper.succeed();
    }

    @GameTest(template = "skill_test", timeoutTicks = 40)
    public static void learnedDefenseSkillsHandleAttackEvents(GameTestHelper helper) {
        for (var entry : WomSkillCatalog.ALL) {
            boolean dodge = entry.group() == WomSkillCatalog.Group.DODGE;
            boolean parry = entry.group() == WomSkillCatalog.Group.GUARD
                    && !entry.id().equals("soul_protection") && !entry.id().equals("shulker_cloak");
            if (!dodge && !parry) continue;
            var maid = helper.spawnWithNoFreeWill(InitEntities.MAID.get(), 1, 2, 1);
            var attacker = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, 3, 2, 3);
            maid.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, new ItemStack(Items.IRON_SWORD));
            maid.setTask(new FightModeTask());
            maid.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES, attacker.getEyePosition());
            MaidPatch<?> patch = EpicFightCapabilities.getEntityPatch(maid, MaidPatch.class);
            var skill = MaidSkillManager.getSkillFor(entry.location());
            patch.addLearnedSkill(entry.location());
            patch.setStamina(patch.getMaxStamina());
            // Isolate the learned skill's defense window from the enemy AI's timing.
            patch.setData(skill, WomMaidSkill.TIMER, 8);
            var event = new com.github.tartaricacid.touhoulittlemaid.api.event.MaidAttackEvent(
                    maid, attacker.damageSources().mobAttack(attacker), 1);
            net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(event);
            if (entry.id().equals("time_travel"))
                helper.assertFalse(event.isCanceled(), "Time travel must rewind rather than evade damage");
            else
                helper.assertTrue(event.isCanceled(), "Learned defense did not handle attack event: " + entry.id());
            if (parry)
                helper.assertTrue(patch.getDataValue(skill, WomMaidSkill.TIMER) == 0,
                        "Parry window was not consumed: " + entry.id());
        }
        helper.succeed();
    }

    @GameTest(template = "skill_test", timeoutTicks = 40)
    public static void nonWeaponSkillsReceiveMaidTicks(GameTestHelper helper) {
        int maidIndex = 0;
        for (var entry : WomSkillCatalog.ALL) {
            if (entry.group() == WomSkillCatalog.Group.WEAPON_INNATE) continue;
            // Spread the 48 maids out so entity cramming cannot consume defensive stacks.
            var maid = helper.spawnWithNoFreeWill(InitEntities.MAID.get(), 1 + maidIndex % 4, 2, 1 + maidIndex / 4 % 4);
            maidIndex++;
            maid.setNoGravity(true);
            var weapon = entry.weapon().isEmpty() ? Items.IRON_SWORD
                    : ForgeRegistries.ITEMS.getValue(ResourceLocation.fromNamespaceAndPath("wom", entry.weapon()));
            maid.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, new ItemStack(weapon));
            maid.setTask(new FightModeTask());
            MaidPatch<?> patch = EpicFightCapabilities.getEntityPatch(maid, MaidPatch.class);
            var skill = MaidSkillManager.getSkillFor(entry.location());
            patch.addLearnedSkill(entry.location());
            if (skill instanceof WomMaidSkill) {
                patch.setData(skill, WomMaidSkill.TIMER, 40);
                helper.runAfterDelay(10, () -> {
                    helper.assertTrue(skill.canExecute(patch), "Non-weapon skill rejected maid: " + entry.id());
                    helper.assertTrue(patch.getDataValue(skill, WomMaidSkill.TIMER) < 40,
                            "Learned skill did not receive EFTLM ticks: " + entry.id());
                    if (entry.id().equals("soul_protection") || entry.id().equals("shulker_cloak"))
                        helper.assertTrue(patch.getDataValue(skill, WomMaidSkill.STACK) > 0,
                                "Defensive skill did not build protection: " + entry.id()
                                        + "; health=" + maid.getHealth()
                                        + "; cooldown=" + patch.getDataValue(skill, WomMaidSkill.COOLDOWN));
                    if (entry.id().equals("heart_shield"))
                        helper.assertTrue(maid.getAbsorptionAmount() > 0, "Heart shield did not apply absorption");
                });
                // Force the next ordinary tick to reach the shield's periodic application.
                if (entry.id().equals("heart_shield")) maid.tickCount = 29;
            } else if (skill instanceof org.dengzi76.eftlm_wom.EF.Skills.Passive.Meditation meditation) {
                patch.setData(skill, org.dengzi76.eftlm_wom.EF.Skills.Passive.Meditation.COOLDOWN_END_TICK, -1000);
                helper.runAfterDelay(10, () -> helper.assertTrue(
                        patch.getDataValue(meditation, org.dengzi76.eftlm_wom.EF.Skills.Passive.Meditation.MEDITATION_TIMER) > 0,
                        "Meditation did not start through EFTLM tick dispatcher"));
            } else {
                helper.assertTrue(false, "Uncovered skill implementation: " + entry.id());
            }
        }
        helper.runAfterDelay(15, helper::succeed);
    }

    @GameTest(template = "skill_test", timeoutTicks = 40)
    public static void registeredSkillDataSurvivesSaveLoad(GameTestHelper helper) {
        var first = helper.spawnWithNoFreeWill(InitEntities.MAID.get(), 1, 2, 1);
        var restored = helper.spawnWithNoFreeWill(InitEntities.MAID.get(), 3, 2, 1);
        MaidPatch<?> patch = EpicFightCapabilities.getEntityPatch(first, MaidPatch.class);
        MaidPatch<?> restoredPatch = EpicFightCapabilities.getEntityPatch(restored, MaidPatch.class);
        var weapon = MaidSkillManager.getSkillFor(ResourceLocation.fromNamespaceAndPath("eftlm_wom", "agony_plunge"));
        var guard = MaidSkillManager.getSkillFor(ResourceLocation.fromNamespaceAndPath("eftlm_wom", "guard"));
        var meditation = MaidSkillManager.getSkillFor(ResourceLocation.fromNamespaceAndPath("eftlm_wom", "meditation"));
        for (var skill : java.util.List.of(weapon, guard, meditation)) {
            patch.addLearnedSkill(skill.getRegistryName());
            skill.onInit(new MaidSkillInitEvent(patch));
        }
        var keys = org.dengzi76.eftlm_wom.EF.Register.WomSkillDataKeys.ALL;
        helper.assertTrue(keys.size() == 23, "Data key catalog changed unexpectedly");
        for (var key : keys) helper.assertTrue(net.EFTLM.EF.Skill.MaidSkillDataManager.SkillDataKey.byId(key.getId()) == key,
                "Data key was not globally registered: " + key.getId());
        patch.setData(weapon, WomWeaponSkill.ENERGY, 17.5F);
        patch.setData(weapon, WomWeaponSkill.CHARGES, 1);
        patch.setData(weapon, WomMaidSkill.COOLDOWN, 43);
        patch.setData(guard, org.dengzi76.eftlm_wom.EF.Skills.Guard.GUARD_PENALTY, 0.75F);
        patch.setData(guard, org.dengzi76.eftlm_wom.EF.Skills.Guard.Blocking, true);
        patch.setData(meditation, org.dengzi76.eftlm_wom.EF.Skills.Passive.Meditation.CYCLE, 3);
        restoredPatch.deserializeNBT(patch.serializeNBT());
        for (var skill : java.util.List.of(weapon, guard, meditation)) skill.onInit(new MaidSkillInitEvent(restoredPatch));
        helper.assertTrue(restoredPatch.getDataValue(weapon, WomWeaponSkill.ENERGY) == 17.5F
                && restoredPatch.getDataValue(weapon, WomWeaponSkill.CHARGES) == 1
                && restoredPatch.getDataValue(weapon, WomMaidSkill.COOLDOWN) == 43, "Weapon state was lost during loading or init");
        helper.assertTrue(restoredPatch.getDataValue(guard, org.dengzi76.eftlm_wom.EF.Skills.Guard.GUARD_PENALTY) == 0.75F
                && restoredPatch.getDataValue(guard, org.dengzi76.eftlm_wom.EF.Skills.Guard.Blocking), "Guard data was reset during init");
        helper.assertTrue(restoredPatch.getDataValue(meditation, org.dengzi76.eftlm_wom.EF.Skills.Passive.Meditation.CYCLE) == 3,
                "Meditation cycle was reset during init");
        restoredPatch.setData(weapon, WomWeaponSkill.ENERGY, 2F);
        helper.assertTrue(patch.getDataValue(weapon, WomWeaponSkill.ENERGY) == 17.5F, "Loaded data was shared between maids");
        helper.succeed();
    }
    @GameTest(template = "skill_test", timeoutTicks = 160)
    public static void weaponSkillsActivateAndRunOnMaids(GameTestHelper helper) {
        for (var entry : WomSkillCatalog.ALL) {
            if (entry.group() != WomSkillCatalog.Group.WEAPON_INNATE) continue;
            var maid = helper.spawnWithNoFreeWill(InitEntities.MAID.get(), 1, 2, 1);
            var target = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, 3, 2, 3);
            maid.setNoGravity(true);
            target.setNoGravity(true);
            target.setInvulnerable(true);
            var weapon = entry.weapon().equals("staff")
                    ? ForgeRegistries.ITEMS.getValues().stream().filter(item -> {
                        var key = ForgeRegistries.ITEMS.getKey(item);
                        return key != null && key.getNamespace().equals("wom") && key.getPath().endsWith("_staff");
                    }).findFirst().orElseThrow()
                    : ForgeRegistries.ITEMS.getValue(ResourceLocation.fromNamespaceAndPath("wom", entry.weapon()));
            maid.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, new ItemStack(weapon));
            if (entry.id().equals("ender_fusion"))
                maid.setItemInHand(net.minecraft.world.InteractionHand.OFF_HAND, new ItemStack(weapon));
            maid.setTask(new FightModeTask());
            maid.setTarget(target);
            maid.getBrain().setMemory(net.minecraft.world.entity.ai.memory.MemoryModuleType.ATTACK_TARGET, target);
            MaidPatch<?> patch = EpicFightCapabilities.getEntityPatch(maid, MaidPatch.class);
            var skill = (WomWeaponSkill) MaidSkillManager.getSkillFor(entry.location());
            patch.addLearnedSkill(entry.location());
            patch.setStamina(patch.getMaxStamina());
            patch.setData(skill, WomWeaponSkill.CHARGES, 1);
            helper.runAfterDelay(20, () -> {
                helper.assertTrue(skill.canExecute(patch), "Weapon/style restriction rejected: " + entry.id());
                // The normal EFTLM tick dispatcher should have activated the learned skill.
                helper.assertTrue(patch.getDataValue(skill, WomWeaponSkill.COMBO) > 0,
                        "Learned weapon skill did not activate: " + entry.id()
                                + "; target=" + patch.getTarget() + "; state=" + patch.getEntityState()
                                + "; charges=" + patch.getDataValue(skill, WomWeaponSkill.CHARGES)
                                + "; cooldown=" + patch.getDataValue(skill, WomMaidSkill.COOLDOWN));
            });
        }
        // Allow animation callbacks and spawned WOM helper entities to execute on the server.
        helper.runAfterDelay(120, helper::succeed);
    }

    @GameTest(template = "skill_test", timeoutTicks = 40)
    public static void healthPassivesAndWeaponRestriction(GameTestHelper helper) {
        var maid = helper.spawnWithNoFreeWill(InitEntities.MAID.get(), 1, 2, 1);
        var enemy = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, 3, 2, 3);
        MaidPatch<?> patch = EpicFightCapabilities.getEntityPatch(maid, MaidPatch.class);
        helper.assertTrue(patch != null, "Maid capability did not attach");
        var adrenaline = (WomMaidSkill) MaidSkillManager.getSkillFor(ResourceLocation.fromNamespaceAndPath("eftlm_wom", "adrenaline"));
        maid.setHealth(maid.getMaxHealth() * 0.1F);
        var outgoing = new LivingHurtEvent(enemy, maid.damageSources().mobAttack(maid), 10);
        adrenaline.modifyOutgoing(outgoing, patch);
        helper.assertTrue(Math.abs(outgoing.getAmount() - 16) < 0.001F, "Low-health damage bonus incorrect");
        var incoming = new LivingHurtEvent(maid, enemy.damageSources().mobAttack(enemy), 10);
        adrenaline.modifyIncoming(incoming, patch);
        helper.assertTrue(Math.abs(incoming.getAmount() - 6) < 0.001F, "Low-health damage reduction incorrect");
        var vampire = MaidSkillManager.getSkillFor(ResourceLocation.fromNamespaceAndPath("eftlm_wom", "vampirize"));
        maid.setHealth(5);
        vampire.onHurtTargetPost(new MaidHurtTargetEvent.Post(patch, enemy, maid.damageSources().mobAttack(maid), 10));
        helper.assertTrue(Math.abs(maid.getHealth() - 7) < 0.001F, "Lifesteal must heal 20% of damage");
        var agony = WomSkillCatalog.ALL.stream().filter(e -> e.id().equals("agony_plunge")).findFirst().orElseThrow();
        maid.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, new ItemStack(Items.IRON_SWORD));
        helper.assertFalse(agony.matches(patch), "Agony skill accepted another weapon");
        maid.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, new ItemStack(WOMItems.AGONY.get()));
        helper.assertTrue(agony.matches(patch), "Agony skill rejected Agony");
        helper.succeed();
    }

    @GameTest(template = "skill_test", timeoutTicks = 40)
    public static void playerOnlyCallbackIsSkippedForMaids(GameTestHelper helper) {
        var maid = helper.spawnWithNoFreeWill(InitEntities.MAID.get(), 1, 2, 1);
        MaidPatch<?> patch = EpicFightCapabilities.getEntityPatch(maid, MaidPatch.class);
        helper.assertTrue(patch != null, "Maid capability did not attach");
        int[] executions = {0};
        AnimationEvent.E0 callback = (entitypatch, animation, params) -> executions[0]++;
        var event = AnimationEvent.InTimeEvent.create(0.1F, callback, AnimationEvent.Side.SERVER);
        event.execute(patch, AnimsRuine.RUINE_PLUNDER, 0, 0.2F);
        helper.assertTrue(executions[0] == 0, "Player-only Ruine callback was not skipped");
        event.execute(patch, AnimsRuine.RUINE_AUTO_1, 0, 0.2F);
        helper.assertTrue(executions[0] == 1, "Safe WOM callbacks must remain enabled");
        event.executeWithNewParams(patch, AnimsRuine.RUINE_PLUNDER, 0, 0.2F, event.getParameters());
        helper.assertTrue(executions[0] == 1, "Parameterized Ruine callback was not skipped");
        event.executeWithNewParams(patch, AnimsRuine.RUINE_AUTO_1, 0, 0.2F, event.getParameters());
        helper.assertTrue(executions[0] == 2, "Safe parameterized callbacks must remain enabled");
        helper.succeed();
    }

    @GameTest(template = "skill_test", timeoutTicks = 40)
    public static void completeRegistryAndBooks(GameTestHelper helper) {
        helper.assertTrue(WomSkillCatalog.ALL.size() == 62, "WOM registry must contain 62 entries");
        for (var entry : WomSkillCatalog.ALL) {
            var skill = MaidSkillManager.getSkillFor(entry.location());
            helper.assertTrue(skill != null, "Skill was not built: " + entry.id());
            ItemStack book = new ItemStack(Items.BOOK);
            MaidSkillBookItem.setContainingSkill(skill, book);
            helper.assertTrue(MaidSkillBookItem.getContainSkill(book) == skill, "Book does not resolve: " + entry.id());
        }
        helper.succeed();
    }

    @GameTest(template = "skill_test", timeoutTicks = 40)
    public static void independentMaidStateAndWeaponCharge(GameTestHelper helper) {
        var first = helper.spawnWithNoFreeWill(InitEntities.MAID.get(), 1, 2, 1);
        var second = helper.spawnWithNoFreeWill(InitEntities.MAID.get(), 3, 2, 1);
        var target = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, 2, 2, 3);
        MaidPatch<?> firstPatch = EpicFightCapabilities.getEntityPatch(first, MaidPatch.class);
        MaidPatch<?> secondPatch = EpicFightCapabilities.getEntityPatch(second, MaidPatch.class);
        helper.assertTrue(firstPatch != null && secondPatch != null, "Maid capabilities did not attach");
        for (var entry : WomSkillCatalog.ALL) {
            var skill = MaidSkillManager.getSkillFor(entry.location());
            skill.onInit(new MaidSkillInitEvent(firstPatch));
            skill.onInit(new MaidSkillInitEvent(secondPatch));
            if (skill instanceof WomMaidSkill) {
                firstPatch.setData(skill, WomMaidSkill.COOLDOWN, 37);
                helper.assertTrue(secondPatch.getDataValue(skill, WomMaidSkill.COOLDOWN) == 0,
                        "Cooldown leaked across maids: " + entry.id());
                // Learning another book re-fires init; it must preserve existing cooldowns.
                skill.onInit(new MaidSkillInitEvent(firstPatch));
                helper.assertTrue(firstPatch.getDataValue(skill, WomMaidSkill.COOLDOWN) == 37,
                        "Reinitialization reset an active skill: " + entry.id());
            }
            if (skill instanceof WomWeaponSkill && !entry.id().equals("charybdis")
                    && !entry.id().equals("regierung") && !entry.id().equals("rechargement")) {
                skill.onHurtTargetPost(new MaidHurtTargetEvent.Post(firstPatch, target,
                        first.damageSources().mobAttack(first), entry.consumption() * 2.5F));
                helper.assertTrue(firstPatch.getDataValue(skill, WomWeaponSkill.CHARGES) == Math.min(2, entry.maxStacks()),
                        "Incorrect weapon charge conversion: " + entry.id());
                helper.assertTrue(secondPatch.getDataValue(skill, WomWeaponSkill.CHARGES) == 0,
                        "Weapon charge leaked across maids: " + entry.id());
            }
        }
        helper.succeed();
    }
}
