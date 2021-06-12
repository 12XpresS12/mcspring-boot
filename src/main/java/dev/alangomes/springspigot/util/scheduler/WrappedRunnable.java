package dev.alangomes.springspigot.util.scheduler;

class WrappedRunnable implements Runnable {

    private final SchedulerService scheduler;

    private final Runnable runnable;

    public WrappedRunnable(SchedulerService scheduler, Runnable runnable) {
        this.scheduler = scheduler;
        this.runnable = runnable;
    }

    @Override
    public void run() {
        scheduler.scheduleSyncDelayedTask(runnable);
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof WrappedRunnable)) return false;
        final WrappedRunnable other = (WrappedRunnable) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$runnable = this.runnable;
        final Object other$runnable = other.runnable;
        if (this$runnable == null ? other$runnable != null : !this$runnable.equals(other$runnable)) return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof WrappedRunnable;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $runnable = this.runnable;
        result = result * PRIME + ($runnable == null ? 43 : $runnable.hashCode());
        return result;
    }
}