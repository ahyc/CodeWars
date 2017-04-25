import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.io.IOException;
import java.util.EmptyStackException;

public class WhitespaceInterpreter {
  // Transforms space characters to ['s','t','n'] chars;
  public static String unbleach(String code) {
    String temp = code != null ? code.replace(' ', 'S').replace('\t', 'T').replace('\n', 'N') : null;
    return temp.replaceAll("[^STN]", "").toLowerCase();
  }

  // Method for dealing with no OutputStream parameter:
  public static String execute(String code, InputStream input) {
    return execute(code, input, null);
  }

  public static String execute(String code, InputStream input, OutputStream stream) {
    Stack<Integer> stack = new Stack<>();
    Map<Integer,Integer> heap = new HashMap<>();
    Map<String,String> labels = new HashMap<>();

    String codes = unbleach(code);
    String copy = codes, subroutine = "", output = "";
    boolean cleanTermination = false;

    // Flush OutputStream:
    if(stream != null) {
      try {   stream.flush();   }
      catch(Exception e) {  e.printStackTrace();  }
    }

    // Return 'null' string if input code is 'null':
    if(codes == null)
      return null;

    // Read the code part by part until all is read:
    while(codes.length() > 0 ) {
      String imp = "" + codes.charAt(0) + codes.charAt(1) + codes.charAt(2);
      switch(imp) {
        // Stack Manipulation commands:
        case "ssn":   throw new IllegalArgumentException("Can't parse number expression with just [terminal].");

        case "sss":
        case "sst":
        case "stn":
        case "sts":   manipulateStack(stack, codes);
                      codes = nextCommand(codes);
                      break;

        case "sns":
        case "snt":
        case "snn":   manipulateStack(stack, codes);
                      codes = codes.substring(3);
                      break;

        // Arithmetic commands:
        case "tss":
        case "tst":   arithmetic(stack, codes);
                      codes = codes.substring(4);
                      break;

        // Heap Access commands:
        case "tts":
        case "ttt":   heapAccess(stack, codes, heap);
                      codes = codes.substring(3);
                      break;

        // Input & Output commands:
        case "tns":   String temp = getOutput(codes, stack);
                      output += temp;
                      if(stream != null)
                        writeOutput(stream, temp);
                      codes = codes.substring(4);
                      break;

        case "tnt":   getInput(codes, stack, heap, input);
                      codes = codes.substring(4);
                      break;

        // Flow Control Commands:
        case "nss":   String label = parseLabel(codes.substring(3));
                      if(labels.containsKey(label))
                        throw new IllegalArgumentException("Label is not unique.");
                      else
                        labels.put(label, nextCommand(codes));
                      codes = nextCommand(codes);
                      break;

        case "nst":   String sub = parseLabel(codes.substring(3));
                      subroutine = nextCommand(codes);

                      codes = jump(codes, sub, labels, copy);
                      break;

        case "nsn":   String location = parseLabel(codes.substring(3));
                      if(labels.containsKey(location))
                        codes = labels.get(location);
                      else
                        codes = jump(codes, location, labels, copy);
                      break;

        case "nts":   if(stack.empty())
                        throw new EmptyStackException();
                      else if(stack.pop() == 0) {
                        String zeroValueLabel = parseLabel(codes.substring(3));
                        codes = jump(codes, zeroValueLabel, labels, copy);
                      }
                      else
                        codes = nextCommand(codes);
                      break;

        case "ntt":   if(stack.empty())
                        throw new EmptyStackException();
                      else if(stack.pop() < 0) {
                        String lessThanZeroValueLabel = parseLabel(codes.substring(3));
                        codes = jump(codes, lessThanZeroValueLabel, labels, copy);
                      }
                      else
                        codes = nextCommand(codes);
                      break;

        // Exit subroutine and return to code segment after subroutine was called:
        case "ntn":   if(subroutine.isEmpty())
                        throw new IllegalArgumentException("Subroutine was never called.");
                      else
                        codes = subroutine;
                      break;

        // Exit the program:
        case "nnn":   codes = "";
                      cleanTermination = true;
                      break;

        // Throw error if the IMP command is not recognised:
        default:  throw new IllegalArgumentException("IMP invalid");
      }
    }

    // Throw error if code was terminated uncleanly:
    if(!cleanTermination)
      throw new IllegalArgumentException("Unclean termination of code.");

    return output;
  }

  // Method to get the next command, if label or number was passed as a paramter:
  public static String nextCommand(String code) {
    String label = code.substring(3);
    if(label.indexOf("n") == -1)
      throw new IllegalArgumentException("There is no following 'n' in the remaining code.");
    else
      return label.substring(label.indexOf("n") + 1);
  }

  public static void manipulateStack(Stack<Integer> stack, String code) {
    String command = "" + code.charAt(1) + code.charAt(2);
    switch(command) {
      // Push number onto stack:
      case "ss":
      case "st":  stack.push(parseNumber(code.substring(2)));
                  break;
      // Copy the nth item on the stack onto the top of the stack:
      case "ts":  if(stack.empty())
                    throw new EmptyStackException();

                  int index = parseNumber(code.substring(3));
                  if(index >= 0 || index < stack.size())
                    stack.push(stack.get(stack.size() - 1 - index));
                  else
                    throw new IndexOutOfBoundsException("Index of stack can not be greater than the stack size or less than 0.");
                  break;
      // Discard the top n values below the top of the stack from the stack:
      case "tn":  if(stack.empty())
                    throw new EmptyStackException();

                  int number = parseNumber(code.substring(3));
                  Integer first = stack.pop();
                  if(number < 0 || number >= stack.size())
                    stack.clear();
                  else {
                    for(int i = 0; i < number; i++)
                      stack.pop();
                  }
                  stack.push(first);
                  break;
      // Duplicate the top value on the stack:
      case "ns":  if(stack.empty())
                    throw new EmptyStackException();

                  stack.push(stack.peek());
                  break;
      // Swap the top two value on the stack:
      case "nt":  if(stack.size() >= 2) {
                    Integer bottom = stack.pop();   Integer top = stack.pop();
                    stack.push(bottom);             stack.push(top);
                  }
                  else  throw new IndexOutOfBoundsException("Length of stack can not be less than 2.");
                  break;
      // Discard the top value on the stack:
      case "nn":  stack.pop();
                  break;

      default:    throw new IllegalArgumentException("Command does not exist: " +command);
    }
  }

  public static void arithmetic(Stack<Integer> stack, String code) {
    String command = "" + code.charAt(2) + code.charAt(3);
    try {
      Integer A = stack.pop();    Integer B = stack.pop();
      if(command.equals("ss"))
        stack.push(A + B);
      else if (command.equals("st"))
        stack.push(B - A);
      else if (command.equals("sn"))
        stack.push(B * A);
      else if (command.equals("ts")) {
        if (A == 0)   throw new IllegalArgumentException("Can't divide by zero.");
        else  stack.push(Math.floorDiv(B, A));
      }
      else if (command.equals("tt")) {
        if (A == 0)   throw new IllegalArgumentException("Can't perform modulus with zero.");
        else  stack.push(Math.floorMod(B, A));
      }
    } catch (EmptyStackException E)  {
    }
  }

  public static void heapAccess(Stack<Integer> stack, String code, Map<Integer, Integer> map) {
    String command = "" + code.charAt(2);
    // Heap store:
    if(command.equals("s")) {
      if(stack.size() < 2)  throw new EmptyStackException();

      Integer A = stack.pop();    Integer B = stack.pop();
      map.put(B, A);
    }
    // Heap retrieve:
    else if (command.equals("t")) {
      if(stack.empty())  throw new EmptyStackException();

      Integer A = stack.pop();
      if(map.containsKey(A))
        stack.push(map.get(A));
      else
        throw new NullPointerException("There is no value at heap address A.");
    }
  }

  public static String getOutput(String code, Stack<Integer> stack) {
    String output = "";
    String command = "" + code.charAt(2) + code.charAt(3);

    if(stack.empty())
      throw new EmptyStackException();

    // Get output as character or integer:
    if(command.equals("ss"))
      output += parseCharacter(stack.pop());
    else if(command.equals("st"))
      output += stack.pop();
    else
      throw new IllegalArgumentException("Invalid output commands.");

    return output;
  }

  public static void getInput(String code, Stack<Integer> stack, Map<Integer, Integer> heap, InputStream input) {
    String command = "" + code.charAt(2) + code.charAt(3);
    try {
      if(command.equals("ts") || command.equals("tt")) {
        // Input is terminal-separated, so read data can't be equal to ASCII 10:
        int ascii = input.read();
        if(ascii == 10)
          ascii = input.read();

        // Read a number/character from input stream and place it in a given address:
        if(ascii != -1) {
          if(command.charAt(1) == 't')
            heap.put(stack.pop(), Character.getNumericValue(ascii));
          else
            heap.put(stack.pop(), ascii);
        }
        else if(ascii == -1)
          throw new IllegalArgumentException("The input stream is empty.");
      }
      else
        throw new IllegalArgumentException("Invalid input commands.");
    } catch (IOException e)  {
    }
  }

  public static String jump(String code, String label, Map<String, String> labels, String copy) {
    if(!labels.containsKey(label)) {
      // String containing code to mark a location with a specific label:
      String mark = "nss" + label + "n";

      // Search forwards to find the mark:
      if(code.indexOf(mark) != -1) {
        String substring = nextCommand(code.substring(code.indexOf(mark)));
        labels.put(label, substring);

        return substring;
      }
      // Search backwards to find the mark:
      else if(copy.indexOf(mark) != -1) {
        String substring = nextCommand(copy.substring(copy.indexOf(mark)));
        labels.put(label, substring);

        return substring;
      }
      else
        throw new IllegalArgumentException("Cannot jump forwards - label does not exist in code");
    }
    else {
      return labels.get(label);
    }
  }

  public static void writeOutput(OutputStream stream, String output) {
    // Write output to OutputStream:
    byte[] bytes = output.getBytes();
    try {
      stream.write(bytes);
      stream.flush();
    } catch(Exception e) {
      e.printStackTrace();
    }
  }

  // Converting the number parameter into an integer:
  public static int parseNumber(String code) {
    if(code.length() < 2)
      throw new IllegalArgumentException("The string length for parsing numbers should be greater than 1.");

    boolean isPositive;
    if(code.charAt(0) == 's')  isPositive = true;
    else if(code.charAt(0) == 't') isPositive = false;
    else throw new IllegalArgumentException("The sign for parsing number is nether positive or negative.");

    String substring = code.substring(1, code.indexOf("n"));
    if(substring.length() == 0)  return 0;
    else {
      int number = Integer.parseInt(substring.replaceAll("s", "0").replaceAll("t", "1"), 2);
      return (isPositive)?  number: (number*-1);
    }
  }

  // Converting the number parameter into a character:
  public static String parseCharacter(int number) {
    if(number < 0 || number > 127)
      throw new IllegalArgumentException("The decimal value is less than 0 or greater than 127.");
    else
      return String.valueOf((char) number);
  }

  // Retrieving the label parameter from code:
  public static String parseLabel(String code) {
    String substring = code.substring(0, code.indexOf('n'));
    return (substring.isEmpty())? "": substring;
  }
}