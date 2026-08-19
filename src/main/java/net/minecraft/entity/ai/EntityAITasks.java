package net.minecraft.entity.ai;

import com.google.common.collect.Lists;
import java.util.Iterator;
import java.util.List;
import net.minecraft.profiler.Profiler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EntityAITasks
{
    private static final Logger logger = LogManager.getLogger();
    private List<EntityAITasks.EntityAITaskEntry> taskEntries = Lists.<EntityAITasks.EntityAITaskEntry>newArrayList();
    private List<EntityAITasks.EntityAITaskEntry> executingTaskEntries = Lists.<EntityAITasks.EntityAITaskEntry>newArrayList();
    private final Profiler theProfiler;
    private int tickCount;
    private int tickRate = 3;

    public EntityAITasks(Profiler profilerIn)
    {
        this.theProfiler = profilerIn;
    }

    public void addTask(int priority, EntityAIBase task)
    {
        this.taskEntries.add(new EntityAITasks.EntityAITaskEntry(priority, task));
    }

    public void removeTask(EntityAIBase task)
    {
        Iterator<EntityAITasks.EntityAITaskEntry> iterator = this.taskEntries.iterator();

        while (iterator.hasNext())
        {
            EntityAITasks.EntityAITaskEntry taskEntry = iterator.next();
            EntityAIBase entityAIBase = taskEntry.action;

            if (entityAIBase == task)
            {
                if (this.executingTaskEntries.contains(taskEntry))
                {
                    entityAIBase.resetTask();
                    this.executingTaskEntries.remove(taskEntry);
                }

                iterator.remove();
            }
        }
    }

    public void onUpdateTasks()
    {
        this.theProfiler.startSection("goalSetup");

        if (this.tickCount++ % this.tickRate == 0)
        {
            Iterator<EntityAITasks.EntityAITaskEntry> iterator = this.taskEntries.iterator();
            label38:

            while (true)
            {
                EntityAITasks.EntityAITaskEntry taskEntry;

                while (true)
                {
                    if (!iterator.hasNext())
                    {
                        break label38;
                    }

                    taskEntry = iterator.next();
                    boolean isExecuting = this.executingTaskEntries.contains(taskEntry);

                    if (!isExecuting)
                    {
                        break;
                    }

                    if (!this.canUse(taskEntry) || !this.canContinue(taskEntry))
                    {
                        taskEntry.action.resetTask();
                        this.executingTaskEntries.remove(taskEntry);
                        break;
                    }
                }

                if (this.canUse(taskEntry) && taskEntry.action.shouldExecute())
                {
                    taskEntry.action.startExecuting();
                    this.executingTaskEntries.add(taskEntry);
                }
            }
        }
        else
        {
            Iterator<EntityAITasks.EntityAITaskEntry> executingIterator = this.executingTaskEntries.iterator();

            while (executingIterator.hasNext())
            {
                EntityAITasks.EntityAITaskEntry taskEntry = executingIterator.next();

                if (!this.canContinue(taskEntry))
                {
                    taskEntry.action.resetTask();
                    executingIterator.remove();
                }
            }
        }

        this.theProfiler.endSection();
        this.theProfiler.startSection("goalTick");

        for (EntityAITasks.EntityAITaskEntry taskEntry : this.executingTaskEntries)
        {
            taskEntry.action.updateTask();
        }

        this.theProfiler.endSection();
    }

    private boolean canContinue(EntityAITasks.EntityAITaskEntry taskEntry)
    {
        boolean canContinue = taskEntry.action.continueExecuting();
        return canContinue;
    }

    private boolean canUse(EntityAITasks.EntityAITaskEntry taskEntry)
    {
        for (EntityAITasks.EntityAITaskEntry otherTaskEntry : this.taskEntries)
        {
            if (otherTaskEntry != taskEntry)
            {
                if (taskEntry.priority >= otherTaskEntry.priority)
                {
                    if (!this.areTasksCompatible(taskEntry, otherTaskEntry) && this.executingTaskEntries.contains(otherTaskEntry))
                    {
                        return false;
                    }
                }
                else if (!otherTaskEntry.action.isInterruptible() && this.executingTaskEntries.contains(otherTaskEntry))
                {
                    return false;
                }
            }
        }

        return true;
    }

    private boolean areTasksCompatible(EntityAITasks.EntityAITaskEntry taskEntry1, EntityAITasks.EntityAITaskEntry taskEntry2)
    {
        return (taskEntry1.action.getMutexBits() & taskEntry2.action.getMutexBits()) == 0;
    }

    class EntityAITaskEntry
    {
        public EntityAIBase action;
        public int priority;

        public EntityAITaskEntry(int priorityIn, EntityAIBase task)
        {
            this.priority = priorityIn;
            this.action = task;
        }
    }
}
