package ru.luttsev.studio.core.command;

import ru.luttsev.studio.core.command.result.CommandResult;

public interface DocumentCommand {

    CommandResult execute(CommandContext context);
}
