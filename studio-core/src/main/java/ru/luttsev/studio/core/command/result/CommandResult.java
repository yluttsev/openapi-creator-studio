package ru.luttsev.studio.core.command.result;

public sealed interface CommandResult
        permits CommandSucceeded, CommandRejected {
}
