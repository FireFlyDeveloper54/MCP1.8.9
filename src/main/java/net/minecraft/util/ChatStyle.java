package net.minecraft.util;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import java.util.Objects;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;

public class ChatStyle
{
    private ChatStyle parentStyle;
    private EnumChatFormatting color;
    private Boolean bold;
    private Boolean italic;
    private Boolean underlined;
    private Boolean strikethrough;
    private Boolean obfuscated;
    private ClickEvent chatClickEvent;
    private HoverEvent chatHoverEvent;
    private String insertion;
    private static final ChatStyle rootStyle = new ChatStyle()
    {
        public EnumChatFormatting getColor()
        {
            return null;
        }
        public boolean getBold()
        {
            return false;
        }
        public boolean getItalic()
        {
            return false;
        }
        public boolean getStrikethrough()
        {
            return false;
        }
        public boolean getUnderlined()
        {
            return false;
        }
        public boolean getObfuscated()
        {
            return false;
        }
        public ClickEvent getChatClickEvent()
        {
            return null;
        }
        public HoverEvent getChatHoverEvent()
        {
            return null;
        }
        public String getInsertion()
        {
            return null;
        }
        public ChatStyle setColor(EnumChatFormatting color)
        {
            throw new UnsupportedOperationException();
        }
        public ChatStyle setBold(Boolean boldIn)
        {
            throw new UnsupportedOperationException();
        }
        public ChatStyle setItalic(Boolean italic)
        {
            throw new UnsupportedOperationException();
        }
        public ChatStyle setStrikethrough(Boolean strikethrough)
        {
            throw new UnsupportedOperationException();
        }
        public ChatStyle setUnderlined(Boolean underlined)
        {
            throw new UnsupportedOperationException();
        }
        public ChatStyle setObfuscated(Boolean obfuscated)
        {
            throw new UnsupportedOperationException();
        }
        public ChatStyle setChatClickEvent(ClickEvent event)
        {
            throw new UnsupportedOperationException();
        }
        public ChatStyle setChatHoverEvent(HoverEvent event)
        {
            throw new UnsupportedOperationException();
        }
        public ChatStyle setParentStyle(ChatStyle parent)
        {
            throw new UnsupportedOperationException();
        }
        public String toString()
        {
            return "Style.ROOT";
        }
        public ChatStyle createShallowCopy()
        {
            return this;
        }
        public ChatStyle createDeepCopy()
        {
            return this;
        }
        public String getFormattingCode()
        {
            return "";
        }
    };

    public EnumChatFormatting getColor()
    {
        return this.color == null ? this.getParent().getColor() : this.color;
    }

    public boolean getBold()
    {
        return this.bold == null ? this.getParent().getBold() : this.bold.booleanValue();
    }

    public boolean getItalic()
    {
        return this.italic == null ? this.getParent().getItalic() : this.italic.booleanValue();
    }

    public boolean getStrikethrough()
    {
        return this.strikethrough == null ? this.getParent().getStrikethrough() : this.strikethrough.booleanValue();
    }

    public boolean getUnderlined()
    {
        return this.underlined == null ? this.getParent().getUnderlined() : this.underlined.booleanValue();
    }

    public boolean getObfuscated()
    {
        return this.obfuscated == null ? this.getParent().getObfuscated() : this.obfuscated.booleanValue();
    }

    public boolean isEmpty()
    {
        return this.bold == null && this.italic == null && this.strikethrough == null && this.underlined == null && this.obfuscated == null && this.color == null && this.chatClickEvent == null && this.chatHoverEvent == null;
    }

    public ClickEvent getChatClickEvent()
    {
        return this.chatClickEvent == null ? this.getParent().getChatClickEvent() : this.chatClickEvent;
    }

    public HoverEvent getChatHoverEvent()
    {
        return this.chatHoverEvent == null ? this.getParent().getChatHoverEvent() : this.chatHoverEvent;
    }

    public String getInsertion()
    {
        return this.insertion == null ? this.getParent().getInsertion() : this.insertion;
    }

    public ChatStyle setColor(EnumChatFormatting color)
    {
        this.color = color;
        return this;
    }

    public ChatStyle setBold(Boolean boldIn)
    {
        this.bold = boldIn;
        return this;
    }

    public ChatStyle setItalic(Boolean italic)
    {
        this.italic = italic;
        return this;
    }

    public ChatStyle setStrikethrough(Boolean strikethrough)
    {
        this.strikethrough = strikethrough;
        return this;
    }

    public ChatStyle setUnderlined(Boolean underlined)
    {
        this.underlined = underlined;
        return this;
    }

    public ChatStyle setObfuscated(Boolean obfuscated)
    {
        this.obfuscated = obfuscated;
        return this;
    }

    public ChatStyle setChatClickEvent(ClickEvent event)
    {
        this.chatClickEvent = event;
        return this;
    }

    public ChatStyle setChatHoverEvent(HoverEvent event)
    {
        this.chatHoverEvent = event;
        return this;
    }

    public ChatStyle setInsertion(String insertion)
    {
        this.insertion = insertion;
        return this;
    }

    public ChatStyle setParentStyle(ChatStyle parent)
    {
        this.parentStyle = parent;
        return this;
    }

    public String getFormattingCode()
    {
        if (this.isEmpty())
        {
            return this.parentStyle != null ? this.parentStyle.getFormattingCode() : "";
        }
        else
        {
            StringBuilder stringBuilder = new StringBuilder();

            if (this.getColor() != null)
            {
                stringBuilder.append((Object)this.getColor());
            }

            if (this.getBold())
            {
                stringBuilder.append((Object)EnumChatFormatting.BOLD);
            }

            if (this.getItalic())
            {
                stringBuilder.append((Object)EnumChatFormatting.ITALIC);
            }

            if (this.getUnderlined())
            {
                stringBuilder.append((Object)EnumChatFormatting.UNDERLINE);
            }

            if (this.getObfuscated())
            {
                stringBuilder.append((Object)EnumChatFormatting.OBFUSCATED);
            }

            if (this.getStrikethrough())
            {
                stringBuilder.append((Object)EnumChatFormatting.STRIKETHROUGH);
            }

            return stringBuilder.toString();
        }
    }

    private ChatStyle getParent()
    {
        return this.parentStyle == null ? rootStyle : this.parentStyle;
    }

    public String toString()
    {
        return "Style{hasParent=" + (this.parentStyle != null) + ", color=" + this.color + ", bold=" + this.bold + ", italic=" + this.italic + ", underlined=" + this.underlined + ", obfuscated=" + this.obfuscated + ", clickEvent=" + this.getChatClickEvent() + ", hoverEvent=" + this.getChatHoverEvent() + ", insertion=" + this.getInsertion() + '}';
    }

    public boolean equals(Object other)
    {
        if (this == other)
        {
            return true;
        }
        else if (!(other instanceof ChatStyle))
        {
            return false;
        }
        else
        {
            ChatStyle chatStyle = (ChatStyle)other;
            return this.getBold() == chatStyle.getBold()
                    && this.getColor() == chatStyle.getColor()
                    && this.getItalic() == chatStyle.getItalic()
                    && this.getObfuscated() == chatStyle.getObfuscated()
                    && this.getStrikethrough() == chatStyle.getStrikethrough()
                    && this.getUnderlined() == chatStyle.getUnderlined()
                    && Objects.equals(this.getChatClickEvent(), chatStyle.getChatClickEvent())
                    && Objects.equals(this.getChatHoverEvent(), chatStyle.getChatHoverEvent())
                    && Objects.equals(this.getInsertion(), chatStyle.getInsertion());
        }
    }

    public int hashCode()
    {
        int hashCode = this.color.hashCode();
        hashCode = 31 * hashCode + this.bold.hashCode();
        hashCode = 31 * hashCode + this.italic.hashCode();
        hashCode = 31 * hashCode + this.underlined.hashCode();
        hashCode = 31 * hashCode + this.strikethrough.hashCode();
        hashCode = 31 * hashCode + this.obfuscated.hashCode();
        hashCode = 31 * hashCode + this.chatClickEvent.hashCode();
        hashCode = 31 * hashCode + this.chatHoverEvent.hashCode();
        hashCode = 31 * hashCode + this.insertion.hashCode();
        return hashCode;
    }

    public ChatStyle createShallowCopy()
    {
        ChatStyle chatStyle = new ChatStyle();
        chatStyle.bold = this.bold;
        chatStyle.italic = this.italic;
        chatStyle.strikethrough = this.strikethrough;
        chatStyle.underlined = this.underlined;
        chatStyle.obfuscated = this.obfuscated;
        chatStyle.color = this.color;
        chatStyle.chatClickEvent = this.chatClickEvent;
        chatStyle.chatHoverEvent = this.chatHoverEvent;
        chatStyle.parentStyle = this.parentStyle;
        chatStyle.insertion = this.insertion;
        return chatStyle;
    }

    public ChatStyle createDeepCopy()
    {
        ChatStyle chatStyle = new ChatStyle();
        chatStyle.setBold(Boolean.valueOf(this.getBold()));
        chatStyle.setItalic(Boolean.valueOf(this.getItalic()));
        chatStyle.setStrikethrough(Boolean.valueOf(this.getStrikethrough()));
        chatStyle.setUnderlined(Boolean.valueOf(this.getUnderlined()));
        chatStyle.setObfuscated(Boolean.valueOf(this.getObfuscated()));
        chatStyle.setColor(this.getColor());
        chatStyle.setChatClickEvent(this.getChatClickEvent());
        chatStyle.setChatHoverEvent(this.getChatHoverEvent());
        chatStyle.setInsertion(this.getInsertion());
        return chatStyle;
    }

    public static class Serializer implements JsonDeserializer<ChatStyle>, JsonSerializer<ChatStyle>
    {
        public ChatStyle deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
        {
            if (json.isJsonObject())
            {
                ChatStyle chatStyle = new ChatStyle();
                JsonObject styleObject = json.getAsJsonObject();

                if (styleObject == null)
                {
                    return null;
                }
                else
                {
                    if (styleObject.has("bold"))
                    {
                        chatStyle.bold = Boolean.valueOf(styleObject.get("bold").getAsBoolean());
                    }

                    if (styleObject.has("italic"))
                    {
                        chatStyle.italic = Boolean.valueOf(styleObject.get("italic").getAsBoolean());
                    }

                    if (styleObject.has("underlined"))
                    {
                        chatStyle.underlined = Boolean.valueOf(styleObject.get("underlined").getAsBoolean());
                    }

                    if (styleObject.has("strikethrough"))
                    {
                        chatStyle.strikethrough = Boolean.valueOf(styleObject.get("strikethrough").getAsBoolean());
                    }

                    if (styleObject.has("obfuscated"))
                    {
                        chatStyle.obfuscated = Boolean.valueOf(styleObject.get("obfuscated").getAsBoolean());
                    }

                    if (styleObject.has("color"))
                    {
                        chatStyle.color = context.deserialize(styleObject.get("color"), EnumChatFormatting.class);
                    }

                    if (styleObject.has("insertion"))
                    {
                        chatStyle.insertion = styleObject.get("insertion").getAsString();
                    }

                    if (styleObject.has("clickEvent"))
                    {
                        JsonObject clickObject = styleObject.getAsJsonObject("clickEvent");

                        if (clickObject != null)
                        {
                            JsonPrimitive actionElement = clickObject.getAsJsonPrimitive("action");
                            ClickEvent.Action clickAction = actionElement == null ? null : ClickEvent.Action.getValueByCanonicalName(actionElement.getAsString());
                            JsonPrimitive valueElement = clickObject.getAsJsonPrimitive("value");
                            String value = valueElement == null ? null : valueElement.getAsString();

                            if (clickAction != null && value != null && clickAction.shouldAllowInChat())
                            {
                                chatStyle.chatClickEvent = new ClickEvent(clickAction, value);
                            }
                        }
                    }

                    if (styleObject.has("hoverEvent"))
                    {
                        JsonObject hoverObject = styleObject.getAsJsonObject("hoverEvent");

                        if (hoverObject != null)
                        {
                            JsonPrimitive actionElement = hoverObject.getAsJsonPrimitive("action");
                            HoverEvent.Action hoverAction = actionElement == null ? null : HoverEvent.Action.getValueByCanonicalName(actionElement.getAsString());
                            IChatComponent hoverValue = context.deserialize(hoverObject.get("value"), IChatComponent.class);

                            if (hoverAction != null && hoverValue != null && hoverAction.shouldAllowInChat())
                            {
                                chatStyle.chatHoverEvent = new HoverEvent(hoverAction, hoverValue);
                            }
                        }
                    }

                    return chatStyle;
                }
            }
            else
            {
                return null;
            }
        }

        public JsonElement serialize(ChatStyle src, Type typeOfSrc, JsonSerializationContext context)
        {
            if (src.isEmpty())
            {
                return null;
            }
            else
            {
                JsonObject jsonObject = new JsonObject();

                if (src.bold != null)
                {
                    jsonObject.addProperty("bold", src.bold);
                }

                if (src.italic != null)
                {
                    jsonObject.addProperty("italic", src.italic);
                }

                if (src.underlined != null)
                {
                    jsonObject.addProperty("underlined", src.underlined);
                }

                if (src.strikethrough != null)
                {
                    jsonObject.addProperty("strikethrough", src.strikethrough);
                }

                if (src.obfuscated != null)
                {
                    jsonObject.addProperty("obfuscated", src.obfuscated);
                }

                if (src.color != null)
                {
                    jsonObject.add("color", context.serialize(src.color));
                }

                if (src.insertion != null)
                {
                    jsonObject.add("insertion", context.serialize(src.insertion));
                }

                if (src.chatClickEvent != null)
                {
                    JsonObject clickObject = new JsonObject();
                    clickObject.addProperty("action", src.chatClickEvent.getAction().getCanonicalName());
                    clickObject.addProperty("value", src.chatClickEvent.getValue());
                    jsonObject.add("clickEvent", clickObject);
                }

                if (src.chatHoverEvent != null)
                {
                    JsonObject hoverObject = new JsonObject();
                    hoverObject.addProperty("action", src.chatHoverEvent.getAction().getCanonicalName());
                    hoverObject.add("value", context.serialize(src.chatHoverEvent.getValue()));
                    jsonObject.add("hoverEvent", hoverObject);
                }

                return jsonObject;
            }
        }
    }
}
