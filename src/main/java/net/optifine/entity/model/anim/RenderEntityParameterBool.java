package net.optifine.entity.model.anim;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.entity.EntityLivingBase;
import net.optifine.expr.ExpressionType;
import net.optifine.expr.IExpressionBool;

public enum RenderEntityParameterBool implements IExpressionBool
{
    IS_ALIVE("is_alive"),
    IS_BURNING("is_burning"),
    IS_CHILD("is_child"),
    IS_GLOWING("is_glowing"),
    IS_HURT("is_hurt"),
    IS_IN_LAVA("is_in_lava"),
    IS_IN_WATER("is_in_water"),
    IS_INVISIBLE("is_invisible"),
    IS_ON_GROUND("is_on_ground"),
    IS_RIDDEN("is_ridden"),
    IS_RIDING("is_riding"),
    IS_SNEAKING("is_sneaking"),
    IS_SPRINTING("is_sprinting"),
    IS_WET("is_wet");

    private String name;
    private RenderManager renderManager;
    private static final RenderEntityParameterBool[] VALUES = values();

    private RenderEntityParameterBool(String name)
    {
        this.name = name;
        this.renderManager = Minecraft.getMinecraft().getRenderManager();
    }

    public String getName()
    {
        return this.name;
    }

    public ExpressionType getExpressionType()
    {
        return ExpressionType.BOOL;
    }

    public boolean eval()
    {
        Render render = this.renderManager.renderRender;

        if (render == null)
        {
            return false;
        }
        else
        {
            if (render instanceof RendererLivingEntity)
            {
                RendererLivingEntity rendererLivingEntity = (RendererLivingEntity)render;
                EntityLivingBase entityLivingBase = rendererLivingEntity.renderEntity;

                if (entityLivingBase == null)
                {
                    return false;
                }

                switch (this)
                {
                    case IS_ALIVE:
                        return entityLivingBase.isEntityAlive();

                    case IS_BURNING:
                        return entityLivingBase.isBurning();

                    case IS_CHILD:
                        return entityLivingBase.isChild();

                    case IS_HURT:
                        return entityLivingBase.hurtTime > 0;

                    case IS_IN_LAVA:
                        return entityLivingBase.isInLava();

                    case IS_IN_WATER:
                        return entityLivingBase.isInWater();

                    case IS_INVISIBLE:
                        return entityLivingBase.isInvisible();

                    case IS_ON_GROUND:
                        return entityLivingBase.onGround;

                    case IS_RIDDEN:
                        return entityLivingBase.riddenByEntity != null;

                    case IS_RIDING:
                        return entityLivingBase.isRiding();

                    case IS_SNEAKING:
                        return entityLivingBase.isSneaking();

                    case IS_SPRINTING:
                        return entityLivingBase.isSprinting();

                    case IS_WET:
                        return entityLivingBase.isWet();
                }
            }

            return false;
        }
    }

    public static RenderEntityParameterBool parse(String str)
    {
        if (str == null)
        {
            return null;
        }
        else
        {
            for (int parameterIndex = 0; parameterIndex < VALUES.length; ++parameterIndex)
            {
                RenderEntityParameterBool parameter = VALUES[parameterIndex];

                if (parameter.getName().equals(str))
                {
                    return parameter;
                }
            }

            return null;
        }
    }
}
