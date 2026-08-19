package net.optifine.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import net.minecraft.client.resources.AbstractResourcePack;
import net.minecraft.client.resources.DefaultResourcePack;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.src.Config;
import net.minecraft.util.ResourceLocation;

public class ResUtils
{
    public static String[] collectFiles(String prefix, String suffix)
    {
        return collectFiles(new String[] {prefix}, new String[] {suffix});
    }

    public static String[] collectFiles(String[] prefixes, String[] suffixes)
    {
        Set<String> collectedFiles = new LinkedHashSet();
        IResourcePack[] resourcePacks = Config.getResourcePacks();

        for (int packIndex = 0; packIndex < resourcePacks.length; ++packIndex)
        {
            IResourcePack resourcePack = resourcePacks[packIndex];
            String[] packFiles = collectFiles(resourcePack, (String[])prefixes, (String[])suffixes, (String[])null);
            collectedFiles.addAll(Arrays.<String>asList(packFiles));
        }

        String[] collectedFileArray = (String[])collectedFiles.toArray(new String[collectedFiles.size()]);
        return collectedFileArray;
    }

    public static String[] collectFiles(IResourcePack rp, String prefix, String suffix, String[] defaultPaths)
    {
        return collectFiles(rp, new String[] {prefix}, new String[] {suffix}, defaultPaths);
    }

    public static String[] collectFiles(IResourcePack rp, String[] prefixes, String[] suffixes)
    {
        return collectFiles(rp, (String[])prefixes, (String[])suffixes, (String[])null);
    }

    public static String[] collectFiles(IResourcePack rp, String[] prefixes, String[] suffixes, String[] defaultPaths)
    {
        if (rp instanceof DefaultResourcePack)
        {
            return collectFilesFixed(rp, defaultPaths);
        }
        else if (!(rp instanceof AbstractResourcePack))
        {
            Config.warn("Unknown resource pack type: " + rp);
            return new String[0];
        }
        else
        {
            AbstractResourcePack resourcePack = (AbstractResourcePack)rp;
            File resourcePackFile = resourcePack.resourcePackFile;

            if (resourcePackFile == null)
            {
                return new String[0];
            }
            else if (resourcePackFile.isDirectory())
            {
                return collectFilesFolder(resourcePackFile, "", prefixes, suffixes);
            }
            else if (resourcePackFile.isFile())
            {
                return collectFilesZIP(resourcePackFile, prefixes, suffixes);
            }
            else
            {
                Config.warn("Unknown resource pack file: " + resourcePackFile);
                return new String[0];
            }
        }
    }

    private static String[] collectFilesFixed(IResourcePack rp, String[] paths)
    {
        if (paths == null)
        {
            return new String[0];
        }
        else
        {
            List existingPaths = new ArrayList();

            for (int pathIndex = 0; pathIndex < paths.length; ++pathIndex)
            {
                String path = paths[pathIndex];
                ResourceLocation resourceLocation = new ResourceLocation(path);

                if (rp.resourceExists(resourceLocation))
                {
                    existingPaths.add(path);
                }
            }

            String[] existingPathArray = (String[])((String[])existingPaths.toArray(new String[existingPaths.size()]));
            return existingPathArray;
        }
    }

    private static String[] collectFilesFolder(File tpFile, String basePath, String[] prefixes, String[] suffixes)
    {
        List collectedFiles = new ArrayList();
        String assetsPrefix = "assets/minecraft/";
        File[] files = tpFile.listFiles();

        if (files == null)
        {
            return new String[0];
        }
        else
        {
            for (int fileIndex = 0; fileIndex < files.length; ++fileIndex)
            {
                File file = files[fileIndex];

                if (file.isFile())
                {
                    String resourcePath = basePath + file.getName();

                    if (resourcePath.startsWith(assetsPrefix))
                    {
                        resourcePath = resourcePath.substring(assetsPrefix.length());

                        if (StrUtils.startsWith(resourcePath, prefixes) && StrUtils.endsWith(resourcePath, suffixes))
                        {
                            collectedFiles.add(resourcePath);
                        }
                    }
                }
                else if (file.isDirectory())
                {
                    String directoryPath = basePath + file.getName() + "/";
                    String[] childPaths = collectFilesFolder(file, directoryPath, prefixes, suffixes);

                    for (int childIndex = 0; childIndex < childPaths.length; ++childIndex)
                    {
                        String childPath = childPaths[childIndex];
                        collectedFiles.add(childPath);
                    }
                }
            }

            String[] collectedFileArray = (String[])((String[])collectedFiles.toArray(new String[collectedFiles.size()]));
            return collectedFileArray;
        }
    }

    private static String[] collectFilesZIP(File tpFile, String[] prefixes, String[] suffixes)
    {
        List collectedFiles = new ArrayList();
        String assetsPrefix = "assets/minecraft/";

        try
        {
            ZipFile zipFile = new ZipFile(tpFile);
            Enumeration entries = zipFile.entries();

            while (entries.hasMoreElements())
            {
                ZipEntry zipEntry = (ZipEntry)entries.nextElement();
                String resourcePath = zipEntry.getName();

                if (resourcePath.startsWith(assetsPrefix))
                {
                    resourcePath = resourcePath.substring(assetsPrefix.length());

                    if (StrUtils.startsWith(resourcePath, prefixes) && StrUtils.endsWith(resourcePath, suffixes))
                    {
                        collectedFiles.add(resourcePath);
                    }
                }
            }

            zipFile.close();
            String[] collectedFileArray = (String[])((String[])collectedFiles.toArray(new String[collectedFiles.size()]));
            return collectedFileArray;
        }
        catch (IOException ioException)
        {
            net.minecraft.src.Config.warn(ioException.getClass().getName() + ": " + ioException.getMessage(), ioException);
            return new String[0];
        }
    }

    private static boolean isLowercase(String str)
    {
        return str.equals(str.toLowerCase(Locale.ROOT));
    }

    public static Properties readProperties(String path, String module)
    {
        ResourceLocation resourceLocation = new ResourceLocation(path);

        try
        {
            InputStream inputStream = Config.getResourceStream(resourceLocation);

            if (inputStream == null)
            {
                return null;
            }
            else
            {
                Properties properties = new PropertiesOrdered();
                properties.load(inputStream);
                inputStream.close();
                Config.dbg("" + module + ": Loading " + path);
                return properties;
            }
        }
        catch (FileNotFoundException caughtFileNotFoundException)
        {
            return null;
        }
        catch (IOException caughtIoException)
        {
            Config.warn("" + module + ": Error reading " + path);
            return null;
        }
    }

    public static Properties readProperties(InputStream in, String module)
    {
        if (in == null)
        {
            return null;
        }
        else
        {
            try
            {
                Properties properties = new PropertiesOrdered();
                properties.load(in);
                in.close();
                return properties;
            }
            catch (FileNotFoundException caughtFileNotFoundException)
            {
                return null;
            }
            catch (IOException caughtIoException)
            {
                return null;
            }
        }
    }
}
