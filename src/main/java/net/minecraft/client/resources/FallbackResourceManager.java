package net.minecraft.client.resources;

import com.google.common.collect.Lists;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Set;
import net.minecraft.client.resources.data.IMetadataSerializer;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FallbackResourceManager implements IResourceManager
{
    private static final Logger logger = LogManager.getLogger();
    protected final List<IResourcePack> resourcePacks = Lists.<IResourcePack>newArrayList();
    private final IMetadataSerializer frmMetadataSerializer;

    public FallbackResourceManager(IMetadataSerializer frmMetadataSerializerIn)
    {
        this.frmMetadataSerializer = frmMetadataSerializerIn;
    }

    public void addResourcePack(IResourcePack resourcePack)
    {
        this.resourcePacks.add(resourcePack);
    }

    public Set<String> getResourceDomains()
    {
        return null;
    }

    public IResource getResource(ResourceLocation location) throws IOException
    {
        IResourcePack metadataResourcePack = null;
        ResourceLocation metadataLocation = getLocationMcmeta(location);

        for (int packIndex = this.resourcePacks.size() - 1; packIndex >= 0; --packIndex)
        {
            IResourcePack resourcePack = this.resourcePacks.get(packIndex);

            if (metadataResourcePack == null && resourcePack.resourceExists(metadataLocation))
            {
                metadataResourcePack = resourcePack;
            }

            if (resourcePack.resourceExists(location))
            {
                InputStream metadataInputStream = null;

                if (metadataResourcePack != null)
                {
                    metadataInputStream = this.getInputStream(metadataLocation, metadataResourcePack);
                }

                return new SimpleResource(resourcePack.getPackName(), location, this.getInputStream(location, resourcePack), metadataInputStream, this.frmMetadataSerializer);
            }
        }

        throw new FileNotFoundException(location.toString());
    }

    protected InputStream getInputStream(ResourceLocation location, IResourcePack resourcePack) throws IOException
    {
        InputStream inputstream = resourcePack.getInputStream(location);
        return (InputStream)(logger.isDebugEnabled() ? new FallbackResourceManager.InputStreamLeakedResourceLogger(inputstream, location, resourcePack.getPackName()) : inputstream);
    }

    public List<IResource> getAllResources(ResourceLocation location) throws IOException
    {
        List<IResource> list = Lists.<IResource>newArrayList();
        ResourceLocation resourcelocation = getLocationMcmeta(location);

        for (IResourcePack iresourcepack : this.resourcePacks)
        {
            if (iresourcepack.resourceExists(location))
            {
                InputStream inputstream = iresourcepack.resourceExists(resourcelocation) ? this.getInputStream(resourcelocation, iresourcepack) : null;
                list.add(new SimpleResource(iresourcepack.getPackName(), location, this.getInputStream(location, iresourcepack), inputstream, this.frmMetadataSerializer));
            }
        }

        if (list.isEmpty())
        {
            throw new FileNotFoundException(location.toString());
        }
        else
        {
            return list;
        }
    }

    static ResourceLocation getLocationMcmeta(ResourceLocation location)
    {
        return new ResourceLocation(location.getResourceDomain(), location.getResourcePath() + ".mcmeta");
    }

    static class InputStreamLeakedResourceLogger extends InputStream
    {
        private final InputStream inputStream;
        private final FallbackResourceManager.InputStreamLeakedResourceLogger.LeakState leakState;

        public InputStreamLeakedResourceLogger(InputStream inputStreamIn, ResourceLocation location, String resourcePack)
        {
            this.inputStream = inputStreamIn;
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            (new Exception()).printStackTrace(new PrintStream(byteArrayOutputStream));
            this.leakState = new FallbackResourceManager.InputStreamLeakedResourceLogger.LeakState("Leaked resource: \'" + location + "\' loaded from pack: \'" + resourcePack + "\'\n" + byteArrayOutputStream.toString());
        }

        public void close() throws IOException
        {
            try
            {
                this.inputStream.close();
            }
            finally
            {
                this.leakState.markClosed();
            }
        }

        public int read() throws IOException
        {
            return this.inputStream.read();
        }

        protected void finalize() throws Throwable
        {
            try
            {
                if (!this.leakState.closed)
                {
                    FallbackResourceManager.logger.warn(this.leakState.message);
                }
            }
            finally
            {
                super.finalize();
            }
        }

        private static class LeakState implements Runnable
        {
            private final String message;
            private volatile boolean closed;

            private LeakState(String messageIn)
            {
                this.message = messageIn;
            }

            private void markClosed()
            {
                this.closed = true;
            }

            public void run()
            {
                if (!this.closed)
                {
                    FallbackResourceManager.logger.warn(this.message);
                }
            }
        }
    }
}
