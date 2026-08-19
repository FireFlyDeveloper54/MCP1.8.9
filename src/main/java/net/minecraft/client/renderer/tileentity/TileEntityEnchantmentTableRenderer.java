package net.minecraft.client.renderer.tileentity;

import net.minecraft.client.model.ModelBook;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntityEnchantmentTable;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;

public class TileEntityEnchantmentTableRenderer extends TileEntitySpecialRenderer<TileEntityEnchantmentTable>
{
    private static final ResourceLocation TEXTURE_BOOK = new ResourceLocation("textures/entity/enchanting_table_book.png");
    private ModelBook bookModel = new ModelBook();

    public void renderTileEntityAt(TileEntityEnchantmentTable te, double x, double y, double z, float partialTicks, int destroyStage)
    {
        GlStateManager.pushMatrix();
        GlStateManager.translate((float)x + 0.5F, (float)y + 0.75F, (float)z + 0.5F);
        float tickTime = (float)te.tickCount + partialTicks;
        GlStateManager.translate(0.0F, 0.1F + MathHelper.sin(tickTime * 0.1F) * 0.01F, 0.0F);
        float rotationDelta;

        for (rotationDelta = te.bookRotation - te.bookRotationPrev; rotationDelta >= (float)Math.PI; rotationDelta -= ((float)Math.PI * 2F))
        {
            ;
        }

        while (rotationDelta < -(float)Math.PI)
        {
            rotationDelta += ((float)Math.PI * 2F);
        }

        float bookRotation = te.bookRotationPrev + rotationDelta * partialTicks;
        GlStateManager.rotate(-bookRotation * 180.0F / (float)Math.PI, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(80.0F, 0.0F, 0.0F, 1.0F);
        this.bindTexture(TEXTURE_BOOK);
        float leftPageFlip = te.pageFlipPrev + (te.pageFlip - te.pageFlipPrev) * partialTicks + 0.25F;
        float rightPageFlip = te.pageFlipPrev + (te.pageFlip - te.pageFlipPrev) * partialTicks + 0.75F;
        leftPageFlip = (leftPageFlip - (float)MathHelper.truncateDoubleToInt((double)leftPageFlip)) * 1.6F - 0.3F;
        rightPageFlip = (rightPageFlip - (float)MathHelper.truncateDoubleToInt((double)rightPageFlip)) * 1.6F - 0.3F;

        if (leftPageFlip < 0.0F)
        {
            leftPageFlip = 0.0F;
        }

        if (rightPageFlip < 0.0F)
        {
            rightPageFlip = 0.0F;
        }

        if (leftPageFlip > 1.0F)
        {
            leftPageFlip = 1.0F;
        }

        if (rightPageFlip > 1.0F)
        {
            rightPageFlip = 1.0F;
        }

        float bookSpread = te.bookSpreadPrev + (te.bookSpread - te.bookSpreadPrev) * partialTicks;
        GlStateManager.enableCull();
        this.bookModel.render((Entity)null, tickTime, leftPageFlip, rightPageFlip, bookSpread, 0.0F, 0.0625F);
        GlStateManager.popMatrix();
    }
}
