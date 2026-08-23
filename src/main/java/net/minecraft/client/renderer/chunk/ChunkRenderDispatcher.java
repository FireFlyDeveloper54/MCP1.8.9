package net.minecraft.client.renderer.chunk;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListenableFutureTask;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RegionRenderCacheBuilder;
import net.minecraft.client.renderer.VertexBufferUploader;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.util.MathHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ChunkRenderDispatcher
{
    private static final Logger logger = LogManager.getLogger();
    private static final ThreadFactory threadFactory = (new ThreadFactoryBuilder()).setNameFormat("Chunk Batcher %d").setDaemon(true).build();
    private final List<ChunkRenderWorker> listThreadedWorkers;
    private final BlockingQueue<ChunkCompileTaskGenerator> queueChunkUpdates;
    private final BlockingQueue<RegionRenderCacheBuilder> queueFreeRenderBuilders;
    private final VertexBufferUploader vertexUploader;
    private final Queue < ListenableFutureTask<? >> queueChunkUploads;
    private final ChunkRenderWorker renderWorker;
    private final int countRenderBuilders;
    private List<RegionRenderCacheBuilder> listPausedBuilders;

    public ChunkRenderDispatcher()
    {
        this(-1);
    }

    public ChunkRenderDispatcher(int renderBuilderCount)
    {
        this.listThreadedWorkers = Lists.<ChunkRenderWorker>newArrayList();
        this.queueChunkUpdates = Queues.<ChunkCompileTaskGenerator>newArrayBlockingQueue(100);
        this.vertexUploader = new VertexBufferUploader();
        this.queueChunkUploads = Queues. < ListenableFutureTask<? >> newArrayDeque();
        this.listPausedBuilders = new ArrayList<RegionRenderCacheBuilder>();
        int maxBuilderCountByMemory = Math.max(1, (int)((double)Runtime.getRuntime().maxMemory() * 0.3D) / 10485760);
        int workerCount = Math.max(1, MathHelper.clamp_int(Runtime.getRuntime().availableProcessors() - 2, 1, maxBuilderCountByMemory / 5));

        if (renderBuilderCount < 0)
        {
            this.countRenderBuilders = MathHelper.clamp_int(workerCount * 8, 1, maxBuilderCountByMemory);
        }
        else
        {
            this.countRenderBuilders = renderBuilderCount;
        }

        for (int workerIndex = 0; workerIndex < workerCount; ++workerIndex)
        {
            ChunkRenderWorker chunkRenderWorker = new ChunkRenderWorker(this);
            Thread thread = threadFactory.newThread(chunkRenderWorker);
            thread.start();
            this.listThreadedWorkers.add(chunkRenderWorker);
        }

        this.queueFreeRenderBuilders = Queues.<RegionRenderCacheBuilder>newArrayBlockingQueue(this.countRenderBuilders);

        for (int builderIndex = 0; builderIndex < this.countRenderBuilders; ++builderIndex)
        {
            this.queueFreeRenderBuilders.add(new RegionRenderCacheBuilder());
        }

        this.renderWorker = new ChunkRenderWorker(this, new RegionRenderCacheBuilder());
    }

    public String getDebugInfo()
    {
        return String.format(Locale.ROOT, "pC: %03d, pU: %1d, aB: %1d", this.queueChunkUpdates.size(), this.queueChunkUploads.size(), this.queueFreeRenderBuilders.size());
    }

    public boolean runChunkUploads(long finishTimeNano)
    {
        boolean ranUpload = false;

        while (true)
        {
            boolean ranTask = false;
            ListenableFutureTask<?> listenableFutureTask = null;

            synchronized (this.queueChunkUploads)
            {
                listenableFutureTask = this.queueChunkUploads.poll();
            }

            if (listenableFutureTask != null)
            {
                listenableFutureTask.run();
                ranTask = true;
                ranUpload = true;
            }

            if (finishTimeNano == 0L || !ranTask)
            {
                break;
            }

            long timeRemaining = finishTimeNano - System.nanoTime();

            if (timeRemaining < 0L)
            {
                break;
            }
        }

        return ranUpload;
    }

    public boolean updateChunkLater(RenderChunk chunkRenderer)
    {
        chunkRenderer.getLockCompileTask().lock();
        boolean queued;

        try
        {
            final ChunkCompileTaskGenerator chunkCompileTaskGenerator = chunkRenderer.makeCompileTaskChunk();
            chunkCompileTaskGenerator.addFinishRunnable(new Runnable()
            {
                public void run()
                {
                    ChunkRenderDispatcher.this.queueChunkUpdates.remove(chunkCompileTaskGenerator);
                }
            });
            boolean offered = this.queueChunkUpdates.offer(chunkCompileTaskGenerator);

            if (!offered)
            {
                chunkCompileTaskGenerator.finish();
            }

            queued = offered;
        }
        finally
        {
            chunkRenderer.getLockCompileTask().unlock();
        }

        return queued;
    }

    public boolean updateChunkNow(RenderChunk chunkRenderer)
    {
        chunkRenderer.getLockCompileTask().lock();
        boolean updated;

        try
        {
            ChunkCompileTaskGenerator chunkCompileTaskGenerator = chunkRenderer.makeCompileTaskChunk();

            try
            {
                this.renderWorker.processTask(chunkCompileTaskGenerator);
            }
            catch (InterruptedException caughtInterruptedException)
            {
                ;
            }

            updated = true;
        }
        finally
        {
            chunkRenderer.getLockCompileTask().unlock();
        }

        return updated;
    }

    public void stopChunkUpdates()
    {
        this.clearChunkUpdates();

        while (this.runChunkUploads(0L))
        {
            ;
        }

        List<RegionRenderCacheBuilder> list = Lists.<RegionRenderCacheBuilder>newArrayList();

        while (list.size() != this.countRenderBuilders)
        {
            try
            {
                list.add(this.allocateRenderBuilder());
            }
            catch (InterruptedException caughtInterruptedException)
            {
                ;
            }
        }

        this.queueFreeRenderBuilders.addAll(list);
    }

    public void freeRenderBuilder(RegionRenderCacheBuilder renderBuilder)
    {
        this.queueFreeRenderBuilders.add(renderBuilder);
    }

    public RegionRenderCacheBuilder allocateRenderBuilder() throws InterruptedException
    {
        return this.queueFreeRenderBuilders.take();
    }

    public ChunkCompileTaskGenerator getNextChunkUpdate() throws InterruptedException
    {
        return this.queueChunkUpdates.take();
    }

    public boolean updateTransparencyLater(RenderChunk chunkRenderer)
    {
        chunkRenderer.getLockCompileTask().lock();
        boolean queued;

        try
        {
            final ChunkCompileTaskGenerator chunkCompileTaskGenerator = chunkRenderer.makeCompileTaskTransparency();

            if (chunkCompileTaskGenerator != null)
            {
                chunkCompileTaskGenerator.addFinishRunnable(new Runnable()
                {
                    public void run()
                    {
                        ChunkRenderDispatcher.this.queueChunkUpdates.remove(chunkCompileTaskGenerator);
                    }
                });
                boolean offered = this.queueChunkUpdates.offer(chunkCompileTaskGenerator);
                return offered;
            }

            queued = true;
        }
        finally
        {
            chunkRenderer.getLockCompileTask().unlock();
        }

        return queued;
    }

    public ListenableFuture<Object> uploadChunk(final EnumWorldBlockLayer layer, final WorldRenderer worldRenderer, final RenderChunk chunkRenderer, final CompiledChunk compiledChunkIn)
    {
        if (Minecraft.getMinecraft().isCallingFromMinecraftThread())
        {
            this.uploadVertexBuffer(worldRenderer, chunkRenderer.getVertexBufferByLayer(layer.ordinal()));

            worldRenderer.setTranslation(0.0D, 0.0D, 0.0D);
            return Futures.<Object>immediateFuture((Object)null);
        }
        else
        {
            ListenableFutureTask<Object> listenablefuturetask = ListenableFutureTask.<Object>create(new Runnable()
            {
                public void run()
                {
                    ChunkRenderDispatcher.this.uploadChunk(layer, worldRenderer, chunkRenderer, compiledChunkIn);
                }
            }, (Object)null);

            synchronized (this.queueChunkUploads)
            {
                this.queueChunkUploads.add(listenablefuturetask);
                return listenablefuturetask;
            }
        }
    }

    private void uploadVertexBuffer(WorldRenderer worldRenderer, VertexBuffer vertexBufferIn)
    {
        this.vertexUploader.setVertexBuffer(vertexBufferIn);
        this.vertexUploader.draw(worldRenderer);
    }

    public void clearChunkUpdates()
    {
        while (!this.queueChunkUpdates.isEmpty())
        {
            ChunkCompileTaskGenerator chunkCompileTaskGenerator = this.queueChunkUpdates.poll();

            if (chunkCompileTaskGenerator != null)
            {
                chunkCompileTaskGenerator.finish();
            }
        }
    }

    public boolean hasChunkUpdates()
    {
        return this.queueChunkUpdates.isEmpty() && this.queueChunkUploads.isEmpty();
    }

    public void pauseChunkUpdates()
    {
        while (this.listPausedBuilders.size() != this.countRenderBuilders)
        {
            try
            {
                this.runChunkUploads(Long.MAX_VALUE);
                RegionRenderCacheBuilder regionRenderCacheBuilder = this.queueFreeRenderBuilders.poll(100L, TimeUnit.MILLISECONDS);

                if (regionRenderCacheBuilder != null)
                {
                    this.listPausedBuilders.add(regionRenderCacheBuilder);
                }
            }
            catch (InterruptedException caughtInterruptedException)
            {
                ;
            }
        }
    }

    public void resumeChunkUpdates()
    {
        this.queueFreeRenderBuilders.addAll(this.listPausedBuilders);
        this.listPausedBuilders.clear();
    }
}
