package net.minecraft.client.renderer;

import com.google.common.base.Predicate;
import com.google.gson.JsonSyntaxException;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBed;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiDownloadTerrain;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.MapItemRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.client.particle.ParticleCulling;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.client.renderer.culling.ClippingHelper;
import net.minecraft.client.renderer.culling.ClippingHelperImpl;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.shader.ShaderGroup;
import net.minecraft.client.shader.ShaderLinkHelper;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.BossStatus;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.src.Config;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MouseFilter;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.ReportedException;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.biome.BiomeGenBase;
import net.optifine.CustomColors;
import net.optifine.GlErrors;
import net.optifine.Lagometer;
import net.optifine.RandomEntities;
import net.optifine.gui.GuiChatOF;
import net.optifine.reflect.Reflector;
import net.optifine.reflect.ReflectorResolver;
import net.optifine.shaders.Shaders;
import net.optifine.shaders.ShadersRender;
import net.optifine.util.MemoryMonitor;
import net.optifine.util.TextureUtils;
import net.optifine.util.TimedEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GLContext;
import org.lwjgl.util.glu.Project;

public class EntityRenderer implements IResourceManagerReloadListener
{
    private static final Logger logger = LogManager.getLogger();
    private static final ResourceLocation locationRainPng = new ResourceLocation("textures/environment/rain.png");
    private static final ResourceLocation locationSnowPng = new ResourceLocation("textures/environment/snow.png");
    private static final Vec3 FOG_SUN_DIRECTION_NEGATIVE_X = new Vec3(-1.0D, 0.0D, 0.0D);
    private static final Vec3 FOG_SUN_DIRECTION_POSITIVE_X = new Vec3(1.0D, 0.0D, 0.0D);
    private static final Predicate<Entity> COLLIDABLE_NOT_SPECTATING = new Predicate<Entity>()
    {
        public boolean apply(Entity entity)
        {
            return EntitySelectors.NOT_SPECTATING.apply(entity) && entity.canBeCollidedWith();
        }
    };
    public static boolean anaglyphEnable;
    public static int anaglyphField;
    private Minecraft mc;
    private final IResourceManager resourceManager;
    private Random random = new Random();
    private final BlockPos.MutableBlockPos lightSamplePos = new BlockPos.MutableBlockPos();
    private final BlockPos.MutableBlockPos rainSamplePos = new BlockPos.MutableBlockPos();
    private final BlockPos.MutableBlockPos rainBlockPos = new BlockPos.MutableBlockPos();
    private final BlockPos.MutableBlockPos rainRenderPos = new BlockPos.MutableBlockPos();
    private float farPlaneDistance;
    public ItemRenderer itemRenderer;
    private final MapItemRenderer theMapItemRenderer;
    private int rendererUpdateCount;
    private Entity pointedEntity;
    private MouseFilter mouseFilterXAxis = new MouseFilter();
    private MouseFilter mouseFilterYAxis = new MouseFilter();
    private float thirdPersonDistance = 4.0F;
    private float thirdPersonDistanceTemp = 4.0F;
    private float smoothCamYaw;
    private float smoothCamPitch;
    private float smoothCamFilterX;
    private float smoothCamFilterY;
    private float smoothCamPartialTicks;
    private float fovModifierHand;
    private float fovModifierHandPrev;
    private float bossColorModifier;
    private float bossColorModifierPrev;
    private boolean cloudFog;
    private boolean renderHand = true;
    private boolean drawBlockOutline = true;
    private long prevFrameTime = Minecraft.getSystemTime();
    private long renderEndNanoTime;
    private final DynamicTexture lightmapTexture;
    private final int[] lightmapColors;
    private final ResourceLocation locationLightMap;
    private boolean lightmapUpdateNeeded;
    private float torchFlickerX;
    private float torchFlickerDX;
    private int rainSoundCounter;
    private float[] rainXCoords = new float[1024];
    private float[] rainYCoords = new float[1024];
    private FloatBuffer fogColorBuffer = GLAllocation.createDirectFloatBuffer(16);
    public float fogColorRed;
    public float fogColorGreen;
    public float fogColorBlue;
    private float fogColor2;
    private float fogColor1;
    private int debugViewDirection = 0;
    private boolean debugView = false;
    private double cameraZoom = 1.0D;
    private double cameraYaw;
    private double cameraPitch;
    private ShaderGroup theShaderGroup;
    private static final ResourceLocation[] shaderResourceLocations = new ResourceLocation[] {new ResourceLocation("shaders/post/notch.json"), new ResourceLocation("shaders/post/fxaa.json"), new ResourceLocation("shaders/post/art.json"), new ResourceLocation("shaders/post/bumpy.json"), new ResourceLocation("shaders/post/blobs2.json"), new ResourceLocation("shaders/post/pencil.json"), new ResourceLocation("shaders/post/color_convolve.json"), new ResourceLocation("shaders/post/deconverge.json"), new ResourceLocation("shaders/post/flip.json"), new ResourceLocation("shaders/post/invert.json"), new ResourceLocation("shaders/post/ntsc.json"), new ResourceLocation("shaders/post/outline.json"), new ResourceLocation("shaders/post/phosphor.json"), new ResourceLocation("shaders/post/scan_pincushion.json"), new ResourceLocation("shaders/post/sobel.json"), new ResourceLocation("shaders/post/bits.json"), new ResourceLocation("shaders/post/desaturate.json"), new ResourceLocation("shaders/post/green.json"), new ResourceLocation("shaders/post/blur.json"), new ResourceLocation("shaders/post/wobble.json"), new ResourceLocation("shaders/post/blobs.json"), new ResourceLocation("shaders/post/antialias.json"), new ResourceLocation("shaders/post/creeper.json"), new ResourceLocation("shaders/post/spider.json")};
    public static final int shaderCount = shaderResourceLocations.length;
    private int shaderIndex;
    private boolean useShader;
    public int frameCount;
    private boolean initialized = false;
    private World updatedWorld = null;
    private boolean showDebugInfo = false;
    public boolean fogStandard = false;
    private float clipDistance = 128.0F;
    private long lastServerTime = 0L;
    private int lastServerTicks = 0;
    private int serverWaitTime = 0;
    private int serverWaitTimeCurrent = 0;
    private float avgServerTimeDiff = 0.0F;
    private float avgServerTickDiff = 0.0F;
    private ShaderGroup[] fxaaShaders = new ShaderGroup[10];
    private boolean loadVisibleChunks = false;
    private Frustum renderFrustum;

    public EntityRenderer(Minecraft mcIn, IResourceManager resourceManagerIn)
    {
        this.shaderIndex = shaderCount;
        this.useShader = false;
        this.frameCount = 0;
        this.mc = mcIn;
        this.resourceManager = resourceManagerIn;
        this.itemRenderer = mcIn.getItemRenderer();
        this.theMapItemRenderer = new MapItemRenderer(mcIn.getTextureManager());
        this.lightmapTexture = new DynamicTexture(16, 16);
        this.locationLightMap = mcIn.getTextureManager().getDynamicTextureLocation("lightMap", this.lightmapTexture);
        this.lightmapColors = this.lightmapTexture.getTextureData();
        this.theShaderGroup = null;

        for (int i = 0; i < 32; ++i)
        {
            for (int j = 0; j < 32; ++j)
            {
                float f = (float)(j - 16);
                float floatValue2 = (float)(i - 16);
                float floatValue3 = MathHelper.sqrt_float(f * f + floatValue2 * floatValue2);
                this.rainXCoords[i << 5 | j] = -floatValue2 / floatValue3;
                this.rainYCoords[i << 5 | j] = f / floatValue3;
            }
        }
    }

    public boolean isShaderActive()
    {
        return OpenGlHelper.shadersSupported && this.theShaderGroup != null;
    }

    public void stopUseShader()
    {
        if (this.theShaderGroup != null)
        {
            this.theShaderGroup.deleteShaderGroup();
        }

        this.theShaderGroup = null;
        this.shaderIndex = shaderCount;
    }

    public void switchUseShader()
    {
        this.useShader = !this.useShader;
    }

    public void loadEntityShader(Entity entityIn)
    {
        if (OpenGlHelper.shadersSupported)
        {
            if (this.theShaderGroup != null)
            {
                this.theShaderGroup.deleteShaderGroup();
            }

            this.theShaderGroup = null;

            if (entityIn instanceof EntityCreeper)
            {
                this.loadShader(new ResourceLocation("shaders/post/creeper.json"));
            }
            else if (entityIn instanceof EntitySpider)
            {
                this.loadShader(new ResourceLocation("shaders/post/spider.json"));
            }
            else if (entityIn instanceof EntityEnderman)
            {
                this.loadShader(new ResourceLocation("shaders/post/invert.json"));
            }
        }
    }

    public void activateNextShader()
    {
        if (OpenGlHelper.shadersSupported && this.mc.getRenderViewEntity() instanceof EntityPlayer)
        {
            if (this.theShaderGroup != null)
            {
                this.theShaderGroup.deleteShaderGroup();
            }

            this.shaderIndex = (this.shaderIndex + 1) % (shaderResourceLocations.length + 1);

            if (this.shaderIndex != shaderCount)
            {
                this.loadShader(shaderResourceLocations[this.shaderIndex]);
            }
            else
            {
                this.theShaderGroup = null;
            }
        }
    }

    private void loadShader(ResourceLocation resourceLocationIn)
    {
        if (OpenGlHelper.isFramebufferEnabled())
        {
            try
            {
                this.theShaderGroup = new ShaderGroup(this.mc.getTextureManager(), this.resourceManager, this.mc.getFramebuffer(), resourceLocationIn);
                this.theShaderGroup.createBindFramebuffers(this.mc.displayWidth, this.mc.displayHeight);
                this.useShader = true;
            }
            catch (IOException iOException)
            {
                logger.warn((String)("Failed to load shader: " + resourceLocationIn), (Throwable)iOException);
                this.shaderIndex = shaderCount;
                this.useShader = false;
            }
            catch (JsonSyntaxException jsonSyntaxException)
            {
                logger.warn((String)("Failed to load shader: " + resourceLocationIn), (Throwable)jsonSyntaxException);
                this.shaderIndex = shaderCount;
                this.useShader = false;
            }
        }
    }

    public void onResourceManagerReload(IResourceManager resourceManager)
    {
        if (this.theShaderGroup != null)
        {
            this.theShaderGroup.deleteShaderGroup();
        }

        this.theShaderGroup = null;

        if (this.shaderIndex != shaderCount)
        {
            this.loadShader(shaderResourceLocations[this.shaderIndex]);
        }
        else
        {
            this.loadEntityShader(this.mc.getRenderViewEntity());
        }
    }

    public void updateRenderer()
    {
        if (OpenGlHelper.shadersSupported && ShaderLinkHelper.getStaticShaderLinkHelper() == null)
        {
            ShaderLinkHelper.setNewStaticShaderLinkHelper();
        }

        this.updateFovModifierHand();
        this.updateTorchFlicker();
        this.fogColor2 = this.fogColor1;
        this.thirdPersonDistanceTemp = this.thirdPersonDistance;

        if (this.mc.gameSettings.smoothCamera)
        {
            float f = this.mc.gameSettings.mouseSensitivity * 0.6F + 0.2F;
            float floatValue = f * f * f * 8.0F;
            this.smoothCamFilterX = this.mouseFilterXAxis.smooth(this.smoothCamYaw, 0.05F * floatValue);
            this.smoothCamFilterY = this.mouseFilterYAxis.smooth(this.smoothCamPitch, 0.05F * floatValue);
            this.smoothCamPartialTicks = 0.0F;
            this.smoothCamYaw = 0.0F;
            this.smoothCamPitch = 0.0F;
        }
        else
        {
            this.smoothCamFilterX = 0.0F;
            this.smoothCamFilterY = 0.0F;
            this.mouseFilterXAxis.reset();
            this.mouseFilterYAxis.reset();
        }

        if (this.mc.getRenderViewEntity() == null)
        {
            this.mc.setRenderViewEntity(this.mc.thePlayer);
        }

        Entity entity = this.mc.getRenderViewEntity();
        double doubleValue = entity.posX;
        double secondDoubleValue = entity.posY + (double)entity.getEyeHeight();
        double thirdDoubleValue = entity.posZ;
        this.lightSamplePos.set(MathHelper.floor_double(doubleValue), MathHelper.floor_double(secondDoubleValue), MathHelper.floor_double(thirdDoubleValue));
        float secondFloatValue = this.mc.theWorld.getLightBrightness(this.lightSamplePos);
        float thirdFloatValue = (float)this.mc.gameSettings.renderDistanceChunks / 16.0F;
        thirdFloatValue = MathHelper.clamp_float(thirdFloatValue, 0.0F, 1.0F);
        float fourthFloatValue = secondFloatValue * (1.0F - thirdFloatValue) + thirdFloatValue;
        this.fogColor1 += (fourthFloatValue - this.fogColor1) * 0.1F;
        ++this.rendererUpdateCount;
        this.itemRenderer.updateEquippedItem();
        this.addRainParticles();
        this.bossColorModifierPrev = this.bossColorModifier;

        if (BossStatus.hasColorModifier)
        {
            this.bossColorModifier += 0.05F;

            if (this.bossColorModifier > 1.0F)
            {
                this.bossColorModifier = 1.0F;
            }

            BossStatus.hasColorModifier = false;
        }
        else if (this.bossColorModifier > 0.0F)
        {
            this.bossColorModifier -= 0.0125F;
        }
    }

    public ShaderGroup getShaderGroup()
    {
        return this.theShaderGroup;
    }

    public void updateShaderGroupSize(int width, int height)
    {
        if (OpenGlHelper.shadersSupported)
        {
            if (this.theShaderGroup != null)
            {
                this.theShaderGroup.createBindFramebuffers(width, height);
            }

            this.mc.renderGlobal.createBindEntityOutlineFbs(width, height);
        }
    }

    public void getMouseOver(float partialTicks)
    {
        Entity entity = this.mc.getRenderViewEntity();

        if (entity != null && this.mc.theWorld != null)
        {
            this.mc.mcProfiler.startSection("pick");
            this.mc.pointedEntity = null;
            double twelfthDoubleValue = (double)this.mc.playerController.getBlockReachDistance();
            this.mc.objectMouseOver = entity.rayTrace(twelfthDoubleValue, partialTicks);
            double doubleValue = twelfthDoubleValue;
            Vec3 eyePosition = entity.getPositionEyes(partialTicks);
            boolean flag = false;
            int i = 3;

            if (this.mc.playerController.extendedReach())
            {
                twelfthDoubleValue = 6.0D;
                doubleValue = 6.0D;
            }
            else if (twelfthDoubleValue > 3.0D)
            {
                flag = true;
            }

            if (this.mc.objectMouseOver != null)
            {
                doubleValue = this.mc.objectMouseOver.hitVec.distanceTo(eyePosition);
            }

            Vec3 lookVector = entity.getLook(partialTicks);
            Vec3 secondLocalValue = eyePosition.addVector(lookVector.xCoord * twelfthDoubleValue, lookVector.yCoord * twelfthDoubleValue, lookVector.zCoord * twelfthDoubleValue);
            this.pointedEntity = null;
            Vec3 secondVec3 = null;
            float f = 1.0F;
            List<Entity> list = this.mc.theWorld.getEntitiesInAABBexcluding(entity, entity.getEntityBoundingBox().addCoord(lookVector.xCoord * twelfthDoubleValue, lookVector.yCoord * twelfthDoubleValue, lookVector.zCoord * twelfthDoubleValue).expand((double)f, (double)f, (double)f), COLLIDABLE_NOT_SPECTATING);
            double secondDoubleValue = doubleValue;

            for (int j = 0; j < list.size(); ++j)
            {
                Entity entity1 = list.get(j);
                float floatValue = entity1.getCollisionBorderSize();
                AxisAlignedBB axisalignedbb = entity1.getEntityBoundingBox().expand((double)floatValue, (double)floatValue, (double)floatValue);
                MovingObjectPosition movingobjectposition = axisalignedbb.calculateIntercept(eyePosition, secondLocalValue);

                if (axisalignedbb.isVecInside(eyePosition))
                {
                    if (secondDoubleValue >= 0.0D)
                    {
                        this.pointedEntity = entity1;
                        secondVec3 = movingobjectposition == null ? eyePosition : movingobjectposition.hitVec;
                        secondDoubleValue = 0.0D;
                    }
                }
                else if (movingobjectposition != null)
                {
                    double thirdDoubleValue = eyePosition.distanceTo(movingobjectposition.hitVec);

                    if (thirdDoubleValue < secondDoubleValue || secondDoubleValue == 0.0D)
                    {
                        if (entity1 == entity.ridingEntity)
                        {
                            if (secondDoubleValue == 0.0D)
                            {
                                this.pointedEntity = entity1;
                                secondVec3 = movingobjectposition.hitVec;
                            }
                        }
                        else
                        {
                            this.pointedEntity = entity1;
                            secondVec3 = movingobjectposition.hitVec;
                            secondDoubleValue = thirdDoubleValue;
                        }
                    }
                }
            }

            if (this.pointedEntity != null && flag && eyePosition.distanceTo(secondVec3) > 3.0D)
            {
                this.pointedEntity = null;
                this.mc.objectMouseOver = new MovingObjectPosition(MovingObjectPosition.MovingObjectType.MISS, secondVec3, (EnumFacing)null, new BlockPos(secondVec3));
            }

            if (this.pointedEntity != null && (secondDoubleValue < doubleValue || this.mc.objectMouseOver == null))
            {
                this.mc.objectMouseOver = new MovingObjectPosition(this.pointedEntity, secondVec3);

                if (this.pointedEntity instanceof EntityLivingBase || this.pointedEntity instanceof EntityItemFrame)
                {
                    this.mc.pointedEntity = this.pointedEntity;
                }
            }

            this.mc.mcProfiler.endSection();
        }
    }

    private void updateFovModifierHand()
    {
        float f = 1.0F;

        if (this.mc.getRenderViewEntity() instanceof AbstractClientPlayer)
        {
            AbstractClientPlayer abstractClientPlayer = (AbstractClientPlayer)this.mc.getRenderViewEntity();
            f = abstractClientPlayer.getFovModifier();
        }

        this.fovModifierHandPrev = this.fovModifierHand;
        this.fovModifierHand += (f - this.fovModifierHand) * 0.5F;

        if (this.fovModifierHand > 1.5F)
        {
            this.fovModifierHand = 1.5F;
        }

        if (this.fovModifierHand < 0.1F)
        {
            this.fovModifierHand = 0.1F;
        }
    }

    private float getFOVModifier(float partialTicks, boolean useFOVSetting)
    {
        if (this.debugView)
        {
            return 90.0F;
        }
        else
        {
            Entity entity = this.mc.getRenderViewEntity();
            float f = 70.0F;

            if (useFOVSetting)
            {
                f = this.mc.gameSettings.fovSetting;

                if (Config.isDynamicFov())
                {
                    f *= this.fovModifierHandPrev + (this.fovModifierHand - this.fovModifierHandPrev) * partialTicks;
                }
            }

            boolean flag = false;

            if (this.mc.currentScreen == null)
            {
                GameSettings gamesettings = this.mc.gameSettings;
                flag = GameSettings.isKeyDown(this.mc.gameSettings.ofKeyBindZoom);
            }

            if (flag)
            {
                if (!Config.zoomMode)
                {
                    Config.zoomMode = true;
                    Config.zoomSmoothCamera = this.mc.gameSettings.smoothCamera;
                    this.mc.gameSettings.smoothCamera = true;
                    this.mc.renderGlobal.displayListEntitiesDirty = true;
                }

                if (Config.zoomMode)
                {
                    f /= 4.0F;
                }
            }
            else if (Config.zoomMode)
            {
                Config.zoomMode = false;
                this.mc.gameSettings.smoothCamera = Config.zoomSmoothCamera;
                this.mouseFilterXAxis = new MouseFilter();
                this.mouseFilterYAxis = new MouseFilter();
                this.mc.renderGlobal.displayListEntitiesDirty = true;
            }

            if (entity instanceof EntityLivingBase && ((EntityLivingBase)entity).getHealth() <= 0.0F)
            {
                float floatValue = (float)((EntityLivingBase)entity).deathTime + partialTicks;
                f /= (1.0F - 500.0F / (floatValue + 500.0F)) * 2.0F + 1.0F;
            }

            Block block = ActiveRenderInfo.getBlockAtEntityViewpoint(this.mc.theWorld, entity, partialTicks);

            if (block.getMaterial() == Material.water)
            {
                f = f * 60.0F / 70.0F;
            }

            return f;
        }
    }

    private void hurtCameraEffect(float partialTicks)
    {
        if (this.mc.getRenderViewEntity() instanceof EntityLivingBase)
        {
            EntityLivingBase entityLivingBase = (EntityLivingBase)this.mc.getRenderViewEntity();
            float f = (float)entityLivingBase.hurtTime - partialTicks;

            if (entityLivingBase.getHealth() <= 0.0F)
            {
                float floatValue2 = (float)entityLivingBase.deathTime + partialTicks;
                GlStateManager.rotate(40.0F - 8000.0F / (floatValue2 + 200.0F), 0.0F, 0.0F, 1.0F);
            }

            if (f < 0.0F)
            {
                return;
            }

            f = f / (float)entityLivingBase.maxHurtTime;
            f = MathHelper.sin(f * f * f * f * (float)Math.PI);
            float floatValue3 = entityLivingBase.attackedAtYaw;
            GlStateManager.rotate(-floatValue3, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(-f * 14.0F, 0.0F, 0.0F, 1.0F);
            GlStateManager.rotate(floatValue3, 0.0F, 1.0F, 0.0F);
        }
    }

    private void setupViewBobbing(float partialTicks)
    {
        if (this.mc.getRenderViewEntity() instanceof EntityPlayer)
        {
            EntityPlayer entityplayer = (EntityPlayer)this.mc.getRenderViewEntity();
            float f = entityplayer.distanceWalkedModified - entityplayer.prevDistanceWalkedModified;
            float nineteenthFloatValue = -(entityplayer.distanceWalkedModified + f * partialTicks);
            float twentyFourthFloatValue = entityplayer.prevCameraYaw + (entityplayer.cameraYaw - entityplayer.prevCameraYaw) * partialTicks;
            float twentySeventhFloatValue = entityplayer.prevCameraPitch + (entityplayer.cameraPitch - entityplayer.prevCameraPitch) * partialTicks;
            GlStateManager.translate(MathHelper.sin(nineteenthFloatValue * (float)Math.PI) * twentyFourthFloatValue * 0.5F, -Math.abs(MathHelper.cos(nineteenthFloatValue * (float)Math.PI) * twentyFourthFloatValue), 0.0F);
            GlStateManager.rotate(MathHelper.sin(nineteenthFloatValue * (float)Math.PI) * twentyFourthFloatValue * 3.0F, 0.0F, 0.0F, 1.0F);
            GlStateManager.rotate(Math.abs(MathHelper.cos(nineteenthFloatValue * (float)Math.PI - 0.2F) * twentyFourthFloatValue) * 5.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(twentySeventhFloatValue, 1.0F, 0.0F, 0.0F);
        }
    }

    private void orientCamera(float partialTicks)
    {
        Entity entity = this.mc.getRenderViewEntity();
        float f = entity.getEyeHeight();
        double doubleValue = entity.prevPosX + (entity.posX - entity.prevPosX) * (double)partialTicks;
        double secondDoubleValue = entity.prevPosY + (entity.posY - entity.prevPosY) * (double)partialTicks + (double)f;
        double thirdDoubleValue = entity.prevPosZ + (entity.posZ - entity.prevPosZ) * (double)partialTicks;

        if (entity instanceof EntityLivingBase && ((EntityLivingBase)entity).isPlayerSleeping())
        {
            f = (float)((double)f + 1.0D);
            GlStateManager.translate(0.0F, 0.3F, 0.0F);

            if (!this.mc.gameSettings.debugCamEnable)
            {
                BlockPos blockpos = new BlockPos(entity);
                IBlockState iblockstate = this.mc.theWorld.getBlockState(blockpos);
                Block block = iblockstate.getBlock();

                if (block == Blocks.bed)
                {
                    int j = ((EnumFacing)iblockstate.getValue(BlockBed.FACING)).getHorizontalIndex();
                    GlStateManager.rotate((float)(j * 90), 0.0F, 1.0F, 0.0F);
                }

                GlStateManager.rotate(entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTicks + 180.0F, 0.0F, -1.0F, 0.0F);
                GlStateManager.rotate(entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks, -1.0F, 0.0F, 0.0F);
            }
        }
        else if (this.mc.gameSettings.thirdPersonView > 0)
        {
            double fourthDoubleValue = (double)(this.thirdPersonDistanceTemp + (this.thirdPersonDistance - this.thirdPersonDistanceTemp) * partialTicks);

            if (this.mc.gameSettings.debugCamEnable)
            {
                GlStateManager.translate(0.0F, 0.0F, (float)(-fourthDoubleValue));
            }
            else
            {
                float seventeenthFloatValue = entity.rotationYaw;
                float twentySecondFloatValue = entity.rotationPitch;

                if (this.mc.gameSettings.thirdPersonView == 2)
                {
                    twentySecondFloatValue += 180.0F;
                }

                double eighteenthDoubleValue = (double)(-MathHelper.sin(seventeenthFloatValue / 180.0F * (float)Math.PI) * MathHelper.cos(twentySecondFloatValue / 180.0F * (float)Math.PI)) * fourthDoubleValue;
                double fifthDoubleValue = (double)(MathHelper.cos(seventeenthFloatValue / 180.0F * (float)Math.PI) * MathHelper.cos(twentySecondFloatValue / 180.0F * (float)Math.PI)) * fourthDoubleValue;
                double sixthDoubleValue = (double)(-MathHelper.sin(twentySecondFloatValue / 180.0F * (float)Math.PI)) * fourthDoubleValue;

                for (int i = 0; i < 8; ++i)
                {
                    float floatValue = (float)((i & 1) * 2 - 1);
                    float secondFloatValue = (float)((i >> 1 & 1) * 2 - 1);
                    float thirdFloatValue = (float)((i >> 2 & 1) * 2 - 1);
                    floatValue = floatValue * 0.1F;
                    secondFloatValue = secondFloatValue * 0.1F;
                    thirdFloatValue = thirdFloatValue * 0.1F;
                    MovingObjectPosition movingobjectposition = this.mc.theWorld.rayTraceBlocks(new Vec3(doubleValue + (double)floatValue, secondDoubleValue + (double)secondFloatValue, thirdDoubleValue + (double)thirdFloatValue), new Vec3(doubleValue - eighteenthDoubleValue + (double)floatValue + (double)thirdFloatValue, secondDoubleValue - sixthDoubleValue + (double)secondFloatValue, thirdDoubleValue - fifthDoubleValue + (double)thirdFloatValue));

                    if (movingobjectposition != null)
                    {
                        double deltaX = doubleValue - movingobjectposition.hitVec.xCoord;
                        double deltaY = secondDoubleValue - movingobjectposition.hitVec.yCoord;
                        double deltaZ = thirdDoubleValue - movingobjectposition.hitVec.zCoord;
                        double seventhDoubleValue = (double)MathHelper.sqrt_double(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);

                        if (seventhDoubleValue < fourthDoubleValue)
                        {
                            fourthDoubleValue = seventhDoubleValue;
                        }
                    }
                }

                if (this.mc.gameSettings.thirdPersonView == 2)
                {
                    GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
                }

                GlStateManager.rotate(entity.rotationPitch - twentySecondFloatValue, 1.0F, 0.0F, 0.0F);
                GlStateManager.rotate(entity.rotationYaw - seventeenthFloatValue, 0.0F, 1.0F, 0.0F);
                GlStateManager.translate(0.0F, 0.0F, (float)(-fourthDoubleValue));
                GlStateManager.rotate(seventeenthFloatValue - entity.rotationYaw, 0.0F, 1.0F, 0.0F);
                GlStateManager.rotate(twentySecondFloatValue - entity.rotationPitch, 1.0F, 0.0F, 0.0F);
            }
        }
        else
        {
            GlStateManager.translate(0.0F, 0.0F, -0.1F);
        }

        if (!this.mc.gameSettings.debugCamEnable)
        {
            GlStateManager.rotate(entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks, 1.0F, 0.0F, 0.0F);

            if (entity instanceof EntityAnimal)
            {
                EntityAnimal entityanimal = (EntityAnimal)entity;
                GlStateManager.rotate(entityanimal.prevRotationYawHead + (entityanimal.rotationYawHead - entityanimal.prevRotationYawHead) * partialTicks + 180.0F, 0.0F, 1.0F, 0.0F);
            }
            else
            {
                GlStateManager.rotate(entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTicks + 180.0F, 0.0F, 1.0F, 0.0F);
            }
        }

        GlStateManager.translate(0.0F, -f, 0.0F);
        doubleValue = entity.prevPosX + (entity.posX - entity.prevPosX) * (double)partialTicks;
        secondDoubleValue = entity.prevPosY + (entity.posY - entity.prevPosY) * (double)partialTicks + (double)f;
        thirdDoubleValue = entity.prevPosZ + (entity.posZ - entity.prevPosZ) * (double)partialTicks;
        this.cloudFog = this.mc.renderGlobal.hasCloudFog(doubleValue, secondDoubleValue, thirdDoubleValue, partialTicks);
    }

    public void setupCameraTransform(float partialTicks, int pass)
    {
        this.farPlaneDistance = (float)(this.mc.gameSettings.renderDistanceChunks * 16);

        if (Config.isFogFancy())
        {
            this.farPlaneDistance *= 0.95F;
        }

        if (Config.isFogFast())
        {
            this.farPlaneDistance *= 0.83F;
        }

        GlStateManager.matrixMode(5889);
        GlStateManager.loadIdentity();
        float f = 0.07F;

        if (this.mc.gameSettings.anaglyph)
        {
            GlStateManager.translate((float)(-(pass * 2 - 1)) * f, 0.0F, 0.0F);
        }

        this.clipDistance = this.farPlaneDistance * 2.0F;

        if (this.clipDistance < 173.0F)
        {
            this.clipDistance = 173.0F;
        }

        if (this.cameraZoom != 1.0D)
        {
            GlStateManager.translate((float)this.cameraYaw, (float)(-this.cameraPitch), 0.0F);
            GlStateManager.scale(this.cameraZoom, this.cameraZoom, 1.0D);
        }

        Project.gluPerspective(this.getFOVModifier(partialTicks, true), (float)this.mc.displayWidth / (float)this.mc.displayHeight, 0.05F, this.clipDistance);
        GlStateManager.matrixMode(5888);
        GlStateManager.loadIdentity();

        if (this.mc.gameSettings.anaglyph)
        {
            GlStateManager.translate((float)(pass * 2 - 1) * 0.1F, 0.0F, 0.0F);
        }

        this.hurtCameraEffect(partialTicks);

        if (this.mc.gameSettings.viewBobbing)
        {
            this.setupViewBobbing(partialTicks);
        }

        float eighteenthFloatValue = this.mc.thePlayer.prevTimeInPortal + (this.mc.thePlayer.timeInPortal - this.mc.thePlayer.prevTimeInPortal) * partialTicks;

        if (eighteenthFloatValue > 0.0F)
        {
            int i = 20;

            if (this.mc.thePlayer.isPotionActive(Potion.confusion))
            {
                i = 7;
            }

            float twentyThirdFloatValue = 5.0F / (eighteenthFloatValue * eighteenthFloatValue + 5.0F) - eighteenthFloatValue * 0.04F;
            twentyThirdFloatValue = twentyThirdFloatValue * twentyThirdFloatValue;
            GlStateManager.rotate(((float)this.rendererUpdateCount + partialTicks) * (float)i, 0.0F, 1.0F, 1.0F);
            GlStateManager.scale(1.0F / twentyThirdFloatValue, 1.0F, 1.0F);
            GlStateManager.rotate(-((float)this.rendererUpdateCount + partialTicks) * (float)i, 0.0F, 1.0F, 1.0F);
        }

        this.orientCamera(partialTicks);

        if (this.debugView)
        {
            switch (this.debugViewDirection)
            {
                case 0:
                    GlStateManager.rotate(90.0F, 0.0F, 1.0F, 0.0F);
                    break;

                case 1:
                    GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
                    break;

                case 2:
                    GlStateManager.rotate(-90.0F, 0.0F, 1.0F, 0.0F);
                    break;

                case 3:
                    GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
                    break;

                case 4:
                    GlStateManager.rotate(-90.0F, 1.0F, 0.0F, 0.0F);
            }
        }
    }

    private void renderHand(float partialTicks, int xOffset)
    {
        this.renderHand(partialTicks, xOffset, true, true, false);
    }

    public void renderHand(float partialTicks, int xOffset, boolean renderItem, boolean renderOverlay, boolean renderTranslucent)
    {
        if (!this.debugView)
        {
            GlStateManager.matrixMode(5889);
            GlStateManager.loadIdentity();
            float f = 0.07F;

            if (this.mc.gameSettings.anaglyph)
            {
                GlStateManager.translate((float)(-(xOffset * 2 - 1)) * f, 0.0F, 0.0F);
            }

            if (Config.isShaders())
            {
                Shaders.applyHandDepth();
            }

            Project.gluPerspective(this.getFOVModifier(partialTicks, false), (float)this.mc.displayWidth / (float)this.mc.displayHeight, 0.05F, this.farPlaneDistance * 2.0F);
            GlStateManager.matrixMode(5888);
            GlStateManager.loadIdentity();

            if (this.mc.gameSettings.anaglyph)
            {
                GlStateManager.translate((float)(xOffset * 2 - 1) * 0.1F, 0.0F, 0.0F);
            }

            boolean flag = false;

            if (renderItem)
            {
                GlStateManager.pushMatrix();
                this.hurtCameraEffect(partialTicks);

                if (this.mc.gameSettings.viewBobbing)
                {
                    this.setupViewBobbing(partialTicks);
                }

                flag = this.mc.getRenderViewEntity() instanceof EntityLivingBase && ((EntityLivingBase)this.mc.getRenderViewEntity()).isPlayerSleeping();

                if (this.mc.gameSettings.thirdPersonView == 0 && !flag && !this.mc.gameSettings.hideGUI && !this.mc.playerController.isSpectator())
                {
                    this.enableLightmap();

                    if (Config.isShaders())
                    {
                        ShadersRender.renderItemFP(this.itemRenderer, partialTicks, renderTranslucent);
                    }
                    else
                    {
                        this.itemRenderer.renderItemInFirstPerson(partialTicks);
                    }

                    this.disableLightmap();
                }

                GlStateManager.popMatrix();
            }

            if (!renderOverlay)
            {
                return;
            }

            this.disableLightmap();

            if (this.mc.gameSettings.thirdPersonView == 0 && !flag)
            {
                this.itemRenderer.renderOverlays(partialTicks);
                this.hurtCameraEffect(partialTicks);
            }

            if (this.mc.gameSettings.viewBobbing)
            {
                this.setupViewBobbing(partialTicks);
            }
        }
    }

    public void disableLightmap()
    {
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.disableTexture2D();
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);

        if (Config.isShaders())
        {
            Shaders.disableLightmap();
        }
    }

    public void enableLightmap()
    {
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.matrixMode(5890);
        GlStateManager.loadIdentity();
        float f = 0.00390625F;
        GlStateManager.scale(f, f, f);
        GlStateManager.translate(8.0F, 8.0F, 8.0F);
        GlStateManager.matrixMode(5888);
        this.mc.getTextureManager().bindTexture(this.locationLightMap);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableTexture2D();
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);

        if (Config.isShaders())
        {
            Shaders.enableLightmap();
        }
    }

    private void updateTorchFlicker()
    {
        this.torchFlickerDX = (float)((double)this.torchFlickerDX + (this.random.nextDouble() - this.random.nextDouble()) * this.random.nextDouble() * this.random.nextDouble());
        this.torchFlickerDX = (float)((double)this.torchFlickerDX * 0.9D);
        this.torchFlickerX += (this.torchFlickerDX - this.torchFlickerX) * 1.0F;
        this.lightmapUpdateNeeded = true;
    }

    private void updateLightmap(float partialTicks)
    {
        if (this.lightmapUpdateNeeded)
        {
            this.mc.mcProfiler.startSection("lightTex");
            World world = this.mc.theWorld;

            if (world != null)
            {
                if (Config.isCustomColors() && CustomColors.updateLightmap(world, this.torchFlickerX, this.lightmapColors, this.mc.thePlayer.isPotionActive(Potion.nightVision), partialTicks))
                {
                    this.lightmapTexture.updateDynamicTexture();
                    this.lightmapUpdateNeeded = false;
                    this.mc.mcProfiler.endSection();
                    return;
                }

                float f = world.getSunBrightness(1.0F);
                float floatValue = f * 0.95F + 0.05F;

                for (int i = 0; i < 256; ++i)
                {
                    float secondFloatValue = world.provider.getLightBrightnessTable()[i / 16] * floatValue;
                    float thirdFloatValue = world.provider.getLightBrightnessTable()[i % 16] * (this.torchFlickerX * 0.1F + 1.5F);

                    if (world.getLastLightningBolt() > 0)
                    {
                        secondFloatValue = world.provider.getLightBrightnessTable()[i / 16];
                    }

                    float twentyNinthFloatValue = secondFloatValue * (f * 0.65F + 0.35F);
                    float fourthFloatValue = secondFloatValue * (f * 0.65F + 0.35F);
                    float fifthFloatValue = thirdFloatValue * ((thirdFloatValue * 0.6F + 0.4F) * 0.6F + 0.4F);
                    float sixthFloatValue = thirdFloatValue * (thirdFloatValue * thirdFloatValue * 0.6F + 0.4F);
                    float seventhFloatValue = twentyNinthFloatValue + thirdFloatValue;
                    float eighthFloatValue = fourthFloatValue + fifthFloatValue;
                    float ninthFloatValue = secondFloatValue + sixthFloatValue;
                    seventhFloatValue = seventhFloatValue * 0.96F + 0.03F;
                    eighthFloatValue = eighthFloatValue * 0.96F + 0.03F;
                    ninthFloatValue = ninthFloatValue * 0.96F + 0.03F;

                    if (this.bossColorModifier > 0.0F)
                    {
                        float tenthFloatValue = this.bossColorModifierPrev + (this.bossColorModifier - this.bossColorModifierPrev) * partialTicks;
                        seventhFloatValue = seventhFloatValue * (1.0F - tenthFloatValue) + seventhFloatValue * 0.7F * tenthFloatValue;
                        eighthFloatValue = eighthFloatValue * (1.0F - tenthFloatValue) + eighthFloatValue * 0.6F * tenthFloatValue;
                        ninthFloatValue = ninthFloatValue * (1.0F - tenthFloatValue) + ninthFloatValue * 0.6F * tenthFloatValue;
                    }

                    if (world.provider.getDimensionId() == 1)
                    {
                        seventhFloatValue = 0.22F + thirdFloatValue * 0.75F;
                        eighthFloatValue = 0.28F + fifthFloatValue * 0.75F;
                        ninthFloatValue = 0.25F + sixthFloatValue * 0.75F;
                    }

                    if (this.mc.thePlayer.isPotionActive(Potion.nightVision))
                    {
                        float eleventhFloatValue = this.getNightVisionBrightness(this.mc.thePlayer, partialTicks);
                        float twelfthFloatValue = 1.0F / seventhFloatValue;

                        if (twelfthFloatValue > 1.0F / eighthFloatValue)
                        {
                            twelfthFloatValue = 1.0F / eighthFloatValue;
                        }

                        if (twelfthFloatValue > 1.0F / ninthFloatValue)
                        {
                            twelfthFloatValue = 1.0F / ninthFloatValue;
                        }

                        seventhFloatValue = seventhFloatValue * (1.0F - eleventhFloatValue) + seventhFloatValue * twelfthFloatValue * eleventhFloatValue;
                        eighthFloatValue = eighthFloatValue * (1.0F - eleventhFloatValue) + eighthFloatValue * twelfthFloatValue * eleventhFloatValue;
                        ninthFloatValue = ninthFloatValue * (1.0F - eleventhFloatValue) + ninthFloatValue * twelfthFloatValue * eleventhFloatValue;
                    }

                    if (seventhFloatValue > 1.0F)
                    {
                        seventhFloatValue = 1.0F;
                    }

                    if (eighthFloatValue > 1.0F)
                    {
                        eighthFloatValue = 1.0F;
                    }

                    if (ninthFloatValue > 1.0F)
                    {
                        ninthFloatValue = 1.0F;
                    }

                    float twentyFirstFloatValue = this.mc.gameSettings.gammaSetting;
                    float thirteenthFloatValue = 1.0F - seventhFloatValue;
                    float fourteenthFloatValue = 1.0F - eighthFloatValue;
                    float fifteenthFloatValue = 1.0F - ninthFloatValue;
                    thirteenthFloatValue = 1.0F - thirteenthFloatValue * thirteenthFloatValue * thirteenthFloatValue * thirteenthFloatValue;
                    fourteenthFloatValue = 1.0F - fourteenthFloatValue * fourteenthFloatValue * fourteenthFloatValue * fourteenthFloatValue;
                    fifteenthFloatValue = 1.0F - fifteenthFloatValue * fifteenthFloatValue * fifteenthFloatValue * fifteenthFloatValue;
                    seventhFloatValue = seventhFloatValue * (1.0F - twentyFirstFloatValue) + thirteenthFloatValue * twentyFirstFloatValue;
                    eighthFloatValue = eighthFloatValue * (1.0F - twentyFirstFloatValue) + fourteenthFloatValue * twentyFirstFloatValue;
                    ninthFloatValue = ninthFloatValue * (1.0F - twentyFirstFloatValue) + fifteenthFloatValue * twentyFirstFloatValue;
                    seventhFloatValue = seventhFloatValue * 0.96F + 0.03F;
                    eighthFloatValue = eighthFloatValue * 0.96F + 0.03F;
                    ninthFloatValue = ninthFloatValue * 0.96F + 0.03F;

                    if (seventhFloatValue > 1.0F)
                    {
                        seventhFloatValue = 1.0F;
                    }

                    if (eighthFloatValue > 1.0F)
                    {
                        eighthFloatValue = 1.0F;
                    }

                    if (ninthFloatValue > 1.0F)
                    {
                        ninthFloatValue = 1.0F;
                    }

                    if (seventhFloatValue < 0.0F)
                    {
                        seventhFloatValue = 0.0F;
                    }

                    if (eighthFloatValue < 0.0F)
                    {
                        eighthFloatValue = 0.0F;
                    }

                    if (ninthFloatValue < 0.0F)
                    {
                        ninthFloatValue = 0.0F;
                    }

                    int j = 255;
                    int k = (int)(seventhFloatValue * 255.0F);
                    int l = (int)(eighthFloatValue * 255.0F);
                    int intValue = (int)(ninthFloatValue * 255.0F);
                    this.lightmapColors[i] = j << 24 | k << 16 | l << 8 | intValue;
                }

                this.lightmapTexture.updateDynamicTexture();
                this.lightmapUpdateNeeded = false;
                this.mc.mcProfiler.endSection();
            }
        }
    }

    public float getNightVisionBrightness(EntityLivingBase entitylivingbaseIn, float partialTicks)
    {
        int i = entitylivingbaseIn.getActivePotionEffect(Potion.nightVision).getDuration();
        return i > 200 ? 1.0F : 0.7F + MathHelper.sin(((float)i - partialTicks) * (float)Math.PI * 0.2F) * 0.3F;
    }

    public void updateCameraAndRender(float partialTicks, long nanoTime)
    {
        Config.renderPartialTicks = partialTicks;
        this.frameInit();
        boolean flag = Display.isActive();

        if (!flag && this.mc.gameSettings.pauseOnLostFocus && (!this.mc.gameSettings.touchscreen || !Mouse.isButtonDown(1)))
        {
            if (Minecraft.getSystemTime() - this.prevFrameTime > 500L)
            {
                this.mc.displayInGameMenu();
            }
        }
        else
        {
            this.prevFrameTime = Minecraft.getSystemTime();
        }

        this.mc.mcProfiler.startSection("mouse");

        if (flag && Minecraft.isRunningOnMac && this.mc.inGameHasFocus && !Mouse.isInsideWindow())
        {
            Mouse.setGrabbed(false);
            Mouse.setCursorPosition(Display.getWidth() / 2, Display.getHeight() / 2);
            Mouse.setGrabbed(true);
        }

        if (this.mc.inGameHasFocus && flag)
        {
            this.mc.mouseHelper.mouseXYChange();
            float f = this.mc.gameSettings.mouseSensitivity * 0.6F + 0.2F;
            float sixteenthFloatValue = f * f * f * 8.0F;
            float twentyFifthFloatValue = (float)this.mc.mouseHelper.deltaX * sixteenthFloatValue;
            float twentySixthFloatValue = (float)this.mc.mouseHelper.deltaY * sixteenthFloatValue;
            int i = 1;

            if (this.mc.gameSettings.invertMouse)
            {
                i = -1;
            }

            if (this.mc.gameSettings.smoothCamera)
            {
                this.smoothCamYaw += twentyFifthFloatValue;
                this.smoothCamPitch += twentySixthFloatValue;
                float twentyEighthFloatValue = partialTicks - this.smoothCamPartialTicks;
                this.smoothCamPartialTicks = partialTicks;
                twentyFifthFloatValue = this.smoothCamFilterX * twentyEighthFloatValue;
                twentySixthFloatValue = this.smoothCamFilterY * twentyEighthFloatValue;
                this.mc.thePlayer.setAngles(twentyFifthFloatValue, twentySixthFloatValue * (float)i);
            }
            else
            {
                this.smoothCamYaw = 0.0F;
                this.smoothCamPitch = 0.0F;
                this.mc.thePlayer.setAngles(twentyFifthFloatValue, twentySixthFloatValue * (float)i);
            }
        }

        this.mc.mcProfiler.endSection();

        if (!this.mc.skipRenderWorld)
        {
            anaglyphEnable = this.mc.gameSettings.anaglyph;
            final ScaledResolution scaledresolution = new ScaledResolution(this.mc);
            int fourteenthIntValue = scaledresolution.getScaledWidth();
            int seventeenthIntValue = scaledresolution.getScaledHeight();
            final int nineteenthIntValue = Mouse.getX() * fourteenthIntValue / this.mc.displayWidth;
            final int twentiethIntValue = seventeenthIntValue - Mouse.getY() * seventeenthIntValue / this.mc.displayHeight - 1;
            int fifteenthIntValue = this.mc.gameSettings.limitFramerate;

            if (this.mc.theWorld != null)
            {
                this.mc.mcProfiler.startSection("level");
                int j = Math.min(Minecraft.getDebugFPS(), fifteenthIntValue);
                j = Math.max(j, 60);
                long k = System.nanoTime() - nanoTime;
                long l = Math.max((long)(1000000000 / j / 4) - k, 0L);
                this.renderWorld(partialTicks, System.nanoTime() + l);

                if (OpenGlHelper.shadersSupported)
                {
                    this.mc.renderGlobal.renderEntityOutlineFramebuffer();

                    if (this.theShaderGroup != null && this.useShader)
                    {
                        GlStateManager.matrixMode(5890);
                        GlStateManager.pushMatrix();
                        GlStateManager.loadIdentity();
                        this.theShaderGroup.loadShaderGroup(partialTicks);
                        GlStateManager.popMatrix();
                    }

                    this.mc.getFramebuffer().bindFramebuffer(true);
                }

                this.renderEndNanoTime = System.nanoTime();
                this.mc.mcProfiler.endStartSection("gui");

                if (!this.mc.gameSettings.hideGUI || this.mc.currentScreen != null)
                {
                    GlStateManager.alphaFunc(516, 0.1F);
                    this.mc.ingameGUI.renderGameOverlay(partialTicks);

                    if (this.mc.gameSettings.ofShowFps && !this.mc.gameSettings.showDebugInfo)
                    {
                        Config.drawFps();
                    }

                    if (this.mc.gameSettings.showDebugInfo)
                    {
                        Lagometer.showLagometer(scaledresolution);
                    }
                }

                this.mc.mcProfiler.endSection();
            }
            else
            {
                GlStateManager.viewport(0, 0, this.mc.displayWidth, this.mc.displayHeight);
                GlStateManager.matrixMode(5889);
                GlStateManager.loadIdentity();
                GlStateManager.matrixMode(5888);
                GlStateManager.loadIdentity();
                this.setupOverlayRendering();
                this.renderEndNanoTime = System.nanoTime();
                TileEntityRendererDispatcher.instance.renderEngine = this.mc.getTextureManager();
                TileEntityRendererDispatcher.instance.fontRenderer = this.mc.fontRendererObj;
            }

            if (this.mc.currentScreen != null)
            {
                GlStateManager.clear(256);

                try
                {
                    
                    this.mc.currentScreen.drawScreen(nineteenthIntValue, twentiethIntValue, partialTicks);
                
                }
                catch (Throwable throwable)
                {
                    CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Rendering screen");
                    CrashReportCategory crashreportcategory = crashreport.makeCategory("Screen render details");
                    crashreportcategory.addCrashSectionCallable("Screen name", new Callable<String>()
                    {
                        public String call() throws Exception
                        {
                            return EntityRenderer.this.mc.currentScreen.getClass().getCanonicalName();
                        }
                    });
                    crashreportcategory.addCrashSectionCallable("Mouse location", new Callable<String>()
                    {
                        public String call() throws Exception
                        {
                            return "Scaled: (" + nineteenthIntValue + ", " + twentiethIntValue + "). Absolute: (" + Mouse.getX() + ", " + Mouse.getY() + ")";
                        }
                    });
                    crashreportcategory.addCrashSectionCallable("Screen size", new Callable<String>()
                    {
                        public String call() throws Exception
                        {
                            return "Scaled: (" + scaledresolution.getScaledWidth() + ", " + scaledresolution.getScaledHeight() + "). Absolute: (" + EntityRenderer.this.mc.displayWidth + ", " + EntityRenderer.this.mc.displayHeight + "). Scale factor of " + scaledresolution.getScaleFactor();
                        }
                    });
                    throw new ReportedException(crashreport);
                }
            }
        }

        this.frameFinish();
        this.waitForServerThread();
        MemoryMonitor.update();
        Lagometer.updateLagometer();

        if (this.mc.gameSettings.ofProfiler)
        {
            this.mc.gameSettings.showDebugProfilerChart = true;
        }
    }

    public void renderStreamIndicator(float partialTicks)
    {
        this.setupOverlayRendering();
        this.mc.ingameGUI.renderStreamIndicator(new ScaledResolution(this.mc));
    }

    private boolean isDrawBlockOutline()
    {
        if (!this.drawBlockOutline)
        {
            return false;
        }
        else
        {
            Entity entity = this.mc.getRenderViewEntity();
            boolean flag = entity instanceof EntityPlayer && !this.mc.gameSettings.hideGUI;

            if (flag && !((EntityPlayer)entity).capabilities.allowEdit)
            {
                ItemStack itemStack = ((EntityPlayer)entity).getCurrentEquippedItem();

                if (this.mc.objectMouseOver != null && this.mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK)
                {
                    BlockPos blockPos = this.mc.objectMouseOver.getBlockPos();
                    IBlockState iblockstate = this.mc.theWorld.getBlockState(blockPos);
                    Block block = iblockstate.getBlock();

                    if (this.mc.playerController.getCurrentGameType() == WorldSettings.GameType.SPECTATOR)
                    {
                        flag = iblockstate.getBlock().hasTileEntity() && this.mc.theWorld.getTileEntity(blockPos) instanceof IInventory;
                    }
                    else
                    {
                        flag = itemStack != null && (itemStack.canDestroy(block) || itemStack.canPlaceOn(block));
                    }
                }
            }

            return flag;
        }
    }

    private void renderWorldDirections(float partialTicks)
    {
        if (this.mc.gameSettings.showDebugInfo && !this.mc.gameSettings.hideGUI && !this.mc.thePlayer.hasReducedDebug() && !this.mc.gameSettings.reducedDebugInfo)
        {
            Entity entity = this.mc.getRenderViewEntity();
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            GL11.glLineWidth(1.0F);
            GlStateManager.disableTexture2D();
            GlStateManager.depthMask(false);
            GlStateManager.pushMatrix();
            GlStateManager.matrixMode(5888);
            GlStateManager.loadIdentity();
            this.orientCamera(partialTicks);
            GlStateManager.translate(0.0F, entity.getEyeHeight(), 0.0F);
            RenderGlobal.drawOutlinedBoundingBox(new AxisAlignedBB(0.0D, 0.0D, 0.0D, 0.005D, 1.0E-4D, 1.0E-4D), 255, 0, 0, 255);
            RenderGlobal.drawOutlinedBoundingBox(new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0E-4D, 1.0E-4D, 0.005D), 0, 0, 255, 255);
            RenderGlobal.drawOutlinedBoundingBox(new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0E-4D, 0.0033D, 1.0E-4D), 0, 255, 0, 255);
            GlStateManager.popMatrix();
            GlStateManager.depthMask(true);
            GlStateManager.enableTexture2D();
            GlStateManager.disableBlend();
        }
    }

    public void renderWorld(float partialTicks, long finishTimeNano)
    {
        this.updateLightmap(partialTicks);

        if (this.mc.getRenderViewEntity() == null)
        {
            this.mc.setRenderViewEntity(this.mc.thePlayer);
        }

        this.getMouseOver(partialTicks);

        if (Config.isShaders())
        {
            Shaders.beginRender(this.mc, partialTicks, finishTimeNano);
        }

        GlStateManager.enableDepth();
        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(516, 0.1F);
        this.mc.mcProfiler.startSection("center");

        if (this.mc.gameSettings.anaglyph)
        {
            anaglyphField = 0;
            GlStateManager.colorMask(false, true, true, false);
            this.renderWorldPass(0, partialTicks, finishTimeNano);
            anaglyphField = 1;
            GlStateManager.colorMask(true, false, false, false);
            this.renderWorldPass(1, partialTicks, finishTimeNano);
            GlStateManager.colorMask(true, true, true, false);
        }
        else
        {
            this.renderWorldPass(2, partialTicks, finishTimeNano);
        }

        this.mc.mcProfiler.endSection();
    }

    private void renderWorldPass(int pass, float partialTicks, long finishTimeNano)
    {
        boolean flag = Config.isShaders();

        if (flag)
        {
            Shaders.beginRenderPass(pass, partialTicks, finishTimeNano);
        }

        RenderGlobal renderglobal = this.mc.renderGlobal;
        EffectRenderer effectrenderer = this.mc.effectRenderer;
        boolean flag1 = this.isDrawBlockOutline();
        GlStateManager.enableCull();
        this.mc.mcProfiler.endStartSection("clear");

        if (flag)
        {
            Shaders.setViewport(0, 0, this.mc.displayWidth, this.mc.displayHeight);
        }
        else
        {
            GlStateManager.viewport(0, 0, this.mc.displayWidth, this.mc.displayHeight);
        }

        this.updateFogColor(partialTicks);
        GlStateManager.clear(16640);

        if (flag)
        {
            Shaders.clearRenderBuffer();
        }

        this.mc.mcProfiler.endStartSection("camera");
        this.setupCameraTransform(partialTicks, pass);

        if (flag)
        {
            Shaders.setCamera(partialTicks);
        }

        ActiveRenderInfo.updateRenderInfo(this.mc.thePlayer, this.mc.gameSettings.thirdPersonView == 2);
        this.mc.mcProfiler.endStartSection("frustum");
        ClippingHelper clippinghelper = ClippingHelperImpl.getInstance();
        this.mc.mcProfiler.endStartSection("culling");
        clippinghelper.disabled = Config.isShaders() && !Shaders.isFrustumCulling();
        if (this.renderFrustum == null)
        {
            this.renderFrustum = new Frustum(clippinghelper);
        }

        ICamera icamera = this.renderFrustum;
        Entity entity = this.mc.getRenderViewEntity();
        double doubleValue = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * (double)partialTicks;
        double secondDoubleValue = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * (double)partialTicks;
        double thirdDoubleValue = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * (double)partialTicks;

        if (flag)
        {
            ShadersRender.setFrustrumPosition(icamera, doubleValue, secondDoubleValue, thirdDoubleValue);
        }
        else
        {
            icamera.setPosition(doubleValue, secondDoubleValue, thirdDoubleValue);
        }

        ParticleCulling.setCamera(icamera);

        if ((Config.isSkyEnabled() || Config.isSunMoonEnabled() || Config.isStarsEnabled()) && !Shaders.isShadowPass)
        {
            this.setupFog(-1, partialTicks);
            this.mc.mcProfiler.endStartSection("sky");
            GlStateManager.matrixMode(5889);
            GlStateManager.loadIdentity();
            Project.gluPerspective(this.getFOVModifier(partialTicks, true), (float)this.mc.displayWidth / (float)this.mc.displayHeight, 0.05F, this.clipDistance);
            GlStateManager.matrixMode(5888);

            if (flag)
            {
                Shaders.beginSky();
            }

            renderglobal.renderSky(partialTicks, pass);

            if (flag)
            {
                Shaders.endSky();
            }

            GlStateManager.matrixMode(5889);
            GlStateManager.loadIdentity();
            Project.gluPerspective(this.getFOVModifier(partialTicks, true), (float)this.mc.displayWidth / (float)this.mc.displayHeight, 0.05F, this.clipDistance);
            GlStateManager.matrixMode(5888);
        }
        else
        {
            GlStateManager.disableBlend();
        }

        this.setupFog(0, partialTicks);
        GlStateManager.shadeModel(7425);

        if (entity.posY + (double)entity.getEyeHeight() < 128.0D + (double)(this.mc.gameSettings.ofCloudsHeight * 128.0F))
        {
            this.renderCloudsCheck(renderglobal, partialTicks, pass);
        }

        this.mc.mcProfiler.endStartSection("prepareterrain");
        this.setupFog(0, partialTicks);
        this.mc.getTextureManager().bindTexture(TextureMap.locationBlocksTexture);
        RenderHelper.disableStandardItemLighting();
        this.mc.mcProfiler.endStartSection("terrain_setup");
        this.checkLoadVisibleChunks(entity, partialTicks, icamera, this.mc.thePlayer.isSpectator());

        if (flag)
        {
            ShadersRender.setupTerrain(renderglobal, entity, (double)partialTicks, icamera, this.frameCount++, this.mc.thePlayer.isSpectator());
        }
        else
        {
            renderglobal.setupTerrain(entity, (double)partialTicks, icamera, this.frameCount++, this.mc.thePlayer.isSpectator());
        }

        if (pass == 0 || pass == 2)
        {
            this.mc.mcProfiler.endStartSection("updatechunks");
            Lagometer.timerChunkUpload.start();
            this.mc.renderGlobal.updateChunks(finishTimeNano);
            Lagometer.timerChunkUpload.end();
        }

        this.mc.mcProfiler.endStartSection("terrain");
        Lagometer.timerTerrain.start();

        if (this.mc.gameSettings.ofSmoothFps && pass > 0)
        {
            this.mc.mcProfiler.endStartSection("finish");
            GL11.glFinish();
            this.mc.mcProfiler.endStartSection("terrain");
        }

        GlStateManager.matrixMode(5888);
        GlStateManager.pushMatrix();
        GlStateManager.disableAlpha();

        if (flag)
        {
            ShadersRender.beginTerrainSolid();
        }

        renderglobal.renderBlockLayer(EnumWorldBlockLayer.SOLID, (double)partialTicks, pass, entity);
        GlStateManager.enableAlpha();

        if (flag)
        {
            ShadersRender.beginTerrainCutoutMipped();
        }

        this.mc.getTextureManager().getTexture(TextureMap.locationBlocksTexture).setBlurMipmap(false, this.mc.gameSettings.mipmapLevels > 0);
        renderglobal.renderBlockLayer(EnumWorldBlockLayer.CUTOUT_MIPPED, (double)partialTicks, pass, entity);
        this.mc.getTextureManager().getTexture(TextureMap.locationBlocksTexture).restoreLastBlurMipmap();
        this.mc.getTextureManager().getTexture(TextureMap.locationBlocksTexture).setBlurMipmap(false, false);

        if (flag)
        {
            ShadersRender.beginTerrainCutout();
        }

        renderglobal.renderBlockLayer(EnumWorldBlockLayer.CUTOUT, (double)partialTicks, pass, entity);
        this.mc.getTextureManager().getTexture(TextureMap.locationBlocksTexture).restoreLastBlurMipmap();

        if (flag)
        {
            ShadersRender.endTerrain();
        }

        Lagometer.timerTerrain.end();
        GlStateManager.shadeModel(7424);
        GlStateManager.alphaFunc(516, 0.1F);

        if (!this.debugView)
        {
            GlStateManager.matrixMode(5888);
            GlStateManager.popMatrix();
            GlStateManager.pushMatrix();
            RenderHelper.enableStandardItemLighting();
            this.mc.mcProfiler.endStartSection("entities");

            
            renderglobal.renderEntities(entity, icamera, partialTicks);

            
            RenderHelper.disableStandardItemLighting();
            this.disableLightmap();
            GlStateManager.matrixMode(5888);
            GlStateManager.popMatrix();
            GlStateManager.pushMatrix();

            if (this.mc.objectMouseOver != null && entity.isInsideOfMaterial(Material.water) && flag1)
            {
                EntityPlayer entityplayer = (EntityPlayer)entity;
                GlStateManager.disableAlpha();
                this.mc.mcProfiler.endStartSection("outline");
                renderglobal.drawSelectionBox(entityplayer, this.mc.objectMouseOver, 0, partialTicks);
                GlStateManager.enableAlpha();
            }
        }

        GlStateManager.matrixMode(5888);
        GlStateManager.popMatrix();

        if (flag1 && this.mc.objectMouseOver != null && !entity.isInsideOfMaterial(Material.water))
        {
            EntityPlayer entityplayer1 = (EntityPlayer)entity;
            GlStateManager.disableAlpha();
            this.mc.mcProfiler.endStartSection("outline");

            if (!this.mc.gameSettings.hideGUI)
            {
                renderglobal.drawSelectionBox(entityplayer1, this.mc.objectMouseOver, 0, partialTicks);
            }
            GlStateManager.enableAlpha();
        }

        if (!renderglobal.damagedBlocks.isEmpty())
        {
            this.mc.mcProfiler.endStartSection("destroyProgress");
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 1, 1, 0);
            this.mc.getTextureManager().getTexture(TextureMap.locationBlocksTexture).setBlurMipmap(false, false);
            renderglobal.drawBlockDamageTexture(Tessellator.getInstance(), Tessellator.getInstance().getWorldRenderer(), entity, partialTicks);
            this.mc.getTextureManager().getTexture(TextureMap.locationBlocksTexture).restoreLastBlurMipmap();
            GlStateManager.disableBlend();
        }

        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.disableBlend();

        if (!this.debugView)
        {
            this.enableLightmap();
            this.mc.mcProfiler.endStartSection("litParticles");

            if (flag)
            {
                Shaders.beginLitParticles();
            }

            effectrenderer.renderLitParticles(entity, partialTicks);
            RenderHelper.disableStandardItemLighting();
            this.setupFog(0, partialTicks);
            this.mc.mcProfiler.endStartSection("particles");

            if (flag)
            {
                Shaders.beginParticles();
            }

            effectrenderer.renderParticles(entity, partialTicks);

            if (flag)
            {
                Shaders.endParticles();
            }

            this.disableLightmap();
        }

        GlStateManager.depthMask(false);

        if (Config.isShaders())
        {
            GlStateManager.depthMask(Shaders.isRainDepth());
        }

        GlStateManager.enableCull();
        this.mc.mcProfiler.endStartSection("weather");

        if (flag)
        {
            Shaders.beginWeather();
        }

        this.renderRainSnow(partialTicks);

        if (flag)
        {
            Shaders.endWeather();
        }

        GlStateManager.depthMask(true);
        renderglobal.renderWorldBorder(entity, partialTicks);

        if (flag)
        {
            ShadersRender.renderHand0(this, partialTicks, pass);
            Shaders.preWater();
        }

        GlStateManager.disableBlend();
        GlStateManager.enableCull();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.alphaFunc(516, 0.1F);
        this.setupFog(0, partialTicks);
        GlStateManager.enableBlend();
        GlStateManager.depthMask(false);
        this.mc.getTextureManager().bindTexture(TextureMap.locationBlocksTexture);
        GlStateManager.shadeModel(7425);
        this.mc.mcProfiler.endStartSection("translucent");

        if (flag)
        {
            Shaders.beginWater();
        }

        renderglobal.renderBlockLayer(EnumWorldBlockLayer.TRANSLUCENT, (double)partialTicks, pass, entity);

        if (flag)
        {
            Shaders.endWater();
        }

        
        GlStateManager.shadeModel(7424);
        GlStateManager.depthMask(true);
        GlStateManager.enableCull();
        GlStateManager.disableBlend();
        GlStateManager.disableFog();

        if (entity.posY + (double)entity.getEyeHeight() >= 128.0D + (double)(this.mc.gameSettings.ofCloudsHeight * 128.0F))
        {
            this.mc.mcProfiler.endStartSection("aboveClouds");
            this.renderCloudsCheck(renderglobal, partialTicks, pass);
        }

        
        this.mc.mcProfiler.endStartSection("hand");

        if (this.renderHand && !Shaders.isShadowPass)
        {
            if (flag)
            {
                ShadersRender.renderHand1(this, partialTicks, pass);
                Shaders.renderCompositeFinal();
            }

            GlStateManager.clear(256);

            if (flag)
            {
                ShadersRender.renderFPOverlay(this, partialTicks, pass);
            }
            else
            {
                this.renderHand(partialTicks, pass);
            }

            this.renderWorldDirections(partialTicks);
        }

        if (flag)
        {
            Shaders.endRender();
        }
    }

    private void renderCloudsCheck(RenderGlobal renderGlobalIn, float partialTicks, int pass)
    {
        if (this.mc.gameSettings.renderDistanceChunks >= 4 && !Config.isCloudsOff() && Shaders.shouldRenderClouds(this.mc.gameSettings))
        {
            this.mc.mcProfiler.endStartSection("clouds");
            GlStateManager.matrixMode(5889);
            GlStateManager.loadIdentity();
            Project.gluPerspective(this.getFOVModifier(partialTicks, true), (float)this.mc.displayWidth / (float)this.mc.displayHeight, 0.05F, this.clipDistance * 4.0F);
            GlStateManager.matrixMode(5888);
            GlStateManager.pushMatrix();
            this.setupFog(0, partialTicks);
            renderGlobalIn.renderClouds(partialTicks, pass);
            GlStateManager.disableFog();
            GlStateManager.popMatrix();
            GlStateManager.matrixMode(5889);
            GlStateManager.loadIdentity();
            Project.gluPerspective(this.getFOVModifier(partialTicks, true), (float)this.mc.displayWidth / (float)this.mc.displayHeight, 0.05F, this.clipDistance);
            GlStateManager.matrixMode(5888);
        }
    }

    private void addRainParticles()
    {
        float f = this.mc.theWorld.getRainStrength(1.0F);

        if (!Config.isRainFancy())
        {
            f /= 2.0F;
        }

        if (f != 0.0F && Config.isRainSplash())
        {
            this.random.setSeed((long)this.rendererUpdateCount * 312987231L);
            Entity entity = this.mc.getRenderViewEntity();
            World world = this.mc.theWorld;
            BlockPos blockpos = new BlockPos(entity);
            int i = 10;
            double thirteenthDoubleValue = 0.0D;
            double fourteenthDoubleValue = 0.0D;
            double fifteenthDoubleValue = 0.0D;
            int j = 0;
            int k = (int)(100.0F * f * f);

            if (this.mc.gameSettings.particleSetting == 1)
            {
                k >>= 1;
            }
            else if (this.mc.gameSettings.particleSetting == 2)
            {
                k = 0;
            }

            for (int l = 0; l < k; ++l)
            {
                int rainSampleX = blockpos.getX() + this.random.nextInt(i) - this.random.nextInt(i);
                int rainSampleZ = blockpos.getZ() + this.random.nextInt(i) - this.random.nextInt(i);
                BlockPos blockpos1 = world.getPrecipitationHeight(rainSamplePos.set(rainSampleX, blockpos.getY(), rainSampleZ));
                BiomeGenBase biomegenbase = world.getBiomeGenForCoords(blockpos1);
                BlockPos blockpos2 = rainBlockPos.set(blockpos1.getX(), blockpos1.getY() - 1, blockpos1.getZ());
                Block block = world.getBlockState(blockpos2).getBlock();

                if (blockpos1.getY() <= blockpos.getY() + i && blockpos1.getY() >= blockpos.getY() - i && biomegenbase.canRain() && biomegenbase.getFloatTemperature(blockpos1) >= 0.15F)
                {
                    double sixteenthDoubleValue = this.random.nextDouble();
                    double seventeenthDoubleValue = this.random.nextDouble();

                    if (block.getMaterial() == Material.lava)
                    {
                        this.mc.theWorld.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, (double)blockpos1.getX() + sixteenthDoubleValue, (double)((float)blockpos1.getY() + 0.1F) - block.getBlockBoundsMinY(), (double)blockpos1.getZ() + seventeenthDoubleValue, 0.0D, 0.0D, 0.0D, EnumParticleTypes.EMPTY_ARGS);
                    }
                    else if (block.getMaterial() != Material.air)
                    {
                        block.setBlockBoundsBasedOnState(world, blockpos2);
                        ++j;

                        if (this.random.nextInt(j) == 0)
                        {
                            thirteenthDoubleValue = (double)blockpos2.getX() + sixteenthDoubleValue;
                            fourteenthDoubleValue = (double)((float)blockpos2.getY() + 0.1F) + block.getBlockBoundsMaxY() - 1.0D;
                            fifteenthDoubleValue = (double)blockpos2.getZ() + seventeenthDoubleValue;
                        }

                        this.mc.theWorld.spawnParticle(EnumParticleTypes.WATER_DROP, (double)blockpos2.getX() + sixteenthDoubleValue, (double)((float)blockpos2.getY() + 0.1F) + block.getBlockBoundsMaxY(), (double)blockpos2.getZ() + seventeenthDoubleValue, 0.0D, 0.0D, 0.0D, EnumParticleTypes.EMPTY_ARGS);
                    }
                }
            }

            if (j > 0 && this.random.nextInt(3) < this.rainSoundCounter++)
            {
                this.rainSoundCounter = 0;

                if (fourteenthDoubleValue > (double)(blockpos.getY() + 1) && world.getPrecipitationHeight(blockpos).getY() > MathHelper.floor_float((float)blockpos.getY()))
                {
                    this.mc.theWorld.playSound(thirteenthDoubleValue, fourteenthDoubleValue, fifteenthDoubleValue, "ambient.weather.rain", 0.1F, 0.5F, false);
                }
                else
                {
                    this.mc.theWorld.playSound(thirteenthDoubleValue, fourteenthDoubleValue, fifteenthDoubleValue, "ambient.weather.rain", 0.2F, 1.0F, false);
                }
            }
        }
    }

    protected void renderRainSnow(float partialTicks)
    {
        
        float thirtiethFloatValue = this.mc.theWorld.getRainStrength(partialTicks);

        if (thirtiethFloatValue > 0.0F)
        {
            if (Config.isRainOff())
            {
                return;
            }

            this.enableLightmap();
            Entity entity = this.mc.getRenderViewEntity();
            World world = this.mc.theWorld;
            int i = MathHelper.floor_double(entity.posX);
            int j = MathHelper.floor_double(entity.posY);
            int k = MathHelper.floor_double(entity.posZ);
            Tessellator tessellator = Tessellator.getInstance();
            WorldRenderer worldrenderer = tessellator.getWorldRenderer();
            GlStateManager.disableCull();
            GL11.glNormal3f(0.0F, 1.0F, 0.0F);
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            GlStateManager.alphaFunc(516, 0.1F);
            double doubleValue = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * (double)partialTicks;
            double secondDoubleValue = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * (double)partialTicks;
            double thirdDoubleValue = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * (double)partialTicks;
            int l = MathHelper.floor_double(secondDoubleValue);
            int intValue = 5;

            if (Config.isRainFancy())
            {
                intValue = 10;
            }

            int eighteenthIntValue = -1;
            float f = (float)this.rendererUpdateCount + partialTicks;
            worldrenderer.setTranslation(-doubleValue, -secondDoubleValue, -thirdDoubleValue);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            BlockPos.MutableBlockPos blockpos$mutableblockpos = this.rainRenderPos;

            for (int secondIntValue = k - intValue; secondIntValue <= k + intValue; ++secondIntValue)
            {
                for (int thirdIntValue = i - intValue; thirdIntValue <= i + intValue; ++thirdIntValue)
                {
                    int fourthIntValue = (secondIntValue - k + 16) * 32 + thirdIntValue - i + 16;
                    double fourthDoubleValue = (double)this.rainXCoords[fourthIntValue] * 0.5D;
                    double fifthDoubleValue = (double)this.rainYCoords[fourthIntValue] * 0.5D;
                    blockpos$mutableblockpos.set(thirdIntValue, 0, secondIntValue);
                    BiomeGenBase biomegenbase = world.getBiomeGenForCoords(blockpos$mutableblockpos);

                    if (biomegenbase.canRain() || biomegenbase.getEnableSnow())
                    {
                        int fifthIntValue = world.getPrecipitationHeight(blockpos$mutableblockpos).getY();
                        int sixthIntValue = j - intValue;
                        int seventhIntValue = j + intValue;

                        if (sixthIntValue < fifthIntValue)
                        {
                            sixthIntValue = fifthIntValue;
                        }

                        if (seventhIntValue < fifthIntValue)
                        {
                            seventhIntValue = fifthIntValue;
                        }

                        int sixteenthIntValue = fifthIntValue;

                        if (fifthIntValue < l)
                        {
                            sixteenthIntValue = l;
                        }

                        if (sixthIntValue != seventhIntValue)
                        {
                            this.random.setSeed((long)(thirdIntValue * thirdIntValue * 3121 + thirdIntValue * 45238971 ^ secondIntValue * secondIntValue * 418711 + secondIntValue * 13761));
                            blockpos$mutableblockpos.set(thirdIntValue, sixthIntValue, secondIntValue);
                            float floatValue = biomegenbase.getFloatTemperature(blockpos$mutableblockpos);

                            if (world.getWorldChunkManager().getTemperatureAtHeight(floatValue, fifthIntValue) >= 0.15F)
                            {
                                if (eighteenthIntValue != 0)
                                {
                                    if (eighteenthIntValue >= 0)
                                    {
                                        tessellator.draw();
                                    }

                                    eighteenthIntValue = 0;
                                    this.mc.getTextureManager().bindTexture(locationRainPng);
                                    worldrenderer.begin(7, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
                                }

                                double nineteenthDoubleValue = ((double)(this.rendererUpdateCount + thirdIntValue * thirdIntValue * 3121 + thirdIntValue * 45238971 + secondIntValue * secondIntValue * 418711 + secondIntValue * 13761 & 31) + (double)partialTicks) / 32.0D * (3.0D + this.random.nextDouble());
                                double sixthDoubleValue = (double)((float)thirdIntValue + 0.5F) - entity.posX;
                                double seventhDoubleValue = (double)((float)secondIntValue + 0.5F) - entity.posZ;
                                float secondFloatValue = MathHelper.sqrt_double(sixthDoubleValue * sixthDoubleValue + seventhDoubleValue * seventhDoubleValue) / (float)intValue;
                                float thirdFloatValue = ((1.0F - secondFloatValue * secondFloatValue) * 0.5F + 0.5F) * thirtiethFloatValue;
                                blockpos$mutableblockpos.set(thirdIntValue, sixteenthIntValue, secondIntValue);
                                int eighthIntValue = world.getCombinedLight(blockpos$mutableblockpos, 0);
                                int ninthIntValue = eighthIntValue >> 16 & 65535;
                                int tenthIntValue = eighthIntValue & 65535;
                                worldrenderer.pos((double)thirdIntValue - fourthDoubleValue + 0.5D, (double)sixthIntValue, (double)secondIntValue - fifthDoubleValue + 0.5D).tex(0.0D, (double)sixthIntValue * 0.25D + nineteenthDoubleValue).color(1.0F, 1.0F, 1.0F, thirdFloatValue).lightmap(ninthIntValue, tenthIntValue).endVertex();
                                worldrenderer.pos((double)thirdIntValue + fourthDoubleValue + 0.5D, (double)sixthIntValue, (double)secondIntValue + fifthDoubleValue + 0.5D).tex(1.0D, (double)sixthIntValue * 0.25D + nineteenthDoubleValue).color(1.0F, 1.0F, 1.0F, thirdFloatValue).lightmap(ninthIntValue, tenthIntValue).endVertex();
                                worldrenderer.pos((double)thirdIntValue + fourthDoubleValue + 0.5D, (double)seventhIntValue, (double)secondIntValue + fifthDoubleValue + 0.5D).tex(1.0D, (double)seventhIntValue * 0.25D + nineteenthDoubleValue).color(1.0F, 1.0F, 1.0F, thirdFloatValue).lightmap(ninthIntValue, tenthIntValue).endVertex();
                                worldrenderer.pos((double)thirdIntValue - fourthDoubleValue + 0.5D, (double)seventhIntValue, (double)secondIntValue - fifthDoubleValue + 0.5D).tex(0.0D, (double)seventhIntValue * 0.25D + nineteenthDoubleValue).color(1.0F, 1.0F, 1.0F, thirdFloatValue).lightmap(ninthIntValue, tenthIntValue).endVertex();
                            }
                            else
                            {
                                if (eighteenthIntValue != 1)
                                {
                                    if (eighteenthIntValue >= 0)
                                    {
                                        tessellator.draw();
                                    }

                                    eighteenthIntValue = 1;
                                    this.mc.getTextureManager().bindTexture(locationSnowPng);
                                    worldrenderer.begin(7, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
                                }

                                double twentiethDoubleValue = (double)(((float)(this.rendererUpdateCount & 511) + partialTicks) / 512.0F);
                                double eighthDoubleValue = this.random.nextDouble() + (double)f * 0.01D * (double)((float)this.random.nextGaussian());
                                double ninthDoubleValue = this.random.nextDouble() + (double)(f * (float)this.random.nextGaussian()) * 0.001D;
                                double tenthDoubleValue = (double)((float)thirdIntValue + 0.5F) - entity.posX;
                                double eleventhDoubleValue = (double)((float)secondIntValue + 0.5F) - entity.posZ;
                                float fourthFloatValue = MathHelper.sqrt_double(tenthDoubleValue * tenthDoubleValue + eleventhDoubleValue * eleventhDoubleValue) / (float)intValue;
                                float fifthFloatValue = ((1.0F - fourthFloatValue * fourthFloatValue) * 0.3F + 0.5F) * thirtiethFloatValue;
                                blockpos$mutableblockpos.set(thirdIntValue, sixteenthIntValue, secondIntValue);
                                int eleventhIntValue = (world.getCombinedLight(blockpos$mutableblockpos, 0) * 3 + 15728880) / 4;
                                int twelfthIntValue = eleventhIntValue >> 16 & 65535;
                                int thirteenthIntValue = eleventhIntValue & 65535;
                                worldrenderer.pos((double)thirdIntValue - fourthDoubleValue + 0.5D, (double)sixthIntValue, (double)secondIntValue - fifthDoubleValue + 0.5D).tex(0.0D + eighthDoubleValue, (double)sixthIntValue * 0.25D + twentiethDoubleValue + ninthDoubleValue).color(1.0F, 1.0F, 1.0F, fifthFloatValue).lightmap(twelfthIntValue, thirteenthIntValue).endVertex();
                                worldrenderer.pos((double)thirdIntValue + fourthDoubleValue + 0.5D, (double)sixthIntValue, (double)secondIntValue + fifthDoubleValue + 0.5D).tex(1.0D + eighthDoubleValue, (double)sixthIntValue * 0.25D + twentiethDoubleValue + ninthDoubleValue).color(1.0F, 1.0F, 1.0F, fifthFloatValue).lightmap(twelfthIntValue, thirteenthIntValue).endVertex();
                                worldrenderer.pos((double)thirdIntValue + fourthDoubleValue + 0.5D, (double)seventhIntValue, (double)secondIntValue + fifthDoubleValue + 0.5D).tex(1.0D + eighthDoubleValue, (double)seventhIntValue * 0.25D + twentiethDoubleValue + ninthDoubleValue).color(1.0F, 1.0F, 1.0F, fifthFloatValue).lightmap(twelfthIntValue, thirteenthIntValue).endVertex();
                                worldrenderer.pos((double)thirdIntValue - fourthDoubleValue + 0.5D, (double)seventhIntValue, (double)secondIntValue - fifthDoubleValue + 0.5D).tex(0.0D + eighthDoubleValue, (double)seventhIntValue * 0.25D + twentiethDoubleValue + ninthDoubleValue).color(1.0F, 1.0F, 1.0F, fifthFloatValue).lightmap(twelfthIntValue, thirteenthIntValue).endVertex();
                            }
                        }
                    }
                }
            }

            if (eighteenthIntValue >= 0)
            {
                tessellator.draw();
            }

            worldrenderer.setTranslation(0.0D, 0.0D, 0.0D);
            GlStateManager.enableCull();
            GlStateManager.disableBlend();
            GlStateManager.alphaFunc(516, 0.1F);
            this.disableLightmap();
        }
    }

    public void setupOverlayRendering()
    {
        ScaledResolution scaledResolution = new ScaledResolution(this.mc);
        GlStateManager.clear(256);
        GlStateManager.matrixMode(5889);
        GlStateManager.loadIdentity();
        GlStateManager.ortho(0.0D, scaledResolution.getScaledWidth_double(), scaledResolution.getScaledHeight_double(), 0.0D, 1000.0D, 3000.0D);
        GlStateManager.matrixMode(5888);
        GlStateManager.loadIdentity();
        GlStateManager.translate(0.0F, 0.0F, -2000.0F);
    }

    private void updateFogColor(float partialTicks)
    {
        World world = this.mc.theWorld;
        Entity entity = this.mc.getRenderViewEntity();
        float f = 0.25F + 0.75F * (float)this.mc.gameSettings.renderDistanceChunks / 32.0F;
        f = 1.0F - (float)Math.pow((double)f, 0.25D);
        Vec3 localValue = world.getSkyColor(this.mc.getRenderViewEntity(), partialTicks);
        localValue = CustomColors.getWorldSkyColor(localValue, world, this.mc.getRenderViewEntity(), partialTicks);
        float floatValue = (float)localValue.xCoord;
        float secondFloatValue = (float)localValue.yCoord;
        float thirdFloatValue = (float)localValue.zCoord;
        Vec3 secondLocalValue = world.getFogColor(partialTicks);
        secondLocalValue = CustomColors.getWorldFogColor(secondLocalValue, world, this.mc.getRenderViewEntity(), partialTicks);
        this.fogColorRed = (float)secondLocalValue.xCoord;
        this.fogColorGreen = (float)secondLocalValue.yCoord;
        this.fogColorBlue = (float)secondLocalValue.zCoord;

        if (this.mc.gameSettings.renderDistanceChunks >= 4)
        {
            Vec3 thirdLocalValue = MathHelper.sin(world.getCelestialAngleRadians(partialTicks)) > 0.0F ? FOG_SUN_DIRECTION_NEGATIVE_X : FOG_SUN_DIRECTION_POSITIVE_X;
            float fourthFloatValue = (float)entity.getLook(partialTicks).dotProduct(thirdLocalValue);

            if (fourthFloatValue < 0.0F)
            {
                fourthFloatValue = 0.0F;
            }

            if (fourthFloatValue > 0.0F)
            {
                float[] afloat = world.provider.calcSunriseSunsetColors(world.getCelestialAngle(partialTicks), partialTicks);

                if (afloat != null)
                {
                    fourthFloatValue = fourthFloatValue * afloat[3];
                    this.fogColorRed = this.fogColorRed * (1.0F - fourthFloatValue) + afloat[0] * fourthFloatValue;
                    this.fogColorGreen = this.fogColorGreen * (1.0F - fourthFloatValue) + afloat[1] * fourthFloatValue;
                    this.fogColorBlue = this.fogColorBlue * (1.0F - fourthFloatValue) + afloat[2] * fourthFloatValue;
                }
            }
        }

        this.fogColorRed += (floatValue - this.fogColorRed) * f;
        this.fogColorGreen += (secondFloatValue - this.fogColorGreen) * f;
        this.fogColorBlue += (thirdFloatValue - this.fogColorBlue) * f;
        float fifthFloatValue = world.getRainStrength(partialTicks);

        if (fifthFloatValue > 0.0F)
        {
            float sixthFloatValue = 1.0F - fifthFloatValue * 0.5F;
            float seventhFloatValue = 1.0F - fifthFloatValue * 0.4F;
            this.fogColorRed *= sixthFloatValue;
            this.fogColorGreen *= sixthFloatValue;
            this.fogColorBlue *= seventhFloatValue;
        }

        float number34FloatValue = world.getThunderStrength(partialTicks);

        if (number34FloatValue > 0.0F)
        {
            float eighthFloatValue = 1.0F - number34FloatValue * 0.5F;
            this.fogColorRed *= eighthFloatValue;
            this.fogColorGreen *= eighthFloatValue;
            this.fogColorBlue *= eighthFloatValue;
        }

        Block block = ActiveRenderInfo.getBlockAtEntityViewpoint(this.mc.theWorld, entity, partialTicks);

        if (this.cloudFog)
        {
            Vec3 fourthLocalValue = world.getCloudColour(partialTicks);
            this.fogColorRed = (float)fourthLocalValue.xCoord;
            this.fogColorGreen = (float)fourthLocalValue.yCoord;
            this.fogColorBlue = (float)fourthLocalValue.zCoord;
        }
        else if (block.getMaterial() == Material.water)
        {
            float ninthFloatValue = (float)EnchantmentHelper.getRespiration(entity) * 0.2F;
            ninthFloatValue = Config.limit(ninthFloatValue, 0.0F, 0.6F);

            if (entity instanceof EntityLivingBase && ((EntityLivingBase)entity).isPotionActive(Potion.waterBreathing))
            {
                ninthFloatValue = ninthFloatValue * 0.3F + 0.6F;
            }

            this.fogColorRed = 0.02F + ninthFloatValue;
            this.fogColorGreen = 0.02F + ninthFloatValue;
            this.fogColorBlue = 0.2F + ninthFloatValue;
            Vec3 fifthLocalValue = CustomColors.getUnderwaterColor(this.mc.theWorld, this.mc.getRenderViewEntity().posX, this.mc.getRenderViewEntity().posY + 1.0D, this.mc.getRenderViewEntity().posZ);

            if (fifthLocalValue != null)
            {
                this.fogColorRed = (float)fifthLocalValue.xCoord;
                this.fogColorGreen = (float)fifthLocalValue.yCoord;
                this.fogColorBlue = (float)fifthLocalValue.zCoord;
            }
        }
        else if (block.getMaterial() == Material.lava)
        {
            this.fogColorRed = 0.6F;
            this.fogColorGreen = 0.1F;
            this.fogColorBlue = 0.0F;
            Vec3 sixthLocalValue = CustomColors.getUnderlavaColor(this.mc.theWorld, this.mc.getRenderViewEntity().posX, this.mc.getRenderViewEntity().posY + 1.0D, this.mc.getRenderViewEntity().posZ);

            if (sixthLocalValue != null)
            {
                this.fogColorRed = (float)sixthLocalValue.xCoord;
                this.fogColorGreen = (float)sixthLocalValue.yCoord;
                this.fogColorBlue = (float)sixthLocalValue.zCoord;
            }
        }

        float twentiethFloatValue = this.fogColor2 + (this.fogColor1 - this.fogColor2) * partialTicks;
        this.fogColorRed *= twentiethFloatValue;
        this.fogColorGreen *= twentiethFloatValue;
        this.fogColorBlue *= twentiethFloatValue;
        double secondDoubleValue = (entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * (double)partialTicks) * world.provider.getVoidFogYFactor();

        if (entity instanceof EntityLivingBase && ((EntityLivingBase)entity).isPotionActive(Potion.blindness))
        {
            int i = ((EntityLivingBase)entity).getActivePotionEffect(Potion.blindness).getDuration();

            if (i < 20)
            {
                secondDoubleValue *= (double)(1.0F - (float)i / 20.0F);
            }
            else
            {
                secondDoubleValue = 0.0D;
            }
        }

        if (secondDoubleValue < 1.0D)
        {
            if (secondDoubleValue < 0.0D)
            {
                secondDoubleValue = 0.0D;
            }

            secondDoubleValue = secondDoubleValue * secondDoubleValue;
            this.fogColorRed = (float)((double)this.fogColorRed * secondDoubleValue);
            this.fogColorGreen = (float)((double)this.fogColorGreen * secondDoubleValue);
            this.fogColorBlue = (float)((double)this.fogColorBlue * secondDoubleValue);
        }

        if (this.bossColorModifier > 0.0F)
        {
            float tenthFloatValue = this.bossColorModifierPrev + (this.bossColorModifier - this.bossColorModifierPrev) * partialTicks;
            this.fogColorRed = this.fogColorRed * (1.0F - tenthFloatValue) + this.fogColorRed * 0.7F * tenthFloatValue;
            this.fogColorGreen = this.fogColorGreen * (1.0F - tenthFloatValue) + this.fogColorGreen * 0.6F * tenthFloatValue;
            this.fogColorBlue = this.fogColorBlue * (1.0F - tenthFloatValue) + this.fogColorBlue * 0.6F * tenthFloatValue;
        }

        if (entity instanceof EntityLivingBase && ((EntityLivingBase)entity).isPotionActive(Potion.nightVision))
        {
            float eleventhFloatValue = this.getNightVisionBrightness((EntityLivingBase)entity, partialTicks);
            float twelfthFloatValue = 1.0F / this.fogColorRed;

            if (twelfthFloatValue > 1.0F / this.fogColorGreen)
            {
                twelfthFloatValue = 1.0F / this.fogColorGreen;
            }

            if (twelfthFloatValue > 1.0F / this.fogColorBlue)
            {
                twelfthFloatValue = 1.0F / this.fogColorBlue;
            }

            if (Float.isInfinite(twelfthFloatValue))
            {
                twelfthFloatValue = Math.nextAfter(twelfthFloatValue, 0.0D);
            }

            this.fogColorRed = this.fogColorRed * (1.0F - eleventhFloatValue) + this.fogColorRed * twelfthFloatValue * eleventhFloatValue;
            this.fogColorGreen = this.fogColorGreen * (1.0F - eleventhFloatValue) + this.fogColorGreen * twelfthFloatValue * eleventhFloatValue;
            this.fogColorBlue = this.fogColorBlue * (1.0F - eleventhFloatValue) + this.fogColorBlue * twelfthFloatValue * eleventhFloatValue;
        }

        if (this.mc.gameSettings.anaglyph)
        {
            float thirteenthFloatValue = (this.fogColorRed * 30.0F + this.fogColorGreen * 59.0F + this.fogColorBlue * 11.0F) / 100.0F;
            float fourteenthFloatValue = (this.fogColorRed * 30.0F + this.fogColorGreen * 70.0F) / 100.0F;
            float fifteenthFloatValue = (this.fogColorRed * 30.0F + this.fogColorBlue * 70.0F) / 100.0F;
            this.fogColorRed = thirteenthFloatValue;
            this.fogColorGreen = fourteenthFloatValue;
            this.fogColorBlue = fifteenthFloatValue;
        }

        
        Shaders.setClearColor(this.fogColorRed, this.fogColorGreen, this.fogColorBlue, 0.0F);
    }

    private void setupFog(int startCoords, float partialTicks)
    {
        this.fogStandard = false;
        Entity entity = this.mc.getRenderViewEntity();
        boolean flag = false;

        if (entity instanceof EntityPlayer)
        {
            flag = ((EntityPlayer)entity).capabilities.isCreativeMode;
        }

        GL11.glFog(GL11.GL_FOG_COLOR, (FloatBuffer)this.setFogColorBuffer(this.fogColorRed, this.fogColorGreen, this.fogColorBlue, 1.0F));
        GL11.glNormal3f(0.0F, -1.0F, 0.0F);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        Block block = ActiveRenderInfo.getBlockAtEntityViewpoint(this.mc.theWorld, entity, partialTicks);
        float f = -1.0F;

        
        if (f >= 0.0F)
        {
            GlStateManager.setFogDensity(f);
        }
        else if (entity instanceof EntityLivingBase && ((EntityLivingBase)entity).isPotionActive(Potion.blindness))
        {
            float floatValue = 5.0F;
            int i = ((EntityLivingBase)entity).getActivePotionEffect(Potion.blindness).getDuration();

            if (i < 20)
            {
                floatValue = 5.0F + (this.farPlaneDistance - 5.0F) * (1.0F - (float)i / 20.0F);
            }

            GlStateManager.setFog(9729);

            if (startCoords == -1)
            {
                GlStateManager.setFogStart(0.0F);
                GlStateManager.setFogEnd(floatValue * 0.8F);
            }
            else
            {
                GlStateManager.setFogStart(floatValue * 0.25F);
                GlStateManager.setFogEnd(floatValue);
            }

            if (GLContext.getCapabilities().GL_NV_fog_distance && Config.isFogFancy())
            {
                GL11.glFogi(34138, 34139);
            }
        }
        else if (this.cloudFog)
        {
            GlStateManager.setFog(2048);
            GlStateManager.setFogDensity(0.1F);
        }
        else if (block.getMaterial() == Material.water)
        {
            GlStateManager.setFog(2048);
            float secondFloatValue = Config.isClearWater() ? 0.02F : 0.1F;

            if (entity instanceof EntityLivingBase && ((EntityLivingBase)entity).isPotionActive(Potion.waterBreathing))
            {
                GlStateManager.setFogDensity(0.01F);
            }
            else
            {
                float thirdFloatValue = 0.1F - (float)EnchantmentHelper.getRespiration(entity) * 0.03F;
                GlStateManager.setFogDensity(Config.limit(thirdFloatValue, 0.0F, secondFloatValue));
            }
        }
        else if (block.getMaterial() == Material.lava)
        {
            GlStateManager.setFog(2048);
            GlStateManager.setFogDensity(2.0F);
        }
        else
        {
            float fourthFloatValue = this.farPlaneDistance;
            this.fogStandard = true;
            GlStateManager.setFog(9729);

            if (startCoords == -1)
            {
                GlStateManager.setFogStart(0.0F);
                GlStateManager.setFogEnd(fourthFloatValue);
            }
            else
            {
                GlStateManager.setFogStart(fourthFloatValue * Config.getFogStart());
                GlStateManager.setFogEnd(fourthFloatValue);
            }

            if (GLContext.getCapabilities().GL_NV_fog_distance)
            {
                if (Config.isFogFancy())
                {
                    GL11.glFogi(34138, 34139);
                }

                if (Config.isFogFast())
                {
                    GL11.glFogi(34138, 34140);
                }
            }

            if (this.mc.theWorld.provider.doesXZShowFog((int)entity.posX, (int)entity.posZ))
            {
                GlStateManager.setFogStart(fourthFloatValue * 0.05F);
                GlStateManager.setFogEnd(fourthFloatValue);
            }
        }

        GlStateManager.enableColorMaterial();
        GlStateManager.enableFog();
        GlStateManager.colorMaterial(1028, 4608);
    }

    private FloatBuffer setFogColorBuffer(float red, float green, float blue, float alpha)
    {
        if (Config.isShaders())
        {
            Shaders.setFogColor(red, green, blue);
        }

        this.fogColorBuffer.clear();
        this.fogColorBuffer.put(red).put(green).put(blue).put(alpha);
        this.fogColorBuffer.flip();
        return this.fogColorBuffer;
    }

    public MapItemRenderer getMapItemRenderer()
    {
        return this.theMapItemRenderer;
    }

    private void waitForServerThread()
    {
        this.serverWaitTimeCurrent = 0;

        if (Config.isSmoothWorld() && Config.isSingleProcessor())
        {
            if (this.mc.isIntegratedServerRunning())
            {
                IntegratedServer integratedServer = this.mc.getIntegratedServer();

                if (integratedServer != null)
                {
                    boolean flag = this.mc.isGamePaused();

                    if (!flag && !(this.mc.currentScreen instanceof GuiDownloadTerrain))
                    {
                        if (this.serverWaitTime > 0)
                        {
                            Lagometer.timerServer.start();
                            Config.sleep((long)this.serverWaitTime);
                            Lagometer.timerServer.end();
                            this.serverWaitTimeCurrent = this.serverWaitTime;
                        }

                        long i = System.nanoTime() / 1000000L;

                        if (this.lastServerTime != 0L && this.lastServerTicks != 0)
                        {
                            long j = i - this.lastServerTime;

                            if (j < 0L)
                            {
                                this.lastServerTime = i;
                                j = 0L;
                            }

                            if (j >= 50L)
                            {
                                this.lastServerTime = i;
                                int k = integratedServer.getTickCounter();
                                int l = k - this.lastServerTicks;

                                if (l < 0)
                                {
                                    this.lastServerTicks = k;
                                    l = 0;
                                }

                                if (l < 1 && this.serverWaitTime < 100)
                                {
                                    this.serverWaitTime += 2;
                                }

                                if (l > 1 && this.serverWaitTime > 0)
                                {
                                    --this.serverWaitTime;
                                }

                                this.lastServerTicks = k;
                            }
                        }
                        else
                        {
                            this.lastServerTime = i;
                            this.lastServerTicks = integratedServer.getTickCounter();
                            this.avgServerTickDiff = 1.0F;
                            this.avgServerTimeDiff = 50.0F;
                        }
                    }
                    else
                    {
                        if (this.mc.currentScreen instanceof GuiDownloadTerrain)
                        {
                            Config.sleep(20L);
                        }

                        this.lastServerTime = 0L;
                        this.lastServerTicks = 0;
                    }
                }
            }
        }
        else
        {
            this.lastServerTime = 0L;
            this.lastServerTicks = 0;
        }
    }

    private void frameInit()
    {
        GlErrors.frameStart();

        if (!this.initialized)
        {
            ReflectorResolver.resolve();
            TextureUtils.registerResourceListener();

            if (Config.getBitsOs() == 64 && Config.getBitsJre() == 32)
            {
                Config.setNotify64BitJava(true);
            }

            this.initialized = true;
        }

        Config.checkDisplayMode();
        World world = this.mc.theWorld;

        if (world != null)
        {
            if (Config.getNewRelease() != null)
            {
                String s = "HD_U".replace("HD_U", "HD Ultra").replace("L", "Light");
                String text = s + " " + Config.getNewRelease();
                ChatComponentText chatComponentText = new ChatComponentText(I18n.format("of.message.newVersion", new Object[] {"\u00a7n" + text + "\u00a7r"}));
                this.mc.ingameGUI.getChatGUI().printChatMessage(chatComponentText);
                Config.setNewRelease((String)null);
            }

            if (Config.isNotify64BitJava())
            {
                Config.setNotify64BitJava(false);
                ChatComponentText chatcomponenttext1 = new ChatComponentText(I18n.format("of.message.java64Bit", new Object[0]));
                this.mc.ingameGUI.getChatGUI().printChatMessage(chatcomponenttext1);
            }
        }

        if (this.mc.currentScreen instanceof GuiMainMenu)
        {
            this.updateMainMenu((GuiMainMenu)this.mc.currentScreen);
        }

        if (this.updatedWorld != world)
        {
            RandomEntities.worldChanged(this.updatedWorld, world);
            Config.updateThreadPriorities();
            this.lastServerTime = 0L;
            this.lastServerTicks = 0;
            this.updatedWorld = world;
        }

        if (!this.setFxaaShader(Shaders.configAntialiasingLevel))
        {
            Shaders.configAntialiasingLevel = 0;
        }

        if (this.mc.currentScreen != null && this.mc.currentScreen.getClass() == GuiChat.class)
        {
            this.mc.displayGuiScreen(new GuiChatOF((GuiChat)this.mc.currentScreen));
        }
    }

    private void frameFinish()
    {
        if (this.mc.theWorld != null && Config.isShowGlErrors() && TimedEvent.isActive("CheckGlErrorFrameFinish", 10000L))
        {
            int i = GlStateManager.glGetError();

            if (i != 0 && GlErrors.isEnabled(i))
            {
                String s = Config.getGlErrorString(i);
                ChatComponentText chatComponentText = new ChatComponentText(I18n.format("of.message.openglError", new Object[] {Integer.valueOf(i), s}));
                this.mc.ingameGUI.getChatGUI().printChatMessage(chatComponentText);
            }
        }
    }

    private void updateMainMenu(GuiMainMenu mainMenu)
    {
        try
        {
            String s = null;
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            int i = calendar.get(5);
            int j = calendar.get(2) + 1;

            if (i == 8 && j == 4)
            {
                s = "Happy birthday, OptiFine!";
            }

            if (i == 14 && j == 8)
            {
                s = "Happy birthday, sp614x!";
            }

            if (s == null)
            {
                return;
            }

            Reflector.setFieldValue(mainMenu, Reflector.GuiMainMenu_splashText, s);
        }
        catch (Throwable ignored)
        {
            ;
        }
    }

    public boolean setFxaaShader(int fxaaLevel)
    {
        if (!OpenGlHelper.isFramebufferEnabled())
        {
            return false;
        }
        else if (this.theShaderGroup != null && this.theShaderGroup != this.fxaaShaders[2] && this.theShaderGroup != this.fxaaShaders[4])
        {
            return true;
        }
        else if (fxaaLevel != 2 && fxaaLevel != 4)
        {
            if (this.theShaderGroup == null)
            {
                return true;
            }
            else
            {
                this.theShaderGroup.deleteShaderGroup();
                this.theShaderGroup = null;
                return true;
            }
        }
        else if (this.theShaderGroup != null && this.theShaderGroup == this.fxaaShaders[fxaaLevel])
        {
            return true;
        }
        else if (this.mc.theWorld == null)
        {
            return true;
        }
        else
        {
            this.loadShader(new ResourceLocation("shaders/post/fxaa_of_" + fxaaLevel + "x.json"));
            this.fxaaShaders[fxaaLevel] = this.theShaderGroup;
            return this.useShader;
        }
    }

    private void checkLoadVisibleChunks(Entity renderViewEntity, float partialTicks, ICamera camera, boolean playerSpectator)
    {
        int i = 201435902;

        if (this.loadVisibleChunks)
        {
            this.loadVisibleChunks = false;
            this.loadAllVisibleChunks(renderViewEntity, (double)partialTicks, camera, playerSpectator);
            this.mc.ingameGUI.getChatGUI().deleteChatLine(i);
        }

        if (Keyboard.isKeyDown(61) && Keyboard.isKeyDown(38))
        {
            if (this.mc.currentScreen != null)
            {
                return;
            }

            this.loadVisibleChunks = true;
            ChatComponentText chatComponentText = new ChatComponentText(I18n.format("of.message.loadingVisibleChunks", new Object[0]));
            this.mc.ingameGUI.getChatGUI().printChatMessageWithOptionalDeletion(chatComponentText, i);
        }
    }

    private void loadAllVisibleChunks(Entity renderViewEntity, double partialTicks, ICamera camera, boolean playerSpectator)
    {
        int i = this.mc.gameSettings.ofChunkUpdates;
        boolean flag = this.mc.gameSettings.ofLazyChunkLoading;

        try
        {
            this.mc.gameSettings.ofChunkUpdates = 1000;
            this.mc.gameSettings.ofLazyChunkLoading = false;
            RenderGlobal renderGlobal = Config.getRenderGlobal();
            int j = renderGlobal.getCountLoadedChunks();
            long k = System.currentTimeMillis();
            Config.dbg("Loading visible chunks");
            long l = System.currentTimeMillis() + 5000L;
            int intValue2 = 0;
            boolean flag1 = false;

            while (true)
            {
                flag1 = false;

                for (int innerIndex = 0; innerIndex < 100; ++innerIndex)
                {
                    renderGlobal.displayListEntitiesDirty = true;
                    renderGlobal.setupTerrain(renderViewEntity, partialTicks, camera, this.frameCount++, playerSpectator);

                    if (!renderGlobal.hasNoChunkUpdates())
                    {
                        flag1 = true;
                    }

                    intValue2 = intValue2 + renderGlobal.getCountChunksToUpdate();

                    while (!renderGlobal.hasNoChunkUpdates())
                    {
                        renderGlobal.updateChunks(System.nanoTime() + 1000000000L);
                    }

                    intValue2 = intValue2 - renderGlobal.getCountChunksToUpdate();

                    if (!flag1)
                    {
                        break;
                    }
                }

                if (renderGlobal.getCountLoadedChunks() != j)
                {
                    flag1 = true;
                    j = renderGlobal.getCountLoadedChunks();
                }

                if (System.currentTimeMillis() > l)
                {
                    Config.log("Chunks loaded: " + intValue2);
                    l = System.currentTimeMillis() + 5000L;
                }

                if (!flag1)
                {
                    break;
                }
            }

            Config.log("Chunks loaded: " + intValue2);
            Config.log("Finished loading visible chunks");
            RenderChunk.renderChunksUpdated = 0;
        }
        finally
        {
            this.mc.gameSettings.ofChunkUpdates = i;
            this.mc.gameSettings.ofLazyChunkLoading = flag;
        }
    }
}
