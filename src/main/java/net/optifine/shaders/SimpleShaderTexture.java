package net.optifine.shaders;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.data.AnimationMetadataSection;
import net.minecraft.client.resources.data.AnimationMetadataSectionSerializer;
import net.minecraft.client.resources.data.FontMetadataSection;
import net.minecraft.client.resources.data.FontMetadataSectionSerializer;
import net.minecraft.client.resources.data.IMetadataSerializer;
import net.minecraft.client.resources.data.LanguageMetadataSection;
import net.minecraft.client.resources.data.LanguageMetadataSectionSerializer;
import net.minecraft.client.resources.data.PackMetadataSection;
import net.minecraft.client.resources.data.PackMetadataSectionSerializer;
import net.minecraft.client.resources.data.TextureMetadataSection;
import net.minecraft.client.resources.data.TextureMetadataSectionSerializer;
import org.apache.commons.io.IOUtils;

public class SimpleShaderTexture extends AbstractTexture
{
    private String texturePath;
    private static final IMetadataSerializer METADATA_SERIALIZER = makeMetadataSerializer();

    public SimpleShaderTexture(String texturePath)
    {
        this.texturePath = texturePath;
    }

    public void loadTexture(IResourceManager resourceManager) throws IOException
    {
        this.deleteGlTexture();
        InputStream inputStream = Shaders.getShaderPackResourceStream(this.texturePath);

        if (inputStream == null)
        {
            throw new FileNotFoundException("Shader texture not found: " + this.texturePath);
        }
        else
        {
            try
            {
                BufferedImage bufferedImage = TextureUtil.readBufferedImage(inputStream);
                TextureMetadataSection textureMetadata = loadTextureMetadataSection(this.texturePath, new TextureMetadataSection(false, false, new ArrayList()));
                TextureUtil.uploadTextureImageAllocate(this.getGlTextureId(), bufferedImage, textureMetadata.getTextureBlur(), textureMetadata.getTextureClamp());
            }
            finally
            {
                IOUtils.closeQuietly(inputStream);
            }
        }
    }

    public static TextureMetadataSection loadTextureMetadataSection(String texturePath, TextureMetadataSection def)
    {
        String metadataPath = texturePath + ".mcmeta";
        String metadataSectionName = "texture";
        InputStream inputStream = Shaders.getShaderPackResourceStream(metadataPath);

        if (inputStream != null)
        {
            IMetadataSerializer metadataSerializer = METADATA_SERIALIZER;
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            TextureMetadataSection textureMetadataResult;

            try
            {
                JsonObject jsonObject = (new JsonParser()).parse((Reader)bufferedReader).getAsJsonObject();
                TextureMetadataSection textureMetadata = (TextureMetadataSection)metadataSerializer.parseMetadataSection(metadataSectionName, jsonObject);

                if (textureMetadata == null)
                {
                    return def;
                }

                textureMetadataResult = textureMetadata;
            }
            catch (RuntimeException runtimeException)
            {
                SMCLog.warning("Error reading metadata: " + metadataPath);
                SMCLog.warning("" + runtimeException.getClass().getName() + ": " + runtimeException.getMessage());
                return def;
            }
            finally
            {
                IOUtils.closeQuietly((Reader)bufferedReader);
                IOUtils.closeQuietly(inputStream);
            }

            return textureMetadataResult;
        }
        else
        {
            return def;
        }
    }

    private static IMetadataSerializer makeMetadataSerializer()
    {
        IMetadataSerializer metadataSerializer = new IMetadataSerializer();
        metadataSerializer.registerMetadataSectionType(new TextureMetadataSectionSerializer(), TextureMetadataSection.class);
        metadataSerializer.registerMetadataSectionType(new FontMetadataSectionSerializer(), FontMetadataSection.class);
        metadataSerializer.registerMetadataSectionType(new AnimationMetadataSectionSerializer(), AnimationMetadataSection.class);
        metadataSerializer.registerMetadataSectionType(new PackMetadataSectionSerializer(), PackMetadataSection.class);
        metadataSerializer.registerMetadataSectionType(new LanguageMetadataSectionSerializer(), LanguageMetadataSection.class);
        return metadataSerializer;
    }
}
