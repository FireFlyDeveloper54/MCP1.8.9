package net.minecraft.client.renderer.texture;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.src.Config;
import net.minecraft.util.ReportedException;
import net.minecraft.util.ResourceLocation;
import net.optifine.CustomGuis;
import net.optifine.EmissiveTextures;
import net.optifine.RandomEntities;
import net.optifine.shaders.ShadersTex;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TextureManager implements ITickable, IResourceManagerReloadListener
{
    private static final Logger logger = LogManager.getLogger();
    private final Map<ResourceLocation, ITextureObject> mapTextureObjects = Maps.<ResourceLocation, ITextureObject>newHashMap();
    private final List<ITickable> listTickables = Lists.<ITickable>newArrayList();
    private final Map<String, Integer> mapTextureCounters = Maps.<String, Integer>newHashMap();
    private IResourceManager theResourceManager;
    private ITextureObject boundTexture;
    private ResourceLocation boundTextureLocation;

    public TextureManager(IResourceManager resourceManager)
    {
        this.theResourceManager = resourceManager;
    }

    public void bindTexture(ResourceLocation resource)
    {
        if (Config.isRandomEntities())
        {
            resource = RandomEntities.getTextureLocation(resource);
        }

        if (Config.isCustomGuis())
        {
            resource = CustomGuis.getTextureLocation(resource);
        }

        ITextureObject textureObject = this.mapTextureObjects.get(resource);

        if (EmissiveTextures.isActive())
        {
            textureObject = EmissiveTextures.getEmissiveTexture(textureObject, this.mapTextureObjects);
        }

        if (textureObject == null)
        {
            textureObject = new SimpleTexture(resource);
            this.loadTexture(resource, textureObject);
        }

        if (Config.isShaders())
        {
            ShadersTex.bindTexture(textureObject);
        }
        else
        {
            TextureUtil.bindTexture(textureObject.getGlTextureId());
        }

        this.boundTexture = textureObject;
        this.boundTextureLocation = resource;
    }

    public boolean loadTickableTexture(ResourceLocation textureLocation, ITickableTextureObject textureObj)
    {
        if (this.loadTexture(textureLocation, textureObj))
        {
            this.listTickables.add(textureObj);
            return true;
        }
        else
        {
            return false;
        }
    }

    public boolean loadTexture(ResourceLocation textureLocation, ITextureObject textureObj)
    {
        boolean loaded = true;

        try
        {
            textureObj.loadTexture(this.theResourceManager);
        }
        catch (IOException ioexception)
        {
            logger.warn((String)("Failed to load texture: " + textureLocation), (Throwable)ioexception);
            textureObj = TextureUtil.missingTexture;
            this.mapTextureObjects.put(textureLocation, textureObj);
            loaded = false;
        }
        catch (Throwable throwable)
        {
            final ITextureObject textureObjectForCrash = textureObj;
            CrashReport crashReport = CrashReport.makeCrashReport(throwable, "Registering texture");
            CrashReportCategory crashReportCategory = crashReport.makeCategory("Resource location being registered");
            crashReportCategory.addCrashSection("Resource location", textureLocation);
            crashReportCategory.addCrashSectionCallable("Texture object class", new Callable<String>()
            {
                public String call() throws Exception
                {
                    return textureObjectForCrash.getClass().getName();
                }
            });
            throw new ReportedException(crashReport);
        }

        this.mapTextureObjects.put(textureLocation, textureObj);
        return loaded;
    }

    public ITextureObject getTexture(ResourceLocation textureLocation)
    {
        return this.mapTextureObjects.get(textureLocation);
    }

    public ResourceLocation getDynamicTextureLocation(String name, DynamicTexture texture)
    {
        if (name.equals("logo"))
        {
            texture = Config.getMojangLogoTexture(texture);
        }

        Integer textureCount = this.mapTextureCounters.get(name);

        if (textureCount == null)
        {
            textureCount = Integer.valueOf(1);
        }
        else
        {
            textureCount = Integer.valueOf(textureCount.intValue() + 1);
        }

        this.mapTextureCounters.put(name, textureCount);
        ResourceLocation resourceLocation = new ResourceLocation("dynamic/" + name + "_" + textureCount);
        this.loadTexture(resourceLocation, texture);
        return resourceLocation;
    }

    public void tick()
    {
        for (ITickable tickable : this.listTickables)
        {
            tickable.tick();
        }
    }

    public void deleteTexture(ResourceLocation textureLocation)
    {
        ITextureObject textureObject = this.getTexture(textureLocation);

        if (textureObject != null)
        {
            this.mapTextureObjects.remove(textureLocation);
            TextureUtil.deleteTexture(textureObject.getGlTextureId());
        }
    }

    public void onResourceManagerReload(IResourceManager resourceManager)
    {
        Config.dbg("*** Reloading textures ***");
        Config.log("Resource packs: " + Config.getResourcePackNames());
        Iterator iterator = this.mapTextureObjects.keySet().iterator();

        while (iterator.hasNext())
        {
            ResourceLocation resourceLocation = (ResourceLocation)iterator.next();
            String resourcePath = resourceLocation.getResourcePath();

            if (resourcePath.startsWith("mcpatcher/") || resourcePath.startsWith("optifine/") || EmissiveTextures.isEmissive(resourceLocation))
            {
                ITextureObject textureObject = this.mapTextureObjects.get(resourceLocation);

                if (textureObject instanceof AbstractTexture)
                {
                    AbstractTexture abstractTexture = (AbstractTexture)textureObject;
                    abstractTexture.deleteGlTexture();
                }

                iterator.remove();
            }
        }

        EmissiveTextures.update();

        for (Entry<ResourceLocation, ITextureObject> entry : new HashSet<Entry<ResourceLocation, ITextureObject>>(this.mapTextureObjects.entrySet()))
        {
            this.loadTexture(entry.getKey(), entry.getValue());
        }
    }

    public void reloadBannerTextures()
    {
        for (Entry<ResourceLocation, ITextureObject> entry : new HashSet<Entry<ResourceLocation, ITextureObject>>(this.mapTextureObjects.entrySet()))
        {
            ResourceLocation resourceLocation = entry.getKey();
            ITextureObject textureObject = entry.getValue();

            if (textureObject instanceof LayeredColorMaskTexture)
            {
                this.loadTexture(resourceLocation, textureObject);
            }
        }
    }

    public ITextureObject getBoundTexture()
    {
        return this.boundTexture;
    }

    public ResourceLocation getBoundTextureLocation()
    {
        return this.boundTextureLocation;
    }
}
