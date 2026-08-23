package net.minecraft.client.renderer;

import com.google.common.collect.Lists;
import optimization.FastTrig;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonSyntaxException;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Callable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockChest;
import net.minecraft.block.BlockEnderChest;
import net.minecraft.block.BlockSign;
import net.minecraft.block.BlockSkull;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.client.renderer.chunk.CompiledChunk;
import net.minecraft.client.renderer.chunk.IRenderChunkFactory;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.client.renderer.chunk.VboChunkFactory;
import net.minecraft.client.renderer.chunk.VisGraph;
import net.minecraft.client.renderer.culling.ClippingHelper;
import net.minecraft.client.renderer.culling.ClippingHelperImpl;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.RenderItemFrame;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySignRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.client.shader.ShaderGroup;
import net.minecraft.client.shader.ShaderLinkHelper;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityWitherSkull;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemRecord;
import net.minecraft.src.Config;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ClassInheritanceMultiMap;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.util.LongHashMap;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Matrix4f;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.ReportedException;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraft.util.Vector3d;
import net.minecraft.world.IWorldAccess;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.optifine.CustomColors;
import net.optifine.CustomSky;
import net.optifine.DynamicLights;
import net.optifine.Lagometer;
import net.optifine.RandomEntities;
import net.optifine.SmartAnimations;
import net.optifine.model.BlockModelUtils;
import net.optifine.reflect.Reflector;
import net.optifine.render.ChunkVisibility;
import net.optifine.render.CloudRenderer;
import net.optifine.render.RenderEnv;
import net.optifine.shaders.Shaders;
import net.optifine.shaders.ShadersRender;
import net.optifine.shaders.ShadowUtils;
import net.optifine.shaders.gui.GuiShaderOptions;
import net.optifine.util.ChunkUtils;
import net.optifine.util.RenderChunkUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.minecraft.client.GameWindow;
import org.lwjgl.opengl.GL11;
import net.minecraft.util.vector.Vector3f;
import net.minecraft.util.vector.Vector4f;

public class RenderGlobal implements IWorldAccess, IResourceManagerReloadListener
{
    private static final Logger logger = LogManager.getLogger();
    private static final ResourceLocation locationMoonPhasesPng = new ResourceLocation("textures/environment/moon_phases.png");
    private static final ResourceLocation locationSunPng = new ResourceLocation("textures/environment/sun.png");
    private static final ResourceLocation locationCloudsPng = new ResourceLocation("textures/environment/clouds.png");
    private static final ResourceLocation locationEndSkyPng = new ResourceLocation("textures/environment/end_sky.png");
    private static final ResourceLocation locationForcefieldPng = new ResourceLocation("textures/misc/forcefield.png");
    private static final String[] RENDER_LAYER_PROFILER_NAMES = new String[] {"render_Solid", "render_Mipped Cutout", "render_Cutout", "render_Translucent"};
    public final Minecraft mc;
    private final TextureManager renderEngine;
    private final RenderManager renderManager;
    private WorldClient theWorld;
    private Set<RenderChunk> chunksToUpdate = Sets.<RenderChunk>newLinkedHashSet();
    private Set<RenderChunk> chunksToUpdateScratch = Sets.<RenderChunk>newLinkedHashSet();
    private List<RenderGlobal.ContainerLocalRenderInformation> renderInfos = Lists.<RenderGlobal.ContainerLocalRenderInformation>newArrayListWithCapacity(69696);
    private final Set<TileEntity> setTileEntities = Sets.<TileEntity>newHashSet();
    private ViewFrustum viewFrustum;
    private VertexFormat vertexBufferFormat;
    private VertexBuffer starVBO;
    private VertexBuffer skyVBO;
    private VertexBuffer sky2VBO;
    private int cloudTickCounter;
    public final Map<Integer, DestroyBlockProgress> damagedBlocks = Maps.<Integer, DestroyBlockProgress>newHashMap();
    private final Map<BlockPos, ISound> mapSoundPositions = Maps.<BlockPos, ISound>newHashMap();
    private final TextureAtlasSprite[] destroyBlockIcons = new TextureAtlasSprite[10];
    private Framebuffer entityOutlineFramebuffer;
    private ShaderGroup entityOutlineShader;
    private double frustumUpdatePosX = Double.MIN_VALUE;
    private double frustumUpdatePosY = Double.MIN_VALUE;
    private double frustumUpdatePosZ = Double.MIN_VALUE;
    private int frustumUpdatePosChunkX = Integer.MIN_VALUE;
    private int frustumUpdatePosChunkY = Integer.MIN_VALUE;
    private int frustumUpdatePosChunkZ = Integer.MIN_VALUE;
    private double lastViewEntityX = Double.MIN_VALUE;
    private double lastViewEntityY = Double.MIN_VALUE;
    private double lastViewEntityZ = Double.MIN_VALUE;
    private double lastViewEntityPitch = Double.MIN_VALUE;
    private double lastViewEntityYaw = Double.MIN_VALUE;
    private final ChunkRenderDispatcher renderDispatcher = new ChunkRenderDispatcher();
    private ChunkRenderContainer renderContainer;
    private int renderDistanceChunks = -1;
    private int renderEntitiesStartupCounter = 2;
    private int countEntitiesTotal;
    private int countEntitiesRendered;
    private int countEntitiesHidden;
    private boolean debugFixTerrainFrustum = false;
    private ClippingHelper debugFixedClippingHelper;
    private final Vector4f[] debugTerrainMatrix = new Vector4f[8];
    private final Vector3d debugTerrainFrustumPosition = new Vector3d();
    IRenderChunkFactory renderChunkFactory;
    private double prevRenderSortX;
    private double prevRenderSortY;
    private double prevRenderSortZ;
    public boolean displayListEntitiesDirty = true;
    private CloudRenderer cloudRenderer;
    public Entity renderedEntity;
    public Set chunksToResortTransparency = new LinkedHashSet();
    public Set chunksToUpdateForced = new LinkedHashSet();
    private Deque visibilityDeque = new ArrayDeque();
    private List renderInfosEntities = new ArrayList(1024);
    private List renderInfosTileEntities = new ArrayList(1024);
    private List renderInfosNormal = new ArrayList(1024);
    private List renderInfosEntitiesNormal = new ArrayList(1024);
    private List renderInfosTileEntitiesNormal = new ArrayList(1024);
    private List renderInfosShadow = new ArrayList(1024);
    private List renderInfosEntitiesShadow = new ArrayList(1024);
    private List renderInfosTileEntitiesShadow = new ArrayList(1024);
    private int renderDistance = 0;
    private int renderDistanceSq = 0;
    private static final Set SET_ALL_FACINGS = Collections.unmodifiableSet(new HashSet(Arrays.asList(EnumFacing.VALUES)));
    private int countTileEntitiesRendered;
    private IChunkProvider worldChunkProvider = null;
    private LongHashMap worldChunkProviderMap = null;
    private int countLoadedChunksPrev = 0;
    private RenderEnv renderEnv = new RenderEnv(Blocks.air.getDefaultState(), new BlockPos(0, 0, 0));
    private final BlockPos.MutableBlockPos entityBlockCheckPos = new BlockPos.MutableBlockPos();
    private final BlockPos.MutableBlockPos terrainViewPos = new BlockPos.MutableBlockPos();
    private final BlockPos.MutableBlockPos terrainLookupPos = new BlockPos.MutableBlockPos();
    public boolean renderOverlayDamaged = false;
    public boolean renderOverlayEyes = false;
    private boolean firstWorldLoad = false;
    private static int renderEntitiesCounter = 0;

    public RenderGlobal(Minecraft mcIn)
    {
        this.cloudRenderer = new CloudRenderer(mcIn);
        this.mc = mcIn;
        this.renderManager = mcIn.getRenderManager();
        this.renderEngine = mcIn.getTextureManager();
        this.renderEngine.bindTexture(locationForcefieldPng);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
        GlStateManager.bindTexture(0);
        this.updateDestroyBlockIcons();
        this.renderContainer = new VboRenderList();
        this.renderChunkFactory = new VboChunkFactory();

        this.vertexBufferFormat = new VertexFormat();
        this.vertexBufferFormat.addElement(new VertexFormatElement(0, VertexFormatElement.EnumType.FLOAT, VertexFormatElement.EnumUsage.POSITION, 3));
        this.generateStars();
        this.generateSky();
        this.generateSky2();
    }

    public void onResourceManagerReload(IResourceManager resourceManager)
    {
        this.updateDestroyBlockIcons();
    }

    private void updateDestroyBlockIcons()
    {
        TextureMap textureMap = this.mc.getTextureMapBlocks();

        for (int i = 0; i < this.destroyBlockIcons.length; ++i)
        {
            this.destroyBlockIcons[i] = textureMap.getAtlasSprite("minecraft:blocks/destroy_stage_" + i);
        }
    }

    public void makeEntityOutlineShader()
    {
        if (OpenGlHelper.shadersSupported)
        {
            if (ShaderLinkHelper.getStaticShaderLinkHelper() == null)
            {
                ShaderLinkHelper.setNewStaticShaderLinkHelper();
            }

            ResourceLocation resourcelocation = new ResourceLocation("shaders/post/entity_outline.json");

            try
            {
                this.entityOutlineShader = new ShaderGroup(this.mc.getTextureManager(), this.mc.getResourceManager(), this.mc.getFramebuffer(), resourcelocation);
                this.entityOutlineShader.createBindFramebuffers(this.mc.displayWidth, this.mc.displayHeight);
                this.entityOutlineFramebuffer = this.entityOutlineShader.getFramebufferRaw("final");
            }
            catch (IOException iOException)
            {
                logger.warn((String)("Failed to load shader: " + resourcelocation), (Throwable)iOException);
                this.entityOutlineShader = null;
                this.entityOutlineFramebuffer = null;
            }
            catch (JsonSyntaxException jsonSyntaxException)
            {
                logger.warn((String)("Failed to load shader: " + resourcelocation), (Throwable)jsonSyntaxException);
                this.entityOutlineShader = null;
                this.entityOutlineFramebuffer = null;
            }
        }
        else
        {
            this.entityOutlineShader = null;
            this.entityOutlineFramebuffer = null;
        }
    }

    public void renderEntityOutlineFramebuffer()
    {
        if (this.isRenderEntityOutlines())
        {
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1);
            this.entityOutlineFramebuffer.framebufferRenderExt(this.mc.displayWidth, this.mc.displayHeight, false);
            GlStateManager.disableBlend();
        }
    }

    protected boolean isRenderEntityOutlines()
    {
        return !Config.isFastRender() && !Config.isShaders() && !Config.isAntialiasing() ? this.entityOutlineFramebuffer != null && this.entityOutlineShader != null && this.mc.thePlayer != null && this.mc.thePlayer.isSpectator() && this.mc.gameSettings.keyBindSpectatorOutlines.isKeyDown() : false;
    }

    private void generateSky2()
    {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldRenderer = tessellator.getWorldRenderer();

        if (this.sky2VBO != null)
        {
            this.sky2VBO.deleteGlBuffers();
        }

        this.sky2VBO = new VertexBuffer(this.vertexBufferFormat);
        this.renderSky(worldRenderer, -16.0F, true);
        worldRenderer.finishDrawing();
        this.sky2VBO.bufferData(worldRenderer.getByteBuffer());
        worldRenderer.reset();
    }

    private void generateSky()
    {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldRenderer = tessellator.getWorldRenderer();

        if (this.skyVBO != null)
        {
            this.skyVBO.deleteGlBuffers();
        }

        this.skyVBO = new VertexBuffer(this.vertexBufferFormat);
        this.renderSky(worldRenderer, 16.0F, false);
        worldRenderer.finishDrawing();
        this.skyVBO.bufferData(worldRenderer.getByteBuffer());
        worldRenderer.reset();
    }

    private void drawPositionVbo(VertexBuffer buffer)
    {
        buffer.bindDrawState();
        CorePipeline.prepareDraw(false, false);
        buffer.drawArrays(7);
    }

    private void renderSky(WorldRenderer worldRendererIn, float posY, boolean reverseX)
    {
        int i = 64;
        int j = 6;
        worldRendererIn.begin(7, DefaultVertexFormats.POSITION);
        int k = (this.renderDistance / 64 + 1) * 64 + 64;

        for (int l = -k; l <= k; l += 64)
        {
            for (int index = -k; index <= k; index += 64)
            {
                float f = (float)l;
                float floatValue2 = (float)(l + 64);

                if (reverseX)
                {
                    floatValue2 = (float)l;
                    f = (float)(l + 64);
                }

                worldRendererIn.pos((double)f, (double)posY, (double)index).endVertex();
                worldRendererIn.pos((double)floatValue2, (double)posY, (double)index).endVertex();
                worldRendererIn.pos((double)floatValue2, (double)posY, (double)(index + 64)).endVertex();
                worldRendererIn.pos((double)f, (double)posY, (double)(index + 64)).endVertex();
            }
        }
    }

    private void generateStars()
    {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldRenderer = tessellator.getWorldRenderer();

        if (this.starVBO != null)
        {
            this.starVBO.deleteGlBuffers();
        }

        this.starVBO = new VertexBuffer(this.vertexBufferFormat);
        this.renderStars(worldRenderer);
        worldRenderer.finishDrawing();
        this.starVBO.bufferData(worldRenderer.getByteBuffer());
        worldRenderer.reset();
    }

    private void renderStars(WorldRenderer worldRendererIn)
    {
        Random random = new Random(10842L);
        worldRendererIn.begin(7, DefaultVertexFormats.POSITION);

        for (int i = 0; i < 1500; ++i)
        {
            double randomX = (double)(random.nextFloat() * 2.0F - 1.0F);
            double randomY = (double)(random.nextFloat() * 2.0F - 1.0F);
            double randomZ = (double)(random.nextFloat() * 2.0F - 1.0F);
            double starSize = (double)(0.15F + random.nextFloat() * 0.1F);
            double distanceSq = randomX * randomX + randomY * randomY + randomZ * randomZ;

            if (distanceSq < 1.0D && distanceSq > 0.01D)
            {
                double inverseDistance = MathHelper.fastInvSqrt(distanceSq);
                randomX = randomX * inverseDistance;
                randomY = randomY * inverseDistance;
                randomZ = randomZ * inverseDistance;
                double starX = randomX * 100.0D;
                double starY = randomY * 100.0D;
                double starZ = randomZ * 100.0D;
                double yaw = FastTrig.atan2(randomX, randomZ);
                double sinYaw = Math.sin(yaw);
                double cosYaw = Math.cos(yaw);
                double pitch = FastTrig.atan2(MathHelper.fastSqrt_double(randomX * randomX + randomZ * randomZ), randomY);
                double sinPitch = Math.sin(pitch);
                double cosPitch = Math.cos(pitch);
                double roll = random.nextDouble() * Math.PI * 2.0D;
                double sinRoll = Math.sin(roll);
                double cosRoll = Math.cos(roll);

                for (int j = 0; j < 4; ++j)
                {
                    double cornerX = (double)((j & 2) - 1) * starSize;
                    double cornerY = (double)((j + 1 & 2) - 1) * starSize;
                    double rotatedX = cornerX * cosRoll - cornerY * sinRoll;
                    double rotatedY = cornerY * cosRoll + cornerX * sinRoll;
                    double pitchedY = rotatedX * sinPitch;
                    double pitchedZ = -rotatedX * cosPitch;
                    double finalX = pitchedZ * sinYaw - rotatedY * cosYaw;
                    double finalZ = rotatedY * sinYaw + pitchedZ * cosYaw;
                    worldRendererIn.pos(starX + finalX, starY + pitchedY, starZ + finalZ).endVertex();
                }
            }
        }
    }

    public void setWorldAndLoadRenderers(WorldClient worldClientIn)
    {
        if (this.theWorld != null)
        {
            this.theWorld.removeWorldAccess(this);
        }

        this.frustumUpdatePosX = Double.MIN_VALUE;
        this.frustumUpdatePosY = Double.MIN_VALUE;
        this.frustumUpdatePosZ = Double.MIN_VALUE;
        this.frustumUpdatePosChunkX = Integer.MIN_VALUE;
        this.frustumUpdatePosChunkY = Integer.MIN_VALUE;
        this.frustumUpdatePosChunkZ = Integer.MIN_VALUE;
        this.renderManager.set(worldClientIn);
        this.theWorld = worldClientIn;

        if (Config.isDynamicLights())
        {
            DynamicLights.clear();
        }

        ChunkVisibility.reset();
        this.worldChunkProvider = null;
        this.worldChunkProviderMap = null;
        this.renderEnv.reset((IBlockState)null, (BlockPos)null);
        Shaders.checkWorldChanged(this.theWorld);

        if (worldClientIn != null)
        {
            worldClientIn.addWorldAccess(this);
            this.loadRenderers();
        }
        else
        {
            this.chunksToUpdate.clear();
            this.chunksToUpdateScratch.clear();
            this.clearRenderInfos();

            if (this.viewFrustum != null)
            {
                this.viewFrustum.deleteGlResources();
            }

            this.viewFrustum = null;
        }
    }

    public void loadRenderers()
    {
        if (this.theWorld != null)
        {
            this.displayListEntitiesDirty = true;
            Blocks.leaves.setGraphicsLevel(Config.isTreesFancy());
            Blocks.leaves2.setGraphicsLevel(Config.isTreesFancy());
            BlockModelRenderer.updateAoLightValue();

            if (Config.isDynamicLights())
            {
                DynamicLights.clear();
            }

            SmartAnimations.update();
            this.renderDistanceChunks = this.mc.gameSettings.renderDistanceChunks;
            this.renderDistance = this.renderDistanceChunks * 16;
            this.renderDistanceSq = this.renderDistance * this.renderDistance;
            this.renderContainer = new VboRenderList();
            this.renderChunkFactory = new VboChunkFactory();

            this.generateStars();
            this.generateSky();
            this.generateSky2();

            if (this.viewFrustum != null)
            {
                this.viewFrustum.deleteGlResources();
            }

            this.stopChunkUpdates();

            synchronized (this.setTileEntities)
            {
                this.setTileEntities.clear();
            }

            this.viewFrustum = new ViewFrustum(this.theWorld, this.mc.gameSettings.renderDistanceChunks, this, this.renderChunkFactory);

            if (this.theWorld != null)
            {
                Entity entity = this.mc.getRenderViewEntity();

                if (entity != null)
                {
                    this.viewFrustum.updateChunkPositions(entity.posX, entity.posZ);
                }
            }

            this.renderEntitiesStartupCounter = 2;
        }

        if (this.mc.thePlayer == null)
        {
            this.firstWorldLoad = true;
        }
    }

    protected void stopChunkUpdates()
    {
        this.chunksToUpdate.clear();
        this.chunksToUpdateScratch.clear();
        this.renderDispatcher.stopChunkUpdates();
    }

    public void createBindEntityOutlineFbs(int width, int height)
    {
        if (OpenGlHelper.shadersSupported && this.entityOutlineShader != null)
        {
            this.entityOutlineShader.createBindFramebuffers(width, height);
        }
    }

    public void renderEntities(Entity renderViewEntity, ICamera camera, float partialTicks)
    {
        int i = 0;

        if (this.renderEntitiesStartupCounter > 0)
        {
            --this.renderEntitiesStartupCounter;
        }
        else
        {
            double doubleValue = renderViewEntity.prevPosX + (renderViewEntity.posX - renderViewEntity.prevPosX) * (double)partialTicks;
            double secondDoubleValue = renderViewEntity.prevPosY + (renderViewEntity.posY - renderViewEntity.prevPosY) * (double)partialTicks;
            double thirdDoubleValue = renderViewEntity.prevPosZ + (renderViewEntity.posZ - renderViewEntity.prevPosZ) * (double)partialTicks;
            this.theWorld.theProfiler.startSection("prepare");
            TileEntityRendererDispatcher.instance.cacheActiveRenderInfo(this.theWorld, this.mc.getTextureManager(), this.mc.fontRendererObj, this.mc.getRenderViewEntity(), partialTicks);
            this.renderManager.cacheActiveRenderInfo(this.theWorld, this.mc.fontRendererObj, this.mc.getRenderViewEntity(), this.mc.pointedEntity, this.mc.gameSettings, partialTicks);
            ++renderEntitiesCounter;

            if (i == 0)
            {
                this.countEntitiesTotal = 0;
                this.countEntitiesRendered = 0;
                this.countEntitiesHidden = 0;
                this.countTileEntitiesRendered = 0;
            }

            Entity entity = this.mc.getRenderViewEntity();
            double fourthDoubleValue = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * (double)partialTicks;
            double fifthDoubleValue = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * (double)partialTicks;
            double sixthDoubleValue = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * (double)partialTicks;
            TileEntityRendererDispatcher.staticPlayerX = fourthDoubleValue;
            TileEntityRendererDispatcher.staticPlayerY = fifthDoubleValue;
            TileEntityRendererDispatcher.staticPlayerZ = sixthDoubleValue;
            this.renderManager.setRenderPosition(fourthDoubleValue, fifthDoubleValue, sixthDoubleValue);
            this.mc.entityRenderer.enableLightmap();
            this.theWorld.theProfiler.endStartSection("global");
            List<Entity> list = this.theWorld.getLoadedEntityList();

            if (i == 0)
            {
                this.countEntitiesTotal = list.size();
            }

            if (Config.isFogOff() && this.mc.entityRenderer.fogStandard)
            {
                GlStateManager.disableFog();
            }

            for (int j = 0; j < this.theWorld.weatherEffects.size(); ++j)
            {
                Entity entity1 = this.theWorld.weatherEffects.get(j);
                ++this.countEntitiesRendered;

                if (entity1.isInRangeToRender3d(doubleValue, secondDoubleValue, thirdDoubleValue))
                {
                    this.renderManager.renderEntitySimple(entity1, partialTicks);
                }
            }

            if (this.isRenderEntityOutlines())
            {
                GlStateManager.depthFunc(519);
                GlStateManager.disableFog();
                this.entityOutlineFramebuffer.framebufferClear();
                this.entityOutlineFramebuffer.bindFramebuffer(false);
                this.theWorld.theProfiler.endStartSection("entityOutlines");
                RenderHelper.disableStandardItemLighting();
                this.renderManager.setRenderOutlines(true);

                for (int k = 0; k < list.size(); ++k)
                {
                    Entity entity3 = list.get(k);
                    boolean flag2 = this.mc.getRenderViewEntity() instanceof EntityLivingBase && ((EntityLivingBase)this.mc.getRenderViewEntity()).isPlayerSleeping();
                    boolean flag3 = entity3.isInRangeToRender3d(doubleValue, secondDoubleValue, thirdDoubleValue) && (entity3.ignoreFrustumCheck || camera.isBoundingBoxInFrustum(entity3.getEntityBoundingBox()) || entity3.riddenByEntity == this.mc.thePlayer) && entity3 instanceof EntityPlayer;

                    if ((entity3 != this.mc.getRenderViewEntity() || this.mc.gameSettings.thirdPersonView != 0 || flag2) && flag3)
                    {
                        this.renderManager.renderEntitySimple(entity3, partialTicks);
                    }
                }

                this.renderManager.setRenderOutlines(false);
                RenderHelper.enableStandardItemLighting();
                GlStateManager.depthMask(false);
                this.entityOutlineShader.loadShaderGroup(partialTicks);
                GlStateManager.enableLighting();
                GlStateManager.depthMask(true);
                this.mc.getFramebuffer().bindFramebuffer(false);
                GlStateManager.enableFog();
                GlStateManager.enableBlend();
                GlStateManager.enableColorMaterial();
                GlStateManager.depthFunc(515);
                GlStateManager.enableDepth();
                GlStateManager.enableAlpha();
            }

            this.theWorld.theProfiler.endStartSection("entities");
            boolean flag6 = Config.isShaders();

            if (flag6)
            {
                Shaders.beginEntities();
            }

            RenderItemFrame.updateItemRenderDistance();
            boolean flag7 = this.mc.gameSettings.fancyGraphics;
            this.mc.gameSettings.fancyGraphics = Config.isDroppedItemsFancy();
            boolean flag8 = Shaders.isShadowPass && !this.mc.thePlayer.isSpectator();
            label926:

            for (Object o : this.renderInfosEntities)
            {
                RenderGlobal.ContainerLocalRenderInformation renderglobal$containerlocalrenderinformation = (ContainerLocalRenderInformation) o;
                Chunk chunk = renderglobal$containerlocalrenderinformation.renderChunk.getChunk();
                ClassInheritanceMultiMap<Entity> classinheritancemultimap = chunk.getEntityLists()[renderglobal$containerlocalrenderinformation.renderChunk.getPosition().getY() / 16];

                if (!classinheritancemultimap.isEmpty())
                {
                    Iterator iterator = classinheritancemultimap.iterator();

                    while (true)
                    {
                        Entity entity2;
                        boolean flag4;

                        while (true)
                        {
                            if (!iterator.hasNext())
                            {
                                continue label926;
                            }

                            entity2 = (Entity)iterator.next();
                            flag4 = this.renderManager.shouldRender(entity2, camera, doubleValue, secondDoubleValue, thirdDoubleValue) || entity2.riddenByEntity == this.mc.thePlayer;

                            if (!flag4)
                            {
                                break;
                            }

                            boolean flag5 = this.mc.getRenderViewEntity() instanceof EntityLivingBase ? ((EntityLivingBase)this.mc.getRenderViewEntity()).isPlayerSleeping() : false;

                            if ((entity2 != this.mc.getRenderViewEntity() || flag8 || this.mc.gameSettings.thirdPersonView != 0 || flag5) && (entity2.posY < 0.0D || entity2.posY >= 256.0D || this.theWorld.isBlockLoaded(this.entityBlockCheckPos.set(MathHelper.floor_double(entity2.posX), MathHelper.floor_double(entity2.posY), MathHelper.floor_double(entity2.posZ)))))
                            {
                                ++this.countEntitiesRendered;
                                this.renderedEntity = entity2;

                                if (flag6)
                                {
                                    Shaders.nextEntity(entity2);
                                }

                                this.renderManager.renderEntitySimple(entity2, partialTicks);
                                this.renderedEntity = null;
                                break;
                            }
                        }

                        if (!flag4 && entity2 instanceof EntityWitherSkull)
                        {
                            this.renderedEntity = entity2;

                            if (flag6)
                            {
                                Shaders.nextEntity(entity2);
                            }

                            this.mc.getRenderManager().renderWitherSkull(entity2, partialTicks);
                            this.renderedEntity = null;
                        }
                    }
                }
            }

            this.mc.gameSettings.fancyGraphics = flag7;

            if (flag6)
            {
                Shaders.endEntities();
                Shaders.beginBlockEntities();
            }

            this.theWorld.theProfiler.endStartSection("blockentities");
            RenderHelper.enableStandardItemLighting();
            TileEntitySignRenderer.updateTextRenderDistance();

            for (Object o : this.renderInfosTileEntities)
            {
                RenderGlobal.ContainerLocalRenderInformation renderglobal$containerlocalrenderinformation1 = (ContainerLocalRenderInformation) o;
                List<TileEntity> list1 = renderglobal$containerlocalrenderinformation1.renderChunk.getCompiledChunk().getTileEntities();

                if (!list1.isEmpty())
                {
                    for (TileEntity tileentity1 : list1)
                    {
                        if (flag6)
                        {
                            Shaders.nextBlockEntity(tileentity1);
                        }

                        TileEntityRendererDispatcher.instance.renderTileEntity(tileentity1, partialTicks, -1);
                        ++this.countTileEntitiesRendered;
                    }
                }
            }

            synchronized (this.setTileEntities)
            {
                for (TileEntity tileentity : this.setTileEntities)
                {
                    if (flag6)
                    {
                        Shaders.nextBlockEntity(tileentity);
                    }

                    TileEntityRendererDispatcher.instance.renderTileEntity(tileentity, partialTicks, -1);
                }
            }

            this.renderOverlayDamaged = true;
            this.preRenderDamagedBlocks();

            for (DestroyBlockProgress destroyblockprogress : this.damagedBlocks.values())
            {
                BlockPos blockpos = destroyblockprogress.getPosition();
                TileEntity tileentity2 = this.theWorld.getTileEntity(blockpos);

                if (tileentity2 instanceof TileEntityChest)
                {
                    TileEntityChest tileentitychest = (TileEntityChest)tileentity2;

                    if (tileentitychest.adjacentChestXNeg != null)
                    {
                        blockpos = blockpos.offset(EnumFacing.WEST);
                        tileentity2 = this.theWorld.getTileEntity(blockpos);
                    }
                    else if (tileentitychest.adjacentChestZNeg != null)
                    {
                        blockpos = blockpos.offset(EnumFacing.NORTH);
                        tileentity2 = this.theWorld.getTileEntity(blockpos);
                    }
                }

                Block block = this.theWorld.getBlockState(blockpos).getBlock();
                boolean flag9 = tileentity2 != null && (block instanceof BlockChest || block instanceof BlockEnderChest || block instanceof BlockSign || block instanceof BlockSkull);

                if (flag9)
                {
                    if (flag6)
                    {
                        Shaders.nextBlockEntity(tileentity2);
                    }

                    TileEntityRendererDispatcher.instance.renderTileEntity(tileentity2, partialTicks, destroyblockprogress.getPartialBlockDamage());
                }
            }

            this.postRenderDamagedBlocks();
            this.renderOverlayDamaged = false;

            if (flag6)
            {
                Shaders.endBlockEntities();
            }

            --renderEntitiesCounter;
            this.mc.entityRenderer.disableLightmap();
            this.mc.mcProfiler.endSection();
        }
    }

    public String getDebugInfoRenders()
    {
        int i = this.viewFrustum.renderChunks.length;
        int j = 0;

        for (RenderGlobal.ContainerLocalRenderInformation renderglobal$containerlocalrenderinformation : this.renderInfos)
        {
            CompiledChunk compiledchunk = renderglobal$containerlocalrenderinformation.renderChunk.compiledChunk;

            if (compiledchunk != CompiledChunk.DUMMY && !compiledchunk.isEmpty())
            {
                ++j;
            }
        }

        return "C: " + j + "/" + i + " " + (this.mc.renderChunksMany ? "(s) " : "") + "D: " + this.renderDistanceChunks + ", " + this.renderDispatcher.getDebugInfo();
    }

    public String getDebugInfoEntities()
    {
        return "E: " + this.countEntitiesRendered + "/" + this.countEntitiesTotal + ", B: " + this.countEntitiesHidden + ", I: " + (this.countEntitiesTotal - this.countEntitiesHidden - this.countEntitiesRendered) + ", " + Config.getVersionDebug();
    }

    public void setupTerrain(Entity viewEntity, double partialTicks, ICamera camera, int frameCount, boolean playerSpectator)
    {
        if (this.mc.gameSettings.renderDistanceChunks != this.renderDistanceChunks)
        {
            this.loadRenderers();
        }

        this.theWorld.theProfiler.startSection("camera");
        double eleventhDoubleValue = viewEntity.posX - this.frustumUpdatePosX;
        double fifteenthDoubleValue = viewEntity.posY - this.frustumUpdatePosY;
        double number44DoubleValue = viewEntity.posZ - this.frustumUpdatePosZ;

        if (this.frustumUpdatePosChunkX != viewEntity.chunkCoordX || this.frustumUpdatePosChunkY != viewEntity.chunkCoordY || this.frustumUpdatePosChunkZ != viewEntity.chunkCoordZ || eleventhDoubleValue * eleventhDoubleValue + fifteenthDoubleValue * fifteenthDoubleValue + number44DoubleValue * number44DoubleValue > 16.0D)
        {
            this.frustumUpdatePosX = viewEntity.posX;
            this.frustumUpdatePosY = viewEntity.posY;
            this.frustumUpdatePosZ = viewEntity.posZ;
            this.frustumUpdatePosChunkX = viewEntity.chunkCoordX;
            this.frustumUpdatePosChunkY = viewEntity.chunkCoordY;
            this.frustumUpdatePosChunkZ = viewEntity.chunkCoordZ;
            this.viewFrustum.updateChunkPositions(viewEntity.posX, viewEntity.posZ);
        }

        if (Config.isDynamicLights())
        {
            DynamicLights.update(this);
        }

        this.theWorld.theProfiler.endStartSection("renderlistcamera");
        double number60DoubleValue = viewEntity.lastTickPosX + (viewEntity.posX - viewEntity.lastTickPosX) * partialTicks;
        double number63DoubleValue = viewEntity.lastTickPosY + (viewEntity.posY - viewEntity.lastTickPosY) * partialTicks;
        double number68DoubleValue = viewEntity.lastTickPosZ + (viewEntity.posZ - viewEntity.lastTickPosZ) * partialTicks;
        this.renderContainer.initialize(number60DoubleValue, number63DoubleValue, number68DoubleValue);
        this.theWorld.theProfiler.endStartSection("cull");

        if (this.debugFixedClippingHelper != null)
        {
            Frustum frustum = new Frustum(this.debugFixedClippingHelper);
            frustum.setPosition(this.debugTerrainFrustumPosition.x, this.debugTerrainFrustumPosition.y, this.debugTerrainFrustumPosition.z);
            camera = frustum;
        }

        this.mc.mcProfiler.endStartSection("culling");
        BlockPos blockpos = this.terrainViewPos.set(MathHelper.floor_double(number60DoubleValue), MathHelper.floor_double(number63DoubleValue + (double)viewEntity.getEyeHeight()), MathHelper.floor_double(number68DoubleValue));
        RenderChunk renderchunk = this.viewFrustum.getRenderChunk(blockpos);
        this.displayListEntitiesDirty = this.displayListEntitiesDirty || !this.chunksToUpdate.isEmpty() || viewEntity.posX != this.lastViewEntityX || viewEntity.posY != this.lastViewEntityY || viewEntity.posZ != this.lastViewEntityZ || (double)viewEntity.rotationPitch != this.lastViewEntityPitch || (double)viewEntity.rotationYaw != this.lastViewEntityYaw;
        this.lastViewEntityX = viewEntity.posX;
        this.lastViewEntityY = viewEntity.posY;
        this.lastViewEntityZ = viewEntity.posZ;
        this.lastViewEntityPitch = (double)viewEntity.rotationPitch;
        this.lastViewEntityYaw = (double)viewEntity.rotationYaw;
        boolean flag = this.debugFixedClippingHelper != null;
        this.mc.mcProfiler.endStartSection("update");
        Lagometer.timerVisibility.start();
        int i = this.getCountLoadedChunks();

        if (i != this.countLoadedChunksPrev)
        {
            this.countLoadedChunksPrev = i;
            this.displayListEntitiesDirty = true;
        }

        int j = 256;

        if (!ChunkVisibility.isFinished())
        {
            this.displayListEntitiesDirty = true;
        }

        if (!flag && this.displayListEntitiesDirty && Config.isIntegratedServerRunning())
        {
            j = ChunkVisibility.getMaxChunkY(this.theWorld, viewEntity, this.renderDistanceChunks);
        }

        RenderChunk renderchunk1 = this.viewFrustum.getRenderChunk(this.terrainLookupPos.set(MathHelper.floor_double(viewEntity.posX), MathHelper.floor_double(viewEntity.posY), MathHelper.floor_double(viewEntity.posZ)));

        if (Shaders.isShadowPass)
        {
            this.renderInfos = this.renderInfosShadow;
            this.renderInfosEntities = this.renderInfosEntitiesShadow;
            this.renderInfosTileEntities = this.renderInfosTileEntitiesShadow;

            if (!flag && this.displayListEntitiesDirty)
            {
                this.clearRenderInfos();

                if (renderchunk1 != null && renderchunk1.getPosition().getY() > j)
                {
                    this.renderInfosEntities.add(renderchunk1.getRenderInfo());
                }

                Iterator<RenderChunk> iterator = ShadowUtils.makeShadowChunkIterator(this.theWorld, partialTicks, viewEntity, this.renderDistanceChunks, this.viewFrustum);

                while (iterator.hasNext())
                {
                    RenderChunk renderchunk2 = (RenderChunk)iterator.next();

                    if (renderchunk2 != null && renderchunk2.getPosition().getY() <= j)
                    {
                        RenderGlobal.ContainerLocalRenderInformation renderglobal$containerlocalrenderinformation = renderchunk2.getRenderInfo();

                        if (!renderchunk2.compiledChunk.isEmpty() || renderchunk2.isNeedsUpdate())
                        {
                            this.renderInfos.add(renderglobal$containerlocalrenderinformation);
                        }

                        if (ChunkUtils.hasEntities(renderchunk2.getChunk()))
                        {
                            this.renderInfosEntities.add(renderglobal$containerlocalrenderinformation);
                        }

                        if (renderchunk2.getCompiledChunk().getTileEntities().size() > 0)
                        {
                            this.renderInfosTileEntities.add(renderglobal$containerlocalrenderinformation);
                        }
                    }
                }
            }
        }
        else
        {
            this.renderInfos = this.renderInfosNormal;
            this.renderInfosEntities = this.renderInfosEntitiesNormal;
            this.renderInfosTileEntities = this.renderInfosTileEntitiesNormal;
        }

        if (!flag && this.displayListEntitiesDirty && !Shaders.isShadowPass)
        {
            this.displayListEntitiesDirty = false;
            this.clearRenderInfos();
            this.visibilityDeque.clear();
            Deque deque = this.visibilityDeque;
            boolean flag1 = this.mc.renderChunksMany;

            if (renderchunk != null && renderchunk.getPosition().getY() <= j)
            {
                boolean flag2 = false;
                RenderGlobal.ContainerLocalRenderInformation renderglobal$containerlocalrenderinformation4 = new RenderGlobal.ContainerLocalRenderInformation(renderchunk, (EnumFacing)null, 0);
                Set secondSet = SET_ALL_FACINGS;

                if (secondSet.size() == 1)
                {
                    Vector3f vector3f = this.getViewVector(viewEntity, partialTicks);
                    EnumFacing enumfacing2 = EnumFacing.getFacingFromVector(vector3f.x, vector3f.y, vector3f.z).getOpposite();
                    secondSet.remove(enumfacing2);
                }

                if (secondSet.isEmpty())
                {
                    flag2 = true;
                }

                if (flag2 && !playerSpectator)
                {
                    this.renderInfos.add(renderglobal$containerlocalrenderinformation4);
                }
                else
                {
                    if (playerSpectator && this.theWorld.getBlockState(blockpos).getBlock().isOpaqueCube())
                    {
                        flag1 = false;
                    }

                    renderchunk.setFrameIndex(frameCount);
                    deque.add(renderglobal$containerlocalrenderinformation4);
                }
            }
            else
            {
                int fifthIntValue = blockpos.getY() > 0 ? Math.min(j, 248) : 8;

                if (renderchunk1 != null)
                {
                    this.renderInfosEntities.add(renderchunk1.getRenderInfo());
                }

                for (int k = -this.renderDistanceChunks; k <= this.renderDistanceChunks; ++k)
                {
                    for (int l = -this.renderDistanceChunks; l <= this.renderDistanceChunks; ++l)
                    {
                        RenderChunk renderchunk3 = this.viewFrustum.getRenderChunk(this.terrainLookupPos.set((k << 4) + 8, fifthIntValue, (l << 4) + 8));

                        if (renderchunk3 != null && renderchunk3.isBoundingBoxInFrustum((ICamera)camera, frameCount))
                        {
                            renderchunk3.setFrameIndex(frameCount);
                            RenderGlobal.ContainerLocalRenderInformation renderglobal$containerlocalrenderinformation1 = renderchunk3.getRenderInfo();
                            renderglobal$containerlocalrenderinformation1.initialize((EnumFacing)null, 0);
                            deque.add(renderglobal$containerlocalrenderinformation1);
                        }
                    }
                }
            }

            this.mc.mcProfiler.startSection("iteration");
            boolean flag3 = Config.isFogOn();

            while (!deque.isEmpty())
            {
                RenderGlobal.ContainerLocalRenderInformation renderglobal$containerlocalrenderinformation5 = (RenderGlobal.ContainerLocalRenderInformation)deque.poll();
                RenderChunk renderchunk6 = renderglobal$containerlocalrenderinformation5.renderChunk;
                EnumFacing enumfacing1 = renderglobal$containerlocalrenderinformation5.facing;
                CompiledChunk compiledchunk = renderchunk6.compiledChunk;

                if (!compiledchunk.isEmpty() || renderchunk6.isNeedsUpdate())
                {
                    this.renderInfos.add(renderglobal$containerlocalrenderinformation5);
                }

                if (ChunkUtils.hasEntities(renderchunk6.getChunk()))
                {
                    this.renderInfosEntities.add(renderglobal$containerlocalrenderinformation5);
                }

                if (compiledchunk.getTileEntities().size() > 0)
                {
                    this.renderInfosTileEntities.add(renderglobal$containerlocalrenderinformation5);
                }

                for (EnumFacing enumfacing : flag1 ? ChunkVisibility.getFacingsNotOpposite(renderglobal$containerlocalrenderinformation5.setFacing) : EnumFacing.VALUES)
                {
                    if (!flag1 || enumfacing1 == null || compiledchunk.isVisible(enumfacing1.getOpposite(), enumfacing))
                    {
                        RenderChunk renderchunk4 = this.getRenderChunkOffset(blockpos, renderchunk6, enumfacing, flag3, j);

                        if (renderchunk4 != null && renderchunk4.setFrameIndex(frameCount) && renderchunk4.isBoundingBoxInFrustum((ICamera)camera, frameCount))
                        {
                            int fourthIntValue = renderglobal$containerlocalrenderinformation5.setFacing | 1 << enumfacing.ordinal();
                            RenderGlobal.ContainerLocalRenderInformation renderglobal$containerlocalrenderinformation2 = renderchunk4.getRenderInfo();
                            renderglobal$containerlocalrenderinformation2.initialize(enumfacing, fourthIntValue);
                            deque.add(renderglobal$containerlocalrenderinformation2);
                        }
                    }
                }
            }

            this.mc.mcProfiler.endSection();
        }

        this.mc.mcProfiler.endStartSection("captureFrustum");

        if (this.debugFixTerrainFrustum)
        {
            this.fixTerrainFrustum(number60DoubleValue, number63DoubleValue, number68DoubleValue);
            this.debugFixTerrainFrustum = false;
        }

        Lagometer.timerVisibility.end();

        if (Shaders.isShadowPass)
        {
            Shaders.mcProfilerEndSection();
        }
        else
        {
            this.mc.mcProfiler.endStartSection("rebuildNear");
            this.renderDispatcher.clearChunkUpdates();
            Set<RenderChunk> set = this.chunksToUpdate;
            this.chunksToUpdate = this.chunksToUpdateScratch;
            this.chunksToUpdate.clear();
            this.chunksToUpdateScratch = set;
            Lagometer.timerChunkUpdate.start();

            for (RenderGlobal.ContainerLocalRenderInformation renderglobal$containerlocalrenderinformation3 : this.renderInfos)
            {
                RenderChunk renderchunk5 = renderglobal$containerlocalrenderinformation3.renderChunk;

                if (renderchunk5.isNeedsUpdate() || set.contains(renderchunk5))
                {
                    this.displayListEntitiesDirty = true;
                    BlockPos blockpos1 = renderchunk5.getPosition();
                    boolean flag4 = blockpos.distanceSq((double)(blockpos1.getX() + 8), (double)(blockpos1.getY() + 8), (double)(blockpos1.getZ() + 8)) < 768.0D;

                    if (!flag4)
                    {
                        this.chunksToUpdate.add(renderchunk5);
                    }
                    else if (!renderchunk5.isPlayerUpdate())
                    {
                        this.chunksToUpdateForced.add(renderchunk5);
                    }
                    else
                    {
                        this.mc.mcProfiler.startSection("build near");
                        this.renderDispatcher.updateChunkNow(renderchunk5);
                        renderchunk5.setNeedsUpdate(false);
                        this.mc.mcProfiler.endSection();
                    }
                }
            }

            Lagometer.timerChunkUpdate.end();
            this.chunksToUpdate.addAll(set);
            set.clear();
            this.mc.mcProfiler.endSection();
        }
    }

    private boolean isPositionInRenderChunk(BlockPos pos, RenderChunk renderChunkIn)
    {
        BlockPos blockPos = renderChunkIn.getPosition();
        return MathHelper.abs_int(pos.getX() - blockPos.getX()) > 16 ? false : (MathHelper.abs_int(pos.getY() - blockPos.getY()) > 16 ? false : MathHelper.abs_int(pos.getZ() - blockPos.getZ()) <= 16);
    }

    private Set<EnumFacing> getVisibleFacings(BlockPos pos)
    {
        VisGraph visGraph = new VisGraph();
        BlockPos blockPos = new BlockPos(pos.getX() >> 4 << 4, pos.getY() >> 4 << 4, pos.getZ() >> 4 << 4);
        Chunk chunk = this.theWorld.getChunkFromBlockCoords(blockPos);

        for (BlockPos.MutableBlockPos blockpos$mutableblockpos : BlockPos.getAllInBoxMutable(blockPos, blockPos.add(15, 15, 15)))
        {
            if (chunk.getBlock(blockpos$mutableblockpos).isOpaqueCube())
            {
                visGraph.markBlockOpaque(blockpos$mutableblockpos);
            }
        }
        return visGraph.getVisibleFacings(pos);
    }

    private RenderChunk getRenderChunkOffset(BlockPos pos, RenderChunk renderChunkIn, EnumFacing facing, boolean checkDistance, int maxY)
    {
        RenderChunk renderChunk = renderChunkIn.getRenderChunkNeighbour(facing);

        if (renderChunk == null)
        {
            return null;
        }
        else if (renderChunk.getPosition().getY() > maxY)
        {
            return null;
        }
        else
        {
            if (checkDistance)
            {
                BlockPos chunkPos = renderChunk.getPosition();
                int xOffset = pos.getX() - chunkPos.getX();
                int zOffset = pos.getZ() - chunkPos.getZ();
                int distanceSq = xOffset * xOffset + zOffset * zOffset;

                if (distanceSq > this.renderDistanceSq)
                {
                    return null;
                }
            }

            return renderChunk;
        }
    }

    private void fixTerrainFrustum(double x, double y, double z)
    {
        this.debugFixedClippingHelper = new ClippingHelperImpl();
        ((ClippingHelperImpl)this.debugFixedClippingHelper).init();
        Matrix4f matrix4f = new Matrix4f(this.debugFixedClippingHelper.modelviewMatrix);
        matrix4f.transpose();
        Matrix4f matrix4f1 = new Matrix4f(this.debugFixedClippingHelper.projectionMatrix);
        matrix4f1.transpose();
        Matrix4f matrix4f2 = new Matrix4f();
        Matrix4f.mul(matrix4f1, matrix4f, matrix4f2);
        matrix4f2.invert();
        this.debugTerrainFrustumPosition.x = x;
        this.debugTerrainFrustumPosition.y = y;
        this.debugTerrainFrustumPosition.z = z;
        this.debugTerrainMatrix[0] = new Vector4f(-1.0F, -1.0F, -1.0F, 1.0F);
        this.debugTerrainMatrix[1] = new Vector4f(1.0F, -1.0F, -1.0F, 1.0F);
        this.debugTerrainMatrix[2] = new Vector4f(1.0F, 1.0F, -1.0F, 1.0F);
        this.debugTerrainMatrix[3] = new Vector4f(-1.0F, 1.0F, -1.0F, 1.0F);
        this.debugTerrainMatrix[4] = new Vector4f(-1.0F, -1.0F, 1.0F, 1.0F);
        this.debugTerrainMatrix[5] = new Vector4f(1.0F, -1.0F, 1.0F, 1.0F);
        this.debugTerrainMatrix[6] = new Vector4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.debugTerrainMatrix[7] = new Vector4f(-1.0F, 1.0F, 1.0F, 1.0F);

        for (int i = 0; i < 8; ++i)
        {
            Matrix4f.transform(matrix4f2, this.debugTerrainMatrix[i], this.debugTerrainMatrix[i]);
            this.debugTerrainMatrix[i].x /= this.debugTerrainMatrix[i].w;
            this.debugTerrainMatrix[i].y /= this.debugTerrainMatrix[i].w;
            this.debugTerrainMatrix[i].z /= this.debugTerrainMatrix[i].w;
            this.debugTerrainMatrix[i].w = 1.0F;
        }
    }

    protected Vector3f getViewVector(Entity entityIn, double partialTicks)
    {
        float f = (float)((double)entityIn.prevRotationPitch + (double)(entityIn.rotationPitch - entityIn.prevRotationPitch) * partialTicks);
        float floatValue2 = (float)((double)entityIn.prevRotationYaw + (double)(entityIn.rotationYaw - entityIn.prevRotationYaw) * partialTicks);

        if (Minecraft.getMinecraft().gameSettings.thirdPersonView == 2)
        {
            f += 180.0F;
        }

        float[] yawSC = new float[2];
        float[] pitchSC = new float[2];
        MathHelper.sinCosDeg(-floatValue2, yawSC);
        MathHelper.sinCosDeg(-f, pitchSC);
        float thirtiethFloatValue = -yawSC[1];
        float floatValue4 = -yawSC[0];
        float floatValue5 = -pitchSC[1];
        float floatValue6 = pitchSC[0];
        return new Vector3f(floatValue4 * floatValue5, floatValue6, thirtiethFloatValue * floatValue5);
    }

    public int renderBlockLayer(EnumWorldBlockLayer blockLayerIn, double partialTicks, int pass, Entity entityIn)
    {
        RenderHelper.disableStandardItemLighting();

        if (blockLayerIn == EnumWorldBlockLayer.TRANSLUCENT && !Shaders.isShadowPass)
        {
            this.mc.mcProfiler.startSection("translucent_sort");
            double xCoordinate = entityIn.posX - this.prevRenderSortX;
            double yCoordinate = entityIn.posY - this.prevRenderSortY;
            double zCoordinate = entityIn.posZ - this.prevRenderSortZ;

            if (xCoordinate * xCoordinate + yCoordinate * yCoordinate + zCoordinate * zCoordinate > 1.0D)
            {
                this.prevRenderSortX = entityIn.posX;
                this.prevRenderSortY = entityIn.posY;
                this.prevRenderSortZ = entityIn.posZ;
                int k = 0;
                this.chunksToResortTransparency.clear();

                for (RenderGlobal.ContainerLocalRenderInformation renderglobal$containerlocalrenderinformation : this.renderInfos)
                {
                    if (renderglobal$containerlocalrenderinformation.renderChunk.compiledChunk.isLayerStarted(blockLayerIn) && k++ < 15)
                    {
                        this.chunksToResortTransparency.add(renderglobal$containerlocalrenderinformation.renderChunk);
                    }
                }
            }

            this.mc.mcProfiler.endSection();
        }

        this.mc.mcProfiler.startSection("filterempty");
        int l = 0;
        boolean flag = blockLayerIn == EnumWorldBlockLayer.TRANSLUCENT;
        int count = flag ? this.renderInfos.size() - 1 : 0;
        int i = flag ? -1 : this.renderInfos.size();
        int secondIntValue = flag ? -1 : 1;

        for (int j = count; j != i; j += secondIntValue)
        {
            RenderChunk renderchunk = ((RenderGlobal.ContainerLocalRenderInformation)this.renderInfos.get(j)).renderChunk;

            if (!renderchunk.getCompiledChunk().isLayerEmpty(blockLayerIn))
            {
                ++l;
                this.renderContainer.addRenderChunk(renderchunk, blockLayerIn);
            }
        }

        if (l == 0)
        {
            this.mc.mcProfiler.endSection();
            return l;
        }
        else
        {
            if (Config.isFogOff() && this.mc.entityRenderer.fogStandard)
            {
                GlStateManager.disableFog();
            }

            this.mc.mcProfiler.endStartSection(RENDER_LAYER_PROFILER_NAMES[blockLayerIn.ordinal()]);
            this.renderBlockLayer(blockLayerIn);
            this.mc.mcProfiler.endSection();
            return l;
        }
    }

    @SuppressWarnings("incomplete-switch")
    private void renderBlockLayer(EnumWorldBlockLayer blockLayerIn)
    {
        this.mc.entityRenderer.enableLightmap();

        OpenGlHelper.bindDefaultVertexArray();

        if (Config.isShaders())
        {
            ShadersRender.preRenderChunkLayer(blockLayerIn);
        }

        this.renderContainer.renderChunkLayer(blockLayerIn);

        if (Config.isShaders())
        {
            ShadersRender.postRenderChunkLayer(blockLayerIn);
        }

        WorldVertexBufferUploader.clearVertexFormat(DefaultVertexFormats.BLOCK);

        this.mc.entityRenderer.disableLightmap();
    }

    private void cleanupDamagedBlocks(Iterator<DestroyBlockProgress> iteratorIn)
    {
        while (iteratorIn.hasNext())
        {
            DestroyBlockProgress destroyBlockProgress = (DestroyBlockProgress)iteratorIn.next();
            int i = destroyBlockProgress.getCreationCloudUpdateTick();

            if (this.cloudTickCounter - i > 400)
            {
                iteratorIn.remove();
            }
        }
    }

    public void updateClouds()
    {
        if (Config.isShaders())
        {
            if (GameWindow.isKeyDown(61) && GameWindow.isKeyDown(24))
            {
                GuiShaderOptions guiShaderOptions = new GuiShaderOptions((GuiScreen)null, Config.getGameSettings());
                Config.getMinecraft().displayGuiScreen(guiShaderOptions);
            }

            if (GameWindow.isKeyDown(61) && GameWindow.isKeyDown(19))
            {
                Shaders.uninit();
                Shaders.loadShaderPack();
            }
        }

        ++this.cloudTickCounter;

        if (this.cloudTickCounter % 20 == 0)
        {
            this.cleanupDamagedBlocks(this.damagedBlocks.values().iterator());
        }
    }

    private void renderSkyEnd()
    {
        if (Config.isSkyEnabled())
        {
            GlStateManager.disableFog();
            GlStateManager.disableAlpha();
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            RenderHelper.disableStandardItemLighting();
            GlStateManager.depthMask(false);
            this.renderEngine.bindTexture(locationEndSkyPng);
            Tessellator tessellator = Tessellator.getInstance();
            WorldRenderer worldRenderer = tessellator.getWorldRenderer();

            for (int i = 0; i < 6; ++i)
            {
                GlStateManager.pushMatrix();

                if (i == 1)
                {
                    GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
                }

                if (i == 2)
                {
                    GlStateManager.rotate(-90.0F, 1.0F, 0.0F, 0.0F);
                }

                if (i == 3)
                {
                    GlStateManager.rotate(180.0F, 1.0F, 0.0F, 0.0F);
                }

                if (i == 4)
                {
                    GlStateManager.rotate(90.0F, 0.0F, 0.0F, 1.0F);
                }

                if (i == 5)
                {
                    GlStateManager.rotate(-90.0F, 0.0F, 0.0F, 1.0F);
                }

                worldRenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
                int j = 40;
                int k = 40;
                int l = 40;

                if (Config.isCustomColors())
                {
                    Vec3 localValue = new Vec3((double)j / 255.0D, (double)k / 255.0D, (double)l / 255.0D);
                    localValue = CustomColors.getWorldSkyColor(localValue, this.theWorld, this.mc.getRenderViewEntity(), 0.0F);
                    j = (int)(localValue.xCoord * 255.0D);
                    k = (int)(localValue.yCoord * 255.0D);
                    l = (int)(localValue.zCoord * 255.0D);
                }

                worldRenderer.pos(-100.0D, -100.0D, -100.0D).tex(0.0D, 0.0D).color(j, k, l, 255).endVertex();
                worldRenderer.pos(-100.0D, -100.0D, 100.0D).tex(0.0D, 16.0D).color(j, k, l, 255).endVertex();
                worldRenderer.pos(100.0D, -100.0D, 100.0D).tex(16.0D, 16.0D).color(j, k, l, 255).endVertex();
                worldRenderer.pos(100.0D, -100.0D, -100.0D).tex(16.0D, 0.0D).color(j, k, l, 255).endVertex();
                tessellator.draw();
                GlStateManager.popMatrix();
            }

            GlStateManager.depthMask(true);
            GlStateManager.enableTexture2D();
            GlStateManager.enableAlpha();
            GlStateManager.disableBlend();
        }
    }

    public void renderSky(float partialTicks, int pass)
    {
        
        if (this.mc.theWorld.provider.getDimensionId() == 1)
        {
            this.renderSkyEnd();
        }
        else if (this.mc.theWorld.provider.isSurfaceWorld())
        {
            GlStateManager.disableTexture2D();
            boolean flag = Config.isShaders();

            if (flag)
            {
                Shaders.disableTexture2D();
            }

            Vec3 vec3 = this.theWorld.getSkyColor(this.mc.getRenderViewEntity(), partialTicks);
            vec3 = CustomColors.getSkyColor(vec3, this.mc.theWorld, this.mc.getRenderViewEntity().posX, this.mc.getRenderViewEntity().posY + 1.0D, this.mc.getRenderViewEntity().posZ);

            if (flag)
            {
                Shaders.setSkyColor(vec3);
            }

            float f = (float)vec3.xCoord;
            float floatValue = (float)vec3.yCoord;
            float secondFloatValue = (float)vec3.zCoord;

            if (pass != 2)
            {
                float thirdFloatValue = (f * 30.0F + floatValue * 59.0F + secondFloatValue * 11.0F) / 100.0F;
                float fourthFloatValue = (f * 30.0F + floatValue * 70.0F) / 100.0F;
                float fifthFloatValue = (f * 30.0F + secondFloatValue * 70.0F) / 100.0F;
                f = thirdFloatValue;
                floatValue = fourthFloatValue;
                secondFloatValue = fifthFloatValue;
            }

            GlStateManager.color(f, floatValue, secondFloatValue);
            Tessellator tessellator = Tessellator.getInstance();
            WorldRenderer worldrenderer = tessellator.getWorldRenderer();
            GlStateManager.depthMask(false);
            GlStateManager.enableFog();

            if (flag)
            {
                Shaders.enableFog();
            }

            GlStateManager.color(f, floatValue, secondFloatValue);

            if (flag)
            {
                Shaders.preSkyList();
            }

            if (Config.isSkyEnabled())
            {
                this.drawPositionVbo(this.skyVBO);
            }

            GlStateManager.disableFog();

            if (flag)
            {
                Shaders.disableFog();
            }

            GlStateManager.disableAlpha();
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            RenderHelper.disableStandardItemLighting();
            float[] afloat = this.theWorld.provider.calcSunriseSunsetColors(this.theWorld.getCelestialAngle(partialTicks), partialTicks);

            if (afloat != null && Config.isSunMoonEnabled())
            {
                GlStateManager.disableTexture2D();

                if (flag)
                {
                    Shaders.disableTexture2D();
                }

                GlStateManager.shadeModel(7425);
                GlStateManager.pushMatrix();
                GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
                GlStateManager.rotate(MathHelper.sin(this.theWorld.getCelestialAngleRadians(partialTicks)) < 0.0F ? 180.0F : 0.0F, 0.0F, 0.0F, 1.0F);
                GlStateManager.rotate(90.0F, 0.0F, 0.0F, 1.0F);
                float sixthFloatValue = afloat[0];
                float seventhFloatValue = afloat[1];
                float eighthFloatValue = afloat[2];

                if (pass != 2)
                {
                    float ninthFloatValue = (sixthFloatValue * 30.0F + seventhFloatValue * 59.0F + eighthFloatValue * 11.0F) / 100.0F;
                    float tenthFloatValue = (sixthFloatValue * 30.0F + seventhFloatValue * 70.0F) / 100.0F;
                    float eleventhFloatValue = (sixthFloatValue * 30.0F + eighthFloatValue * 70.0F) / 100.0F;
                    sixthFloatValue = ninthFloatValue;
                    seventhFloatValue = tenthFloatValue;
                    eighthFloatValue = eleventhFloatValue;
                }

                worldrenderer.begin(6, DefaultVertexFormats.POSITION_COLOR);
                worldrenderer.pos(0.0D, 100.0D, 0.0D).color(sixthFloatValue, seventhFloatValue, eighthFloatValue, afloat[3]).endVertex();
                int j = 16;

                for (int l = 0; l <= 16; ++l)
                {
                    float twelfthFloatValue = (float)l * (float)Math.PI * 2.0F / 16.0F;
                    float thirteenthFloatValue = MathHelper.sin(twelfthFloatValue);
                    float fourteenthFloatValue = MathHelper.cos(twelfthFloatValue);
                    worldrenderer.pos((double)(thirteenthFloatValue * 120.0F), (double)(fourteenthFloatValue * 120.0F), (double)(-fourteenthFloatValue * 40.0F * afloat[3])).color(afloat[0], afloat[1], afloat[2], 0.0F).endVertex();
                }

                tessellator.draw();
                GlStateManager.popMatrix();
                GlStateManager.shadeModel(7424);
            }

            GlStateManager.enableTexture2D();

            if (flag)
            {
                Shaders.enableTexture2D();
            }

            GlStateManager.tryBlendFuncSeparate(770, 1, 1, 0);
            GlStateManager.pushMatrix();
            float fifteenthFloatValue = 1.0F - this.theWorld.getRainStrength(partialTicks);
            GlStateManager.color(1.0F, 1.0F, 1.0F, fifteenthFloatValue);
            GlStateManager.rotate(-90.0F, 0.0F, 1.0F, 0.0F);
            CustomSky.renderSky(this.theWorld, this.renderEngine, partialTicks);

            if (flag)
            {
                Shaders.preCelestialRotate();
            }

            GlStateManager.rotate(this.theWorld.getCelestialAngle(partialTicks) * 360.0F, 1.0F, 0.0F, 0.0F);

            if (flag)
            {
                Shaders.postCelestialRotate();
            }

            float twentyEighthFloatValue = 30.0F;

            if (Config.isSunTexture())
            {
                this.renderEngine.bindTexture(locationSunPng);
                worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
                worldrenderer.pos((double)(-twentyEighthFloatValue), 100.0D, (double)(-twentyEighthFloatValue)).tex(0.0D, 0.0D).endVertex();
                worldrenderer.pos((double)twentyEighthFloatValue, 100.0D, (double)(-twentyEighthFloatValue)).tex(1.0D, 0.0D).endVertex();
                worldrenderer.pos((double)twentyEighthFloatValue, 100.0D, (double)twentyEighthFloatValue).tex(1.0D, 1.0D).endVertex();
                worldrenderer.pos((double)(-twentyEighthFloatValue), 100.0D, (double)twentyEighthFloatValue).tex(0.0D, 1.0D).endVertex();
                tessellator.draw();
            }

            twentyEighthFloatValue = 20.0F;

            if (Config.isMoonTexture())
            {
                this.renderEngine.bindTexture(locationMoonPhasesPng);
                int i = this.theWorld.getMoonPhase();
                int k = i % 4;
                int intValue = i / 4 % 2;
                float sixteenthFloatValue = (float)(k + 0) / 4.0F;
                float seventeenthFloatValue = (float)(intValue + 0) / 2.0F;
                float eighteenthFloatValue = (float)(k + 1) / 4.0F;
                float nineteenthFloatValue = (float)(intValue + 1) / 2.0F;
                worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
                worldrenderer.pos((double)(-twentyEighthFloatValue), -100.0D, (double)twentyEighthFloatValue).tex((double)eighteenthFloatValue, (double)nineteenthFloatValue).endVertex();
                worldrenderer.pos((double)twentyEighthFloatValue, -100.0D, (double)twentyEighthFloatValue).tex((double)sixteenthFloatValue, (double)nineteenthFloatValue).endVertex();
                worldrenderer.pos((double)twentyEighthFloatValue, -100.0D, (double)(-twentyEighthFloatValue)).tex((double)sixteenthFloatValue, (double)seventeenthFloatValue).endVertex();
                worldrenderer.pos((double)(-twentyEighthFloatValue), -100.0D, (double)(-twentyEighthFloatValue)).tex((double)eighteenthFloatValue, (double)seventeenthFloatValue).endVertex();
                tessellator.draw();
            }

            GlStateManager.disableTexture2D();

            if (flag)
            {
                Shaders.disableTexture2D();
            }

            float twentyNinthFloatValue = this.theWorld.getStarBrightness(partialTicks) * fifteenthFloatValue;

            if (twentyNinthFloatValue > 0.0F && Config.isStarsEnabled() && !CustomSky.hasSkyLayers(this.theWorld))
            {
                GlStateManager.color(twentyNinthFloatValue, twentyNinthFloatValue, twentyNinthFloatValue, twentyNinthFloatValue);

                this.drawPositionVbo(this.starVBO);
            }

            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.disableBlend();
            GlStateManager.enableAlpha();
            GlStateManager.enableFog();

            if (flag)
            {
                Shaders.enableFog();
            }

            GlStateManager.popMatrix();
            GlStateManager.disableTexture2D();

            if (flag)
            {
                Shaders.disableTexture2D();
            }

            GlStateManager.color(0.0F, 0.0F, 0.0F);
            double doubleValue = this.mc.thePlayer.getPositionEyes(partialTicks).yCoord - this.theWorld.getHorizon();

            if (doubleValue < 0.0D)
            {
                GlStateManager.pushMatrix();
                GlStateManager.translate(0.0F, 12.0F, 0.0F);

                this.drawPositionVbo(this.sky2VBO);

                GlStateManager.popMatrix();
                float twentiethFloatValue = 1.0F;
                float floatValue21 = -((float)(doubleValue + 65.0D));
                float floatValue22 = -1.0F;
                worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
                worldrenderer.pos(-1.0D, (double)floatValue21, 1.0D).color(0, 0, 0, 255).endVertex();
                worldrenderer.pos(1.0D, (double)floatValue21, 1.0D).color(0, 0, 0, 255).endVertex();
                worldrenderer.pos(1.0D, -1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
                worldrenderer.pos(-1.0D, -1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
                worldrenderer.pos(-1.0D, -1.0D, -1.0D).color(0, 0, 0, 255).endVertex();
                worldrenderer.pos(1.0D, -1.0D, -1.0D).color(0, 0, 0, 255).endVertex();
                worldrenderer.pos(1.0D, (double)floatValue21, -1.0D).color(0, 0, 0, 255).endVertex();
                worldrenderer.pos(-1.0D, (double)floatValue21, -1.0D).color(0, 0, 0, 255).endVertex();
                worldrenderer.pos(1.0D, -1.0D, -1.0D).color(0, 0, 0, 255).endVertex();
                worldrenderer.pos(1.0D, -1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
                worldrenderer.pos(1.0D, (double)floatValue21, 1.0D).color(0, 0, 0, 255).endVertex();
                worldrenderer.pos(1.0D, (double)floatValue21, -1.0D).color(0, 0, 0, 255).endVertex();
                worldrenderer.pos(-1.0D, (double)floatValue21, -1.0D).color(0, 0, 0, 255).endVertex();
                worldrenderer.pos(-1.0D, (double)floatValue21, 1.0D).color(0, 0, 0, 255).endVertex();
                worldrenderer.pos(-1.0D, -1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
                worldrenderer.pos(-1.0D, -1.0D, -1.0D).color(0, 0, 0, 255).endVertex();
                worldrenderer.pos(-1.0D, -1.0D, -1.0D).color(0, 0, 0, 255).endVertex();
                worldrenderer.pos(-1.0D, -1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
                worldrenderer.pos(1.0D, -1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
                worldrenderer.pos(1.0D, -1.0D, -1.0D).color(0, 0, 0, 255).endVertex();
                tessellator.draw();
            }

            if (this.theWorld.provider.isSkyColored())
            {
                GlStateManager.color(f * 0.2F + 0.04F, floatValue * 0.2F + 0.04F, secondFloatValue * 0.6F + 0.1F);
            }
            else
            {
                GlStateManager.color(f, floatValue, secondFloatValue);
            }

            if (this.mc.gameSettings.renderDistanceChunks <= 4)
            {
                GlStateManager.color(this.mc.entityRenderer.fogColorRed, this.mc.entityRenderer.fogColorGreen, this.mc.entityRenderer.fogColorBlue);
            }

            GlStateManager.pushMatrix();
            GlStateManager.translate(0.0F, -((float)(doubleValue - 16.0D)), 0.0F);

            if (Config.isSkyEnabled())
            {
                this.drawPositionVbo(this.sky2VBO);
            }

            GlStateManager.popMatrix();
            GlStateManager.enableTexture2D();

            if (flag)
            {
                Shaders.enableTexture2D();
            }

            GlStateManager.depthMask(true);
        }
    }

    public void renderClouds(float partialTicks, int pass)
    {
        if (!Config.isCloudsOff())
        {
            
            if (this.mc.theWorld.provider.isSurfaceWorld())
            {
                if (Config.isShaders())
                {
                    Shaders.beginClouds();
                }

                if (Config.isCloudsFancy())
                {
                    this.renderCloudsFancy(partialTicks, pass);
                }
                else
                {
                    float floatValue = partialTicks;
                    partialTicks = 0.0F;
                    GlStateManager.disableCull();
                    float secondFloatValue = (float)(this.mc.getRenderViewEntity().lastTickPosY + (this.mc.getRenderViewEntity().posY - this.mc.getRenderViewEntity().lastTickPosY) * (double)partialTicks);
                    int i = 32;
                    int j = 8;
                    Tessellator tessellator = Tessellator.getInstance();
                    WorldRenderer worldrenderer = tessellator.getWorldRenderer();
                    this.renderEngine.bindTexture(locationCloudsPng);
                    GlStateManager.enableBlend();
                    GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
                    Vec3 localValue = this.theWorld.getCloudColour(partialTicks);
                    float f = (float)localValue.xCoord;
                    float thirdFloatValue = (float)localValue.yCoord;
                    float fourthFloatValue = (float)localValue.zCoord;
                    this.cloudRenderer.prepareToRender(false, this.cloudTickCounter, floatValue, localValue);

                    if (this.cloudRenderer.shouldUpdateGlList())
                    {
                        this.cloudRenderer.startUpdateGlList();

                        try
                        {
                            if (pass != 2)
                            {
                                float fifthFloatValue = (f * 30.0F + thirdFloatValue * 59.0F + fourthFloatValue * 11.0F) / 100.0F;
                                float sixthFloatValue = (f * 30.0F + thirdFloatValue * 70.0F) / 100.0F;
                                float seventhFloatValue = (f * 30.0F + fourthFloatValue * 70.0F) / 100.0F;
                                f = fifthFloatValue;
                                thirdFloatValue = sixthFloatValue;
                                fourthFloatValue = seventhFloatValue;
                            }

                            float twentyThirdFloatValue = 4.8828125E-4F;
                            double doubleValue = (double)((float)this.cloudTickCounter + partialTicks);
                            double secondDoubleValue = this.mc.getRenderViewEntity().prevPosX + (this.mc.getRenderViewEntity().posX - this.mc.getRenderViewEntity().prevPosX) * (double)partialTicks + doubleValue * 0.029999999329447746D;
                            double thirdDoubleValue = this.mc.getRenderViewEntity().prevPosZ + (this.mc.getRenderViewEntity().posZ - this.mc.getRenderViewEntity().prevPosZ) * (double)partialTicks;
                            int k = MathHelper.floor_double(secondDoubleValue / 2048.0D);
                            int l = MathHelper.floor_double(thirdDoubleValue / 2048.0D);
                            secondDoubleValue = secondDoubleValue - (double)(k * 2048);
                            thirdDoubleValue = thirdDoubleValue - (double)(l * 2048);
                            float eighthFloatValue = this.theWorld.provider.getCloudHeight() - secondFloatValue + 0.33F;
                            eighthFloatValue = eighthFloatValue + this.mc.gameSettings.ofCloudsHeight * 128.0F;
                            float ninthFloatValue = (float)(secondDoubleValue * 4.8828125E-4D);
                            float tenthFloatValue = (float)(thirdDoubleValue * 4.8828125E-4D);
                            worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);

                            for (int intValue = -256; intValue < 256; intValue += 32)
                            {
                                for (int secondIntValue = -256; secondIntValue < 256; secondIntValue += 32)
                                {
                                    worldrenderer.pos((double)(intValue + 0), (double)eighthFloatValue, (double)(secondIntValue + 32)).tex((double)((float)(intValue + 0) * 4.8828125E-4F + ninthFloatValue), (double)((float)(secondIntValue + 32) * 4.8828125E-4F + tenthFloatValue)).color(f, thirdFloatValue, fourthFloatValue, 0.8F).endVertex();
                                    worldrenderer.pos((double)(intValue + 32), (double)eighthFloatValue, (double)(secondIntValue + 32)).tex((double)((float)(intValue + 32) * 4.8828125E-4F + ninthFloatValue), (double)((float)(secondIntValue + 32) * 4.8828125E-4F + tenthFloatValue)).color(f, thirdFloatValue, fourthFloatValue, 0.8F).endVertex();
                                    worldrenderer.pos((double)(intValue + 32), (double)eighthFloatValue, (double)(secondIntValue + 0)).tex((double)((float)(intValue + 32) * 4.8828125E-4F + ninthFloatValue), (double)((float)(secondIntValue + 0) * 4.8828125E-4F + tenthFloatValue)).color(f, thirdFloatValue, fourthFloatValue, 0.8F).endVertex();
                                    worldrenderer.pos((double)(intValue + 0), (double)eighthFloatValue, (double)(secondIntValue + 0)).tex((double)((float)(intValue + 0) * 4.8828125E-4F + ninthFloatValue), (double)((float)(secondIntValue + 0) * 4.8828125E-4F + tenthFloatValue)).color(f, thirdFloatValue, fourthFloatValue, 0.8F).endVertex();
                                }
                            }

                            tessellator.draw();
                        }
                        catch (Throwable throwable)
                        {
                            this.cloudRenderer.abortCapture();
                            throw throwable instanceof RuntimeException ? (RuntimeException)throwable : new RuntimeException(throwable);
                        }
                        finally
                        {
                            if (CloudRenderer.isCapturing())
                            {
                                this.cloudRenderer.endUpdateGlList();
                            }
                        }
                    }

                    this.cloudRenderer.renderGlList();
                    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                    GlStateManager.disableBlend();
                    GlStateManager.enableCull();
                }

                if (Config.isShaders())
                {
                    Shaders.endClouds();
                }
            }
        }
    }

    public boolean hasCloudFog(double x, double y, double z, float partialTicks)
    {
        return false;
    }

    private void renderCloudsFancy(float partialTicks, int pass)
    {
        partialTicks = 0.0F;
        GlStateManager.disableCull();
        float f = (float)(this.mc.getRenderViewEntity().lastTickPosY + (this.mc.getRenderViewEntity().posY - this.mc.getRenderViewEntity().lastTickPosY) * (double)partialTicks);
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldRenderer = tessellator.getWorldRenderer();
        float floatValue2 = 12.0F;
        float floatValue3 = 4.0F;
        double doubleValue = (double)((float)this.cloudTickCounter + partialTicks);
        double xCoordinate = (this.mc.getRenderViewEntity().prevPosX + (this.mc.getRenderViewEntity().posX - this.mc.getRenderViewEntity().prevPosX) * (double)partialTicks + doubleValue * 0.029999999329447746D) / 12.0D;
        double zCoordinate = (this.mc.getRenderViewEntity().prevPosZ + (this.mc.getRenderViewEntity().posZ - this.mc.getRenderViewEntity().prevPosZ) * (double)partialTicks) / 12.0D + 0.33000001311302185D;
        float floatValue4 = this.theWorld.provider.getCloudHeight() - f + 0.33F;
        floatValue4 = floatValue4 + this.mc.gameSettings.ofCloudsHeight * 128.0F;
        int i = MathHelper.floor_double(xCoordinate / 2048.0D);
        int j = MathHelper.floor_double(zCoordinate / 2048.0D);
        xCoordinate = xCoordinate - (double)(i * 2048);
        zCoordinate = zCoordinate - (double)(j * 2048);
        this.renderEngine.bindTexture(locationCloudsPng);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        Vec3 localValue = this.theWorld.getCloudColour(partialTicks);
        float floatValue5 = (float)localValue.xCoord;
        float floatValue6 = (float)localValue.yCoord;
        float floatValue7 = (float)localValue.zCoord;
        this.cloudRenderer.prepareToRender(true, this.cloudTickCounter, partialTicks, localValue);

        if (pass != 2)
        {
            float floatValue8 = (floatValue5 * 30.0F + floatValue6 * 59.0F + floatValue7 * 11.0F) / 100.0F;
            float floatValue9 = (floatValue5 * 30.0F + floatValue6 * 70.0F) / 100.0F;
            float floatValue10 = (floatValue5 * 30.0F + floatValue7 * 70.0F) / 100.0F;
            floatValue5 = floatValue8;
            floatValue6 = floatValue9;
            floatValue7 = floatValue10;
        }

        float number32FloatValue = floatValue5 * 0.9F;
        float floatValue12 = floatValue6 * 0.9F;
        float floatValue13 = floatValue7 * 0.9F;
        float floatValue14 = floatValue5 * 0.7F;
        float floatValue15 = floatValue6 * 0.7F;
        float floatValue16 = floatValue7 * 0.7F;
        float floatValue17 = floatValue5 * 0.8F;
        float floatValue18 = floatValue6 * 0.8F;
        float floatValue19 = floatValue7 * 0.8F;
        float floatValue20 = 0.00390625F;
        float floatValue21 = (float)MathHelper.floor_double(xCoordinate) * 0.00390625F;
        float floatValue22 = (float)MathHelper.floor_double(zCoordinate) * 0.00390625F;
        float floatValue23 = (float)(xCoordinate - (double)MathHelper.floor_double(xCoordinate));
        float floatValue24 = (float)(zCoordinate - (double)MathHelper.floor_double(zCoordinate));
        int k = 8;
        int l = 4;
        float floatValue25 = 9.765625E-4F;
        GlStateManager.scale(12.0F, 1.0F, 12.0F);

        for (int index = 0; index < 2; ++index)
        {
            if (index == 0)
            {
                GlStateManager.colorMask(false, false, false, false);
            }
            else
            {
                switch (pass)
                {
                    case 0:
                        GlStateManager.colorMask(false, true, true, true);
                        break;

                    case 1:
                        GlStateManager.colorMask(true, false, false, true);
                        break;

                    case 2:
                        GlStateManager.colorMask(true, true, true, true);
                }
            }

            this.cloudRenderer.renderGlList();
        }

        if (this.cloudRenderer.shouldUpdateGlList())
        {
            this.cloudRenderer.startUpdateGlList();

            try
            {
            for (int outerIndex = -3; outerIndex <= 4; ++outerIndex)
            {
                for (int innerIndex = -3; innerIndex <= 4; ++innerIndex)
                {
                    worldRenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
                    float floatValue26 = (float)(outerIndex * 8);
                    float floatValue27 = (float)(innerIndex * 8);
                    float floatValue28 = floatValue26 - floatValue23;
                    float floatValue29 = floatValue27 - floatValue24;

                    if (floatValue4 > -5.0F)
                    {
                        worldRenderer.pos((double)(floatValue28 + 0.0F), (double)(floatValue4 + 0.0F), (double)(floatValue29 + 8.0F)).tex((double)((floatValue26 + 0.0F) * 0.00390625F + floatValue21), (double)((floatValue27 + 8.0F) * 0.00390625F + floatValue22)).color(floatValue14, floatValue15, floatValue16, 0.8F).normal(0.0F, -1.0F, 0.0F).endVertex();
                        worldRenderer.pos((double)(floatValue28 + 8.0F), (double)(floatValue4 + 0.0F), (double)(floatValue29 + 8.0F)).tex((double)((floatValue26 + 8.0F) * 0.00390625F + floatValue21), (double)((floatValue27 + 8.0F) * 0.00390625F + floatValue22)).color(floatValue14, floatValue15, floatValue16, 0.8F).normal(0.0F, -1.0F, 0.0F).endVertex();
                        worldRenderer.pos((double)(floatValue28 + 8.0F), (double)(floatValue4 + 0.0F), (double)(floatValue29 + 0.0F)).tex((double)((floatValue26 + 8.0F) * 0.00390625F + floatValue21), (double)((floatValue27 + 0.0F) * 0.00390625F + floatValue22)).color(floatValue14, floatValue15, floatValue16, 0.8F).normal(0.0F, -1.0F, 0.0F).endVertex();
                        worldRenderer.pos((double)(floatValue28 + 0.0F), (double)(floatValue4 + 0.0F), (double)(floatValue29 + 0.0F)).tex((double)((floatValue26 + 0.0F) * 0.00390625F + floatValue21), (double)((floatValue27 + 0.0F) * 0.00390625F + floatValue22)).color(floatValue14, floatValue15, floatValue16, 0.8F).normal(0.0F, -1.0F, 0.0F).endVertex();
                    }

                    if (floatValue4 <= 5.0F)
                    {
                        worldRenderer.pos((double)(floatValue28 + 0.0F), (double)(floatValue4 + 4.0F - 9.765625E-4F), (double)(floatValue29 + 8.0F)).tex((double)((floatValue26 + 0.0F) * 0.00390625F + floatValue21), (double)((floatValue27 + 8.0F) * 0.00390625F + floatValue22)).color(floatValue5, floatValue6, floatValue7, 0.8F).normal(0.0F, 1.0F, 0.0F).endVertex();
                        worldRenderer.pos((double)(floatValue28 + 8.0F), (double)(floatValue4 + 4.0F - 9.765625E-4F), (double)(floatValue29 + 8.0F)).tex((double)((floatValue26 + 8.0F) * 0.00390625F + floatValue21), (double)((floatValue27 + 8.0F) * 0.00390625F + floatValue22)).color(floatValue5, floatValue6, floatValue7, 0.8F).normal(0.0F, 1.0F, 0.0F).endVertex();
                        worldRenderer.pos((double)(floatValue28 + 8.0F), (double)(floatValue4 + 4.0F - 9.765625E-4F), (double)(floatValue29 + 0.0F)).tex((double)((floatValue26 + 8.0F) * 0.00390625F + floatValue21), (double)((floatValue27 + 0.0F) * 0.00390625F + floatValue22)).color(floatValue5, floatValue6, floatValue7, 0.8F).normal(0.0F, 1.0F, 0.0F).endVertex();
                        worldRenderer.pos((double)(floatValue28 + 0.0F), (double)(floatValue4 + 4.0F - 9.765625E-4F), (double)(floatValue29 + 0.0F)).tex((double)((floatValue26 + 0.0F) * 0.00390625F + floatValue21), (double)((floatValue27 + 0.0F) * 0.00390625F + floatValue22)).color(floatValue5, floatValue6, floatValue7, 0.8F).normal(0.0F, 1.0F, 0.0F).endVertex();
                    }

                    if (outerIndex > -1)
                    {
                        for (int nestedIndex = 0; nestedIndex < 8; ++nestedIndex)
                        {
                            worldRenderer.pos((double)(floatValue28 + (float)nestedIndex + 0.0F), (double)(floatValue4 + 0.0F), (double)(floatValue29 + 8.0F)).tex((double)((floatValue26 + (float)nestedIndex + 0.5F) * 0.00390625F + floatValue21), (double)((floatValue27 + 8.0F) * 0.00390625F + floatValue22)).color(number32FloatValue, floatValue12, floatValue13, 0.8F).normal(-1.0F, 0.0F, 0.0F).endVertex();
                            worldRenderer.pos((double)(floatValue28 + (float)nestedIndex + 0.0F), (double)(floatValue4 + 4.0F), (double)(floatValue29 + 8.0F)).tex((double)((floatValue26 + (float)nestedIndex + 0.5F) * 0.00390625F + floatValue21), (double)((floatValue27 + 8.0F) * 0.00390625F + floatValue22)).color(number32FloatValue, floatValue12, floatValue13, 0.8F).normal(-1.0F, 0.0F, 0.0F).endVertex();
                            worldRenderer.pos((double)(floatValue28 + (float)nestedIndex + 0.0F), (double)(floatValue4 + 4.0F), (double)(floatValue29 + 0.0F)).tex((double)((floatValue26 + (float)nestedIndex + 0.5F) * 0.00390625F + floatValue21), (double)((floatValue27 + 0.0F) * 0.00390625F + floatValue22)).color(number32FloatValue, floatValue12, floatValue13, 0.8F).normal(-1.0F, 0.0F, 0.0F).endVertex();
                            worldRenderer.pos((double)(floatValue28 + (float)nestedIndex + 0.0F), (double)(floatValue4 + 0.0F), (double)(floatValue29 + 0.0F)).tex((double)((floatValue26 + (float)nestedIndex + 0.5F) * 0.00390625F + floatValue21), (double)((floatValue27 + 0.0F) * 0.00390625F + floatValue22)).color(number32FloatValue, floatValue12, floatValue13, 0.8F).normal(-1.0F, 0.0F, 0.0F).endVertex();
                        }
                    }

                    if (outerIndex <= 1)
                    {
                        for (int index2 = 0; index2 < 8; ++index2)
                        {
                            worldRenderer.pos((double)(floatValue28 + (float)index2 + 1.0F - 9.765625E-4F), (double)(floatValue4 + 0.0F), (double)(floatValue29 + 8.0F)).tex((double)((floatValue26 + (float)index2 + 0.5F) * 0.00390625F + floatValue21), (double)((floatValue27 + 8.0F) * 0.00390625F + floatValue22)).color(number32FloatValue, floatValue12, floatValue13, 0.8F).normal(1.0F, 0.0F, 0.0F).endVertex();
                            worldRenderer.pos((double)(floatValue28 + (float)index2 + 1.0F - 9.765625E-4F), (double)(floatValue4 + 4.0F), (double)(floatValue29 + 8.0F)).tex((double)((floatValue26 + (float)index2 + 0.5F) * 0.00390625F + floatValue21), (double)((floatValue27 + 8.0F) * 0.00390625F + floatValue22)).color(number32FloatValue, floatValue12, floatValue13, 0.8F).normal(1.0F, 0.0F, 0.0F).endVertex();
                            worldRenderer.pos((double)(floatValue28 + (float)index2 + 1.0F - 9.765625E-4F), (double)(floatValue4 + 4.0F), (double)(floatValue29 + 0.0F)).tex((double)((floatValue26 + (float)index2 + 0.5F) * 0.00390625F + floatValue21), (double)((floatValue27 + 0.0F) * 0.00390625F + floatValue22)).color(number32FloatValue, floatValue12, floatValue13, 0.8F).normal(1.0F, 0.0F, 0.0F).endVertex();
                            worldRenderer.pos((double)(floatValue28 + (float)index2 + 1.0F - 9.765625E-4F), (double)(floatValue4 + 0.0F), (double)(floatValue29 + 0.0F)).tex((double)((floatValue26 + (float)index2 + 0.5F) * 0.00390625F + floatValue21), (double)((floatValue27 + 0.0F) * 0.00390625F + floatValue22)).color(number32FloatValue, floatValue12, floatValue13, 0.8F).normal(1.0F, 0.0F, 0.0F).endVertex();
                        }
                    }

                    if (innerIndex > -1)
                    {
                        for (int index3 = 0; index3 < 8; ++index3)
                        {
                            worldRenderer.pos((double)(floatValue28 + 0.0F), (double)(floatValue4 + 4.0F), (double)(floatValue29 + (float)index3 + 0.0F)).tex((double)((floatValue26 + 0.0F) * 0.00390625F + floatValue21), (double)((floatValue27 + (float)index3 + 0.5F) * 0.00390625F + floatValue22)).color(floatValue17, floatValue18, floatValue19, 0.8F).normal(0.0F, 0.0F, -1.0F).endVertex();
                            worldRenderer.pos((double)(floatValue28 + 8.0F), (double)(floatValue4 + 4.0F), (double)(floatValue29 + (float)index3 + 0.0F)).tex((double)((floatValue26 + 8.0F) * 0.00390625F + floatValue21), (double)((floatValue27 + (float)index3 + 0.5F) * 0.00390625F + floatValue22)).color(floatValue17, floatValue18, floatValue19, 0.8F).normal(0.0F, 0.0F, -1.0F).endVertex();
                            worldRenderer.pos((double)(floatValue28 + 8.0F), (double)(floatValue4 + 0.0F), (double)(floatValue29 + (float)index3 + 0.0F)).tex((double)((floatValue26 + 8.0F) * 0.00390625F + floatValue21), (double)((floatValue27 + (float)index3 + 0.5F) * 0.00390625F + floatValue22)).color(floatValue17, floatValue18, floatValue19, 0.8F).normal(0.0F, 0.0F, -1.0F).endVertex();
                            worldRenderer.pos((double)(floatValue28 + 0.0F), (double)(floatValue4 + 0.0F), (double)(floatValue29 + (float)index3 + 0.0F)).tex((double)((floatValue26 + 0.0F) * 0.00390625F + floatValue21), (double)((floatValue27 + (float)index3 + 0.5F) * 0.00390625F + floatValue22)).color(floatValue17, floatValue18, floatValue19, 0.8F).normal(0.0F, 0.0F, -1.0F).endVertex();
                        }
                    }

                    if (innerIndex <= 1)
                    {
                        for (int index4 = 0; index4 < 8; ++index4)
                        {
                            worldRenderer.pos((double)(floatValue28 + 0.0F), (double)(floatValue4 + 4.0F), (double)(floatValue29 + (float)index4 + 1.0F - 9.765625E-4F)).tex((double)((floatValue26 + 0.0F) * 0.00390625F + floatValue21), (double)((floatValue27 + (float)index4 + 0.5F) * 0.00390625F + floatValue22)).color(floatValue17, floatValue18, floatValue19, 0.8F).normal(0.0F, 0.0F, 1.0F).endVertex();
                            worldRenderer.pos((double)(floatValue28 + 8.0F), (double)(floatValue4 + 4.0F), (double)(floatValue29 + (float)index4 + 1.0F - 9.765625E-4F)).tex((double)((floatValue26 + 8.0F) * 0.00390625F + floatValue21), (double)((floatValue27 + (float)index4 + 0.5F) * 0.00390625F + floatValue22)).color(floatValue17, floatValue18, floatValue19, 0.8F).normal(0.0F, 0.0F, 1.0F).endVertex();
                            worldRenderer.pos((double)(floatValue28 + 8.0F), (double)(floatValue4 + 0.0F), (double)(floatValue29 + (float)index4 + 1.0F - 9.765625E-4F)).tex((double)((floatValue26 + 8.0F) * 0.00390625F + floatValue21), (double)((floatValue27 + (float)index4 + 0.5F) * 0.00390625F + floatValue22)).color(floatValue17, floatValue18, floatValue19, 0.8F).normal(0.0F, 0.0F, 1.0F).endVertex();
                            worldRenderer.pos((double)(floatValue28 + 0.0F), (double)(floatValue4 + 0.0F), (double)(floatValue29 + (float)index4 + 1.0F - 9.765625E-4F)).tex((double)((floatValue26 + 0.0F) * 0.00390625F + floatValue21), (double)((floatValue27 + (float)index4 + 0.5F) * 0.00390625F + floatValue22)).color(floatValue17, floatValue18, floatValue19, 0.8F).normal(0.0F, 0.0F, 1.0F).endVertex();
                        }
                    }

                    tessellator.draw();
                }
            }
            }
            catch (Throwable throwable)
            {
                this.cloudRenderer.abortCapture();
                throw throwable instanceof RuntimeException ? (RuntimeException)throwable : new RuntimeException(throwable);
            }
            finally
            {
                if (CloudRenderer.isCapturing())
                {
                    this.cloudRenderer.endUpdateGlList();
                }
            }
        }

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.disableBlend();
        GlStateManager.enableCull();
    }

    public void updateChunks(long finishTimeNano)
    {
        finishTimeNano = (long)((double)finishTimeNano + 1.0E8D);
        this.displayListEntitiesDirty |= this.renderDispatcher.runChunkUploads(finishTimeNano);

        if (this.chunksToUpdateForced.size() > 0)
        {
            Iterator iterator = this.chunksToUpdateForced.iterator();

            while (iterator.hasNext())
            {
                RenderChunk renderChunk = (RenderChunk)iterator.next();

                if (!this.renderDispatcher.updateChunkLater(renderChunk))
                {
                    break;
                }

                renderChunk.setNeedsUpdate(false);
                iterator.remove();
                this.chunksToUpdate.remove(renderChunk);
                this.chunksToResortTransparency.remove(renderChunk);
            }
        }

        if (this.chunksToResortTransparency.size() > 0)
        {
            Iterator iterator2 = this.chunksToResortTransparency.iterator();

            if (iterator2.hasNext())
            {
                RenderChunk renderChunk2 = (RenderChunk)iterator2.next();

                if (this.renderDispatcher.updateTransparencyLater(renderChunk2))
                {
                    iterator2.remove();
                }
            }
        }

        double sixteenthDoubleValue = 0.0D;
        int i = Config.getUpdatesPerFrame();

        if (this.mc.gameSettings.ofLimitChunkUpdates)
        {
            i = Math.min(i, this.mc.gameSettings.ofChunkUpdateLimit);
        }

        if (!this.chunksToUpdate.isEmpty())
        {
            Iterator<RenderChunk> iterator1 = this.chunksToUpdate.iterator();

            while (iterator1.hasNext())
            {
                RenderChunk renderchunk1 = (RenderChunk)iterator1.next();
                boolean flag = renderchunk1.isChunkRegionEmpty();
                boolean flag1;

                if (flag)
                {
                    flag1 = this.renderDispatcher.updateChunkNow(renderchunk1);
                }
                else
                {
                    flag1 = this.renderDispatcher.updateChunkLater(renderchunk1);
                }

                if (!flag1)
                {
                    break;
                }

                renderchunk1.setNeedsUpdate(false);
                iterator1.remove();

                if (!flag)
                {
                    double doubleValue2 = 2.0D * RenderChunkUtils.getRelativeBufferSize(renderchunk1);
                    sixteenthDoubleValue += doubleValue2;

                    if (sixteenthDoubleValue > (double)i)
                    {
                        break;
                    }
                }
            }
        }
    }

    public void renderWorldBorder(Entity entityIn, float partialTicks)
    {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        WorldBorder worldborder = this.theWorld.getWorldBorder();
        double tenthDoubleValue = (double)(this.mc.gameSettings.renderDistanceChunks * 16);

        if (entityIn.posX >= worldborder.maxX() - tenthDoubleValue || entityIn.posX <= worldborder.minX() + tenthDoubleValue || entityIn.posZ >= worldborder.maxZ() - tenthDoubleValue || entityIn.posZ <= worldborder.minZ() + tenthDoubleValue)
        {
            if (Config.isShaders())
            {
                Shaders.pushProgram();
                Shaders.useProgram(Shaders.ProgramTexturedLit);
            }

            double fourteenthDoubleValue = 1.0D - worldborder.getClosestDistance(entityIn) / tenthDoubleValue;
            fourteenthDoubleValue *= fourteenthDoubleValue;
            fourteenthDoubleValue *= fourteenthDoubleValue;
            double number46DoubleValue = entityIn.lastTickPosX + (entityIn.posX - entityIn.lastTickPosX) * (double)partialTicks;
            double number61DoubleValue = entityIn.lastTickPosY + (entityIn.posY - entityIn.lastTickPosY) * (double)partialTicks;
            double number65DoubleValue = entityIn.lastTickPosZ + (entityIn.posZ - entityIn.lastTickPosZ) * (double)partialTicks;
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 1, 1, 0);
            this.renderEngine.bindTexture(locationForcefieldPng);
            GlStateManager.depthMask(false);
            GlStateManager.pushMatrix();
            int i = worldborder.getStatus().getID();
            float f = (float)(i >> 16 & 255) / 255.0F;
            float twentyFirstFloatValue = (float)(i >> 8 & 255) / 255.0F;
            float number31FloatValue = (float)(i & 255) / 255.0F;
            GlStateManager.color(f, twentyFirstFloatValue, number31FloatValue, (float)fourteenthDoubleValue);
            GlStateManager.doPolygonOffset(-3.0F, -3.0F);
            GlStateManager.enablePolygonOffset();
            GlStateManager.alphaFunc(516, 0.1F);
            GlStateManager.enableAlpha();
            GlStateManager.disableCull();
            float number33FloatValue = (float)(Minecraft.getSystemTime() % 3000L) / 3000.0F;
            float number34FloatValue = 0.0F;
            float number35FloatValue = 0.0F;
            float number36FloatValue = 128.0F;
            worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
            worldrenderer.setTranslation(-number46DoubleValue, -number61DoubleValue, -number65DoubleValue);
            double number69DoubleValue = Math.max((double)MathHelper.floor_double(number65DoubleValue - tenthDoubleValue), worldborder.minZ());
            double number71DoubleValue = Math.min((double)MathHelper.ceiling_double_int(number65DoubleValue + tenthDoubleValue), worldborder.maxZ());

            if (number46DoubleValue > worldborder.maxX() - tenthDoubleValue)
            {
                float number37FloatValue = 0.0F;

                for (double number72DoubleValue = number69DoubleValue; number72DoubleValue < number71DoubleValue; number37FloatValue += 0.5F)
                {
                    double number74DoubleValue = Math.min(1.0D, number71DoubleValue - number72DoubleValue);
                    float number38FloatValue = (float)number74DoubleValue * 0.5F;
                    worldrenderer.pos(worldborder.maxX(), 256.0D, number72DoubleValue).tex((double)(number33FloatValue + number37FloatValue), (double)(number33FloatValue + 0.0F)).endVertex();
                    worldrenderer.pos(worldborder.maxX(), 256.0D, number72DoubleValue + number74DoubleValue).tex((double)(number33FloatValue + number38FloatValue + number37FloatValue), (double)(number33FloatValue + 0.0F)).endVertex();
                    worldrenderer.pos(worldborder.maxX(), 0.0D, number72DoubleValue + number74DoubleValue).tex((double)(number33FloatValue + number38FloatValue + number37FloatValue), (double)(number33FloatValue + 128.0F)).endVertex();
                    worldrenderer.pos(worldborder.maxX(), 0.0D, number72DoubleValue).tex((double)(number33FloatValue + number37FloatValue), (double)(number33FloatValue + 128.0F)).endVertex();
                    ++number72DoubleValue;
                }
            }

            if (number46DoubleValue < worldborder.minX() + tenthDoubleValue)
            {
                float number39FloatValue = 0.0F;

                for (double number77DoubleValue = number69DoubleValue; number77DoubleValue < number71DoubleValue; number39FloatValue += 0.5F)
                {
                    double twentyFifthDoubleValue = Math.min(1.0D, number71DoubleValue - number77DoubleValue);
                    float twentyFifthFloatValue = (float)twentyFifthDoubleValue * 0.5F;
                    worldrenderer.pos(worldborder.minX(), 256.0D, number77DoubleValue).tex((double)(number33FloatValue + number39FloatValue), (double)(number33FloatValue + 0.0F)).endVertex();
                    worldrenderer.pos(worldborder.minX(), 256.0D, number77DoubleValue + twentyFifthDoubleValue).tex((double)(number33FloatValue + twentyFifthFloatValue + number39FloatValue), (double)(number33FloatValue + 0.0F)).endVertex();
                    worldrenderer.pos(worldborder.minX(), 0.0D, number77DoubleValue + twentyFifthDoubleValue).tex((double)(number33FloatValue + twentyFifthFloatValue + number39FloatValue), (double)(number33FloatValue + 128.0F)).endVertex();
                    worldrenderer.pos(worldborder.minX(), 0.0D, number77DoubleValue).tex((double)(number33FloatValue + number39FloatValue), (double)(number33FloatValue + 128.0F)).endVertex();
                    ++number77DoubleValue;
                }
            }

            number69DoubleValue = Math.max((double)MathHelper.floor_double(number46DoubleValue - tenthDoubleValue), worldborder.minX());
            number71DoubleValue = Math.min((double)MathHelper.ceiling_double_int(number46DoubleValue + tenthDoubleValue), worldborder.maxX());

            if (number65DoubleValue > worldborder.maxZ() - tenthDoubleValue)
            {
                float twentySecondFloatValue = 0.0F;

                for (double twentiethDoubleValue = number69DoubleValue; twentiethDoubleValue < number71DoubleValue; twentySecondFloatValue += 0.5F)
                {
                    double twentyEighthDoubleValue = Math.min(1.0D, number71DoubleValue - twentiethDoubleValue);
                    float twentySixthFloatValue = (float)twentyEighthDoubleValue * 0.5F;
                    worldrenderer.pos(twentiethDoubleValue, 256.0D, worldborder.maxZ()).tex((double)(number33FloatValue + twentySecondFloatValue), (double)(number33FloatValue + 0.0F)).endVertex();
                    worldrenderer.pos(twentiethDoubleValue + twentyEighthDoubleValue, 256.0D, worldborder.maxZ()).tex((double)(number33FloatValue + twentySixthFloatValue + twentySecondFloatValue), (double)(number33FloatValue + 0.0F)).endVertex();
                    worldrenderer.pos(twentiethDoubleValue + twentyEighthDoubleValue, 0.0D, worldborder.maxZ()).tex((double)(number33FloatValue + twentySixthFloatValue + twentySecondFloatValue), (double)(number33FloatValue + 128.0F)).endVertex();
                    worldrenderer.pos(twentiethDoubleValue, 0.0D, worldborder.maxZ()).tex((double)(number33FloatValue + twentySecondFloatValue), (double)(number33FloatValue + 128.0F)).endVertex();
                    ++twentiethDoubleValue;
                }
            }

            if (number65DoubleValue < worldborder.minZ() + tenthDoubleValue)
            {
                float twentyFourthFloatValue = 0.0F;

                for (double twentySecondDoubleValue = number69DoubleValue; twentySecondDoubleValue < number71DoubleValue; twentyFourthFloatValue += 0.5F)
                {
                    double number32DoubleValue = Math.min(1.0D, number71DoubleValue - twentySecondDoubleValue);
                    float twentySeventhFloatValue = (float)number32DoubleValue * 0.5F;
                    worldrenderer.pos(twentySecondDoubleValue, 256.0D, worldborder.minZ()).tex((double)(number33FloatValue + twentyFourthFloatValue), (double)(number33FloatValue + 0.0F)).endVertex();
                    worldrenderer.pos(twentySecondDoubleValue + number32DoubleValue, 256.0D, worldborder.minZ()).tex((double)(number33FloatValue + twentySeventhFloatValue + twentyFourthFloatValue), (double)(number33FloatValue + 0.0F)).endVertex();
                    worldrenderer.pos(twentySecondDoubleValue + number32DoubleValue, 0.0D, worldborder.minZ()).tex((double)(number33FloatValue + twentySeventhFloatValue + twentyFourthFloatValue), (double)(number33FloatValue + 128.0F)).endVertex();
                    worldrenderer.pos(twentySecondDoubleValue, 0.0D, worldborder.minZ()).tex((double)(number33FloatValue + twentyFourthFloatValue), (double)(number33FloatValue + 128.0F)).endVertex();
                    ++twentySecondDoubleValue;
                }
            }

            tessellator.draw();
            worldrenderer.setTranslation(0.0D, 0.0D, 0.0D);
            GlStateManager.enableCull();
            GlStateManager.disableAlpha();
            GlStateManager.doPolygonOffset(0.0F, 0.0F);
            GlStateManager.disablePolygonOffset();
            GlStateManager.enableAlpha();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            GlStateManager.disableBlend();
            GlStateManager.popMatrix();
            GlStateManager.depthMask(true);

            if (Config.isShaders())
            {
                Shaders.popProgram();
            }
        }
    }

    private void preRenderDamagedBlocks()
    {
        GlStateManager.tryBlendFuncSeparate(774, 768, 1, 0);
        GlStateManager.enableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 0.5F);
        GlStateManager.doPolygonOffset(-1.0F, -10.0F);
        GlStateManager.enablePolygonOffset();
        GlStateManager.alphaFunc(516, 0.1F);
        GlStateManager.enableAlpha();
        GlStateManager.pushMatrix();

        if (Config.isShaders())
        {
            ShadersRender.beginBlockDamage();
        }
    }

    private void postRenderDamagedBlocks()
    {
        GlStateManager.disableAlpha();
        GlStateManager.doPolygonOffset(0.0F, 0.0F);
        GlStateManager.disablePolygonOffset();
        GlStateManager.enableAlpha();
        GlStateManager.depthMask(true);
        GlStateManager.popMatrix();

        if (Config.isShaders())
        {
            ShadersRender.endBlockDamage();
        }
    }

    public void drawBlockDamageTexture(Tessellator tessellatorIn, WorldRenderer worldRendererIn, Entity entityIn, float partialTicks)
    {
        double doubleValue = entityIn.lastTickPosX + (entityIn.posX - entityIn.lastTickPosX) * (double)partialTicks;
        double secondDoubleValue = entityIn.lastTickPosY + (entityIn.posY - entityIn.lastTickPosY) * (double)partialTicks;
        double thirdDoubleValue = entityIn.lastTickPosZ + (entityIn.posZ - entityIn.lastTickPosZ) * (double)partialTicks;

        if (!this.damagedBlocks.isEmpty())
        {
            this.renderEngine.bindTexture(TextureMap.locationBlocksTexture);
            this.preRenderDamagedBlocks();
            worldRendererIn.begin(7, DefaultVertexFormats.BLOCK);
            worldRendererIn.setTranslation(-doubleValue, -secondDoubleValue, -thirdDoubleValue);
            worldRendererIn.noColor();
            Iterator<DestroyBlockProgress> iterator = this.damagedBlocks.values().iterator();

            while (iterator.hasNext())
            {
                DestroyBlockProgress destroyblockprogress = (DestroyBlockProgress)iterator.next();
                BlockPos blockpos = destroyblockprogress.getPosition();
                double fourthDoubleValue = (double)blockpos.getX() - doubleValue;
                double fifthDoubleValue = (double)blockpos.getY() - secondDoubleValue;
                double sixthDoubleValue = (double)blockpos.getZ() - thirdDoubleValue;
                Block block = this.theWorld.getBlockState(blockpos).getBlock();
                boolean flag = !(block instanceof BlockChest) && !(block instanceof BlockEnderChest) && !(block instanceof BlockSign) && !(block instanceof BlockSkull);

                if (flag)
                {
                    if (fourthDoubleValue * fourthDoubleValue + fifthDoubleValue * fifthDoubleValue + sixthDoubleValue * sixthDoubleValue > 1024.0D)
                    {
                        iterator.remove();
                    }
                    else
                    {
                        IBlockState iblockstate = this.theWorld.getBlockState(blockpos);

                        if (iblockstate.getBlock().getMaterial() != Material.air)
                        {
                            int i = destroyblockprogress.getPartialBlockDamage();
                            TextureAtlasSprite textureatlassprite = this.destroyBlockIcons[i];
                            BlockRendererDispatcher blockrendererdispatcher = this.mc.getBlockRendererDispatcher();
                            blockrendererdispatcher.renderBlockDamage(iblockstate, blockpos, textureatlassprite, this.theWorld);
                        }
                    }
                }
            }

            tessellatorIn.draw();
            worldRendererIn.setTranslation(0.0D, 0.0D, 0.0D);
            this.postRenderDamagedBlocks();
        }
    }

    public void drawSelectionBox(EntityPlayer player, MovingObjectPosition movingObjectPositionIn, int execute, float partialTicks)
    {
        if (execute == 0 && movingObjectPositionIn.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK)
        {
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            GlStateManager.color(0.0F, 0.0F, 0.0F, 0.4F);
            GL11.glLineWidth(1.0F);
            GlStateManager.disableTexture2D();

            if (Config.isShaders())
            {
                Shaders.disableTexture2D();
            }

            GlStateManager.depthMask(false);
            float f = 0.002F;
            BlockPos blockPos = movingObjectPositionIn.getBlockPos();
            Block block = this.theWorld.getBlockState(blockPos).getBlock();

            if (block.getMaterial() != Material.air && this.theWorld.getWorldBorder().contains(blockPos))
            {
                block.setBlockBoundsBasedOnState(this.theWorld, blockPos);
                double xCoordinate = player.lastTickPosX + (player.posX - player.lastTickPosX) * (double)partialTicks;
                double yCoordinate = player.lastTickPosY + (player.posY - player.lastTickPosY) * (double)partialTicks;
                double zCoordinate = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * (double)partialTicks;
                AxisAlignedBB axisAlignedBB = block.getSelectedBoundingBox(this.theWorld, blockPos);
                Block.EnumOffsetType block$enumoffsettype = block.getOffsetType();

                if (block$enumoffsettype != Block.EnumOffsetType.NONE)
                {
                    axisAlignedBB = BlockModelUtils.getOffsetBoundingBox(axisAlignedBB, block$enumoffsettype, blockPos);
                }

                drawSelectionBoundingBox(axisAlignedBB.expand(0.0020000000949949026D, 0.0020000000949949026D, 0.0020000000949949026D).offset(-xCoordinate, -yCoordinate, -zCoordinate));
            }

            GlStateManager.depthMask(true);
            GlStateManager.enableTexture2D();

            if (Config.isShaders())
            {
                Shaders.enableTexture2D();
            }

            GlStateManager.disableBlend();
        }
    }

    public static void drawSelectionBoundingBox(AxisAlignedBB boundingBox)
    {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldRenderer = tessellator.getWorldRenderer();
        worldRenderer.begin(3, DefaultVertexFormats.POSITION);
        worldRenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).endVertex();
        worldRenderer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.minZ).endVertex();
        worldRenderer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ).endVertex();
        worldRenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.maxZ).endVertex();
        worldRenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).endVertex();
        tessellator.draw();
        worldRenderer.begin(3, DefaultVertexFormats.POSITION);
        worldRenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).endVertex();
        worldRenderer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ).endVertex();
        worldRenderer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ).endVertex();
        worldRenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ).endVertex();
        worldRenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).endVertex();
        tessellator.draw();
        worldRenderer.begin(1, DefaultVertexFormats.POSITION);
        worldRenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).endVertex();
        worldRenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).endVertex();
        worldRenderer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.minZ).endVertex();
        worldRenderer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ).endVertex();
        worldRenderer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ).endVertex();
        worldRenderer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ).endVertex();
        worldRenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.maxZ).endVertex();
        worldRenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ).endVertex();
        tessellator.draw();
    }

    public static void drawOutlinedBoundingBox(AxisAlignedBB boundingBox, int red, int green, int blue, int alpha)
    {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldRenderer = tessellator.getWorldRenderer();
        worldRenderer.begin(3, DefaultVertexFormats.POSITION_COLOR);
        worldRenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).color(red, green, blue, alpha).endVertex();
        worldRenderer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.minZ).color(red, green, blue, alpha).endVertex();
        worldRenderer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ).color(red, green, blue, alpha).endVertex();
        worldRenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.maxZ).color(red, green, blue, alpha).endVertex();
        worldRenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).color(red, green, blue, alpha).endVertex();
        tessellator.draw();
        worldRenderer.begin(3, DefaultVertexFormats.POSITION_COLOR);
        worldRenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).color(red, green, blue, alpha).endVertex();
        worldRenderer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ).color(red, green, blue, alpha).endVertex();
        worldRenderer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ).color(red, green, blue, alpha).endVertex();
        worldRenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ).color(red, green, blue, alpha).endVertex();
        worldRenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).color(red, green, blue, alpha).endVertex();
        tessellator.draw();
        worldRenderer.begin(1, DefaultVertexFormats.POSITION_COLOR);
        worldRenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).color(red, green, blue, alpha).endVertex();
        worldRenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).color(red, green, blue, alpha).endVertex();
        worldRenderer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.minZ).color(red, green, blue, alpha).endVertex();
        worldRenderer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ).color(red, green, blue, alpha).endVertex();
        worldRenderer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ).color(red, green, blue, alpha).endVertex();
        worldRenderer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ).color(red, green, blue, alpha).endVertex();
        worldRenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.maxZ).color(red, green, blue, alpha).endVertex();
        worldRenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ).color(red, green, blue, alpha).endVertex();
        tessellator.draw();
    }

    private void markBlocksForUpdate(int seventhIntValue, int twelfthIntValue, int sixteenthIntValue, int ninthIntValue, int fourteenthIntValue, int seventeenthIntValue)
    {
        this.viewFrustum.markBlocksForUpdate(seventhIntValue, twelfthIntValue, sixteenthIntValue, ninthIntValue, fourteenthIntValue, seventeenthIntValue);
    }

    public void markBlockForUpdate(BlockPos pos)
    {
        int i = pos.getX();
        int j = pos.getY();
        int k = pos.getZ();
        this.markBlocksForUpdate(i - 1, j - 1, k - 1, i + 1, j + 1, k + 1);
    }

    public void notifyLightSet(BlockPos pos)
    {
        int i = pos.getX();
        int j = pos.getY();
        int k = pos.getZ();
        this.markBlocksForUpdate(i - 1, j - 1, k - 1, i + 1, j + 1, k + 1);
    }

    public void markBlockRangeForRenderUpdate(int eighthIntValue, int eleventhIntValue, int fifteenthIntValue, int tenthIntValue, int thirteenthIntValue, int eighteenthIntValue)
    {
        this.markBlocksForUpdate(eighthIntValue - 1, eleventhIntValue - 1, fifteenthIntValue - 1, tenthIntValue + 1, thirteenthIntValue + 1, eighteenthIntValue + 1);
    }

    public void playRecord(String recordName, BlockPos blockPosIn)
    {
        ISound isound = this.mapSoundPositions.get(blockPosIn);

        if (isound != null)
        {
            this.mc.getSoundHandler().stopSound(isound);
            this.mapSoundPositions.remove(blockPosIn);
        }

        if (recordName != null)
        {
            ItemRecord itemRecord = ItemRecord.getRecord(recordName);

            if (itemRecord != null)
            {
                this.mc.ingameGUI.setRecordPlayingMessage(itemRecord.getRecordNameLocal());
            }

            PositionedSoundRecord positionedsoundrecord = PositionedSoundRecord.create(new ResourceLocation(recordName), (float)blockPosIn.getX(), (float)blockPosIn.getY(), (float)blockPosIn.getZ());
            this.mapSoundPositions.put(blockPosIn, positionedsoundrecord);
            this.mc.getSoundHandler().playSound(positionedsoundrecord);
        }
    }

    public void playSound(String soundName, double x, double y, double z, float volume, float pitch)
    {
    }

    public void playSoundToNearExcept(EntityPlayer except, String soundName, double x, double y, double z, float volume, float pitch)
    {
    }

    public void spawnParticle(int particleID, boolean ignoreRange, final double xCoord, final double yCoord, final double zCoord, double xOffset, double yOffset, double zOffset, int... parameters)
    {
        try
        {
            this.spawnEntityFX(particleID, ignoreRange, xCoord, yCoord, zCoord, xOffset, yOffset, zOffset, parameters);
        }
        catch (Throwable throwable)
        {
            CrashReport crashReport = CrashReport.makeCrashReport(throwable, "Exception while adding particle");
            CrashReportCategory crashReportCategory = crashReport.makeCategory("Particle being added");
            crashReportCategory.addCrashSection("ID", Integer.valueOf(particleID));

            if (parameters != null)
            {
                crashReportCategory.addCrashSection("Parameters", parameters);
            }

            crashReportCategory.addCrashSectionCallable("Position", new Callable<String>()
            {
                public String call() throws Exception
                {
                    return CrashReportCategory.getCoordinateInfo(xCoord, yCoord, zCoord);
                }
            });
            throw new ReportedException(crashReport);
        }
    }

    private void spawnParticle(EnumParticleTypes particleIn, double xCoord, double yCoord, double zCoord, double xOffset, double yOffset, double zOffset, int... parameters)
    {
        this.spawnParticle(particleIn.getParticleID(), particleIn.getShouldIgnoreRange(), xCoord, yCoord, zCoord, xOffset, yOffset, zOffset, parameters);
    }

    private EntityFX spawnEntityFX(int particleID, boolean ignoreRange, double xCoord, double yCoord, double zCoord, double xOffset, double yOffset, double zOffset, int... parameters)
    {
        if (this.mc != null && this.mc.getRenderViewEntity() != null && this.mc.effectRenderer != null)
        {
            int i = this.mc.gameSettings.particleSetting;

            if (i == 1 && this.theWorld.rand.nextInt(3) == 0)
            {
                i = 2;
            }

            double thirteenthDoubleValue = this.mc.getRenderViewEntity().posX - xCoord;
            double eighteenthDoubleValue = this.mc.getRenderViewEntity().posY - yCoord;
            double number45DoubleValue = this.mc.getRenderViewEntity().posZ - zCoord;

            if (particleID == EnumParticleTypes.EXPLOSION_HUGE.getParticleID() && !Config.isAnimatedExplosion())
            {
                return null;
            }
            else if (particleID == EnumParticleTypes.EXPLOSION_LARGE.getParticleID() && !Config.isAnimatedExplosion())
            {
                return null;
            }
            else if (particleID == EnumParticleTypes.EXPLOSION_NORMAL.getParticleID() && !Config.isAnimatedExplosion())
            {
                return null;
            }
            else if (particleID == EnumParticleTypes.SUSPENDED.getParticleID() && !Config.isWaterParticles())
            {
                return null;
            }
            else if (particleID == EnumParticleTypes.SUSPENDED_DEPTH.getParticleID() && !Config.isVoidParticles())
            {
                return null;
            }
            else if (particleID == EnumParticleTypes.SMOKE_NORMAL.getParticleID() && !Config.isAnimatedSmoke())
            {
                return null;
            }
            else if (particleID == EnumParticleTypes.SMOKE_LARGE.getParticleID() && !Config.isAnimatedSmoke())
            {
                return null;
            }
            else if (particleID == EnumParticleTypes.SPELL_MOB.getParticleID() && !Config.isPotionParticles())
            {
                return null;
            }
            else if (particleID == EnumParticleTypes.SPELL_MOB_AMBIENT.getParticleID() && !Config.isPotionParticles())
            {
                return null;
            }
            else if (particleID == EnumParticleTypes.SPELL.getParticleID() && !Config.isPotionParticles())
            {
                return null;
            }
            else if (particleID == EnumParticleTypes.SPELL_INSTANT.getParticleID() && !Config.isPotionParticles())
            {
                return null;
            }
            else if (particleID == EnumParticleTypes.SPELL_WITCH.getParticleID() && !Config.isPotionParticles())
            {
                return null;
            }
            else if (particleID == EnumParticleTypes.PORTAL.getParticleID() && !Config.isPortalParticles())
            {
                return null;
            }
            else if (particleID == EnumParticleTypes.FLAME.getParticleID() && !Config.isAnimatedFlame())
            {
                return null;
            }
            else if (particleID == EnumParticleTypes.REDSTONE.getParticleID() && !Config.isAnimatedRedstone())
            {
                return null;
            }
            else if (particleID == EnumParticleTypes.DRIP_WATER.getParticleID() && !Config.isDrippingWaterLava())
            {
                return null;
            }
            else if (particleID == EnumParticleTypes.DRIP_LAVA.getParticleID() && !Config.isDrippingWaterLava())
            {
                return null;
            }
            else if (particleID == EnumParticleTypes.FIREWORKS_SPARK.getParticleID() && !Config.isFireworkParticles())
            {
                return null;
            }
            else
            {
                if (!ignoreRange)
                {
                    double number58DoubleValue = 256.0D;

                    if (particleID == EnumParticleTypes.CRIT.getParticleID())
                    {
                        number58DoubleValue = 38416.0D;
                    }

                    if (thirteenthDoubleValue * thirteenthDoubleValue + eighteenthDoubleValue * eighteenthDoubleValue + number45DoubleValue * number45DoubleValue > number58DoubleValue)
                    {
                        return null;
                    }

                    if (i > 1)
                    {
                        return null;
                    }
                }

                EntityFX entityfx = this.mc.effectRenderer.spawnEffectParticle(particleID, xCoord, yCoord, zCoord, xOffset, yOffset, zOffset, parameters);

                if (particleID == EnumParticleTypes.WATER_BUBBLE.getParticleID())
                {
                    CustomColors.updateWaterFX(entityfx, this.theWorld, xCoord, yCoord, zCoord, this.renderEnv);
                }

                if (particleID == EnumParticleTypes.WATER_SPLASH.getParticleID())
                {
                    CustomColors.updateWaterFX(entityfx, this.theWorld, xCoord, yCoord, zCoord, this.renderEnv);
                }

                if (particleID == EnumParticleTypes.WATER_DROP.getParticleID())
                {
                    CustomColors.updateWaterFX(entityfx, this.theWorld, xCoord, yCoord, zCoord, this.renderEnv);
                }

                if (particleID == EnumParticleTypes.TOWN_AURA.getParticleID())
                {
                    CustomColors.updateMyceliumFX(entityfx);
                }

                if (particleID == EnumParticleTypes.PORTAL.getParticleID())
                {
                    CustomColors.updatePortalFX(entityfx);
                }

                if (particleID == EnumParticleTypes.REDSTONE.getParticleID())
                {
                    CustomColors.updateReddustFX(entityfx, this.theWorld, xCoord, yCoord, zCoord);
                }

                return entityfx;
            }
        }
        else
        {
            return null;
        }
    }

    public void onEntityAdded(Entity entityIn)
    {
        RandomEntities.entityLoaded(entityIn, this.theWorld);

        if (Config.isDynamicLights())
        {
            DynamicLights.entityAdded(entityIn, this);
        }
    }

    public void onEntityRemoved(Entity entityIn)
    {
        RandomEntities.entityUnloaded(entityIn, this.theWorld);

        if (Config.isDynamicLights())
        {
            DynamicLights.entityRemoved(entityIn, this);
        }
    }

    public void deleteAllDisplayLists()
    {
    }

    public void broadcastSound(int soundID, BlockPos pos, int data)
    {
        switch (soundID)
        {
            case 1013:
            case 1018:
                if (this.mc.getRenderViewEntity() != null)
                {
                    double xCoordinate = (double)pos.getX() - this.mc.getRenderViewEntity().posX;
                    double yCoordinate = (double)pos.getY() - this.mc.getRenderViewEntity().posY;
                    double zCoordinate = (double)pos.getZ() - this.mc.getRenderViewEntity().posZ;
                    double doubleValue = MathHelper.length_double(xCoordinate, yCoordinate, zCoordinate);
                    double xCoordinate2 = this.mc.getRenderViewEntity().posX;
                    double yCoordinate2 = this.mc.getRenderViewEntity().posY;
                    double zCoordinate2 = this.mc.getRenderViewEntity().posZ;

                    if (doubleValue > 0.0D)
                    {
                        xCoordinate2 += xCoordinate / doubleValue * 2.0D;
                        yCoordinate2 += yCoordinate / doubleValue * 2.0D;
                        zCoordinate2 += zCoordinate / doubleValue * 2.0D;
                    }

                    if (soundID == 1013)
                    {
                        this.theWorld.playSound(xCoordinate2, yCoordinate2, zCoordinate2, "mob.wither.spawn", 1.0F, 1.0F, false);
                    }
                    else
                    {
                        this.theWorld.playSound(xCoordinate2, yCoordinate2, zCoordinate2, "mob.enderdragon.end", 5.0F, 1.0F, false);
                    }
                }

            default:
        }
    }

    public void playAuxSFX(EntityPlayer player, int sfxType, BlockPos blockPosIn, int data)
    {
        Random random = this.theWorld.rand;

        switch (sfxType)
        {
            case 1000:
                this.theWorld.playSoundAtPos(blockPosIn, "random.click", 1.0F, 1.0F, false);
                break;

            case 1001:
                this.theWorld.playSoundAtPos(blockPosIn, "random.click", 1.0F, 1.2F, false);
                break;

            case 1002:
                this.theWorld.playSoundAtPos(blockPosIn, "random.bow", 1.0F, 1.2F, false);
                break;

            case 1003:
                this.theWorld.playSoundAtPos(blockPosIn, "random.door_open", 1.0F, this.theWorld.rand.nextFloat() * 0.1F + 0.9F, false);
                break;

            case 1004:
                this.theWorld.playSoundAtPos(blockPosIn, "random.fizz", 0.5F, 2.6F + (random.nextFloat() - random.nextFloat()) * 0.8F, false);
                break;

            case 1005:
                if (Item.getItemById(data) instanceof ItemRecord)
                {
                    this.theWorld.playRecord(blockPosIn, "records." + ((ItemRecord)Item.getItemById(data)).recordName);
                }
                else
                {
                    this.theWorld.playRecord(blockPosIn, (String)null);
                }

                break;

            case 1006:
                this.theWorld.playSoundAtPos(blockPosIn, "random.door_close", 1.0F, this.theWorld.rand.nextFloat() * 0.1F + 0.9F, false);
                break;

            case 1007:
                this.theWorld.playSoundAtPos(blockPosIn, "mob.ghast.charge", 10.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F, false);
                break;

            case 1008:
                this.theWorld.playSoundAtPos(blockPosIn, "mob.ghast.fireball", 10.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F, false);
                break;

            case 1009:
                this.theWorld.playSoundAtPos(blockPosIn, "mob.ghast.fireball", 2.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F, false);
                break;

            case 1010:
                this.theWorld.playSoundAtPos(blockPosIn, "mob.zombie.wood", 2.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F, false);
                break;

            case 1011:
                this.theWorld.playSoundAtPos(blockPosIn, "mob.zombie.metal", 2.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F, false);
                break;

            case 1012:
                this.theWorld.playSoundAtPos(blockPosIn, "mob.zombie.woodbreak", 2.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F, false);
                break;

            case 1014:
                this.theWorld.playSoundAtPos(blockPosIn, "mob.wither.shoot", 2.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F, false);
                break;

            case 1015:
                this.theWorld.playSoundAtPos(blockPosIn, "mob.bat.takeoff", 0.05F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F, false);
                break;

            case 1016:
                this.theWorld.playSoundAtPos(blockPosIn, "mob.zombie.infect", 2.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F, false);
                break;

            case 1017:
                this.theWorld.playSoundAtPos(blockPosIn, "mob.zombie.unfect", 2.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F, false);
                break;

            case 1020:
                this.theWorld.playSoundAtPos(blockPosIn, "random.anvil_break", 1.0F, this.theWorld.rand.nextFloat() * 0.1F + 0.9F, false);
                break;

            case 1021:
                this.theWorld.playSoundAtPos(blockPosIn, "random.anvil_use", 1.0F, this.theWorld.rand.nextFloat() * 0.1F + 0.9F, false);
                break;

            case 1022:
                this.theWorld.playSoundAtPos(blockPosIn, "random.anvil_land", 0.3F, this.theWorld.rand.nextFloat() * 0.1F + 0.9F, false);
                break;

            case 2000:
                int i = data % 3 - 1;
                int j = data / 3 % 3 - 1;
                double doubleValue = (double)blockPosIn.getX() + (double)i * 0.6D + 0.5D;
                double secondDoubleValue = (double)blockPosIn.getY() + 0.5D;
                double thirdDoubleValue = (double)blockPosIn.getZ() + (double)j * 0.6D + 0.5D;

                for (int intValue = 0; intValue < 10; ++intValue)
                {
                    double fourthDoubleValue = random.nextDouble() * 0.2D + 0.01D;
                    double number35DoubleValue = doubleValue + (double)i * 0.01D + (random.nextDouble() - 0.5D) * (double)j * 0.5D;
                    double number38DoubleValue = secondDoubleValue + (random.nextDouble() - 0.5D) * 0.5D;
                    double number40DoubleValue = thirdDoubleValue + (double)j * 0.01D + (random.nextDouble() - 0.5D) * (double)i * 0.5D;
                    double number41DoubleValue = (double)i * fourthDoubleValue + random.nextGaussian() * 0.01D;
                    double number47DoubleValue = -0.03D + random.nextGaussian() * 0.01D;
                    double number49DoubleValue = (double)j * fourthDoubleValue + random.nextGaussian() * 0.01D;
                    this.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, number35DoubleValue, number38DoubleValue, number40DoubleValue, number41DoubleValue, number47DoubleValue, number49DoubleValue, EnumParticleTypes.EMPTY_ARGS);
                }

                return;

            case 2001:
                Block block = Block.getBlockById(data & 4095);

                if (block.getMaterial() != Material.air)
                {
                    this.mc.getSoundHandler().playSound(new PositionedSoundRecord(new ResourceLocation(block.stepSound.getBreakSound()), (block.stepSound.getVolume() + 1.0F) / 2.0F, block.stepSound.getFrequency() * 0.8F, (float)blockPosIn.getX() + 0.5F, (float)blockPosIn.getY() + 0.5F, (float)blockPosIn.getZ() + 0.5F));
                }

                this.mc.effectRenderer.addBlockDestroyEffects(blockPosIn, block.getStateFromMeta(data >> 12 & 255));
                break;

            case 2002:
                double fifthDoubleValue = (double)blockPosIn.getX();
                double number64DoubleValue = (double)blockPosIn.getY();
                double number67DoubleValue = (double)blockPosIn.getZ();
                int[] potionParticleParameters = new int[] {Item.getIdFromItem(Items.potionitem), data};

                for (int k = 0; k < 8; ++k)
                {
                    this.spawnParticle(EnumParticleTypes.ITEM_CRACK, fifthDoubleValue, number64DoubleValue, number67DoubleValue, random.nextGaussian() * 0.15D, random.nextDouble() * 0.2D, random.nextGaussian() * 0.15D, potionParticleParameters);
                }

                int sixthIntValue = Items.potionitem.getColorFromDamage(data);
                float f = (float)(sixthIntValue >> 16 & 255) / 255.0F;
                float floatValue = (float)(sixthIntValue >> 8 & 255) / 255.0F;
                float secondFloatValue = (float)(sixthIntValue >> 0 & 255) / 255.0F;
                EnumParticleTypes enumparticletypes = EnumParticleTypes.SPELL;

                if (Items.potionitem.isEffectInstant(data))
                {
                    enumparticletypes = EnumParticleTypes.SPELL_INSTANT;
                }

                for (int secondIntValue = 0; secondIntValue < 100; ++secondIntValue)
                {
                    double sixthDoubleValue = random.nextDouble() * 4.0D;
                    double seventhDoubleValue = random.nextDouble() * Math.PI * 2.0D;
                    double twentyThirdDoubleValue = Math.cos(seventhDoubleValue) * sixthDoubleValue;
                    double number53DoubleValue = 0.01D + random.nextDouble() * 0.5D;
                    double number55DoubleValue = Math.sin(seventhDoubleValue) * sixthDoubleValue;
                    EntityFX entityfx = this.spawnEntityFX(enumparticletypes.getParticleID(), enumparticletypes.getShouldIgnoreRange(), fifthDoubleValue + twentyThirdDoubleValue * 0.1D, number64DoubleValue + 0.3D, number67DoubleValue + number55DoubleValue * 0.1D, twentyThirdDoubleValue, number53DoubleValue, number55DoubleValue, EnumParticleTypes.EMPTY_ARGS);

                    if (entityfx != null)
                    {
                        float thirdFloatValue = 0.75F + random.nextFloat() * 0.25F;
                        entityfx.setRBGColorF(f * thirdFloatValue, floatValue * thirdFloatValue, secondFloatValue * thirdFloatValue);
                        entityfx.multiplyVelocity((float)sixthDoubleValue);
                    }
                }

                this.theWorld.playSoundAtPos(blockPosIn, "game.potion.smash", 1.0F, this.theWorld.rand.nextFloat() * 0.1F + 0.9F, false);
                break;

            case 2003:
                double eighthDoubleValue = (double)blockPosIn.getX() + 0.5D;
                double number75DoubleValue = (double)blockPosIn.getY();
                double nineteenthDoubleValue = (double)blockPosIn.getZ() + 0.5D;
                int[] enderEyeParticleParameters = new int[] {Item.getIdFromItem(Items.ender_eye)};

                for (int thirdIntValue = 0; thirdIntValue < 8; ++thirdIntValue)
                {
                    this.spawnParticle(EnumParticleTypes.ITEM_CRACK, eighthDoubleValue, number75DoubleValue, nineteenthDoubleValue, random.nextGaussian() * 0.15D, random.nextDouble() * 0.2D, random.nextGaussian() * 0.15D, enderEyeParticleParameters);
                }

                for (double ninthDoubleValue = 0.0D; ninthDoubleValue < (Math.PI * 2D); ninthDoubleValue += 0.15707963267948966D)
                {
                    this.spawnParticle(EnumParticleTypes.PORTAL, eighthDoubleValue + Math.cos(ninthDoubleValue) * 5.0D, number75DoubleValue - 0.4D, nineteenthDoubleValue + Math.sin(ninthDoubleValue) * 5.0D, Math.cos(ninthDoubleValue) * -5.0D, 0.0D, Math.sin(ninthDoubleValue) * -5.0D, EnumParticleTypes.EMPTY_ARGS);
                    this.spawnParticle(EnumParticleTypes.PORTAL, eighthDoubleValue + Math.cos(ninthDoubleValue) * 5.0D, number75DoubleValue - 0.4D, nineteenthDoubleValue + Math.sin(ninthDoubleValue) * 5.0D, Math.cos(ninthDoubleValue) * -7.0D, 0.0D, Math.sin(ninthDoubleValue) * -7.0D, EnumParticleTypes.EMPTY_ARGS);
                }

                return;

            case 2004:
                for (int l = 0; l < 20; ++l)
                {
                    double twentySixthDoubleValue = (double)blockPosIn.getX() + 0.5D + ((double)this.theWorld.rand.nextFloat() - 0.5D) * 2.0D;
                    double thirtiethDoubleValue = (double)blockPosIn.getY() + 0.5D + ((double)this.theWorld.rand.nextFloat() - 0.5D) * 2.0D;
                    double number31DoubleValue = (double)blockPosIn.getZ() + 0.5D + ((double)this.theWorld.rand.nextFloat() - 0.5D) * 2.0D;
                    this.theWorld.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, twentySixthDoubleValue, thirtiethDoubleValue, number31DoubleValue, 0.0D, 0.0D, 0.0D, EnumParticleTypes.EMPTY_ARGS);
                    this.theWorld.spawnParticle(EnumParticleTypes.FLAME, twentySixthDoubleValue, thirtiethDoubleValue, number31DoubleValue, 0.0D, 0.0D, 0.0D, EnumParticleTypes.EMPTY_ARGS);
                }

                return;

            case 2005:
                ItemDye.spawnBonemealParticles(this.theWorld, blockPosIn, data);
        }
    }

    public void sendBlockBreakProgress(int breakerId, BlockPos pos, int progress)
    {
        if (progress >= 0 && progress < 10)
        {
            DestroyBlockProgress destroyblockprogress = this.damagedBlocks.get(Integer.valueOf(breakerId));

            if (destroyblockprogress == null || destroyblockprogress.getPosition().getX() != pos.getX() || destroyblockprogress.getPosition().getY() != pos.getY() || destroyblockprogress.getPosition().getZ() != pos.getZ())
            {
                destroyblockprogress = new DestroyBlockProgress(breakerId, pos);
                this.damagedBlocks.put(Integer.valueOf(breakerId), destroyblockprogress);
            }

            destroyblockprogress.setPartialBlockDamage(progress);
            destroyblockprogress.setCloudUpdateTick(this.cloudTickCounter);
        }
        else
        {
            this.damagedBlocks.remove(Integer.valueOf(breakerId));
        }
    }

    public void setDisplayListEntitiesDirty()
    {
        this.displayListEntitiesDirty = true;
    }

    public boolean hasNoChunkUpdates()
    {
        return this.chunksToUpdate.isEmpty() && this.renderDispatcher.hasChunkUpdates();
    }

    public void resetClouds()
    {
        this.cloudRenderer.reset();
    }

    public int getCountRenderers()
    {
        return this.viewFrustum.renderChunks.length;
    }

    public int getCountActiveRenderers()
    {
        return this.renderInfos.size();
    }

    public int getCountEntitiesRendered()
    {
        return this.countEntitiesRendered;
    }

    public int getCountTileEntitiesRendered()
    {
        return this.countTileEntitiesRendered;
    }

    public int getCountLoadedChunks()
    {
        if (this.theWorld == null)
        {
            return 0;
        }
        else
        {
            IChunkProvider ichunkprovider = this.theWorld.getChunkProvider();

            if (ichunkprovider == null)
            {
                return 0;
            }
            else
            {
                if (ichunkprovider != this.worldChunkProvider)
                {
                    this.worldChunkProvider = ichunkprovider;
                    this.worldChunkProviderMap = (LongHashMap)Reflector.getFieldValue(ichunkprovider, Reflector.ChunkProviderClient_chunkMapping);
                }

                return this.worldChunkProviderMap == null ? 0 : this.worldChunkProviderMap.getNumHashElements();
            }
        }
    }

    public int getCountChunksToUpdate()
    {
        return this.chunksToUpdate.size();
    }

    public RenderChunk getRenderChunk(BlockPos pos)
    {
        return this.viewFrustum.getRenderChunk(pos);
    }

    public WorldClient getWorld()
    {
        return this.theWorld;
    }

    private void clearRenderInfos()
    {
        if (renderEntitiesCounter > 0)
        {
            this.renderInfos = new ArrayList(this.renderInfos.size() + 16);
            this.renderInfosEntities = new ArrayList(this.renderInfosEntities.size() + 16);
            this.renderInfosTileEntities = new ArrayList(this.renderInfosTileEntities.size() + 16);
        }
        else
        {
            this.renderInfos.clear();
            this.renderInfosEntities.clear();
            this.renderInfosTileEntities.clear();
        }
    }

    public void onPlayerPositionSet()
    {
        if (this.firstWorldLoad)
        {
            this.loadRenderers();
            this.firstWorldLoad = false;
        }
    }

    public void pauseChunkUpdates()
    {
        if (this.renderDispatcher != null)
        {
            this.renderDispatcher.pauseChunkUpdates();
        }
    }

    public void resumeChunkUpdates()
    {
        if (this.renderDispatcher != null)
        {
            this.renderDispatcher.resumeChunkUpdates();
        }
    }

    public void updateTileEntities(Collection<TileEntity> tileEntitiesToRemove, Collection<TileEntity> tileEntitiesToAdd)
    {
        synchronized (this.setTileEntities)
        {
            this.setTileEntities.removeAll(tileEntitiesToRemove);
            this.setTileEntities.addAll(tileEntitiesToAdd);
        }
    }

    public static class ContainerLocalRenderInformation
    {
        final RenderChunk renderChunk;
        EnumFacing facing;
        int setFacing;

        public ContainerLocalRenderInformation(RenderChunk renderChunkIn, EnumFacing facingIn, int setFacingIn)
        {
            this.renderChunk = renderChunkIn;
            this.facing = facingIn;
            this.setFacing = setFacingIn;
        }

        public void setFacingBit(byte facingBit, EnumFacing facingIn)
        {
            this.setFacing = this.setFacing | facingBit | 1 << facingIn.ordinal();
        }

        public boolean isFacingBit(EnumFacing facingIn)
        {
            return (this.setFacing & 1 << facingIn.ordinal()) > 0;
        }

        private void initialize(EnumFacing facingIn, int setFacingIn)
        {
            this.facing = facingIn;
            this.setFacing = setFacingIn;
        }
    }
}
