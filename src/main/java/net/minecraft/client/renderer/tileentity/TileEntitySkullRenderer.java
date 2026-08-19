package net.minecraft.client.renderer.tileentity;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;
import java.util.Map;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelHumanoidHead;
import net.minecraft.client.model.ModelSkeletonHead;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

public class TileEntitySkullRenderer extends TileEntitySpecialRenderer<TileEntitySkull>
{
    private static final ResourceLocation SKELETON_TEXTURES = new ResourceLocation("textures/entity/skeleton/skeleton.png");
    private static final ResourceLocation WITHER_SKELETON_TEXTURES = new ResourceLocation("textures/entity/skeleton/wither_skeleton.png");
    private static final ResourceLocation ZOMBIE_TEXTURES = new ResourceLocation("textures/entity/zombie/zombie.png");
    private static final ResourceLocation CREEPER_TEXTURES = new ResourceLocation("textures/entity/creeper/creeper.png");
    public static TileEntitySkullRenderer instance;
    private final ModelSkeletonHead skeletonHead = new ModelSkeletonHead(0, 0, 64, 32);
    private final ModelSkeletonHead humanoidHead = new ModelHumanoidHead();

    public void renderTileEntityAt(TileEntitySkull te, double x, double y, double z, float partialTicks, int destroyStage)
    {
        EnumFacing facing = EnumFacing.getFront(te.getBlockMetadata() & 7);
        this.renderSkull((float)x, (float)y, (float)z, facing, (float)(te.getSkullRotation() * 360) / 16.0F, te.getSkullType(), te.getPlayerProfile(), destroyStage);
    }

    public void setRendererDispatcher(TileEntityRendererDispatcher rendererDispatcherIn)
    {
        super.setRendererDispatcher(rendererDispatcherIn);
        instance = this;
    }

    public void renderSkull(float x, float y, float z, EnumFacing facing, float rotation, int skullType, GameProfile profile, int destroyStage)
    {
        ModelBase modelBase = this.skeletonHead;

        if (destroyStage >= 0)
        {
            this.bindTexture(DESTROY_STAGES[destroyStage]);
            GlStateManager.matrixMode(5890);
            GlStateManager.pushMatrix();
            GlStateManager.scale(4.0F, 2.0F, 1.0F);
            GlStateManager.translate(0.0625F, 0.0625F, 0.0625F);
            GlStateManager.matrixMode(5888);
        }
        else
        {
            switch (skullType)
            {
                case 0:
                default:
                    this.bindTexture(SKELETON_TEXTURES);
                    break;

                case 1:
                    this.bindTexture(WITHER_SKELETON_TEXTURES);
                    break;

                case 2:
                    this.bindTexture(ZOMBIE_TEXTURES);
                    modelBase = this.humanoidHead;
                    break;

                case 3:
                    modelBase = this.humanoidHead;
                    ResourceLocation resourceLocation = DefaultPlayerSkin.getDefaultSkinLegacy();

                    if (profile != null)
                    {
                        Minecraft minecraft = Minecraft.getMinecraft();
                        Map<Type, MinecraftProfileTexture> skinMap = minecraft.getSkinManager().loadSkinFromCache(profile);

                        if (skinMap.containsKey(Type.SKIN))
                        {
                            resourceLocation = minecraft.getSkinManager().loadSkin(skinMap.get(Type.SKIN), Type.SKIN);
                        }
                        else
                        {
                            UUID profileId = EntityPlayer.getUUID(profile);
                            resourceLocation = DefaultPlayerSkin.getDefaultSkin(profileId);
                        }
                    }

                    this.bindTexture(resourceLocation);
                    break;

                case 4:
                    this.bindTexture(CREEPER_TEXTURES);
            }
        }

        GlStateManager.pushMatrix();
        GlStateManager.disableCull();

        if (facing != EnumFacing.UP)
        {
            switch (facing)
            {
                case NORTH:
                    GlStateManager.translate(x + 0.5F, y + 0.25F, z + 0.74F);
                    break;

                case SOUTH:
                    GlStateManager.translate(x + 0.5F, y + 0.25F, z + 0.26F);
                    rotation = 180.0F;
                    break;

                case WEST:
                    GlStateManager.translate(x + 0.74F, y + 0.25F, z + 0.5F);
                    rotation = 270.0F;
                    break;

                case EAST:
                default:
                    GlStateManager.translate(x + 0.26F, y + 0.25F, z + 0.5F);
                    rotation = 90.0F;
            }
        }
        else
        {
            GlStateManager.translate(x + 0.5F, y, z + 0.5F);
        }

        float modelScale = 0.0625F;
        GlStateManager.enableRescaleNormal();
        GlStateManager.scale(-1.0F, -1.0F, 1.0F);
        GlStateManager.enableAlpha();
        modelBase.render((Entity)null, 0.0F, 0.0F, 0.0F, rotation, 0.0F, modelScale);
        GlStateManager.popMatrix();

        if (destroyStage >= 0)
        {
            GlStateManager.matrixMode(5890);
            GlStateManager.popMatrix();
            GlStateManager.matrixMode(5888);
        }
    }
}
