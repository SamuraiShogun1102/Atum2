package com.teammetallurgy.atum.world.biome;

import com.teammetallurgy.atum.init.AtumFeatures;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.IFeatureConfig;

public class SandPlainsBiome extends AtumBiome {

    public SandPlainsBiome() {
        super(new Builder("sand_plains", 30));
        super.addDefaultSpawns(this);
        super.addCamelSpawning(this);
        this.addStructure(AtumFeatures.GIRAFI_TOMB.withConfiguration(IFeatureConfig.NO_FEATURE_CONFIG));
        this.addFeature(GenerationStage.Decoration.SURFACE_STRUCTURES, AtumFeatures.GIRAFI_TOMB.withConfiguration(IFeatureConfig.NO_FEATURE_CONFIG));
        AtumFeatures.Default.addDeadwoodTrees(this, 0, 0.025F, 1);
        AtumFeatures.Default.addCarvers(this);
        AtumFeatures.Default.addSprings(this);
        AtumFeatures.Default.addMaterialPockets(this);
        AtumFeatures.Default.addStoneVariants(this);
        AtumFeatures.Default.addOres(this);
        AtumFeatures.Default.addInfestedLimestone(this);
        AtumFeatures.Default.addShrubs(this);
        AtumFeatures.Default.addFossils(this);
        AtumFeatures.Default.addDungeon(this);
        AtumFeatures.Default.addTomb(this);
        AtumFeatures.Default.addPyramid(this);
        AtumFeatures.Default.addRuins(this);
        AtumFeatures.Default.addMineshaft(this, false);
    }
}