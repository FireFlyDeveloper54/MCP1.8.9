package net.minecraft.client.particle;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Callable;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.src.Config;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.ReportedException;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class EffectRenderer
{
    private static final ResourceLocation particleTextures = new ResourceLocation("textures/particle/particles.png");
    protected World worldObj;
    private List<EntityFX>[][] fxLayers = new List[4][];
    private List<EntityParticleEmitter> particleEmitters = Lists.<EntityParticleEmitter>newArrayList();
    private TextureManager renderer;
    private Random rand = new Random();
    private Map<Integer, IParticleFactory> particleTypes = Maps.<Integer, IParticleFactory>newHashMap();
    private final Set<EntityFX> deadParticles = new HashSet<EntityFX>();
    private final Set<EntityParticleEmitter> deadEmitters = new HashSet<EntityParticleEmitter>();

    public EffectRenderer(World worldIn, TextureManager rendererIn)
    {
        this.worldObj = worldIn;
        this.renderer = rendererIn;

        for (int fxLayer = 0; fxLayer < 4; ++fxLayer)
        {
            this.fxLayers[fxLayer] = new List[2];

            for (int alphaLayer = 0; alphaLayer < 2; ++alphaLayer)
            {
                this.fxLayers[fxLayer][alphaLayer] = Lists.newArrayList();
            }
        }

        this.registerVanillaParticles();
    }

    private void registerVanillaParticles()
    {
        this.registerParticle(EnumParticleTypes.EXPLOSION_NORMAL.getParticleID(), new EntityExplodeFX.Factory());
        this.registerParticle(EnumParticleTypes.WATER_BUBBLE.getParticleID(), new EntityBubbleFX.Factory());
        this.registerParticle(EnumParticleTypes.WATER_SPLASH.getParticleID(), new EntitySplashFX.Factory());
        this.registerParticle(EnumParticleTypes.WATER_WAKE.getParticleID(), new EntityFishWakeFX.Factory());
        this.registerParticle(EnumParticleTypes.WATER_DROP.getParticleID(), new EntityRainFX.Factory());
        this.registerParticle(EnumParticleTypes.SUSPENDED.getParticleID(), new EntitySuspendFX.Factory());
        this.registerParticle(EnumParticleTypes.SUSPENDED_DEPTH.getParticleID(), new EntityAuraFX.Factory());
        this.registerParticle(EnumParticleTypes.CRIT.getParticleID(), new EntityCrit2FX.Factory());
        this.registerParticle(EnumParticleTypes.CRIT_MAGIC.getParticleID(), new EntityCrit2FX.MagicFactory());
        this.registerParticle(EnumParticleTypes.SMOKE_NORMAL.getParticleID(), new EntitySmokeFX.Factory());
        this.registerParticle(EnumParticleTypes.SMOKE_LARGE.getParticleID(), new EntityCritFX.Factory());
        this.registerParticle(EnumParticleTypes.SPELL.getParticleID(), new EntitySpellParticleFX.Factory());
        this.registerParticle(EnumParticleTypes.SPELL_INSTANT.getParticleID(), new EntitySpellParticleFX.InstantFactory());
        this.registerParticle(EnumParticleTypes.SPELL_MOB.getParticleID(), new EntitySpellParticleFX.MobFactory());
        this.registerParticle(EnumParticleTypes.SPELL_MOB_AMBIENT.getParticleID(), new EntitySpellParticleFX.AmbientMobFactory());
        this.registerParticle(EnumParticleTypes.SPELL_WITCH.getParticleID(), new EntitySpellParticleFX.WitchFactory());
        this.registerParticle(EnumParticleTypes.DRIP_WATER.getParticleID(), new EntityDropParticleFX.WaterFactory());
        this.registerParticle(EnumParticleTypes.DRIP_LAVA.getParticleID(), new EntityDropParticleFX.LavaFactory());
        this.registerParticle(EnumParticleTypes.VILLAGER_ANGRY.getParticleID(), new EntityHeartFX.AngryVillagerFactory());
        this.registerParticle(EnumParticleTypes.VILLAGER_HAPPY.getParticleID(), new EntityAuraFX.HappyVillagerFactory());
        this.registerParticle(EnumParticleTypes.TOWN_AURA.getParticleID(), new EntityAuraFX.Factory());
        this.registerParticle(EnumParticleTypes.NOTE.getParticleID(), new EntityNoteFX.Factory());
        this.registerParticle(EnumParticleTypes.PORTAL.getParticleID(), new EntityPortalFX.Factory());
        this.registerParticle(EnumParticleTypes.ENCHANTMENT_TABLE.getParticleID(), new EntityEnchantmentTableParticleFX.EnchantmentTable());
        this.registerParticle(EnumParticleTypes.FLAME.getParticleID(), new EntityFlameFX.Factory());
        this.registerParticle(EnumParticleTypes.LAVA.getParticleID(), new EntityLavaFX.Factory());
        this.registerParticle(EnumParticleTypes.FOOTSTEP.getParticleID(), new EntityFootStepFX.Factory());
        this.registerParticle(EnumParticleTypes.CLOUD.getParticleID(), new EntityCloudFX.Factory());
        this.registerParticle(EnumParticleTypes.REDSTONE.getParticleID(), new EntityReddustFX.Factory());
        this.registerParticle(EnumParticleTypes.SNOWBALL.getParticleID(), new EntityBreakingFX.SnowballFactory());
        this.registerParticle(EnumParticleTypes.SNOW_SHOVEL.getParticleID(), new EntitySnowShovelFX.Factory());
        this.registerParticle(EnumParticleTypes.SLIME.getParticleID(), new EntityBreakingFX.SlimeFactory());
        this.registerParticle(EnumParticleTypes.HEART.getParticleID(), new EntityHeartFX.Factory());
        this.registerParticle(EnumParticleTypes.BARRIER.getParticleID(), new Barrier.Factory());
        this.registerParticle(EnumParticleTypes.ITEM_CRACK.getParticleID(), new EntityBreakingFX.Factory());
        this.registerParticle(EnumParticleTypes.BLOCK_CRACK.getParticleID(), new EntityDiggingFX.Factory());
        this.registerParticle(EnumParticleTypes.BLOCK_DUST.getParticleID(), new EntityBlockDustFX.Factory());
        this.registerParticle(EnumParticleTypes.EXPLOSION_HUGE.getParticleID(), new EntityHugeExplodeFX.Factory());
        this.registerParticle(EnumParticleTypes.EXPLOSION_LARGE.getParticleID(), new EntityLargeExplodeFX.Factory());
        this.registerParticle(EnumParticleTypes.FIREWORKS_SPARK.getParticleID(), new EntityFirework.Factory());
        this.registerParticle(EnumParticleTypes.MOB_APPEARANCE.getParticleID(), new MobAppearance.Factory());
    }

    public void registerParticle(int id, IParticleFactory particleFactory)
    {
        this.particleTypes.put(Integer.valueOf(id), particleFactory);
    }

    public void emitParticleAtEntity(Entity entityIn, EnumParticleTypes particleTypes)
    {
        this.particleEmitters.add(new EntityParticleEmitter(this.worldObj, entityIn, particleTypes));
    }

    public EntityFX spawnEffectParticle(int particleId, double xCoord, double yCoord, double zCoord, double xSpeed, double ySpeed, double zSpeed, int... parameters)
    {
        IParticleFactory particleFactory = this.particleTypes.get(Integer.valueOf(particleId));

        if (particleFactory != null)
        {
            EntityFX entityFX = particleFactory.getEntityFX(particleId, this.worldObj, xCoord, yCoord, zCoord, xSpeed, ySpeed, zSpeed, parameters);

            if (entityFX != null)
            {
                this.addEffect(entityFX);
                return entityFX;
            }
        }

        return null;
    }

    public void addEffect(EntityFX effect)
    {
        if (effect != null)
        {
            if (!(effect instanceof EntityFirework.SparkFX) || Config.isFireworkParticles())
            {
                int fxLayer = effect.getFXLayer();
                int alphaLayer = effect.getAlpha() != 1.0F ? 0 : 1;
                List<EntityFX> layer = this.fxLayers[fxLayer][alphaLayer];

                int particlesLimit = 4000;
                if (Minecraft.getMinecraft() != null && Minecraft.getMinecraft().gameSettings != null)
                {
                    particlesLimit = Minecraft.getMinecraft().gameSettings.ofParticlesLimit;
                }

                if (layer.size() >= particlesLimit)
                {
                    layer.subList(0, layer.size() - particlesLimit + 1).clear();
                }

                layer.add(effect);
            }
        }
    }

    public void updateEffects()
    {
        for (int fxLayer = 0; fxLayer < 4; ++fxLayer)
        {
            this.updateEffectLayer(fxLayer);
        }

        this.deadEmitters.clear();

        for (EntityParticleEmitter entityParticleEmitter : this.particleEmitters)
        {
            entityParticleEmitter.onUpdate();

            if (entityParticleEmitter.isDead)
            {
                this.deadEmitters.add(entityParticleEmitter);
            }
        }

        if (!this.deadEmitters.isEmpty())
        {
            this.particleEmitters.removeAll(this.deadEmitters);
            this.deadEmitters.clear();
        }
    }

    private void updateEffectLayer(int layer)
    {
        for (int alphaLayer = 0; alphaLayer < 2; ++alphaLayer)
        {
            this.updateEffectAlphaLayer(this.fxLayers[layer][alphaLayer]);
        }
    }

    private void updateEffectAlphaLayer(List<EntityFX> entitiesFX)
    {
        if (entitiesFX.isEmpty())
        {
            return;
        }

        this.deadParticles.clear();
        long updateDeadline = System.currentTimeMillis() + 20L;
        int size = entitiesFX.size();
        int remainingParticles = size;

        for (int particleIndex = 0; particleIndex < size; ++particleIndex)
        {
            EntityFX particle = entitiesFX.get(particleIndex);
            this.tickParticle(particle);

            if (particle.isDead)
            {
                this.deadParticles.add(particle);
            }

            --remainingParticles;

            if ((particleIndex & 31) == 31 && System.currentTimeMillis() > updateDeadline)
            {
                break;
            }
        }

        if (remainingParticles > 0)
        {
            int particlesToCull = remainingParticles;
            List<EntityFX> list = entitiesFX;
            for (int i = size - remainingParticles; i < list.size() && particlesToCull > 0; ++i)
            {
                EntityFX culledParticle = list.get(i);
                culledParticle.setDead();
                list.remove(i);
                --i;
                --particlesToCull;
            }
        }

        if (!this.deadParticles.isEmpty())
        {
            entitiesFX.removeAll(this.deadParticles);
            this.deadParticles.clear();
        }
    }

    private void tickParticle(final EntityFX particle)
    {
        try
        {
            particle.onUpdate();
        }
        catch (Throwable throwable)
        {
            CrashReport crashReport = CrashReport.makeCrashReport(throwable, "Ticking Particle");
            CrashReportCategory crashReportCategory = crashReport.makeCategory("Particle being ticked");
            final int particleLayer = particle.getFXLayer();
            crashReportCategory.addCrashSectionCallable("Particle", new Callable<String>()
            {
                public String call() throws Exception
                {
                    return particle.toString();
                }
            });
            crashReportCategory.addCrashSectionCallable("Particle Type", new Callable<String>()
            {
                public String call() throws Exception
                {
                    return particleLayer == 0 ? "MISC_TEXTURE" : (particleLayer == 1 ? "TERRAIN_TEXTURE" : (particleLayer == 3 ? "ENTITY_PARTICLE_TEXTURE" : "Unknown - " + particleLayer));
                }
            });
            throw new ReportedException(crashReport);
        }
    }

    public void renderParticles(Entity entityIn, float partialTicks)
    {
        float rotationX = ActiveRenderInfo.getRotationX();
        float rotationZ = ActiveRenderInfo.getRotationZ();
        float rotationYZ = ActiveRenderInfo.getRotationYZ();
        float rotationXY = ActiveRenderInfo.getRotationXY();
        float rotationXZ = ActiveRenderInfo.getRotationXZ();
        EntityFX.interpPosX = entityIn.lastTickPosX + (entityIn.posX - entityIn.lastTickPosX) * (double)partialTicks;
        EntityFX.interpPosY = entityIn.lastTickPosY + (entityIn.posY - entityIn.lastTickPosY) * (double)partialTicks;
        EntityFX.interpPosZ = entityIn.lastTickPosZ + (entityIn.posZ - entityIn.lastTickPosZ) * (double)partialTicks;
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(770, 771);
        GlStateManager.alphaFunc(516, 0.003921569F);
        Block block = ActiveRenderInfo.getBlockAtEntityViewpoint(this.worldObj, entityIn, partialTicks);
        boolean isViewInWater = block.getMaterial() == Material.water;

        for (int fxLayer = 0; fxLayer < 3; ++fxLayer)
        {
            for (int alphaLayer = 0; alphaLayer < 2; ++alphaLayer)
            {
                List<EntityFX> particles = this.fxLayers[fxLayer][alphaLayer];
                final int renderLayer = fxLayer;

                if (!particles.isEmpty())
                {
                    switch (alphaLayer)
                    {
                        case 0:
                            GlStateManager.depthMask(false);
                            break;

                        case 1:
                            GlStateManager.depthMask(true);
                    }

                    switch (fxLayer)
                    {
                        case 0:
                        default:
                            this.renderer.bindTexture(particleTextures);
                            break;

                        case 1:
                            this.renderer.bindTexture(TextureMap.locationBlocksTexture);
                    }

                    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                    Tessellator tessellator = Tessellator.getInstance();
                    WorldRenderer worldRenderer = tessellator.getWorldRenderer();
                    worldRenderer.begin(7, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);

                    for (int particleIndex = 0; particleIndex < particles.size(); ++particleIndex)
                    {
                        final EntityFX particle = particles.get(particleIndex);

                        try
                        {
                            ParticleCulling.updateCullState(particle);

                            if (particle.isInView() && (isViewInWater || !(particle instanceof EntitySuspendFX)))
                            {
                                particle.renderParticle(worldRenderer, entityIn, partialTicks, rotationX, rotationXZ, rotationZ, rotationYZ, rotationXY);
                            }
                        }
                        catch (Throwable throwable)
                        {
                            CrashReport crashReport = CrashReport.makeCrashReport(throwable, "Rendering Particle");
                            CrashReportCategory crashReportCategory = crashReport.makeCategory("Particle being rendered");
                            crashReportCategory.addCrashSectionCallable("Particle", new Callable<String>()
                            {
                                public String call() throws Exception
                                {
                                    return particle.toString();
                                }
                            });
                            crashReportCategory.addCrashSectionCallable("Particle Type", new Callable<String>()
                            {
                                public String call() throws Exception
                                {
                                    return renderLayer == 0 ? "MISC_TEXTURE" : (renderLayer == 1 ? "TERRAIN_TEXTURE" : (renderLayer == 3 ? "ENTITY_PARTICLE_TEXTURE" : "Unknown - " + renderLayer));
                                }
                            });
                            throw new ReportedException(crashReport);
                        }
                    }

                    tessellator.draw();
                }
            }
        }

        GlStateManager.depthMask(true);
        GlStateManager.disableBlend();
        GlStateManager.alphaFunc(516, 0.1F);
    }

    public void renderLitParticles(Entity entityIn, float partialTick)
    {
        float radiansPerDegree = 0.017453292F;
        float rotationX = MathHelper.cos(entityIn.rotationYaw * radiansPerDegree);
        float rotationYZ = MathHelper.sin(entityIn.rotationYaw * radiansPerDegree);
        float rotationXY = -rotationYZ * MathHelper.sin(entityIn.rotationPitch * radiansPerDegree);
        float rotationXZ = rotationX * MathHelper.sin(entityIn.rotationPitch * radiansPerDegree);
        float rotationZ = MathHelper.cos(entityIn.rotationPitch * radiansPerDegree);

        for (int alphaLayer = 0; alphaLayer < 2; ++alphaLayer)
        {
            List<EntityFX> particles = this.fxLayers[3][alphaLayer];

            if (!particles.isEmpty())
            {
                Tessellator tessellator = Tessellator.getInstance();
                WorldRenderer worldRenderer = tessellator.getWorldRenderer();

                for (int particleIndex = 0; particleIndex < particles.size(); ++particleIndex)
                {
                    EntityFX entityFX = particles.get(particleIndex);
                    ParticleCulling.updateCullState(entityFX);

                    if (entityFX.isInView())
                    {
                        entityFX.renderParticle(worldRenderer, entityIn, partialTick, rotationX, rotationZ, rotationYZ, rotationXY, rotationXZ);
                    }
                }
            }
        }
    }

    public void clearEffects(World worldIn)
    {
        this.worldObj = worldIn;

        for (int fxLayer = 0; fxLayer < 4; ++fxLayer)
        {
            for (int alphaLayer = 0; alphaLayer < 2; ++alphaLayer)
            {
                this.fxLayers[fxLayer][alphaLayer].clear();
            }
        }

        this.particleEmitters.clear();
    }

    public void addBlockDestroyEffects(BlockPos pos, IBlockState state)
    {
        boolean shouldCreateParticles;

        
        shouldCreateParticles = state.getBlock().getMaterial() != Material.air;
    

        if (shouldCreateParticles)
        {
            state = state.getBlock().getActualState(state, this.worldObj, pos);
            int particleGridSize = 4;

            for (int xSlice = 0; xSlice < particleGridSize; ++xSlice)
            {
                for (int ySlice = 0; ySlice < particleGridSize; ++ySlice)
                {
                    for (int zSlice = 0; zSlice < particleGridSize; ++zSlice)
                    {
                        double particleX = (double)pos.getX() + ((double)xSlice + 0.5D) / (double)particleGridSize;
                        double particleY = (double)pos.getY() + ((double)ySlice + 0.5D) / (double)particleGridSize;
                        double particleZ = (double)pos.getZ() + ((double)zSlice + 0.5D) / (double)particleGridSize;
                        this.addEffect((new EntityDiggingFX(this.worldObj, particleX, particleY, particleZ, particleX - (double)pos.getX() - 0.5D, particleY - (double)pos.getY() - 0.5D, particleZ - (double)pos.getZ() - 0.5D, state)).setBlockPos(pos));
                    }
                }
            }
        }
    }

    public void addBlockHitEffects(BlockPos pos, EnumFacing side)
    {
        IBlockState blockState = this.worldObj.getBlockState(pos);
        Block block = blockState.getBlock();

        if (block.getRenderType() != -1)
        {
            int blockX = pos.getX();
            int blockY = pos.getY();
            int blockZ = pos.getZ();
            float particleInset = 0.1F;
            double particleX = (double)blockX + this.rand.nextDouble() * (block.getBlockBoundsMaxX() - block.getBlockBoundsMinX() - (double)(particleInset * 2.0F)) + (double)particleInset + block.getBlockBoundsMinX();
            double particleY = (double)blockY + this.rand.nextDouble() * (block.getBlockBoundsMaxY() - block.getBlockBoundsMinY() - (double)(particleInset * 2.0F)) + (double)particleInset + block.getBlockBoundsMinY();
            double particleZ = (double)blockZ + this.rand.nextDouble() * (block.getBlockBoundsMaxZ() - block.getBlockBoundsMinZ() - (double)(particleInset * 2.0F)) + (double)particleInset + block.getBlockBoundsMinZ();

            if (side == EnumFacing.DOWN)
            {
                particleY = (double)blockY + block.getBlockBoundsMinY() - (double)particleInset;
            }

            if (side == EnumFacing.UP)
            {
                particleY = (double)blockY + block.getBlockBoundsMaxY() + (double)particleInset;
            }

            if (side == EnumFacing.NORTH)
            {
                particleZ = (double)blockZ + block.getBlockBoundsMinZ() - (double)particleInset;
            }

            if (side == EnumFacing.SOUTH)
            {
                particleZ = (double)blockZ + block.getBlockBoundsMaxZ() + (double)particleInset;
            }

            if (side == EnumFacing.WEST)
            {
                particleX = (double)blockX + block.getBlockBoundsMinX() - (double)particleInset;
            }

            if (side == EnumFacing.EAST)
            {
                particleX = (double)blockX + block.getBlockBoundsMaxX() + (double)particleInset;
            }

            this.addEffect((new EntityDiggingFX(this.worldObj, particleX, particleY, particleZ, 0.0D, 0.0D, 0.0D, blockState)).setBlockPos(pos).multiplyVelocity(0.2F).multipleParticleScaleBy(0.6F));
        }
    }

    public void moveToAlphaLayer(EntityFX effect)
    {
        this.moveToLayer(effect, 1, 0);
    }

    public void moveToNoAlphaLayer(EntityFX effect)
    {
        this.moveToLayer(effect, 0, 1);
    }

    private void moveToLayer(EntityFX effect, int layerFrom, int layerTo)
    {
        for (int fxLayer = 0; fxLayer < 4; ++fxLayer)
        {
            if (this.fxLayers[fxLayer][layerFrom].contains(effect))
            {
                this.fxLayers[fxLayer][layerFrom].remove(effect);
                this.fxLayers[fxLayer][layerTo].add(effect);
            }
        }
    }

    public String getStatistics()
    {
        int particleCount = 0;

        for (int fxLayer = 0; fxLayer < 4; ++fxLayer)
        {
            for (int alphaLayer = 0; alphaLayer < 2; ++alphaLayer)
            {
                particleCount += this.fxLayers[fxLayer][alphaLayer].size();
            }
        }

        return "" + particleCount;
    }

    public void addBlockHitEffects(BlockPos pos, MovingObjectPosition target)
    {
        IBlockState blockState = this.worldObj.getBlockState(pos);

        if (blockState != null)
        {
            if (blockState != null)
            {
                this.addBlockHitEffects(pos, target.sideHit);
            }
        }
    }
}
