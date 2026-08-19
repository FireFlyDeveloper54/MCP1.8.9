package net.minecraft.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map.Entry;

public interface IChatComponent extends Iterable<IChatComponent>
{
    IChatComponent setChatStyle(ChatStyle style);

    ChatStyle getChatStyle();

    IChatComponent appendText(String text);

    IChatComponent appendSibling(IChatComponent component);

    String getUnformattedTextForChat();

    String getUnformattedText();

    String getFormattedText();

    List<IChatComponent> getSiblings();

    IChatComponent createCopy();

    public static class Serializer implements JsonDeserializer<IChatComponent>, JsonSerializer<IChatComponent>
    {
        private static final Gson GSON;

        public IChatComponent deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
        {
            if (json.isJsonPrimitive())
            {
                return new ChatComponentText(json.getAsString());
            }
            else if (!json.isJsonObject())
            {
                if (json.isJsonArray())
                {
                    JsonArray componentArray = json.getAsJsonArray();
                    IChatComponent combinedComponent = null;

                    for (JsonElement childElement : componentArray)
                    {
                        IChatComponent childComponent = this.deserialize(childElement, childElement.getClass(), context);

                        if (combinedComponent == null)
                        {
                            combinedComponent = childComponent;
                        }
                        else
                        {
                            combinedComponent.appendSibling(childComponent);
                        }
                    }

                    return combinedComponent;
                }
                else
                {
                    throw new JsonParseException("Don\'t know how to turn " + json.toString() + " into a Component");
                }
            }
            else
            {
                JsonObject componentObject = json.getAsJsonObject();
                IChatComponent component;

                if (componentObject.has("text"))
                {
                    component = new ChatComponentText(componentObject.get("text").getAsString());
                }
                else if (componentObject.has("translate"))
                {
                    String translationKey = componentObject.get("translate").getAsString();

                    if (componentObject.has("with"))
                    {
                        JsonArray formatArgArray = componentObject.getAsJsonArray("with");
                        Object[] formatArgs = new Object[formatArgArray.size()];

                        for (int argumentIndex = 0; argumentIndex < formatArgs.length; ++argumentIndex)
                        {
                            formatArgs[argumentIndex] = this.deserialize(formatArgArray.get(argumentIndex), typeOfT, context);

                            if (formatArgs[argumentIndex] instanceof ChatComponentText)
                            {
                                ChatComponentText textComponent = (ChatComponentText)formatArgs[argumentIndex];

                                if (textComponent.getChatStyle().isEmpty() && textComponent.getSiblings().isEmpty())
                                {
                                    formatArgs[argumentIndex] = textComponent.getChatComponentText_TextValue();
                                }
                            }
                        }

                        component = new ChatComponentTranslation(translationKey, formatArgs);
                    }
                    else
                    {
                        component = new ChatComponentTranslation(translationKey, new Object[0]);
                    }
                }
                else if (componentObject.has("score"))
                {
                    JsonObject scoreObject = componentObject.getAsJsonObject("score");

                    if (!scoreObject.has("name") || !scoreObject.has("objective"))
                    {
                        throw new JsonParseException("A score component needs a least a name and an objective");
                    }

                    component = new ChatComponentScore(JsonUtils.getString(scoreObject, "name"), JsonUtils.getString(scoreObject, "objective"));

                    if (scoreObject.has("value"))
                    {
                        ((ChatComponentScore)component).setValue(JsonUtils.getString(scoreObject, "value"));
                    }
                }
                else
                {
                    if (!componentObject.has("selector"))
                    {
                        throw new JsonParseException("Don\'t know how to turn " + json.toString() + " into a Component");
                    }

                    component = new ChatComponentSelector(JsonUtils.getString(componentObject, "selector"));
                }

                if (componentObject.has("extra"))
                {
                    JsonArray extraArray = componentObject.getAsJsonArray("extra");

                    if (extraArray.size() <= 0)
                    {
                        throw new JsonParseException("Unexpected empty array of components");
                    }

                    for (int extraIndex = 0; extraIndex < extraArray.size(); ++extraIndex)
                    {
                        component.appendSibling(this.deserialize(extraArray.get(extraIndex), typeOfT, context));
                    }
                }

                component.setChatStyle(context.deserialize(json, ChatStyle.class));
                return component;
            }
        }

        private void serializeChatStyle(ChatStyle style, JsonObject object, JsonSerializationContext ctx)
        {
            JsonElement serializedStyle = ctx.serialize(style);

            if (serializedStyle.isJsonObject())
            {
                JsonObject styleObject = (JsonObject)serializedStyle;

                for (Entry<String, JsonElement> entry : styleObject.entrySet())
                {
                    object.add((String)entry.getKey(), (JsonElement)entry.getValue());
                }
            }
        }

        public JsonElement serialize(IChatComponent component, Type typeOfSrc, JsonSerializationContext context)
        {
            if (component instanceof ChatComponentText && component.getChatStyle().isEmpty() && component.getSiblings().isEmpty())
            {
                return new JsonPrimitive(((ChatComponentText)component).getChatComponentText_TextValue());
            }
            else
            {
                JsonObject componentObject = new JsonObject();

                if (!component.getChatStyle().isEmpty())
                {
                    this.serializeChatStyle(component.getChatStyle(), componentObject, context);
                }

                if (!component.getSiblings().isEmpty())
                {
                    JsonArray extraArray = new JsonArray();

                    for (IChatComponent childComponent : component.getSiblings())
                    {
                        extraArray.add(this.serialize(childComponent, childComponent.getClass(), context));
                    }

                    componentObject.add("extra", extraArray);
                }

                if (component instanceof ChatComponentText)
                {
                    componentObject.addProperty("text", ((ChatComponentText)component).getChatComponentText_TextValue());
                }
                else if (component instanceof ChatComponentTranslation)
                {
                    ChatComponentTranslation translationComponent = (ChatComponentTranslation)component;
                    componentObject.addProperty("translate", translationComponent.getKey());

                    if (translationComponent.getFormatArgs() != null && translationComponent.getFormatArgs().length > 0)
                    {
                        JsonArray formatArgArray = new JsonArray();

                        for (Object formatArgument : translationComponent.getFormatArgs())
                        {
                            if (formatArgument instanceof IChatComponent)
                            {
                                formatArgArray.add(this.serialize((IChatComponent)formatArgument, formatArgument.getClass(), context));
                            }
                            else
                            {
                                formatArgArray.add(new JsonPrimitive(String.valueOf(formatArgument)));
                            }
                        }

                        componentObject.add("with", formatArgArray);
                    }
                }
                else if (component instanceof ChatComponentScore)
                {
                    ChatComponentScore scoreComponent = (ChatComponentScore)component;
                    JsonObject scoreObject = new JsonObject();
                    scoreObject.addProperty("name", scoreComponent.getName());
                    scoreObject.addProperty("objective", scoreComponent.getObjective());
                    scoreObject.addProperty("value", scoreComponent.getUnformattedTextForChat());
                    componentObject.add("score", scoreObject);
                }
                else
                {
                    if (!(component instanceof ChatComponentSelector))
                    {
                        throw new IllegalArgumentException("Don\'t know how to serialize " + component + " as a Component");
                    }

                    ChatComponentSelector selectorComponent = (ChatComponentSelector)component;
                    componentObject.addProperty("selector", selectorComponent.getSelector());
                }

                return componentObject;
            }
        }

        public static String componentToJson(IChatComponent component)
        {
            return GSON.toJson((Object)component);
        }

        public static IChatComponent jsonToComponent(String json)
        {
            return (IChatComponent)GSON.fromJson(json, IChatComponent.class);
        }

        static
        {
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.registerTypeHierarchyAdapter(IChatComponent.class, new IChatComponent.Serializer());
            gsonBuilder.registerTypeHierarchyAdapter(ChatStyle.class, new ChatStyle.Serializer());
            gsonBuilder.registerTypeAdapterFactory(new EnumTypeAdapterFactory());
            GSON = gsonBuilder.create();
        }
    }
}
