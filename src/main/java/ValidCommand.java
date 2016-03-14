package main.java;

import main.resources.Trace;

/*
 * Chain-of-Responsibility Design Pattern
 */
public abstract class ValidCommand {
	
    protected ValidCommand successor;

    public void setSuccessor(ValidCommand successor) {
        this.successor = successor;
    }

    abstract public void isValid(Command command);

}

// CLASSES TO PROCESS COMMANDS:

/*
 * invalid if not zero arguments
 */
class NotZeroArguments extends ValidCommand {
	@Override
	public void isValid(Command command) {
		if (command.getArgs().length != 0) {
			command.output("Client: invalid number of arguments. Type /help");
			Trace.getInstance().write(this, command.getPlayer().getName() + ": Invalid number of arguments.");
			command.notValid();
		} else if (successor != null) {
        	successor.isValid(command);
    	}	
	}	
}

/*
 * invalid if less than one argument
 */
class LessThanOneArgument extends ValidCommand {
	@Override
	public void isValid(Command command) {
		if (command.getArgs().length < 1) {
			command.output("Client: invalid number of arguments. Type /help");
			Trace.getInstance().write(this, command.getPlayer().getName() + ": Invalid number of arguments.");
			command.notValid();
		} else if (successor != null) {
        	successor.isValid(command);
    	}	
	}	
}

/*
 * invalid if no arguments
 */
class NoArguments extends ValidCommand {
	@Override
	public void isValid(Command command) {
		if (command.getArgs().length == 0) {
			command.output("Client: invalid number of arguments. Type /help");
			Trace.getInstance().write(this, command.getPlayer().getName() + ": Invalid number of arguments.");
			command.notValid();
		} else if (successor != null) {
        	successor.isValid(command);
    	}	
	}	
}

/*
 * invalid if not one argument
 */
class NotOneArgument extends ValidCommand {
	@Override
	public void isValid(Command command) {
		if (command.getArgs().length != 1) {
			command.output("Client: invalid number of arguments. Type /help");
			Trace.getInstance().write(this, command.getPlayer().getName() + ": Invalid number of arguments.");
			command.notValid();
		} else if (successor != null) {
        	successor.isValid(command);
    	}	
	}	
}

/*
 * invalid if not one or two arguments
 */
class NotOneOrTwoArguments extends ValidCommand {
	@Override
	public void isValid(Command command) {
		if (!((command.getArgs().length == 1) || (command.getArgs().length == 2))) {
			command.output("Client: invalid number of arguments. Type /help");
			Trace.getInstance().write(this, command.getPlayer().getName() + ": Invalid number of arguments.");
			command.notValid();
		} else if (successor != null) {
        	successor.isValid(command);
    	}	
	}	
}

/*
 * invalid if player is not in a tournament
 */
class NotInTournament extends ValidCommand {
	@Override
	public void isValid(Command command) {
		if (!command.inTournament()) {
			command.output("Client: can't perform that action while not in a tournament");
			Trace.getInstance().write(this, command.getPlayer().getName() + ": can't use /" + command.getCmd() + " while not in tournament.");
			command.notValid();
		} else if (successor != null) {
        	successor.isValid(command);
    	}	
	}	
}

