package net.minecraft.client.renderer.chunk;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RegionRenderCacheBuilder;
import net.minecraft.crash.CrashReport;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumWorldBlockLayer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ChunkRenderWorker implements Runnable
{
    private static final Logger LOGGER = LogManager.getLogger();
    private final ChunkRenderDispatcher chunkRenderDispatcher;
    private final RegionRenderCacheBuilder regionRenderCacheBuilder;

    public ChunkRenderWorker(ChunkRenderDispatcher chunkRenderDispatcherIn)
    {
        this(chunkRenderDispatcherIn, (RegionRenderCacheBuilder)null);
    }

    public ChunkRenderWorker(ChunkRenderDispatcher chunkRenderDispatcherIn, RegionRenderCacheBuilder regionRenderCacheBuilderIn)
    {
        this.chunkRenderDispatcher = chunkRenderDispatcherIn;
        this.regionRenderCacheBuilder = regionRenderCacheBuilderIn;
    }

    public void run()
    {
        while (true)
        {
            try
            {
                this.processTask(this.chunkRenderDispatcher.getNextChunkUpdate());
            }
            catch (InterruptedException interruptedException)
            {
                LOGGER.debug("Stopping due to interrupt");
                return;
            }
            catch (Throwable throwable)
            {
                CrashReport crashReport = CrashReport.makeCrashReport(throwable, "Batching chunks");
                Minecraft.getMinecraft().crashed(Minecraft.getMinecraft().addGraphicsAndWorldToCrashReport(crashReport));
                return;
            }
        }
    }

    protected void processTask(final ChunkCompileTaskGenerator generator) throws InterruptedException
    {
        generator.getLock().lock();

        try
        {
            if (generator.getStatus() != ChunkCompileTaskGenerator.Status.PENDING)
            {
                if (!generator.isFinished())
                {
                    LOGGER.warn("Chunk render task was " + generator.getStatus() + " when I expected it to be pending; ignoring task");
                }

                return;
            }

            generator.setStatus(ChunkCompileTaskGenerator.Status.COMPILING);
        }
        finally
        {
            generator.getLock().unlock();
        }

        Entity renderViewEntity = Minecraft.getMinecraft().getRenderViewEntity();

        if (renderViewEntity == null)
        {
            generator.finish();
        }
        else
        {
            generator.setRegionRenderCacheBuilder(this.getRegionRenderCacheBuilder());
            float renderViewX = (float)renderViewEntity.posX;
            float renderViewY = (float)renderViewEntity.posY + renderViewEntity.getEyeHeight();
            float renderViewZ = (float)renderViewEntity.posZ;
            ChunkCompileTaskGenerator.Type generatorType = generator.getType();

            if (generatorType == ChunkCompileTaskGenerator.Type.REBUILD_CHUNK)
            {
                generator.getRenderChunk().rebuildChunk(renderViewX, renderViewY, renderViewZ, generator);
            }
            else if (generatorType == ChunkCompileTaskGenerator.Type.RESORT_TRANSPARENCY)
            {
                generator.getRenderChunk().resortTransparency(renderViewX, renderViewY, renderViewZ, generator);
            }

            generator.getLock().lock();

            try
            {
                if (generator.getStatus() != ChunkCompileTaskGenerator.Status.COMPILING)
                {
                    if (!generator.isFinished())
                    {
                        LOGGER.warn("Chunk render task was " + generator.getStatus() + " when I expected it to be compiling; aborting task");
                    }

                    this.freeRenderBuilder(generator);
                    return;
                }

                generator.setStatus(ChunkCompileTaskGenerator.Status.UPLOADING);
            }
            finally
            {
                generator.getLock().unlock();
            }

            final CompiledChunk compiledChunk = generator.getCompiledChunk();
            ArrayList uploadFutures = Lists.newArrayList();

            if (generatorType == ChunkCompileTaskGenerator.Type.REBUILD_CHUNK)
            {
                for (EnumWorldBlockLayer enumworldblocklayer : RenderChunk.ENUM_WORLD_BLOCK_LAYERS)
                {
                    if (compiledChunk.isLayerStarted(enumworldblocklayer))
                    {
                        uploadFutures.add(this.chunkRenderDispatcher.uploadChunk(enumworldblocklayer, generator.getRegionRenderCacheBuilder().getWorldRendererByLayer(enumworldblocklayer), generator.getRenderChunk(), compiledChunk));
                    }
                }
            }
            else if (generatorType == ChunkCompileTaskGenerator.Type.RESORT_TRANSPARENCY)
            {
                uploadFutures.add(this.chunkRenderDispatcher.uploadChunk(EnumWorldBlockLayer.TRANSLUCENT, generator.getRegionRenderCacheBuilder().getWorldRendererByLayer(EnumWorldBlockLayer.TRANSLUCENT), generator.getRenderChunk(), compiledChunk));
            }

            final ListenableFuture<List<Object>> uploadFuture = Futures.allAsList(uploadFutures);
            generator.addFinishRunnable(new Runnable()
            {
                public void run()
                {
                    uploadFuture.cancel(false);
                }
            });
            Futures.addCallback(uploadFuture, new FutureCallback<List<Object>>()
            {
                public void onSuccess(List<Object> uploadResults)
                {
                    ChunkRenderWorker.this.freeRenderBuilder(generator);
                    generator.getLock().lock();
                    label21:
                    {
                        try
                        {
                            if (generator.getStatus() == ChunkCompileTaskGenerator.Status.UPLOADING)
                            {
                                generator.setStatus(ChunkCompileTaskGenerator.Status.DONE);
                                break label21;
                            }

                            if (!generator.isFinished())
                            {
                                ChunkRenderWorker.LOGGER.warn("Chunk render task was " + generator.getStatus() + " when I expected it to be uploading; aborting task");
                            }
                        }
                        finally
                        {
                            generator.getLock().unlock();
                        }

                        return;
                    }
                    generator.getRenderChunk().setCompiledChunk(compiledChunk);
                }
                public void onFailure(Throwable throwable)
                {
                    ChunkRenderWorker.this.freeRenderBuilder(generator);

                    if (!(throwable instanceof CancellationException) && !(throwable instanceof InterruptedException))
                    {
                        Minecraft.getMinecraft().crashed(CrashReport.makeCrashReport(throwable, "Rendering chunk"));
                    }
                }
            }, MoreExecutors.directExecutor());
        }
    }

    private RegionRenderCacheBuilder getRegionRenderCacheBuilder() throws InterruptedException
    {
        return this.regionRenderCacheBuilder != null ? this.regionRenderCacheBuilder : this.chunkRenderDispatcher.allocateRenderBuilder();
    }

    private void freeRenderBuilder(ChunkCompileTaskGenerator taskGenerator)
    {
        if (this.regionRenderCacheBuilder == null)
        {
            this.chunkRenderDispatcher.freeRenderBuilder(taskGenerator.getRegionRenderCacheBuilder());
        }
    }
}
