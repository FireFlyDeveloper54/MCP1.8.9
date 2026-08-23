package optimization;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityLookHelper;
import net.minecraft.util.MathHelper;

public class FixedEntityLookHelper extends EntityLookHelper
{
    public FixedEntityLookHelper(EntityLiving entity)
    {
        super(entity);
    }

    public void onUpdateLook()
    {
        this.entity.rotationPitch = 0.0F;

        if (this.isLooking)
        {
            this.isLooking = false;
            double deltaX = this.posX - this.entity.posX;
            double deltaY = this.posY - (this.entity.posY + (double)this.entity.getEyeHeight());
            double deltaZ = this.posZ - this.entity.posZ;
            double horizontalDistance = (double)MathHelper.sqrt_double(deltaX * deltaX + deltaZ * deltaZ);
            float targetYaw = (float)(FastTrig.atan2(deltaZ, deltaX) * 180.0D / Math.PI) - 90.0F;
            float targetPitch = (float)(-(FastTrig.atan2(deltaY, horizontalDistance) * 180.0D / Math.PI));
            this.entity.rotationPitch = this.updateRotation(this.entity.rotationPitch, targetPitch, this.deltaLookPitch);
            this.entity.rotationYawHead = this.updateRotation(this.entity.rotationYawHead, targetYaw, this.deltaLookYaw);
        }
        else
        {
            this.entity.rotationYawHead = this.updateRotation(this.entity.rotationYawHead, this.entity.renderYawOffset, 10.0F);
        }

        float headYawOffset = MathHelper.wrapAngleTo180_float(this.entity.rotationYawHead - this.entity.renderYawOffset);

        if (!this.entity.getNavigator().noPath())
        {
            if (headYawOffset < -75.0F)
            {
                this.entity.rotationYawHead = this.entity.renderYawOffset - 75.0F;
            }

            if (headYawOffset > 75.0F)
            {
                this.entity.rotationYawHead = this.entity.renderYawOffset + 75.0F;
            }
        }
    }
}
