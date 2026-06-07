package org.dengzi.eftlm_wom.EF.Skills.guard;

import yesman.epicfight.api.animation.AnimationManager.AnimationAccessor;
import yesman.epicfight.api.animation.types.StaticAnimation;

import java.util.List;

public class MaidGuardData {
    public final AnimationAccessor<? extends StaticAnimation> stance;
    public final List<? extends AnimationAccessor<?>> hits;
    public final AnimationAccessor<? extends StaticAnimation> parry;
    public final AnimationAccessor<? extends StaticAnimation> counter;
    public final float cost;
    public final float parryCost;
    public final float counterCost;
    public final float counterChance;
    public final boolean canBlockProjectile;

    public MaidGuardData(AnimationAccessor<? extends StaticAnimation> stance,
                         List<? extends AnimationAccessor<?>> hits,
                         AnimationAccessor<? extends StaticAnimation> parry,
                         AnimationAccessor<? extends StaticAnimation> counter,
                         float cost, float parryCost, float counterCost, float counterChance,
                         boolean canBlockProjectile) {
        this.stance = stance;
        this.hits = hits;
        this.parry = parry;
        this.counter = counter;
        this.cost = cost;
        this.parryCost = parryCost;
        this.counterCost = counterCost;
        this.counterChance = counterChance;
        this.canBlockProjectile = canBlockProjectile;
    }

    public AnimationAccessor<?> getHit(int index) {
        if (hits == null || hits.isEmpty()) return stance;
        return hits.get(Math.abs(index) % hits.size());
    }

    public static Builder builder(AnimationAccessor<? extends StaticAnimation> stance) {
        return new Builder(stance);
    }

    public static class Builder {
        private final AnimationAccessor<? extends StaticAnimation> stance;
        private List<? extends AnimationAccessor<?>> hits;
        private AnimationAccessor<? extends StaticAnimation> parry;
        private AnimationAccessor<? extends StaticAnimation> counter;
        private float cost = 2.0f;
        private float parryCost = 5.0f;
        private float counterCost = 3.0f;
        private float counterChance = 0.3f;
        private boolean canBlockProjectile = false;

        Builder(AnimationAccessor<? extends StaticAnimation> stance) { this.stance = stance; }

        public Builder hits(List<? extends AnimationAccessor<?>> hits) { this.hits = hits; return this; }
        public Builder parry(AnimationAccessor<? extends StaticAnimation> parry) { this.parry = parry; return this; }
        public Builder counter(AnimationAccessor<? extends StaticAnimation> counter) { this.counter = counter; return this; }
        public Builder cost(float cost) { this.cost = cost; return this; }
        public Builder parryCost(float parryCost) { this.parryCost = parryCost; return this; }
        public Builder counterCost(float counterCost) { this.counterCost = counterCost; return this; }
        public Builder counterChance(float counterChance) { this.counterChance = counterChance; return this; }
        public Builder canBlockProjectile(boolean v) { this.canBlockProjectile = v; return this; }
        public MaidGuardData build() { return new MaidGuardData(stance, hits, parry, counter, cost, parryCost, counterCost, counterChance, canBlockProjectile); }
    }
}
