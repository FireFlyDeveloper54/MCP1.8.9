package net.minecraft.client.renderer.entity;

import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.layers.LayerArrow;
import net.minecraft.client.renderer.entity.layers.LayerBipedArmor;
import net.minecraft.client.renderer.entity.layers.LayerCape;
import net.minecraft.client.renderer.entity.layers.LayerCustomHead;
import net.minecraft.client.renderer.entity.layers.LayerDeadmau5Head;
import net.minecraft.client.renderer.entity.layers.LayerHeldItem;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.ResourceLocation;

public class RenderPlayer extends RendererLivingEntity<AbstractClientPlayer>
{
    private boolean smallArms;

    public RenderPlayer(RenderManager renderManager)
    {
        this(renderManager, false);
    }

    public RenderPlayer(RenderManager renderManager, boolean useSmallArms)
    {
        super(renderManager, new ModelPlayer(0.0F, useSmallArms), 0.5F);
        this.smallArms = useSmallArms;
        this.addLayer(new LayerBipedArmor(this));
        this.addLayer(new LayerHeldItem(this));
        this.addLayer(new LayerArrow(this));
        this.addLayer(new LayerDeadmau5Head(this));
        this.addLayer(new LayerCape(this));
        this.addLayer(new LayerCustomHead(this.getMainModel().bipedHead));
    }

    public ModelPlayer getMainModel()
    {
        return (ModelPlayer)super.getMainModel();
    }

    public void doRender(AbstractClientPlayer entity, double x, double y, double z, float entityYaw, float partialTicks)
    {
        if (!entity.isUser() || this.renderManager.livingPlayer == entity)
        {
            double renderY = y;

            if (entity.isSneaking() && !(entity instanceof EntityPlayerSP))
            {
                renderY = y - 0.125D;
            }

            this.setModelVisibilities(entity);
            super.doRender(entity, x, renderY, z, entityYaw, partialTicks);
        }
    }

    private void setModelVisibilities(AbstractClientPlayer clientPlayer)
    {
        ModelPlayer modelPlayer = this.getMainModel();

        if (clientPlayer.isSpectator())
        {
            modelPlayer.setInvisible(false);
            modelPlayer.bipedHead.showModel = true;
            modelPlayer.bipedHeadwear.showModel = true;
        }
        else
        {
            ItemStack itemStack = clientPlayer.inventory.getCurrentItem();
            modelPlayer.setInvisible(true);
            modelPlayer.bipedHeadwear.showModel = clientPlayer.isWearing(EnumPlayerModelParts.HAT);
            modelPlayer.bipedBodyWear.showModel = clientPlayer.isWearing(EnumPlayerModelParts.JACKET);
            modelPlayer.bipedLeftLegwear.showModel = clientPlayer.isWearing(EnumPlayerModelParts.LEFT_PANTS_LEG);
            modelPlayer.bipedRightLegwear.showModel = clientPlayer.isWearing(EnumPlayerModelParts.RIGHT_PANTS_LEG);
            modelPlayer.bipedLeftArmwear.showModel = clientPlayer.isWearing(EnumPlayerModelParts.LEFT_SLEEVE);
            modelPlayer.bipedRightArmwear.showModel = clientPlayer.isWearing(EnumPlayerModelParts.RIGHT_SLEEVE);
            modelPlayer.heldItemLeft = 0;
            modelPlayer.aimedBow = false;
            modelPlayer.isSneak = clientPlayer.isSneaking();

            if (itemStack == null)
            {
                modelPlayer.heldItemRight = 0;
            }
            else
            {
                modelPlayer.heldItemRight = 1;

                if (clientPlayer.getItemInUseCount() > 0)
                {
                    EnumAction action = itemStack.getItemUseAction();

                    if (action == EnumAction.BLOCK)
                    {
                        modelPlayer.heldItemRight = 3;
                    }
                    else if (action == EnumAction.BOW)
                    {
                        modelPlayer.aimedBow = true;
                    }
                }
            }
        }
    }

    protected ResourceLocation getEntityTexture(AbstractClientPlayer entity)
    {
        return entity.getLocationSkin();
    }

    public void transformHeldFull3DItemLayer()
    {
        GlStateManager.translate(0.0F, 0.1875F, 0.0F);
    }

    protected void preRenderCallback(AbstractClientPlayer entitylivingbaseIn, float partialTickTime)
    {
        float playerScale = 0.9375F;
        GlStateManager.scale(playerScale, playerScale, playerScale);
    }

    protected void renderOffsetLivingLabel(AbstractClientPlayer entityIn, double x, double y, double z, String str, float scale, double distanceSq)
    {
        if (distanceSq < 100.0D)
        {
            Scoreboard scoreboard = entityIn.getWorldScoreboard();
            ScoreObjective scoreobjective = scoreboard.getObjectiveInDisplaySlot(2);

            if (scoreobjective != null)
            {
                Score score = scoreboard.getValueFromObjective(entityIn.getName(), scoreobjective);
                this.renderLivingLabel(entityIn, score.getScorePoints() + " " + scoreobjective.getDisplayName(), x, y, z, 64);
                y += (double)((float)this.getFontRendererFromRenderManager().FONT_HEIGHT * 1.15F * scale);
            }
        }

        super.renderOffsetLivingLabel(entityIn, x, y, z, str, scale, distanceSq);
    }

    public void renderRightArm(AbstractClientPlayer clientPlayer)
    {
        float fullColor = 1.0F;
        GlStateManager.color(fullColor, fullColor, fullColor);
        ModelPlayer modelPlayer = this.getMainModel();
        this.setModelVisibilities(clientPlayer);
        modelPlayer.swingProgress = 0.0F;
        modelPlayer.isSneak = false;
        modelPlayer.setRotationAngles(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F, clientPlayer);
        modelPlayer.renderRightArm();
    }

    public void renderLeftArm(AbstractClientPlayer clientPlayer)
    {
        float fullColor = 1.0F;
        GlStateManager.color(fullColor, fullColor, fullColor);
        ModelPlayer modelPlayer = this.getMainModel();
        this.setModelVisibilities(clientPlayer);
        modelPlayer.isSneak = false;
        modelPlayer.swingProgress = 0.0F;
        modelPlayer.setRotationAngles(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F, clientPlayer);
        modelPlayer.renderLeftArm();
    }

    protected void renderLivingAt(AbstractClientPlayer entityLivingBaseIn, double x, double y, double z)
    {
        if (entityLivingBaseIn.isEntityAlive() && entityLivingBaseIn.isPlayerSleeping())
        {
            super.renderLivingAt(entityLivingBaseIn, x + (double)entityLivingBaseIn.renderOffsetX, y + (double)entityLivingBaseIn.renderOffsetY, z + (double)entityLivingBaseIn.renderOffsetZ);
        }
        else
        {
            super.renderLivingAt(entityLivingBaseIn, x, y, z);
        }
    }

    protected void rotateCorpse(AbstractClientPlayer bat, float ageInTicks, float rotationYaw, float partialTicks)
    {
        if (bat.isEntityAlive() && bat.isPlayerSleeping())
        {
            GlStateManager.rotate(bat.getBedOrientationInDegrees(), 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(this.getDeathMaxRotation(bat), 0.0F, 0.0F, 1.0F);
            GlStateManager.rotate(270.0F, 0.0F, 1.0F, 0.0F);
        }
        else
        {
            super.rotateCorpse(bat, ageInTicks, rotationYaw, partialTicks);
        }
    }
}
