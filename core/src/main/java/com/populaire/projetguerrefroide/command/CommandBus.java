package com.populaire.projetguerrefroide.command;

import com.github.tommyettinger.ds.ObjectDeque;
import com.github.tommyettinger.ds.ObjectObjectMap;
import com.populaire.projetguerrefroide.command.handler.CommandHandler;
import com.populaire.projetguerrefroide.command.request.Command;
import com.populaire.projetguerrefroide.screen.listener.TimeListener;

import java.util.Map;
import java.util.Queue;

public class CommandBus {
    private final Map<Class<? extends Command>, CommandHandler<? extends Command>> handlers;
    private final Queue<Command> commandQueue;

    public CommandBus() {
        this.handlers = new ObjectObjectMap<>();
        this.commandQueue = new ObjectDeque<>();
    }

    public void register(Class<? extends Command> commandType, CommandHandler<? extends Command> handler) {
        this.handlers.put(commandType, handler);
    }

    public void dispatch(Command command) {
        this.commandQueue.add(command);
    }

    @SuppressWarnings("unchecked")
    public void process() {
        while(!this.commandQueue.isEmpty()) {
            Command command = this.commandQueue.poll();
            CommandHandler<Command> commandHandler = (CommandHandler<Command>) this.handlers.get(command.getClass());
            if(commandHandler == null) {
                continue;
            }
            try {
                commandHandler.handle(command);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
