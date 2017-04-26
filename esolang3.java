import java.util.Arrays;
import java.lang.StringBuilder;

public class Paintfuck {
    public static String interpreter(String code, int iterations, int width, int height) {
        int[] position = new int[] {0, 0};
        int[][] array = new int[height][width];

        if(code.isEmpty() || iterations == 0)  return grid(array);

        // Removing invalid characters from string:
        String validCode = code.replaceAll("[^nesw\\*\\[\\]]", "");

        int index = 0;
        while(index < validCode.length() && iterations > 0) {
          char symbol = validCode.charAt(index);

          switch(symbol) {
            case 'n':  position = move(position, -1, 0, width, height);
                        break;
            case 's':  position = move(position, 1, 0, width, height);
                        break;
            case 'e':  position = move(position, 0, 1, width, height);
                        break;
            case 'w':  position = move(position, 0, -1, width, height);
                        break;
            case '*':  array[position[1]][position[0]] ^= 1;
                        break;
            case '[':  if(array[position[1]][position[0]] == 0)  index = moveIndexForward(code, index);
                        break;
            case ']':  if(array[position[1]][position[0]] > 0)  index = moveIndexBackward(code, index);
                        break;
            default: throw new IllegalArgumentException("Illegal character");
          }
          index++;
          iterations--;
        }

        return grid(array);
    }


    public static int[] move(int[] position, int vertical, int horizontal, int width, int height) {
      // Repositioning of the pointer in the vertical axis:
      if(position[0] + horizontal < 0)
        position[0] = Math.floorMod(horizontal, width);
      else if(position[0] + horizontal > width-1)
        position[0] = (position[0] + horizontal)%width;
      else
        position[0] += horizontal;

      // Repositioning of the pointer in the horizontal axis:
      if(position[1] + vertical < 0)
        position[1] = Math.floorMod(vertical, height);
      else if(position[1] + vertical > height-1)
        position[1] = (position[1] + vertical)%height;
      else position[1] += vertical;

      return position;
    }

    public static int moveIndexForward(String code, int index) {
      // Loop forwards to find the index of the matching ']':
      int occurrence = 1, newIndex = index;
      while(occurrence > 0) {
        newIndex += 1;
        if(newIndex > code.length())
                throw new IllegalArgumentException("Matching square bracket doesn't exist.");

        if(code.charAt(newIndex) == '[')  occurrence++;
        else if(code.charAt(newIndex) == ']') occurrence--;
      }
      return newIndex;
    }

    public static int moveIndexBackward(String code, int index) {
      // Loop backwards to find the index of the matching '[':
      int occurrence = 1, newIndex = index;
      while(occurrence > 0) {
        newIndex -= 1;
        if(newIndex < 0)
                throw new IllegalArgumentException("Matching square bracket doesn't exist.");

        if(code.charAt(newIndex) == ']')  occurrence++;
        else if(code.charAt(newIndex) == '[') occurrence--;
      }
      return newIndex;
    }

    public static String grid(int[][] array) {
      // Turns 2D array into a single string:
      StringBuilder build = new StringBuilder();
      for (int i = 0; i < array.length; i++) {
        for(int j = 0; j < array[0].length; j++) {
          build.append(array[i][j]);
        }
        if(i != array.length-1)  build.append("\r\n");
      }
      return build.toString();
    }
}    