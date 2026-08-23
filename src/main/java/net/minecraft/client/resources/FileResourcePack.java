package net.minecraft.client.resources;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import net.minecraft.util.ResourceCleaner;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FileResourcePack extends AbstractResourcePack implements Closeable
{
    public static final Splitter entryNameSplitter = Splitter.on('/').omitEmptyStrings().limit(3);
    private static final Logger logger = LogManager.getLogger();
    private final FileResourcePack.ResourcePackCloseAction closeAction;
    private ZipFile resourcePackZipFile;

    public FileResourcePack(File resourcePackFileIn)
    {
        super(resourcePackFileIn);
        this.closeAction = new FileResourcePack.ResourcePackCloseAction(resourcePackFileIn);
        ResourceCleaner.register(this, this.closeAction);
    }

    private ZipFile getResourcePackZipFile() throws IOException
    {
        if (this.resourcePackZipFile == null)
        {
            this.resourcePackZipFile = new ZipFile(this.resourcePackFile);
            this.closeAction.setZipFile(this.resourcePackZipFile);
        }

        return this.resourcePackZipFile;
    }

    protected InputStream getInputStreamByName(String name) throws IOException
    {
        ZipFile zipfile = this.getResourcePackZipFile();
        ZipEntry zipentry = zipfile.getEntry(name);

        if (zipentry == null)
        {
            throw new ResourcePackFileNotFoundException(this.resourcePackFile, name);
        }
        else
        {
            return zipfile.getInputStream(zipentry);
        }
    }

    public boolean hasResourceName(String name)
    {
        try
        {
            return this.getResourcePackZipFile().getEntry(name) != null;
        }
        catch (IOException caughtIoException)
        {
            return false;
        }
    }

    public Set<String> getResourceDomains()
    {
        ZipFile zipFile;

        try
        {
            zipFile = this.getResourcePackZipFile();
        }
        catch (IOException caughtIoException)
        {
            return Collections.<String>emptySet();
        }

        Enumeration <? extends ZipEntry > enumeration = zipFile.entries();
        Set<String> set = Sets.<String>newHashSet();

        while (enumeration.hasMoreElements())
        {
            ZipEntry zipEntry = (ZipEntry)enumeration.nextElement();
            String entryName = zipEntry.getName();

            if (entryName.startsWith("assets/"))
            {
                List<String> list = Lists.newArrayList(entryNameSplitter.split(entryName));

                if (list.size() > 1)
                {
                    String domainName = list.get(1);

                    if (!domainName.equals(domainName.toLowerCase(java.util.Locale.ROOT)))
                    {
                        this.logNameNotLowercase(domainName);
                    }
                    else
                    {
                        set.add(domainName);
                    }
                }
            }
        }

        return set;
    }

    public void close() throws IOException
    {
        try
        {
            this.closeAction.close();
        }
        finally
        {
            this.resourcePackZipFile = null;
        }
    }

    @SuppressWarnings("deprecation")
    protected void finalize() throws Throwable
    {
        try
        {
            if (!ResourceCleaner.hasCleaner())
            {
                this.closeAction.run();
            }
        }
        finally
        {
            super.finalize();
        }
    }

    private static class ResourcePackCloseAction implements Runnable
    {
        private final File resourcePackFile;
        private ZipFile zipFile;

        private ResourcePackCloseAction(File resourcePackFileIn)
        {
            this.resourcePackFile = resourcePackFileIn;
        }

        private synchronized void setZipFile(ZipFile zipFileIn)
        {
            this.zipFile = zipFileIn;
        }

        private synchronized void close() throws IOException
        {
            if (this.zipFile != null)
            {
                ZipFile zipFileToClose = this.zipFile;
                this.zipFile = null;
                zipFileToClose.close();
            }
        }

        public void run()
        {
            try
            {
                this.close();
            }
            catch (IOException ioException)
            {
                FileResourcePack.logger.warn("Failed to close resource pack: " + this.resourcePackFile, ioException);
            }
        }
    }
}
