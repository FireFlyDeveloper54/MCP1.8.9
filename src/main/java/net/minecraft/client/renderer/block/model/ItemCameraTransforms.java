package net.minecraft.client.renderer.block.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import net.minecraft.client.renderer.GlStateManager;

public class ItemCameraTransforms
{
    public static final ItemCameraTransforms DEFAULT = new ItemCameraTransforms();
    public static float translationX = 0.0F;
    public static float translationY = 0.0F;
    public static float translationZ = 0.0F;
    public static float rotationX = 0.0F;
    public static float rotationY = 0.0F;
    public static float rotationZ = 0.0F;
    public static float scaleX = 0.0F;
    public static float scaleY = 0.0F;
    public static float scaleZ = 0.0F;
    public final ItemTransformVec3f thirdPerson;
    public final ItemTransformVec3f firstPerson;
    public final ItemTransformVec3f head;
    public final ItemTransformVec3f gui;
    public final ItemTransformVec3f ground;
    public final ItemTransformVec3f fixed;

    private ItemCameraTransforms()
    {
        this(ItemTransformVec3f.DEFAULT, ItemTransformVec3f.DEFAULT, ItemTransformVec3f.DEFAULT, ItemTransformVec3f.DEFAULT, ItemTransformVec3f.DEFAULT, ItemTransformVec3f.DEFAULT);
    }

    public ItemCameraTransforms(ItemCameraTransforms transforms)
    {
        this.thirdPerson = transforms.thirdPerson;
        this.firstPerson = transforms.firstPerson;
        this.head = transforms.head;
        this.gui = transforms.gui;
        this.ground = transforms.ground;
        this.fixed = transforms.fixed;
    }

    public ItemCameraTransforms(ItemTransformVec3f thirdPersonIn, ItemTransformVec3f firstPersonIn, ItemTransformVec3f headIn, ItemTransformVec3f guiIn, ItemTransformVec3f groundIn, ItemTransformVec3f fixedIn)
    {
        this.thirdPerson = thirdPersonIn;
        this.firstPerson = firstPersonIn;
        this.head = headIn;
        this.gui = guiIn;
        this.ground = groundIn;
        this.fixed = fixedIn;
    }

    public void applyTransform(ItemCameraTransforms.TransformType type)
    {
        ItemTransformVec3f itemtransformvec3f = this.getTransform(type);

        if (itemtransformvec3f != ItemTransformVec3f.DEFAULT)
        {
            GlStateManager.translate(itemtransformvec3f.translation.x + translationX, itemtransformvec3f.translation.y + translationY, itemtransformvec3f.translation.z + translationZ);
            GlStateManager.rotate(itemtransformvec3f.rotation.y + rotationY, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(itemtransformvec3f.rotation.x + rotationX, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(itemtransformvec3f.rotation.z + rotationZ, 0.0F, 0.0F, 1.0F);
            GlStateManager.scale(itemtransformvec3f.scale.x + scaleX, itemtransformvec3f.scale.y + scaleY, itemtransformvec3f.scale.z + scaleZ);
        }
    }

    public ItemTransformVec3f getTransform(ItemCameraTransforms.TransformType type)
    {
        switch (type)
        {
            case THIRD_PERSON:
                return this.thirdPerson;

            case FIRST_PERSON:
                return this.firstPerson;

            case HEAD:
                return this.head;

            case GUI:
                return this.gui;

            case GROUND:
                return this.ground;

            case FIXED:
                return this.fixed;

            default:
                return ItemTransformVec3f.DEFAULT;
        }
    }

    public boolean hasTransform(ItemCameraTransforms.TransformType type)
    {
        return !this.getTransform(type).equals(ItemTransformVec3f.DEFAULT);
    }

    static class Deserializer implements JsonDeserializer<ItemCameraTransforms>
    {
        public ItemCameraTransforms deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
        {
            JsonObject jsonobject = json.getAsJsonObject();
            ItemTransformVec3f itemtransformvec3f = this.parseTransform(context, jsonobject, "thirdperson");
            ItemTransformVec3f itemtransformvec3f1 = this.parseTransform(context, jsonobject, "firstperson");
            ItemTransformVec3f itemtransformvec3f2 = this.parseTransform(context, jsonobject, "head");
            ItemTransformVec3f itemtransformvec3f3 = this.parseTransform(context, jsonobject, "gui");
            ItemTransformVec3f itemtransformvec3f4 = this.parseTransform(context, jsonobject, "ground");
            ItemTransformVec3f itemtransformvec3f5 = this.parseTransform(context, jsonobject, "fixed");
            return new ItemCameraTransforms(itemtransformvec3f, itemtransformvec3f1, itemtransformvec3f2, itemtransformvec3f3, itemtransformvec3f4, itemtransformvec3f5);
        }

        private ItemTransformVec3f parseTransform(JsonDeserializationContext context, JsonObject object, String name)
        {
            return object.has(name) ? context.deserialize(object.get(name), ItemTransformVec3f.class) : ItemTransformVec3f.DEFAULT;
        }
    }

    public static enum TransformType
    {
        NONE,
        THIRD_PERSON,
        FIRST_PERSON,
        HEAD,
        GUI,
        GROUND,
        FIXED;
    }
}
