package com.populaire.projetguerrefroide.command.handler;

import com.populaire.projetguerrefroide.command.request.Command;

public interface CommandHandler<C extends Command> {
    void handle(C command);
}
