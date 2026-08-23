package net.minecraft.event;

import com.google.common.collect.Maps;
import java.util.Map;

public class ClickEvent
{
    private final ClickEvent.Action action;
    private final String value;

    public ClickEvent(ClickEvent.Action theAction, String theValue)
    {
        this.action = theAction;
        this.value = theValue;
    }

    public ClickEvent.Action getAction()
    {
        return this.action;
    }

    public String getValue()
    {
        return this.value;
    }

    public boolean equals(Object other)
    {
        if (this == other)
        {
            return true;
        }
        else if (other != null && this.getClass() == other.getClass())
        {
            ClickEvent clickEvent = (ClickEvent)other;

            if (this.action != clickEvent.action)
            {
                return false;
            }
            else
            {
                if (this.value != null)
                {
                    if (!this.value.equals(clickEvent.value))
                    {
                        return false;
                    }
                }
                else if (clickEvent.value != null)
                {
                    return false;
                }

                return true;
            }
        }
        else
        {
            return false;
        }
    }

    public String toString()
    {
        return "ClickEvent{action=" + this.action + ", value=\'" + this.value + '\'' + '}';
    }

    public int hashCode()
    {
        int i = this.action.hashCode();
        i = 31 * i + (this.value != null ? this.value.hashCode() : 0);
        return i;
    }

    public static enum Action
    {
        OPEN_URL("open_url", true),
        OPEN_FILE("open_file", false),
        RUN_COMMAND("run_command", true),
        SUGGEST_COMMAND("suggest_command", true),
        CHANGE_PAGE("change_page", true);

        private static final ClickEvent.Action[] VALUES = values();
        private static final Map<String, ClickEvent.Action> nameMapping = Maps.<String, ClickEvent.Action>newHashMap();
        private final boolean allowedInChat;
        private final String canonicalName;

        private Action(String canonicalNameIn, boolean allowedInChatIn)
        {
            this.canonicalName = canonicalNameIn;
            this.allowedInChat = allowedInChatIn;
        }

        public boolean shouldAllowInChat()
        {
            return this.allowedInChat;
        }

        public String getCanonicalName()
        {
            return this.canonicalName;
        }

        public static ClickEvent.Action getValueByCanonicalName(String canonicalNameIn)
        {
            return nameMapping.get(canonicalNameIn);
        }

        static {
            for (ClickEvent.Action clickevent$action : VALUES)
            {
                nameMapping.put(clickevent$action.getCanonicalName(), clickevent$action);
            }
        }
    }
}
