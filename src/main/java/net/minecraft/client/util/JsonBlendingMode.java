package net.minecraft.client.util;

import com.google.gson.JsonObject;
import java.util.Locale;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.JsonUtils;
import org.lwjgl.opengl.GL14;

public class JsonBlendingMode
{
    private static JsonBlendingMode currentBlendMode = null;
    private final int srcColorFactor;
    private final int srcAlphaFactor;
    private final int dstColorFactor;
    private final int dstAlphaFactor;
    private final int blendFunction;
    private final boolean separateBlend;
    private final boolean disabled;

    private JsonBlendingMode(boolean separateBlend, boolean disabled, int srcColorFactor, int dstColorFactor, int srcAlphaFactor, int dstAlphaFactor, int blendFunction)
    {
        this.separateBlend = separateBlend;
        this.srcColorFactor = srcColorFactor;
        this.dstColorFactor = dstColorFactor;
        this.srcAlphaFactor = srcAlphaFactor;
        this.dstAlphaFactor = dstAlphaFactor;
        this.disabled = disabled;
        this.blendFunction = blendFunction;
    }

    public JsonBlendingMode()
    {
        this(false, true, 1, 0, 1, 0, 32774);
    }

    public JsonBlendingMode(int srcFactor, int dstFactor, int blendFunction)
    {
        this(false, false, srcFactor, dstFactor, srcFactor, dstFactor, blendFunction);
    }

    public JsonBlendingMode(int srcColorFactor, int dstColorFactor, int srcAlphaFactor, int dstAlphaFactor, int blendFunction)
    {
        this(true, false, srcColorFactor, dstColorFactor, srcAlphaFactor, dstAlphaFactor, blendFunction);
    }

    public void apply()
    {
        if (!this.equals(currentBlendMode))
        {
            if (currentBlendMode == null || this.disabled != currentBlendMode.isDisabled())
            {
                currentBlendMode = this;

                if (this.disabled)
                {
                    GlStateManager.disableBlend();
                    return;
                }

                GlStateManager.enableBlend();
            }

            GL14.glBlendEquation(this.blendFunction);

            if (this.separateBlend)
            {
                GlStateManager.tryBlendFuncSeparate(this.srcColorFactor, this.dstColorFactor, this.srcAlphaFactor, this.dstAlphaFactor);
            }
            else
            {
                GlStateManager.blendFunc(this.srcColorFactor, this.dstColorFactor);
            }
        }
    }

    public boolean equals(Object other)
    {
        if (this == other)
        {
            return true;
        }
        else if (!(other instanceof JsonBlendingMode))
        {
            return false;
        }
        else
        {
            JsonBlendingMode jsonBlendingMode = (JsonBlendingMode)other;
            return this.blendFunction != jsonBlendingMode.blendFunction ? false : (this.dstAlphaFactor != jsonBlendingMode.dstAlphaFactor ? false : (this.dstColorFactor != jsonBlendingMode.dstColorFactor ? false : (this.disabled != jsonBlendingMode.disabled ? false : (this.separateBlend != jsonBlendingMode.separateBlend ? false : (this.srcAlphaFactor != jsonBlendingMode.srcAlphaFactor ? false : this.srcColorFactor == jsonBlendingMode.srcColorFactor)))));
        }
    }

    public int hashCode()
    {
        int i = this.srcColorFactor;
        i = 31 * i + this.srcAlphaFactor;
        i = 31 * i + this.dstColorFactor;
        i = 31 * i + this.dstAlphaFactor;
        i = 31 * i + this.blendFunction;
        i = 31 * i + (this.separateBlend ? 1 : 0);
        i = 31 * i + (this.disabled ? 1 : 0);
        return i;
    }

    public boolean isDisabled()
    {
        return this.disabled;
    }

    public static JsonBlendingMode fromJson(JsonObject jsonObject)
    {
        if (jsonObject == null)
        {
            return new JsonBlendingMode();
        }
        else
        {
            int i = 32774;
            int j = 1;
            int k = 0;
            int l = 1;
            int dstAlphaFactor = 0;
            boolean flag = true;
            boolean hasSeparateAlpha = false;

            if (JsonUtils.isString(jsonObject, "func"))
            {
                i = parseBlendFunction(jsonObject.get("func").getAsString());

                if (i != 32774)
                {
                    flag = false;
                }
            }

            if (JsonUtils.isString(jsonObject, "srcrgb"))
            {
                j = parseBlendFactor(jsonObject.get("srcrgb").getAsString());

                if (j != 1)
                {
                    flag = false;
                }
            }

            if (JsonUtils.isString(jsonObject, "dstrgb"))
            {
                k = parseBlendFactor(jsonObject.get("dstrgb").getAsString());

                if (k != 0)
                {
                    flag = false;
                }
            }

            if (JsonUtils.isString(jsonObject, "srcalpha"))
            {
                l = parseBlendFactor(jsonObject.get("srcalpha").getAsString());

                if (l != 1)
                {
                    flag = false;
                }

                hasSeparateAlpha = true;
            }

            if (JsonUtils.isString(jsonObject, "dstalpha"))
            {
                dstAlphaFactor = parseBlendFactor(jsonObject.get("dstalpha").getAsString());

                if (dstAlphaFactor != 0)
                {
                    flag = false;
                }

                hasSeparateAlpha = true;
            }

            return flag ? new JsonBlendingMode() : (hasSeparateAlpha ? new JsonBlendingMode(j, k, l, dstAlphaFactor, i) : new JsonBlendingMode(j, k, i));
        }
    }

    private static int parseBlendFunction(String blendFunctionName)
    {
        String s = blendFunctionName.trim().toLowerCase(Locale.ROOT);
        return s.equals("add") ? 32774 : (s.equals("subtract") ? 32778 : (s.equals("reversesubtract") ? 32779 : (s.equals("reverse_subtract") ? 32779 : (s.equals("min") ? 32775 : (s.equals("max") ? 32776 : 32774)))));
    }

    private static int parseBlendFactor(String blendFactorName)
    {
        String blendFactor = blendFactorName.trim().toLowerCase(Locale.ROOT);
        blendFactor = blendFactor.replace("_", "");
        blendFactor = blendFactor.replace("one", "1");
        blendFactor = blendFactor.replace("zero", "0");
        blendFactor = blendFactor.replace("minus", "-");
        return blendFactor.equals("0") ? 0 : (blendFactor.equals("1") ? 1 : (blendFactor.equals("srccolor") ? 768 : (blendFactor.equals("1-srccolor") ? 769 : (blendFactor.equals("dstcolor") ? 774 : (blendFactor.equals("1-dstcolor") ? 775 : (blendFactor.equals("srcalpha") ? 770 : (blendFactor.equals("1-srcalpha") ? 771 : (blendFactor.equals("dstalpha") ? 772 : (blendFactor.equals("1-dstalpha") ? 773 : -1)))))))));
    }
}
