package com.kairos;

import java.util.Scanner;
import java.util.Stack;

public class ExpressionConverter {

    private static boolean isOperator(char ch) {
        return ch == '+' || ch == '-' || ch == '*' || ch == '/' || ch == '^' || ch == '%';
    }

    private static int getPrecedence(char operator) {
        return switch (operator) {
            case '^' -> 3;
            case '*', '/', '%' -> 2;
            case '+', '-' -> 1;
            default -> -1;
        };
    }

    private static boolean isRightAssociative(char operator) {
        return operator == '^';
    }

    private static boolean isOperand(char ch) {
        return Character.isLetterOrDigit(ch);
    }

    private static boolean shouldPopOperator(char stackOp, char currentOp) {
        if (stackOp == '(') {
            return false;
        }

        int stackPrecedence = getPrecedence(stackOp);
        int currentPrecedence = getPrecedence(currentOp);

        if (stackPrecedence > currentPrecedence) {
            return true;
        }

        return stackPrecedence == currentPrecedence && !isRightAssociative(currentOp);
    }

    private static void processOperand(char ch, StringBuilder output) {
        output.append(ch);
    }

    private static void processOpeningParenthesis(Stack<Character> stack) {
        stack.push('(');
    }

    private static void processClosingParenthesis(Stack<Character> stack, StringBuilder output) {
        while (!stack.isEmpty() && stack.peek() != '(') {
            output.append(stack.pop());
        }

        if (!stack.isEmpty() && stack.peek() == '(') {
            stack.pop();
        }
    }

    private static void processOperator(char operator, Stack<Character> stack, StringBuilder output) {
        while (!stack.isEmpty() && shouldPopOperator(stack.peek(), operator)) {
            output.append(stack.pop());
        }
        stack.push(operator);
    }

    private static void popRemainingOperators(Stack<Character> stack, StringBuilder output) {
        while (!stack.isEmpty()) {
            char op = stack.pop();
            if (op != '(') {
                output.append(op);
            }
        }
    }

    public static String infixToPostfix(String infix) {
        StringBuilder postfix = new StringBuilder();
        Stack<Character> stack = new Stack<>();

        for (char ch : infix.toCharArray()) {
            if (ch == ' ') {
                continue;
            }

            if (isOperand(ch)) {
                processOperand(ch, postfix);
            } else if (ch == '(') {
                processOpeningParenthesis(stack);
            } else if (ch == ')') {
                processClosingParenthesis(stack, postfix);
            } else if (isOperator(ch)) {
                processOperator(ch, stack, postfix);
            }
        }

        popRemainingOperators(stack, postfix);
        return postfix.toString();
    }

    private static String reverseAndSwapParentheses(String infix) {
        StringBuilder reversed = new StringBuilder();

        for (int i = infix.length() - 1; i >= 0; i--) {
            char ch = infix.charAt(i);

            if (ch == '(') {
                reversed.append(')');
            } else if (ch == ')') {
                reversed.append('(');
            } else {
                reversed.append(ch);
            }
        }

        return reversed.toString();
    }

    private static boolean shouldPopForPrefix(char stackOp, char currentOp) {
        if (stackOp == '(') {
            return false;
        }

        return getPrecedence(stackOp) > getPrecedence(currentOp);
    }

    private static void processOperatorForPrefix(char operator, Stack<Character> stack, StringBuilder output) {
        while (!stack.isEmpty() && shouldPopForPrefix(stack.peek(), operator)) {
            output.append(stack.pop());
        }
        stack.push(operator);
    }

    private static String convertReversedToPostfix(String reversedInfix) {
        StringBuilder postfix = new StringBuilder();
        Stack<Character> stack = new Stack<>();

        for (char ch : reversedInfix.toCharArray()) {
            if (ch == ' ') {
                continue;
            }

            if (isOperand(ch)) {
                processOperand(ch, postfix);
            } else if (ch == '(') {
                processOpeningParenthesis(stack);
            } else if (ch == ')') {
                processClosingParenthesis(stack, postfix);
            } else if (isOperator(ch)) {
                processOperatorForPrefix(ch, stack, postfix);
            }
        }

        popRemainingOperators(stack, postfix);
        return postfix.toString();
    }

    public static String infixToPrefix(String infix) {
        String reversedInfix = reverseAndSwapParentheses(infix);
        String postfix = convertReversedToPostfix(reversedInfix);
        return new StringBuilder(postfix).reverse().toString();
    }

    public static boolean hasBalancedParentheses(String expression) {
        Stack<Character> stack = new Stack<Character>();

        for (char ch : expression.toCharArray()) {
            if (ch == '(') {
                stack.push(ch);
            } else if (ch == ')') {
                if (stack.isEmpty()) {
                    return false;
                }
                stack.pop();
            }
        }

        return stack.isEmpty();
    }

    private static void printHeader() {
        System.out.println("========================================");
        System.out.println("   Expression Converter Program");
        System.out.println("   Infix → Prefix & Postfix");
        System.out.println("========================================");
        System.out.println();
    }

    private static boolean shouldExit(String input) {
        return input.equalsIgnoreCase("exit") || input.equalsIgnoreCase("quit");
    }

    private static boolean isEmptyInput(String input) {
        return input.isEmpty();
    }

    private static void handleEmptyInput() {
        System.out.println("Error: Empty input. Please try again.\n");
    }

    private static void handleUnbalancedParentheses() {
        System.out.println("Error: Unbalanced parentheses. Please try again.\n");
    }

    private static void handleConversionError(Exception e) {
        System.out.println("Error processing expression: " + e.getMessage());
        System.out.println("Please check your input and try again.\n");
    }

    private static void displayResults(String prefix, String postfix) {
        System.out.println("Prefix is: " + prefix);
        System.out.println("Postfix is: " + postfix);
        System.out.println();
    }

    private static void processExpression(String infix) {
        String prefix = infixToPrefix(infix);
        String postfix = infixToPostfix(infix);
        displayResults(prefix, postfix);
    }

    private static boolean validateInput(String input) {
        if (isEmptyInput(input)) {
            handleEmptyInput();
            return false;
        }

        if (!hasBalancedParentheses(input)) {
            handleUnbalancedParentheses();
            return false;
        }

        return true;
    }

    private static void runConverterLoop(Scanner scanner) {
        while (true) {
            System.out.print("Enter infix: ");
            String infix = scanner.nextLine().trim();

            if (shouldExit(infix)) {
                System.out.println("Thank you for using the Expression Converter!");
                break;
            }

            if (!validateInput(infix)) {
                continue;
            }

            try {
                processExpression(infix);
            } catch (Exception e) {
                handleConversionError(e);
            }
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        printHeader();
        runConverterLoop(scanner);
        scanner.close();
    }
}