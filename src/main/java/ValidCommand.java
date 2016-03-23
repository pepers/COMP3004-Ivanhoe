package main.java;


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
 * invalid if comparison provided with number of arguments
 */
class Arguments extends ValidCommand {
	private String comparison; // comparison asked for
	private int argNum;        // number of arguments
	private String[] validComparisons = { "==", "!=", "<=", ">=", "<", ">" };
	
	public Arguments(String comparison, int argNum) throws IllegalArgumentException {
		// check that comparison asked for is a valid comparison
		boolean error = true;
		for (String c : validComparisons) {
			if (c.equals(comparison)) { error = false; }
		}
		if (error) {
			throw new IllegalArgumentException("'" + comparison + "' is not a valid comparison");
		}
		
		this.comparison = comparison;
		this.argNum = argNum;
	}

	@Override
	public void isValid(Command command) {
		int length = command.getArgs().length;
		boolean valid = true;
		
		// switch over the comparison asked for
		switch (this.comparison) {
			case "==": if (length == this.argNum) { valid = false; }; break;
			case "!=": if (length != this.argNum) { valid = false; }; break;
			case "<=": if (length <= this.argNum) { valid = false; }; break;
			case ">=": if (length >= this.argNum) { valid = false; }; break;
			case "<": if (length < this.argNum) { valid = false; }; break;
			case ">": if (length > this.argNum) { valid = false; }; break;
			default: break;
		}
		
		if (!valid) {
			command.setMessage("invalid number of arguments. Type /help");
			command.notValid();
		} else if (successor != null) {
        	successor.isValid(command);
    	}	
	}
}

/*
 * invalid if comparison#1 and comparison#2 of Arguments
 */
class TwoArguments extends ValidCommand {
	private ValidCommand vcmd1 = null;
	private ValidCommand vcmd2 = null;
	private Command cmd1 = null;
	private Command cmd2 = null;

	public TwoArguments (String comp1, int arg1, String comp2, int arg2) {
		this.vcmd1 = new Arguments(comp1, arg1);
		this.vcmd2 = new Arguments(comp2, arg2);
	}

	@Override
	public void isValid(Command command) {
		cmd1 = cmd2 = command;
		vcmd1.isValid(cmd1);
		vcmd2.isValid(cmd2);
		if (cmd1.isValid() && cmd2.isValid()) {
			command.setMessage("invalid number of arguments. Type /help");
			command.notValid();
		} else if (successor != null) {
        	successor.isValid(command);
    	}	
	
	}
}

/*
 * invalid if player is not in a tournament
 */
class InTournament extends ValidCommand {
	@Override
	public void isValid(Command command) {
		if (!command.getPlayer().getParticipation()) {
			command.setMessage("can't perform that action while not in a tournament");
			command.notValid();
		} else if (successor != null) {
        	successor.isValid(command);
    	}	
	}	
}

/*
 * invalid if player doesn't have card in their hand
 */
class CardInHand extends ValidCommand {
	@Override
	public void isValid(Command command) {
		String args = String.join(" ", command.getArgs());
		Card c = command.getPlayer().getCard(args);
		if (c == null) {
			command.setMessage("don't have that card in your hand");
			command.notValid();
		} else if (successor != null) {
        	successor.isValid(command);
		}
	}
}

/*
 * invalid if player is Stunned and already played a Display Card
 */
class StunnedAndPlayedDC extends ValidCommand {
	@Override
	public void isValid(Command command) {
		if (command.getPlayer().getStunned() && 
				command.getPlayer().getAddedToDisplay()) {
			String args = String.join(" ", command.getArgs());
			Card c = command.getPlayer().getCard(args);
			if (c instanceof DisplayCard) { 
				command.setMessage("can't play another Display Card while stunned.");
				command.notValid();
			} else if (successor != null) {
				successor.isValid(command);
			}
		} else if (successor != null) {
        	successor.isValid(command);
    	}	
	}	
}

