package dev.alangomes.springspigot.command;

import com.google.common.annotations.VisibleForTesting;
import dev.alangomes.springspigot.configuration.DynamicValue;
import dev.alangomes.springspigot.configuration.Instance;
import dev.alangomes.springspigot.picocli.CommandLineDefinition;
import lombok.SneakyThrows;
import lombok.val;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandException;
import org.slf4j.Logger;
import org.springframework.aop.framework.autoproxy.AbstractAutoProxyCreator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import picocli.CommandLine;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import static org.apache.commons.lang.BooleanUtils.toBoolean;

@Primary
@Component
@ConditionalOnBean(annotation = CommandLine.Command.class)
public class DefaultCommandExecutor implements CommandExecutor {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(DefaultCommandExecutor.class);
    @Autowired
    private ApplicationContext applicationContext;

    @Autowired(required = false)
    private AbstractAutoProxyCreator proxyCreator;

    @Autowired
    private CommandLineDefinition cli;

    @DynamicValue("${spigot.messages.command_error:&cAn internal error occurred while attemping to perform this " +
            "command}")
    private Instance<String> commandErrorMessage;

    @DynamicValue("${spigot.messages.missing_parameter_error:&cMissing parameter: %s}")
    private Instance<String> missingParameterErrorMessage;

    @DynamicValue("${spigot.messages.parameter_error:&cInvalid parameter: %s}")
    private Instance<String> parameterErrorMessage;

    @DynamicValue("${spigot.commands.enable_cache:false}")
    private Instance<Boolean> cacheEnabled;

    private CommandLine commandLineCache;

    @Override
    public CommandResult execute(String... commandParts) {
        if (commandParts.length == 0) {
            return CommandResult.unknown();
        }
        try {
            if (!toBoolean(cacheEnabled.get()) || commandLineCache == null) {
                commandLineCache = cli.build(applicationContext);
            }
            val output = new ArrayList<String>();
            val commands = commandLineCache.parse(commandParts);

            if (commands.isEmpty()) {
                return CommandResult.unknown();
            }
            val commandLine = commands.get(commands.size() - 1);
            val command = proxyCreator != null ? proxyCreator.getEarlyBeanReference(commandLine.getCommand(), null) :
                    commandLine.getCommand();

            if (command instanceof Runnable) {
                ((Runnable) command).run();
            } else if (command instanceof Callable) {
                val result = ((Callable) command).call();
                output.addAll(buildOutput(result));
            }
            return new CommandResult(output);
        } catch (CommandLine.InitializationException ex) {
            log.error("Unexpected exception during command initialization", ex);
            return CommandResult.unknown();
        } catch (CommandLine.UnmatchedArgumentException ex) {
            val commandObject = ex.getCommandLine().getCommandSpec().userObject();
            if (commandObject == null || getBaseCommandClass().isInstance(commandObject)) {
                return CommandResult.unknown();
            }
            val message = String.format(parameterErrorMessage.get(), String.join(", ", ex.getUnmatched()));
            return new CommandResult(ChatColor.translateAlternateColorCodes('&', message), true);
        } catch (CommandLine.MissingParameterException ex) {
            val message = String.format(missingParameterErrorMessage.get(), ex.getMissing().get(0).paramLabel());
            return new CommandResult(ChatColor.translateAlternateColorCodes('&', message), true);
        } catch (CommandLine.ParameterException ex) {
            val message = String.format(parameterErrorMessage.get(), ex.getArgSpec().paramLabel());
            return new CommandResult(ChatColor.translateAlternateColorCodes('&', message), true);
        } catch (CommandException ex) {
            return new CommandResult(ChatColor.RED + ex.getMessage(), true);
        } catch (Exception ex) {
            log.error("Unexpected exception while running /" + StringUtils.join(commandParts, " "), ex);
            return new CommandResult(ChatColor.translateAlternateColorCodes('&', commandErrorMessage.get()), true);
        }
    }

    private List<String> buildOutput(Object result) {
        if (result instanceof String) {
            return Collections.singletonList(ChatColor.translateAlternateColorCodes('&', (String) result));
        } else if (result instanceof Collection) {
            return ((Collection<?>) result)
                    .stream()
                    .flatMap(res -> buildOutput(res).stream())
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    @SneakyThrows
    private Class<?> getBaseCommandClass() {
        return Class.forName("dev.alangomes.springspigot.picocli.BaseCommand");
    }

    @VisibleForTesting
    Instance<Boolean> getCacheEnabled() {
        return this.cacheEnabled;
    }

    @VisibleForTesting
    void setCommandErrorMessage(Instance<String> commandErrorMessage) {
        this.commandErrorMessage = commandErrorMessage;
    }

    @VisibleForTesting
    void setMissingParameterErrorMessage(Instance<String> missingParameterErrorMessage) {
        this.missingParameterErrorMessage = missingParameterErrorMessage;
    }

    @VisibleForTesting
    void setParameterErrorMessage(Instance<String> parameterErrorMessage) {
        this.parameterErrorMessage = parameterErrorMessage;
    }

    @VisibleForTesting
    void setCacheEnabled(Instance<Boolean> cacheEnabled) {
        this.cacheEnabled = cacheEnabled;
    }
}
