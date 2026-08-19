package net.minecraft.client.resources;

public class Language implements Comparable<Language>
{
    private final String languageCode;
    private final String region;
    private final String name;
    private final boolean bidirectional;

    public Language(String languageCodeIn, String regionIn, String nameIn, boolean bidirectionalIn)
    {
        this.languageCode = languageCodeIn;
        this.region = regionIn;
        this.name = nameIn;
        this.bidirectional = bidirectionalIn;
    }

    public String getLanguageCode()
    {
        return this.languageCode;
    }

    public boolean isBidirectional()
    {
        return this.bidirectional;
    }

    public String toString()
    {
        return this.name + " (" + this.region + ")";
    }

    public boolean equals(Object other)
    {
        return this == other ? true : (!(other instanceof Language) ? false : this.languageCode.equals(((Language)other).languageCode));
    }

    public int hashCode()
    {
        return this.languageCode.hashCode();
    }

    public int compareTo(Language language)
    {
        return this.languageCode.compareTo(language.languageCode);
    }
}
