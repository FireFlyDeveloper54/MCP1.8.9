package net.minecraft.init;

import com.mojang.authlib.GameProfile;
import java.io.PrintStream;
import java.util.Random;
import java.util.UUID;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDispenser;
import net.minecraft.block.BlockFire;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.BlockPumpkin;
import net.minecraft.block.BlockSkull;
import net.minecraft.block.BlockTNT;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.dispenser.BehaviorDefaultDispenseItem;
import net.minecraft.dispenser.BehaviorProjectileDispense;
import net.minecraft.dispenser.IBehaviorDispenseItem;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.dispenser.IPosition;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityExpBottle;
import net.minecraft.entity.item.EntityFireworkRocket;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityEgg;
import net.minecraft.entity.projectile.EntityPotion;
import net.minecraft.entity.projectile.EntitySmallFireball;
import net.minecraft.entity.projectile.EntitySnowball;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBucket;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemMonsterPlacer;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityDispenser;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.LoggingPrintStream;
import net.minecraft.util.StringUtils;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Bootstrap
{
    private static final PrintStream SYSOUT = System.out;
    private static boolean alreadyRegistered = false;
    private static final Logger LOGGER = LogManager.getLogger();

    public static boolean isRegistered()
    {
        return alreadyRegistered;
    }

    static void registerDispenserBehaviors()
    {
        BlockDispenser.dispenseBehaviorRegistry.putObject(Items.arrow, new BehaviorProjectileDispense()
        {
            protected IProjectile getProjectileEntity(World worldIn, IPosition position)
            {
                EntityArrow entityArrow = new EntityArrow(worldIn, position.getX(), position.getY(), position.getZ());
                entityArrow.canBePickedUp = 1;
                return entityArrow;
            }
        });
        BlockDispenser.dispenseBehaviorRegistry.putObject(Items.egg, new BehaviorProjectileDispense()
        {
            protected IProjectile getProjectileEntity(World worldIn, IPosition position)
            {
                return new EntityEgg(worldIn, position.getX(), position.getY(), position.getZ());
            }
        });
        BlockDispenser.dispenseBehaviorRegistry.putObject(Items.snowball, new BehaviorProjectileDispense()
        {
            protected IProjectile getProjectileEntity(World worldIn, IPosition position)
            {
                return new EntitySnowball(worldIn, position.getX(), position.getY(), position.getZ());
            }
        });
        BlockDispenser.dispenseBehaviorRegistry.putObject(Items.experience_bottle, new BehaviorProjectileDispense()
        {
            protected IProjectile getProjectileEntity(World worldIn, IPosition position)
            {
                return new EntityExpBottle(worldIn, position.getX(), position.getY(), position.getZ());
            }
            protected float getProjectileInaccuracy()
            {
                return super.getProjectileInaccuracy() * 0.5F;
            }
            protected float getProjectileVelocity()
            {
                return super.getProjectileVelocity() * 1.25F;
            }
        });
        BlockDispenser.dispenseBehaviorRegistry.putObject(Items.potionitem, new IBehaviorDispenseItem()
        {
            private final BehaviorDefaultDispenseItem defaultDispenseBehavior = new BehaviorDefaultDispenseItem();
            public ItemStack dispense(IBlockSource source, final ItemStack stack)
            {
                return ItemPotion.isSplash(stack.getMetadata()) ? (new BehaviorProjectileDispense()
                {
                    protected IProjectile getProjectileEntity(World worldIn, IPosition position)
                    {
                        return new EntityPotion(worldIn, position.getX(), position.getY(), position.getZ(), stack.copy());
                    }
                    protected float getProjectileInaccuracy()
                    {
                        return super.getProjectileInaccuracy() * 0.5F;
                    }
                    protected float getProjectileVelocity()
                    {
                        return super.getProjectileVelocity() * 1.25F;
                    }
                }).dispense(source, stack): this.defaultDispenseBehavior.dispense(source, stack);
            }
        });
        BlockDispenser.dispenseBehaviorRegistry.putObject(Items.spawn_egg, new BehaviorDefaultDispenseItem()
        {
            public ItemStack dispenseStack(IBlockSource source, ItemStack stack)
            {
                EnumFacing enumfacing = BlockDispenser.getFacing(source.getBlockMetadata());
                double thirdDoubleValue = source.getX() + (double)enumfacing.getFrontOffsetX();
                double fourthDoubleValue = (double)((float)source.getBlockPos().getY() + 0.2F);
                double sixthDoubleValue = source.getZ() + (double)enumfacing.getFrontOffsetZ();
                Entity entity = ItemMonsterPlacer.spawnCreature(source.getWorld(), stack.getMetadata(), thirdDoubleValue, fourthDoubleValue, sixthDoubleValue);

                if (entity instanceof EntityLivingBase && stack.hasDisplayName())
                {
                    ((EntityLiving)entity).setCustomNameTag(stack.getDisplayName());
                }

                stack.splitStack(1);
                return stack;
            }
        });
        BlockDispenser.dispenseBehaviorRegistry.putObject(Items.fireworks, new BehaviorDefaultDispenseItem()
        {
            public ItemStack dispenseStack(IBlockSource source, ItemStack stack)
            {
                EnumFacing enumfacing = BlockDispenser.getFacing(source.getBlockMetadata());
                double secondDoubleValue = source.getX() + (double)enumfacing.getFrontOffsetX();
                double fifthDoubleValue = (double)((float)source.getBlockPos().getY() + 0.2F);
                double seventhDoubleValue = source.getZ() + (double)enumfacing.getFrontOffsetZ();
                EntityFireworkRocket entityFireworkRocket = new EntityFireworkRocket(source.getWorld(), secondDoubleValue, fifthDoubleValue, seventhDoubleValue, stack);
                source.getWorld().spawnEntityInWorld(entityFireworkRocket);
                stack.splitStack(1);
                return stack;
            }
            protected void playDispenseSound(IBlockSource source)
            {
                source.getWorld().playAuxSFX(1002, source.getBlockPos(), 0);
            }
        });
        BlockDispenser.dispenseBehaviorRegistry.putObject(Items.fire_charge, new BehaviorDefaultDispenseItem()
        {
            public ItemStack dispenseStack(IBlockSource source, ItemStack stack)
            {
                EnumFacing enumfacing = BlockDispenser.getFacing(source.getBlockMetadata());
                IPosition iposition = BlockDispenser.getDispensePosition(source);
                double xCoordinate = iposition.getX() + (double)((float)enumfacing.getFrontOffsetX() * 0.3F);
                double yCoordinate = iposition.getY() + (double)((float)enumfacing.getFrontOffsetY() * 0.3F);
                double zCoordinate = iposition.getZ() + (double)((float)enumfacing.getFrontOffsetZ() * 0.3F);
                World world = source.getWorld();
                Random random = world.rand;
                double doubleValue = random.nextGaussian() * 0.05D + (double)enumfacing.getFrontOffsetX();
                double doubleValue2 = random.nextGaussian() * 0.05D + (double)enumfacing.getFrontOffsetY();
                double doubleValue3 = random.nextGaussian() * 0.05D + (double)enumfacing.getFrontOffsetZ();
                world.spawnEntityInWorld(new EntitySmallFireball(world, xCoordinate, yCoordinate, zCoordinate, doubleValue, doubleValue2, doubleValue3));
                stack.splitStack(1);
                return stack;
            }
            protected void playDispenseSound(IBlockSource source)
            {
                source.getWorld().playAuxSFX(1009, source.getBlockPos(), 0);
            }
        });
        BlockDispenser.dispenseBehaviorRegistry.putObject(Items.boat, new BehaviorDefaultDispenseItem()
        {
            private final BehaviorDefaultDispenseItem defaultDispenseBehavior = new BehaviorDefaultDispenseItem();
            public ItemStack dispenseStack(IBlockSource source, ItemStack stack)
            {
                EnumFacing enumfacing = BlockDispenser.getFacing(source.getBlockMetadata());
                World world = source.getWorld();
                double xCoordinate = source.getX() + (double)((float)enumfacing.getFrontOffsetX() * 1.125F);
                double yCoordinate = source.getY() + (double)((float)enumfacing.getFrontOffsetY() * 1.125F);
                double zCoordinate = source.getZ() + (double)((float)enumfacing.getFrontOffsetZ() * 1.125F);
                BlockPos blockPos = source.getBlockPos().offset(enumfacing);
                Material material = world.getBlockState(blockPos).getBlock().getMaterial();
                double doubleValue;

                if (Material.water.equals(material))
                {
                    doubleValue = 1.0D;
                }
                else
                {
                    if (!Material.air.equals(material) || !Material.water.equals(world.getBlockState(blockPos.down()).getBlock().getMaterial()))
                    {
                        return this.defaultDispenseBehavior.dispense(source, stack);
                    }

                    doubleValue = 0.0D;
                }

                EntityBoat entityboat = new EntityBoat(world, xCoordinate, yCoordinate + doubleValue, zCoordinate);
                world.spawnEntityInWorld(entityboat);
                stack.splitStack(1);
                return stack;
            }
            protected void playDispenseSound(IBlockSource source)
            {
                source.getWorld().playAuxSFX(1000, source.getBlockPos(), 0);
            }
        });
        IBehaviorDispenseItem ibehaviordispenseitem = new BehaviorDefaultDispenseItem()
        {
            private final BehaviorDefaultDispenseItem defaultDispenseBehavior = new BehaviorDefaultDispenseItem();
            public ItemStack dispenseStack(IBlockSource source, ItemStack stack)
            {
                ItemBucket itemBucket = (ItemBucket)stack.getItem();
                BlockPos blockpos = source.getBlockPos().offset(BlockDispenser.getFacing(source.getBlockMetadata()));

                if (itemBucket.tryPlaceContainedLiquid(source.getWorld(), blockpos))
                {
                    stack.setItem(Items.bucket);
                    stack.stackSize = 1;
                    return stack;
                }
                else
                {
                    return this.defaultDispenseBehavior.dispense(source, stack);
                }
            }
        };
        BlockDispenser.dispenseBehaviorRegistry.putObject(Items.lava_bucket, ibehaviordispenseitem);
        BlockDispenser.dispenseBehaviorRegistry.putObject(Items.water_bucket, ibehaviordispenseitem);
        BlockDispenser.dispenseBehaviorRegistry.putObject(Items.bucket, new BehaviorDefaultDispenseItem()
        {
            private final BehaviorDefaultDispenseItem defaultDispenseBehavior = new BehaviorDefaultDispenseItem();
            public ItemStack dispenseStack(IBlockSource source, ItemStack stack)
            {
                World world = source.getWorld();
                BlockPos blockPos = source.getBlockPos().offset(BlockDispenser.getFacing(source.getBlockMetadata()));
                IBlockState iblockstate = world.getBlockState(blockPos);
                Block block = iblockstate.getBlock();
                Material material = block.getMaterial();
                Item item;

                if (Material.water.equals(material) && block instanceof BlockLiquid && ((Integer)iblockstate.getValue(BlockLiquid.LEVEL)).intValue() == 0)
                {
                    item = Items.water_bucket;
                }
                else
                {
                    if (!Material.lava.equals(material) || !(block instanceof BlockLiquid) || ((Integer)iblockstate.getValue(BlockLiquid.LEVEL)).intValue() != 0)
                    {
                        return super.dispenseStack(source, stack);
                    }

                    item = Items.lava_bucket;
                }

                world.setBlockToAir(blockPos);

                if (--stack.stackSize == 0)
                {
                    stack.setItem(item);
                    stack.stackSize = 1;
                }
                else if (((TileEntityDispenser)source.getBlockTileEntity()).addItemStack(new ItemStack(item)) < 0)
                {
                    this.defaultDispenseBehavior.dispense(source, new ItemStack(item));
                }

                return stack;
            }
        });
        BlockDispenser.dispenseBehaviorRegistry.putObject(Items.flint_and_steel, new BehaviorDefaultDispenseItem()
        {
            private boolean dispenseSucceeded = true;
            protected ItemStack dispenseStack(IBlockSource source, ItemStack stack)
            {
                World world = source.getWorld();
                BlockPos blockPos = source.getBlockPos().offset(BlockDispenser.getFacing(source.getBlockMetadata()));

                if (world.isAirBlock(blockPos))
                {
                    world.setBlockState(blockPos, Blocks.fire.getDefaultState());

                    if (stack.attemptDamageItem(1, world.rand))
                    {
                        stack.stackSize = 0;
                    }
                }
                else if (world.getBlockState(blockPos).getBlock() == Blocks.tnt)
                {
                    Blocks.tnt.onBlockDestroyedByPlayer(world, blockPos, Blocks.tnt.getDefaultState().withProperty(BlockTNT.EXPLODE, Boolean.valueOf(true)));
                    world.setBlockToAir(blockPos);
                }
                else
                {
                    this.dispenseSucceeded = false;
                }

                return stack;
            }
            protected void playDispenseSound(IBlockSource source)
            {
                if (this.dispenseSucceeded)
                {
                    source.getWorld().playAuxSFX(1000, source.getBlockPos(), 0);
                }
                else
                {
                    source.getWorld().playAuxSFX(1001, source.getBlockPos(), 0);
                }
            }
        });
        BlockDispenser.dispenseBehaviorRegistry.putObject(Items.dye, new BehaviorDefaultDispenseItem()
        {
            private boolean dispenseSucceeded = true;
            protected ItemStack dispenseStack(IBlockSource source, ItemStack stack)
            {
                if (EnumDyeColor.WHITE == EnumDyeColor.byDyeDamage(stack.getMetadata()))
                {
                    World world = source.getWorld();
                    BlockPos blockPos = source.getBlockPos().offset(BlockDispenser.getFacing(source.getBlockMetadata()));

                    if (ItemDye.applyBonemeal(stack, world, blockPos))
                    {
                        if (!world.isRemote)
                        {
                            world.playAuxSFX(2005, blockPos, 0);
                        }
                    }
                    else
                    {
                        this.dispenseSucceeded = false;
                    }

                    return stack;
                }
                else
                {
                    return super.dispenseStack(source, stack);
                }
            }
            protected void playDispenseSound(IBlockSource source)
            {
                if (this.dispenseSucceeded)
                {
                    source.getWorld().playAuxSFX(1000, source.getBlockPos(), 0);
                }
                else
                {
                    source.getWorld().playAuxSFX(1001, source.getBlockPos(), 0);
                }
            }
        });
        BlockDispenser.dispenseBehaviorRegistry.putObject(Item.getItemFromBlock(Blocks.tnt), new BehaviorDefaultDispenseItem()
        {
            protected ItemStack dispenseStack(IBlockSource source, ItemStack stack)
            {
                World world = source.getWorld();
                BlockPos blockPos = source.getBlockPos().offset(BlockDispenser.getFacing(source.getBlockMetadata()));
                EntityTNTPrimed entityTNTPrimed = new EntityTNTPrimed(world, (double)blockPos.getX() + 0.5D, (double)blockPos.getY(), (double)blockPos.getZ() + 0.5D, (EntityLivingBase)null);
                world.spawnEntityInWorld(entityTNTPrimed);
                world.playSoundAtEntity(entityTNTPrimed, "game.tnt.primed", 1.0F, 1.0F);
                --stack.stackSize;
                return stack;
            }
        });
        BlockDispenser.dispenseBehaviorRegistry.putObject(Items.skull, new BehaviorDefaultDispenseItem()
        {
            private boolean dispenseSucceeded = true;
            protected ItemStack dispenseStack(IBlockSource source, ItemStack stack)
            {
                World world = source.getWorld();
                EnumFacing enumfacing = BlockDispenser.getFacing(source.getBlockMetadata());
                BlockPos blockPos = source.getBlockPos().offset(enumfacing);
                BlockSkull blockSkull = Blocks.skull;

                if (world.isAirBlock(blockPos) && blockSkull.canDispenserPlace(world, blockPos, stack))
                {
                    if (!world.isRemote)
                    {
                        world.setBlockState(blockPos, blockSkull.getDefaultState().withProperty(BlockSkull.FACING, EnumFacing.UP), 3);
                        TileEntity tileEntity = world.getTileEntity(blockPos);

                        if (tileEntity instanceof TileEntitySkull)
                        {
                            if (stack.getMetadata() == 3)
                            {
                                GameProfile gameProfile = null;

                                if (stack.hasTagCompound())
                                {
                                    NBTTagCompound nBTTagCompound = stack.getTagCompound();

                                    if (nBTTagCompound.hasKey("SkullOwner", 10))
                                    {
                                        gameProfile = NBTUtil.readGameProfileFromNBT(nBTTagCompound.getCompoundTag("SkullOwner"));
                                    }
                                    else if (nBTTagCompound.hasKey("SkullOwner", 8))
                                    {
                                        String s = nBTTagCompound.getString("SkullOwner");

                                        if (!StringUtils.isNullOrEmpty(s))
                                        {
                                            gameProfile = new GameProfile((UUID)null, s);
                                        }
                                    }
                                }

                                ((TileEntitySkull)tileEntity).setPlayerProfile(gameProfile);
                            }
                            else
                            {
                                ((TileEntitySkull)tileEntity).setType(stack.getMetadata());
                            }

                            ((TileEntitySkull)tileEntity).setSkullRotation(enumfacing.getOpposite().getHorizontalIndex() * 4);
                            Blocks.skull.checkWitherSpawn(world, blockPos, (TileEntitySkull)tileEntity);
                        }

                        --stack.stackSize;
                    }
                }
                else
                {
                    this.dispenseSucceeded = false;
                }

                return stack;
            }
            protected void playDispenseSound(IBlockSource source)
            {
                if (this.dispenseSucceeded)
                {
                    source.getWorld().playAuxSFX(1000, source.getBlockPos(), 0);
                }
                else
                {
                    source.getWorld().playAuxSFX(1001, source.getBlockPos(), 0);
                }
            }
        });
        BlockDispenser.dispenseBehaviorRegistry.putObject(Item.getItemFromBlock(Blocks.pumpkin), new BehaviorDefaultDispenseItem()
        {
            private boolean dispenseSucceeded = true;
            protected ItemStack dispenseStack(IBlockSource source, ItemStack stack)
            {
                World world = source.getWorld();
                BlockPos blockPos = source.getBlockPos().offset(BlockDispenser.getFacing(source.getBlockMetadata()));
                BlockPumpkin blockPumpkin = (BlockPumpkin)Blocks.pumpkin;

                if (world.isAirBlock(blockPos) && blockPumpkin.canDispenserPlace(world, blockPos))
                {
                    if (!world.isRemote)
                    {
                        world.setBlockState(blockPos, blockPumpkin.getDefaultState(), 3);
                    }

                    --stack.stackSize;
                }
                else
                {
                    this.dispenseSucceeded = false;
                }

                return stack;
            }
            protected void playDispenseSound(IBlockSource source)
            {
                if (this.dispenseSucceeded)
                {
                    source.getWorld().playAuxSFX(1000, source.getBlockPos(), 0);
                }
                else
                {
                    source.getWorld().playAuxSFX(1001, source.getBlockPos(), 0);
                }
            }
        });
    }

    public static void register()
    {
        if (!alreadyRegistered)
        {
            alreadyRegistered = true;

            if (LOGGER.isDebugEnabled())
            {
                redirectOutputToLog();
            }

            Block.registerBlocks();
            BlockFire.init();
            Item.registerItems();
            StatList.init();
            registerDispenserBehaviors();
        }
    }

    private static void redirectOutputToLog()
    {
        System.setErr(new LoggingPrintStream("STDERR", System.err));
        System.setOut(new LoggingPrintStream("STDOUT", SYSOUT));
    }

    public static void printToSYSOUT(String message)
    {
        SYSOUT.println(message);
    }
}
