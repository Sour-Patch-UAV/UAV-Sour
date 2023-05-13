package SHELL;

import java.util.Date;
import java.util.LinkedList;
import java.util.Scanner;

import EXCEPTIONS.CommandException;
import STATICS.Definitions;
import STATICS.GetClassName;
import STATICS.InstructionManagement;

// this class will maintain the current conditions for the user if they choose to use the shell!
public class Shell {

    private class Action {
        
        private Date myTime;
        private String myAction;

        protected Action(Date time, String action) {
            this.myTime = time;
            this.myAction = action;
        }

        protected String GET_ACTION() {
            return this.myAction;
        }

        protected String GET_TIME() {
            return FORMAT_DATE(this.myTime);
        }

        private String FORMAT_DATE(Date dt) {
            return dt.toString();
        }

        protected void PRINT_ACTION() {
            System.out.println("EXE TIME: " + this.GET_TIME() + " | TYPE: " + this.GET_ACTION());
        }
    }

    private LinkedList<Action> listOfActions = new LinkedList<>(); // keep track of all actions by user here!

    // recycle scanner
    public Shell(Scanner scan) throws IllegalArgumentException, CommandException {
        System.out.println(GetClassName.THIS_CLASSNAME(this, "Shell successfully opened!"));
        InstructionManagement.SEND(Definitions.HELP); // print all actionables to user!
        // upon creation, allow user to prompt for input!
        USER_PROMPT(scan);
    };

    public void USER_PROMPT(Scanner scan) {
        while (true) {
            System.out.println("Total Actions: " + this.listOfActions.size());
            System.out.print("> ");
            String input = scan.nextLine();
            if (input.equalsIgnoreCase(Definitions.QUIT)) break;
            // user wants to print the actions
            if (input.startsWith(Definitions.LIST)) {
                if (input.equals(Definitions.LIST)) {
                    LIST_ACTIONS();
                } else {
                    String indexString = input.substring(3);
                    if (indexString.matches("\\d+")) { // check if the string contains only digits
                        int index = Integer.parseInt(indexString);
                        if (index < 0 || index >= listOfActions.size()) {
                            System.out.println("Invalid input. Please try ex: ls " + (this.listOfActions.size() - 1));
                        } else {
                            LIST_ACTIONS(index);
                        }
                    } else {
                        System.out.println("Invalid input. Please try ex: ls " + (this.listOfActions.size() - 1));
                    }
                }
            } else if (InstructionManagement.SEND(input)) {
                listOfActions.add(new Action(new Date(), input)); // keeping even when error, so that the user may be able to go back and see WHY there action failed
            } else break;
        }
        // exited the shell
        System.out.println("-------Exiting Shell-------");
    };
    
    // will print all action objects stored with their respective time and date of execution
    private void LIST_ACTIONS() {
        int i = 0;
        for (Action action : listOfActions) {
            System.out.print("Action: " + i + " | ");   action.PRINT_ACTION();
            i++;
        }
    }

    private void LIST_ACTIONS(int index) {
        listOfActions.get(index).PRINT_ACTION();
    }
};