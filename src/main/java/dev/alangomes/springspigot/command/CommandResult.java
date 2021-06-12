package dev.alangomes.springspigot.command;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public final class CommandResult {

    private static final CommandResult UNKNOWN_COMMAND = new CommandResult(null, false, false);

    private final boolean errored;

    private final boolean exists;

    private final List<String> output;

    private CommandResult(Collection<String> output, boolean errored, boolean exists) {
        this.errored = errored;
        this.exists = exists;
        this.output = output != null ? Collections.unmodifiableList(new LinkedList<>(output)) : Collections.emptyList();
    }

    public CommandResult(Collection<String> output) {
        this(output, false, true);
    }

    public CommandResult(String output, boolean errored) {
        this(Collections.singletonList(output), errored, true);
    }

    public static CommandResult unknown() {
        return UNKNOWN_COMMAND;
    }

    public boolean isErrored() {
        return this.errored;
    }

    public boolean isExists() {
        return this.exists;
    }

    public List<String> getOutput() {
        return this.output;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof CommandResult)) return false;
        final CommandResult other = (CommandResult) o;
        if (this.isErrored() != other.isErrored()) return false;
        if (this.isExists() != other.isExists()) return false;
        final Object this$output = this.getOutput();
        final Object other$output = other.getOutput();
        if (this$output == null ? other$output != null : !this$output.equals(other$output)) return false;
        return true;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        result = result * PRIME + (this.isErrored() ? 79 : 97);
        result = result * PRIME + (this.isExists() ? 79 : 97);
        final Object $output = this.getOutput();
        result = result * PRIME + ($output == null ? 43 : $output.hashCode());
        return result;
    }

    public String toString() {
        return "CommandResult(errored=" + this.isErrored() + ", exists=" + this.isExists() + ", output=" + this.getOutput() + ")";
    }
}
