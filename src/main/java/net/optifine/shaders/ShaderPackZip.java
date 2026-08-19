package net.optifine.shaders;

import com.google.common.base.Joiner;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import net.minecraft.src.Config;
import net.optifine.util.StrUtils;

public class ShaderPackZip implements IShaderPack
{
    protected File packFile;
    protected ZipFile packZipFile;
    protected String baseFolder;

    public ShaderPackZip(String name, File file)
    {
        this.packFile = file;
        this.packZipFile = null;
        this.baseFolder = "";
    }

    public void close()
    {
        if (this.packZipFile != null)
        {
            try
            {
                this.packZipFile.close();
            }
            catch (Exception caughtException)
            {
                ;
            }

            this.packZipFile = null;
        }
    }

    public InputStream getResourceAsStream(String resName)
    {
        try
        {
            if (this.packZipFile == null)
            {
                this.packZipFile = new ZipFile(this.packFile);
                this.baseFolder = this.detectBaseFolder(this.packZipFile);
            }

            String normalizedResourceName = StrUtils.removePrefix(resName, "/");

            if (normalizedResourceName.contains(".."))
            {
                normalizedResourceName = this.resolveRelative(normalizedResourceName);
            }

            ZipEntry zipEntry = this.packZipFile.getEntry(this.baseFolder + normalizedResourceName);
            return zipEntry == null ? null : this.packZipFile.getInputStream(zipEntry);
        }
        catch (Exception caughtException)
        {
            return null;
        }
    }

    private String resolveRelative(String name)
    {
        Deque<String> pathParts = new ArrayDeque();
        String[] tokens = Config.tokenize(name, "/");

        for (int i = 0; i < tokens.length; ++i)
        {
            String token = tokens[i];

            if (token.equals(".."))
            {
                if (pathParts.isEmpty())
                {
                    return "";
                }

                pathParts.removeLast();
            }
            else
            {
                pathParts.add(token);
            }
        }

        String resolvedPath = Joiner.on('/').join(pathParts);
        return resolvedPath;
    }

    private String detectBaseFolder(ZipFile zip)
    {
        ZipEntry shadersEntry = zip.getEntry("shaders/");

        if (shadersEntry != null && shadersEntry.isDirectory())
        {
            return "";
        }
        else
        {
            Pattern shaderFolderPattern = Pattern.compile("([^/]+/)shaders/");
            Enumeration <? extends ZipEntry > zipEntries = zip.entries();

            while (zipEntries.hasMoreElements())
            {
                ZipEntry entry = (ZipEntry)zipEntries.nextElement();
                String entryName = entry.getName();
                Matcher matcher = shaderFolderPattern.matcher(entryName);

                if (matcher.matches())
                {
                    String matchedBaseFolder = matcher.group(1);

                    if (matchedBaseFolder != null)
                    {
                        if (matchedBaseFolder.equals("shaders/"))
                        {
                            return "";
                        }

                        return matchedBaseFolder;
                    }
                }
            }

            return "";
        }
    }

    public boolean hasDirectory(String resName)
    {
        try
        {
            if (this.packZipFile == null)
            {
                this.packZipFile = new ZipFile(this.packFile);
                this.baseFolder = this.detectBaseFolder(this.packZipFile);
            }

            String normalizedResourceName = StrUtils.removePrefix(resName, "/");
            ZipEntry zipEntry = this.packZipFile.getEntry(this.baseFolder + normalizedResourceName);
            return zipEntry != null;
        }
        catch (IOException caughtIoException)
        {
            return false;
        }
    }

    public String getName()
    {
        return this.packFile.getName();
    }
}
