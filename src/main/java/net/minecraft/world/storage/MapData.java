package net.minecraft.world.storage;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S34PacketMaps;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec4b;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;

public class MapData extends WorldSavedData
{
    public int xCenter;
    public int zCenter;
    public byte dimension;
    public byte scale;
    public byte[] colors = new byte[16384];
    public List<MapData.MapInfo> playersArrayList = Lists.<MapData.MapInfo>newArrayList();
    private Map<EntityPlayer, MapData.MapInfo> playersHashMap = Maps.<EntityPlayer, MapData.MapInfo>newHashMap();
    public Map<String, Vec4b> mapDecorations = Maps.<String, Vec4b>newLinkedHashMap();

    public MapData(String mapname)
    {
        super(mapname);
    }

    public void calculateMapCenter(double x, double z, int mapScale)
    {
        int i = 128 * (1 << mapScale);
        int j = MathHelper.floor_double((x + 64.0D) / (double)i);
        int k = MathHelper.floor_double((z + 64.0D) / (double)i);
        this.xCenter = j * i + i / 2 - 64;
        this.zCenter = k * i + i / 2 - 64;
    }

    public void readFromNBT(NBTTagCompound nbt)
    {
        this.dimension = nbt.getByte("dimension");
        this.xCenter = nbt.getInteger("xCenter");
        this.zCenter = nbt.getInteger("zCenter");
        this.scale = nbt.getByte("scale");
        this.scale = (byte)MathHelper.clamp_int(this.scale, 0, 4);
        int i = nbt.getShort("width");
        int j = nbt.getShort("height");

        if (i == 128 && j == 128)
        {
            this.colors = nbt.getByteArray("colors");
        }
        else
        {
            byte[] abyte = nbt.getByteArray("colors");
            this.colors = new byte[16384];
            int k = (128 - i) / 2;
            int l = (128 - j) / 2;

            for (int sourceRow = 0; sourceRow < j; ++sourceRow)
            {
                int secondIntValue2 = sourceRow + l;

                if (secondIntValue2 >= 0 || secondIntValue2 < 128)
                {
                    for (int nestedIndex = 0; nestedIndex < i; ++nestedIndex)
                    {
                        int fourthIntValue2 = nestedIndex + k;

                        if (fourthIntValue2 >= 0 || fourthIntValue2 < 128)
                        {
                            this.colors[fourthIntValue2 + secondIntValue2 * 128] = abyte[nestedIndex + sourceRow * i];
                        }
                    }
                }
            }
        }
    }

    public void writeToNBT(NBTTagCompound nbt)
    {
        nbt.setByte("dimension", this.dimension);
        nbt.setInteger("xCenter", this.xCenter);
        nbt.setInteger("zCenter", this.zCenter);
        nbt.setByte("scale", this.scale);
        nbt.setShort("width", (short)128);
        nbt.setShort("height", (short)128);
        nbt.setByteArray("colors", this.colors);
    }

    public void updateVisiblePlayers(EntityPlayer player, ItemStack mapStack)
    {
        if (!this.playersHashMap.containsKey(player))
        {
            MapData.MapInfo mapdata$mapinfo = new MapData.MapInfo(player);
            this.playersHashMap.put(player, mapdata$mapinfo);
            this.playersArrayList.add(mapdata$mapinfo);
        }

        if (!player.inventory.hasItemStack(mapStack))
        {
            this.mapDecorations.remove(player.getName());
        }

        for (int i = 0; i < this.playersArrayList.size(); ++i)
        {
            MapData.MapInfo mapdata$mapinfo1 = (MapData.MapInfo)this.playersArrayList.get(i);

            if (!mapdata$mapinfo1.entityplayerObj.isDead && (mapdata$mapinfo1.entityplayerObj.inventory.hasItemStack(mapStack) || mapStack.isOnItemFrame()))
            {
                if (!mapStack.isOnItemFrame() && mapdata$mapinfo1.entityplayerObj.dimension == this.dimension)
                {
                    this.updateDecorations(0, mapdata$mapinfo1.entityplayerObj.worldObj, mapdata$mapinfo1.entityplayerObj.getName(), mapdata$mapinfo1.entityplayerObj.posX, mapdata$mapinfo1.entityplayerObj.posZ, (double)mapdata$mapinfo1.entityplayerObj.rotationYaw);
                }
            }
            else
            {
                this.playersHashMap.remove(mapdata$mapinfo1.entityplayerObj);
                this.playersArrayList.remove(mapdata$mapinfo1);
            }
        }

        if (mapStack.isOnItemFrame())
        {
            EntityItemFrame entityItemFrame = mapStack.getItemFrame();
            BlockPos blockPos = entityItemFrame.getHangingPosition();
            this.updateDecorations(1, player.worldObj, "frame-" + entityItemFrame.getEntityId(), (double)blockPos.getX(), (double)blockPos.getZ(), (double)(entityItemFrame.facingDirection.getHorizontalIndex() * 90));
        }

        if (mapStack.hasTagCompound() && mapStack.getTagCompound().hasKey("Decorations", 9))
        {
            NBTTagList nBTTagList = mapStack.getTagCompound().getTagList("Decorations", 10);

            for (int j = 0; j < nBTTagList.tagCount(); ++j)
            {
                NBTTagCompound nBTTagCompound = nBTTagList.getCompoundTagAt(j);

                if (!this.mapDecorations.containsKey(nBTTagCompound.getString("id")))
                {
                    this.updateDecorations(nBTTagCompound.getByte("type"), player.worldObj, nBTTagCompound.getString("id"), nBTTagCompound.getDouble("x"), nBTTagCompound.getDouble("z"), nBTTagCompound.getDouble("rot"));
                }
            }
        }
    }

    private void updateDecorations(int type, World worldIn, String entityIdentifier, double worldX, double worldZ, double rotation)
    {
        int i = 1 << this.scale;
        float f = (float)(worldX - (double)this.xCenter) / (float)i;
        float mapZ = (float)(worldZ - (double)this.zCenter) / (float)i;
        byte byteValue = (byte)((int)((double)(f * 2.0F) + 0.5D));
        byte secondByteValue = (byte)((int)((double)(mapZ * 2.0F) + 0.5D));
        int j = 63;
        byte thirdByteValue;

        if (f >= (float)(-j) && mapZ >= (float)(-j) && f <= (float)j && mapZ <= (float)j)
        {
            rotation = rotation + (rotation < 0.0D ? -8.0D : 8.0D);
            thirdByteValue = (byte)((int)(rotation * 16.0D / 360.0D));

            if (this.dimension < 0)
            {
                int k = (int)(worldIn.getWorldInfo().getWorldTime() / 10L);
                thirdByteValue = (byte)(k * k * 34187121 + k * 121 >> 15 & 15);
            }
        }
        else
        {
            if (Math.abs(f) >= 320.0F || Math.abs(mapZ) >= 320.0F)
            {
                this.mapDecorations.remove(entityIdentifier);
                return;
            }

            type = 6;
            thirdByteValue = 0;

            if (f <= (float)(-j))
            {
                byteValue = (byte)((int)((double)(j * 2) + 2.5D));
            }

            if (mapZ <= (float)(-j))
            {
                secondByteValue = (byte)((int)((double)(j * 2) + 2.5D));
            }

            if (f >= (float)j)
            {
                byteValue = (byte)(j * 2 + 1);
            }

            if (mapZ >= (float)j)
            {
                secondByteValue = (byte)(j * 2 + 1);
            }
        }

        this.mapDecorations.put(entityIdentifier, new Vec4b((byte)type, byteValue, secondByteValue, thirdByteValue));
    }

    public Packet getMapPacket(ItemStack mapStack, World worldIn, EntityPlayer player)
    {
        MapData.MapInfo mapdata$mapinfo = (MapData.MapInfo)this.playersHashMap.get(player);
        return mapdata$mapinfo == null ? null : mapdata$mapinfo.getPacket(mapStack);
    }

    public void updateMapData(int x, int y)
    {
        super.markDirty();

        for (MapData.MapInfo mapdata$mapinfo : this.playersArrayList)
        {
            mapdata$mapinfo.update(x, y);
        }
    }

    public MapData.MapInfo getMapInfo(EntityPlayer player)
    {
        MapData.MapInfo mapdata$mapinfo = (MapData.MapInfo)this.playersHashMap.get(player);

        if (mapdata$mapinfo == null)
        {
            mapdata$mapinfo = new MapData.MapInfo(player);
            this.playersHashMap.put(player, mapdata$mapinfo);
            this.playersArrayList.add(mapdata$mapinfo);
        }

        return mapdata$mapinfo;
    }

    public class MapInfo
    {
        public final EntityPlayer entityplayerObj;
        private boolean dirty = true;
        private int minX = 0;
        private int minY = 0;
        private int maxX = 127;
        private int maxY = 127;
        private int emptyPacketTick;
        public int step;

        public MapInfo(EntityPlayer player)
        {
            this.entityplayerObj = player;
        }

        public Packet getPacket(ItemStack stack)
        {
            if (this.dirty)
            {
                this.dirty = false;
                return new S34PacketMaps(stack.getMetadata(), MapData.this.scale, MapData.this.mapDecorations.values(), MapData.this.colors, this.minX, this.minY, this.maxX + 1 - this.minX, this.maxY + 1 - this.minY);
            }
            else
            {
                return this.emptyPacketTick++ % 5 == 0 ? new S34PacketMaps(stack.getMetadata(), MapData.this.scale, MapData.this.mapDecorations.values(), MapData.this.colors, 0, 0, 0, 0) : null;
            }
        }

        public void update(int x, int y)
        {
            if (this.dirty)
            {
                this.minX = Math.min(this.minX, x);
                this.minY = Math.min(this.minY, y);
                this.maxX = Math.max(this.maxX, x);
                this.maxY = Math.max(this.maxY, y);
            }
            else
            {
                this.dirty = true;
                this.minX = x;
                this.minY = y;
                this.maxX = x;
                this.maxY = y;
            }
        }
    }
}
