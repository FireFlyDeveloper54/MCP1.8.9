package net.minecraft.client.renderer.tileentity;

import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiUtilRenderComponents;
import net.minecraft.client.gui.inventory.GuiEditSign;
import net.minecraft.client.model.ModelSign;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.src.Config;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.ResourceLocation;
import net.optifine.CustomColors;
import net.optifine.shaders.Shaders;
import org.lwjgl.opengl.GL11;

public class TileEntitySignRenderer extends TileEntitySpecialRenderer<TileEntitySign>
{
    private static final ResourceLocation SIGN_TEXTURE = new ResourceLocation("textures/entity/sign.png");
    private final ModelSign model = new ModelSign();
    private static double textRenderDistanceSq = 4096.0D;

    public void renderTileEntityAt(TileEntitySign te, double x, double y, double z, float partialTicks, int destroyStage)
    {
        Block block = te.getBlockType();
        GlStateManager.pushMatrix();
        float modelScale = 0.6666667F;

        if (block == Blocks.standing_sign)
        {
            GlStateManager.translate((float)x + 0.5F, (float)y + 0.75F * modelScale, (float)z + 0.5F);
            float standingRotation = (float)(te.getBlockMetadata() * 360) / 16.0F;
            GlStateManager.rotate(-standingRotation, 0.0F, 1.0F, 0.0F);
            this.model.signStick.showModel = true;
        }
        else
        {
            int metadata = te.getBlockMetadata();
            float wallRotation = 0.0F;

            if (metadata == 2)
            {
                wallRotation = 180.0F;
            }

            if (metadata == 4)
            {
                wallRotation = 90.0F;
            }

            if (metadata == 5)
            {
                wallRotation = -90.0F;
            }

            GlStateManager.translate((float)x + 0.5F, (float)y + 0.75F * modelScale, (float)z + 0.5F);
            GlStateManager.rotate(-wallRotation, 0.0F, 1.0F, 0.0F);
            GlStateManager.translate(0.0F, -0.3125F, -0.4375F);
            this.model.signStick.showModel = false;
        }

        if (destroyStage >= 0)
        {
            this.bindTexture(DESTROY_STAGES[destroyStage]);
            GlStateManager.matrixMode(5890);
            GlStateManager.pushMatrix();
            GlStateManager.scale(4.0F, 2.0F, 1.0F);
            GlStateManager.translate(0.0625F, 0.0625F, 0.0625F);
            GlStateManager.matrixMode(5888);
        }
        else
        {
            this.bindTexture(SIGN_TEXTURE);
        }

        GlStateManager.enableRescaleNormal();
        GlStateManager.pushMatrix();
        GlStateManager.scale(modelScale, -modelScale, -modelScale);
        this.model.renderSign();
        GlStateManager.popMatrix();

        if (isRenderText(te))
        {
            FontRenderer fontRenderer = this.getFontRenderer();
            float textScale = 0.015625F * modelScale;
            GlStateManager.translate(0.0F, 0.5F * modelScale, 0.07F * modelScale);
            GlStateManager.scale(textScale, -textScale, textScale);
            GL11.glNormal3f(0.0F, 0.0F, -1.0F * textScale);
            GlStateManager.depthMask(false);
            int textColor = 0;

            if (Config.isCustomColors())
            {
                textColor = CustomColors.getSignTextColor(textColor);
            }

            if (destroyStage < 0)
            {
                boolean signBeingEdited = Config.getMinecraft().currentScreen instanceof GuiEditSign;

                for (int lineIndex = 0; lineIndex < te.signText.length; ++lineIndex)
                {
                    if (te.signText[lineIndex] != null)
                    {
                        IChatComponent chatComponent = te.signText[lineIndex];
                        List<IChatComponent> wrappedLines = GuiUtilRenderComponents.splitText(chatComponent, 90, fontRenderer, false, true);
                        String lineText = wrappedLines != null && wrappedLines.size() > 0 ? wrappedLines.get(0).getFormattedText() : "";

                        if (lineIndex == te.lineBeingEdited)
                        {
                            lineText = "> " + lineText + " <";
                            fontRenderer.drawString(lineText, (float)(-fontRenderer.getStringWidth(lineText) / 2), (float)(lineIndex * 10 - te.signText.length * 5), textColor, false, signBeingEdited ? 1.0F : 0.0F);
                        }
                        else
                        {
                            fontRenderer.drawString(lineText, (float)(-fontRenderer.getStringWidth(lineText) / 2), (float)(lineIndex * 10 - te.signText.length * 5), textColor, false, signBeingEdited ? 1.0F : 0.0F);
                        }
                    }
                }
            }
        }

        GlStateManager.depthMask(true);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.popMatrix();

        if (destroyStage >= 0)
        {
            GlStateManager.matrixMode(5890);
            GlStateManager.popMatrix();
            GlStateManager.matrixMode(5888);
        }
    }

    private static boolean isRenderText(TileEntitySign sign)
    {
        if (Shaders.isShadowPass)
        {
            return false;
        }
        else if (Config.getMinecraft().currentScreen instanceof GuiEditSign)
        {
            return true;
        }
        else
        {
            if (!Config.zoomMode && sign.lineBeingEdited < 0)
            {
                Entity entity = Config.getMinecraft().getRenderViewEntity();
                double distanceSq = sign.getDistanceSq(entity.posX, entity.posY, entity.posZ);

                if (distanceSq > textRenderDistanceSq)
                {
                    return false;
                }
            }

            return true;
        }
    }

    public static void updateTextRenderDistance()
    {
        Minecraft minecraft = Config.getMinecraft();
        double clampedFov = (double)Config.limit(minecraft.gameSettings.fovSetting, 1.0F, 120.0F);
        double textRenderDistance = Math.max(1.5D * (double)minecraft.displayHeight / clampedFov, 16.0D);
        textRenderDistanceSq = textRenderDistance * textRenderDistance;
    }
}
