package com.kairos;

import java.util.Scanner;
import java.util.Stack;
import java.util.ArrayList;
import java.util.List;

public class ExpressionConverter {

    private static class Token {
        String value;
        TokenType type;

        Token(String value, TokenType type) {
            this.value = value;
            this.type = type;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    private enum TokenType {
        OPERAND, OPERATOR, OPEN_PAREN, CLOSE_PAREN
    }

    private static int getPrecedence(String operator) {
        return switch (operator) {
            case "^", "log" -> 3;
            case "*", "/", "%" -> 2;
            case "+", "-" -> 1;
            default -> -1;
        };
    }

    private static boolean isRightAssociative(String operator) {
        return operator.equals("^") || operator.equals("log");
    }

    private static boolean isWhitespace(char ch) {
        return ch == ' ';
    }

    private static boolean isSingleCharOperator(char ch) {
        return ch == '+' || ch == '-' || ch == '*' || ch == '/' || ch == '^' || ch == '%';
    }

    private static boolean isLogOperator(String expression, int index) {
        return index + 3 <= expression.length() && expression.startsWith("log", index);
    }

    private static int addOpenParenToken(List<Token> tokens, int index) {
        tokens.add(new Token("(", TokenType.OPEN_PAREN));
        return index + 1;
    }

    private static int addCloseParenToken(List<Token> tokens, int index) {
        tokens.add(new Token(")", TokenType.CLOSE_PAREN));
        return index + 1;
    }

    private static int addLogToken(List<Token> tokens, int index) {
        tokens.add(new Token("log", TokenType.OPERATOR));
        return index + 3;
    }

    private static int addSingleCharOperatorToken(List<Token> tokens, char ch, int index) {
        tokens.add(new Token(String.valueOf(ch), TokenType.OPERATOR));
        return index + 1;
    }

    private static String readOperand(String expression, int startIndex) {
        StringBuilder operand = new StringBuilder();
        int i = startIndex;
        boolean hasDecimal = false;

        while (i < expression.length()) {
            char ch = expression.charAt(i);

            if (i + 3 <= expression.length() && expression.startsWith("log", i)) {
                break;
            }

            if (Character.isLetterOrDigit(ch)) {
                operand.append(ch);
                i++;
            } else if (ch == '.' && !hasDecimal && Character.isDigit(expression.charAt(startIndex))) {
                hasDecimal = true;
                operand.append(ch);
                i++;
            } else {
                break;
            }
        }

        return operand.toString();
    }

    private static int addOperandToken(List<Token> tokens, String expression, int index) {
        String operand = readOperand(expression, index);

        if (!operand.isEmpty()) {
            tokens.add(new Token(operand, TokenType.OPERAND));
        }

        return index + operand.length();
    }

    private static int processCharacter(List<Token> tokens, String expression, int index) {
        char ch = expression.charAt(index);

        if (isWhitespace(ch)) {
            return index + 1;
        }

        if (ch == '(') {
            return addOpenParenToken(tokens, index);
        }

        if (ch == ')') {
            return addCloseParenToken(tokens, index);
        }

        if (isSingleCharOperator(ch)) {
            return addSingleCharOperatorToken(tokens, ch, index);
        }

        if (Character.isLetterOrDigit(ch) || (ch == '.' && index + 1 < expression.length() && Character.isDigit(expression.charAt(index + 1)))) {
            if (isLogOperator(expression, index)) {
                return addLogToken(tokens, index);
            }
            return addOperandToken(tokens, expression, index);
        }

        return index + 1;
    }

    private static List<Token> tokenize(String expression) {
        List<Token> tokens = new ArrayList<>();
        int i = 0;

        while (i < expression.length()) {
            i = processCharacter(tokens, expression, i);
        }

        return tokens;
    }

    private static boolean shouldPopOperator(String stackOp, String currentOp) {
        if (stackOp.equals("(")) {
            return false;
        }

        int stackPrecedence = getPrecedence(stackOp);
        int currentPrecedence = getPrecedence(currentOp);

        if (stackPrecedence > currentPrecedence) {
            return true;
        }

        return stackPrecedence == currentPrecedence && !isRightAssociative(currentOp);
    }

    private static void appendToken(Token token, List<String> output) {
        output.add(token.value);
    }

    private static void processOpeningParenthesis(Stack<String> stack) {
        stack.push("(");
    }

    private static void processClosingParenthesis(Stack<String> stack, List<String> output) {
        while (!stack.isEmpty() && !stack.peek().equals("(")) {
            output.add(stack.pop());
        }

        if (!stack.isEmpty() && stack.peek().equals("(")) {
            stack.pop();
        }
    }

    private static void processOperator(String operator, Stack<String> stack, List<String> output) {
        while (!stack.isEmpty() && shouldPopOperator(stack.peek(), operator)) {
            output.add(stack.pop());
        }
        stack.push(operator);
    }

    private static void popRemainingOperators(Stack<String> stack, List<String> output) {
        while (!stack.isEmpty()) {
            String op = stack.pop();
            if (!op.equals("(")) {
                output.add(op);
            }
        }
    }

    private static String joinTokens(List<String> tokens) {
        StringBuilder result = new StringBuilder();
        for (String token : tokens) {
            result.append(token);
        }
        return result.toString();
    }

    public static String infixToPostfix(String infix) {
        List<Token> tokens = tokenize(infix);
        List<String> postfix = new ArrayList<>();
        Stack<String> stack = new Stack<>();

        for (Token token : tokens) {
            if (token.type == TokenType.OPERAND) {
                appendToken(token, postfix);
            } else if (token.type == TokenType.OPEN_PAREN) {
                processOpeningParenthesis(stack);
            } else if (token.type == TokenType.CLOSE_PAREN) {
                processClosingParenthesis(stack, postfix);
            } else if (token.type == TokenType.OPERATOR) {
                processOperator(token.value, stack, postfix);
            }
        }

        popRemainingOperators(stack, postfix);
        return joinTokens(postfix);
    }

    private static List<Token> reverseTokensAndSwapParentheses(List<Token> tokens) {
        List<Token> reversed = new ArrayList<>();

        for (int i = tokens.size() - 1; i >= 0; i--) {
            Token token = tokens.get(i);

            if (token.type == TokenType.OPEN_PAREN) {
                reversed.add(new Token(")", TokenType.CLOSE_PAREN));
            } else if (token.type == TokenType.CLOSE_PAREN) {
                reversed.add(new Token("(", TokenType.OPEN_PAREN));
            } else {
                reversed.add(token);
            }
        }

        return reversed;
    }

    private static boolean shouldPopForPrefix(String stackOp, String currentOp) {
        if (stackOp.equals("(")) {
            return false;
        }

        return getPrecedence(stackOp) > getPrecedence(currentOp);
    }

    private static void processOperatorForPrefix(String operator, Stack<String> stack, List<String> output) {
        while (!stack.isEmpty() && shouldPopForPrefix(stack.peek(), operator)) {
            output.add(stack.pop());
        }
        stack.push(operator);
    }

    private static List<String> convertReversedToPostfixList(List<Token> reversedTokens) {
        List<String> postfix = new ArrayList<>();
        Stack<String> stack = new Stack<>();

        for (Token token : reversedTokens) {
            if (token.type == TokenType.OPERAND) {
                appendToken(token, postfix);
            } else if (token.type == TokenType.OPEN_PAREN) {
                processOpeningParenthesis(stack);
            } else if (token.type == TokenType.CLOSE_PAREN) {
                processClosingParenthesis(stack, postfix);
            } else if (token.type == TokenType.OPERATOR) {
                processOperatorForPrefix(token.value, stack, postfix);
            }
        }

        popRemainingOperators(stack, postfix);
        return postfix;
    }

    private static String reverseTokenList(List<String> tokens) {
        StringBuilder reversed = new StringBuilder();
        for (int i = tokens.size() - 1; i >= 0; i--) {
            reversed.append(tokens.get(i));
        }
        return reversed.toString();
    }

    public static String infixToPrefix(String infix) {
        List<Token> tokens = tokenize(infix);
        List<Token> reversedTokens = reverseTokensAndSwapParentheses(tokens);
        List<String> postfixList = convertReversedToPostfixList(reversedTokens);
        return reverseTokenList(postfixList);
    }

    public static boolean hasBalancedParentheses(String expression) {
        Stack<Character> stack = new Stack<>();

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
        System.out.println("Operators: +, -, *, /, ^, %, log");
        System.out.println("Use log for logarithm: AlogB means log_A(B)");
        System.out.println("Supports: multi-digit numbers (10, 100)");
        System.out.println("          decimal numbers (3.14, 2.5)");
        System.out.println("Type 'exit' or 'quit' to exit");
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