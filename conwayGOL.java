public class ConwayLife {

  public static int[][] getGeneration(int[][] cells, int generations) {
    // Returns 'cells' array if array is null or if 'generations' variable is '0':
    if(generations == 0 || cells == null) return cells;

    int[][] array = resize(cells);
    int[][] count = new int[array.length][array[0].length];

    // Loop through each element of the resized input:
    for(int i = 0; i < count.length; i++) {
      for(int j = 0; j < count[0].length; j++) {

        // For each cell, sum up number of live adjacent neighbours:
        for(int m = ((i-1) >= 0)? i-1: 0; m < (((i+2) > count.length)? i+1: i+2); m++) {
          for(int n = ((j-1) >= 0)? j-1: 0; n < (((j+2) > count[0].length)? j+1: j+2); n++) {
              if(!((m == i) & (n == j)))  count[i][j] += array[m][n];
          }
        }

        // Assigning the status of the cell based on its neighbours:
        if(array[i][j] == 1) {
          if(count[i][j] < 2 || count[i][j] > 3)  count[i][j] = 0;
            else  count[i][j] = 1;
        }
        else {
          if(count[i][j] == 3)  count[i][j] = 1;
            else count[i][j] = 0;
        }
      }
    }

    // Recursive method to repeat process on future generations:
    return getGeneration(crop(count), generations-1);
  }

  public static int[][] resize(int[][] cells){
    // Adding buffer to ensure each provided cell has 8 neighbours:
    int[][] array = new int[cells.length+2][cells[0].length+2];
    for(int i = 0; i < cells.length; i++) {
      for(int j = 0; j < cells[0].length; j++) {
        array[i+1][j+1] = cells[i][j];
      }
    }
    return array;
  }

  public static int[][] crop(int[][] cells) {
    int row1 = cells.length, row2 = 0, col1 = cells[0].length, col2 = 0;

    // Finding the first and last row indices of live cells:
    for(int i = 0; i < cells.length; i++) {
      int rsum1 = 0, rsum2 = 0;
      for(int j = 0; j < cells[0].length; j++) {
        rsum1 += cells[i][j];
        rsum2 += cells[cells.length-1-i][j];

        if(rsum1 > 0)  row1 = Math.min(row1, i);
        if(rsum2 > 0)  row2 = Math.max(row2, cells.length-1-i);
      }
    }

    // Finding the first and last column indices of live cells:
    for(int j = 0; j < cells[0].length; j++) {
      int csum1 = 0, csum2 = 0;
      for(int i = 0; i < cells.length; i++) {
        csum1 += cells[i][j];
        csum2 += cells[i][cells[0].length-1-j];

        if(csum1 > 0)  col1 = Math.min(col1, j);
        if(csum2 > 0)  col2 = Math.max(col2, cells[0].length-1-j);

      }
    }

    // Return null array if there are no live cells:
    if(col2 == 0 || row2 == 0)  return null;

    // Return cropped array:
    int[][] array = new int[row2-row1+1][col2-col1+1];
    for(int i = 0; i < array.length; i++) {
      for(int j = 0; j < array[0].length; j++) {
        array[i][j] = cells[i+row1][j+col1];
      }
    }
    return array;
  }
}