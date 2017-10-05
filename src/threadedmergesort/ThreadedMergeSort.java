package threadedmergesort;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;

/**
 * @author Sean Brady
 * This program sorts an array of integers using a threaded merge sort algorithm
 */
public class ThreadedMergeSort 
{
    
    public static void main(String[] args) throws InterruptedException, FileNotFoundException, IOException 
    {
        //choose a test data file
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select test data file");
        chooser.showOpenDialog(null);
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        File f = chooser.getSelectedFile();
        byte[] bytes = new byte[(int) f.length()];
        try (FileInputStream fis = new FileInputStream(f)) 
        {
            fis.read(bytes);
        }
        
        //get the size of the array
        String[] valueStr = new String(bytes).trim().split("\\s+");
        int[] original = new int[valueStr.length];

        //output file has the same parent file as the test data
        PrintStream out = new PrintStream(new FileOutputStream(f.getParentFile() + "\\output.txt"));
        System.setOut(out);
        
        //fill and print original array
        System.out.print("original:\t");
        for (int i = 0; i < valueStr.length; i++)
        { 
            original[i] = Integer.parseInt(valueStr[i]);
            System.out.print(original[i] + "\t");
        }
        System.out.println();
        
        //begin parent thread
        Sort runner0 = new Sort(original);
        runner0.start();
        runner0.join();
        
        //print sorted array
        System.out.print("sorted:\t\t");
        for (int i = 0; i < runner0.getThreadArray().length; i++)
        { 
            System.out.print(runner0.getThreadArray()[i] + "\t");
        }
        System.out.println();
    }
}

class Sort extends Thread 
{
    private final int[] threadArray;
    
    Sort(int[] arr) 
    {
        threadArray = arr;
    }

    public int[] getThreadArray() 
    {
        return threadArray;
    }
    
    @Override
    public void run() 
    {
        try 
        {
            mergeSort(threadArray);
        } catch (InterruptedException ex) 
        {
            Logger.getLogger(Sort.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void mergeSort(int[] array) throws InterruptedException 
    {
        if (array.length > 1) 
	    {
            //split current array in half		
            int[] left = leftHalf(array);   
            int[] right = rightHalf(array); 
            
            //begin recursive threads
            Sort runner1 = new Sort(left);
            Sort runner2 = new Sort(right);
            runner1.start();
            runner2.start();
            runner1.join();
            runner2.join();

            //merge arrays back together
            merge(array, left, right);
        }
    }

    //fill the left array with values from the left half
    public int[] leftHalf(int[] array) 
    {
        int size1 = array.length / 2;
        int[] left = new int[size1];
        System.arraycopy(array, 0, left, 0, size1);
		
        return left;
    }

    //fill the right array with integers from the right half
    public int[] rightHalf(int[] array) 
    {
        int size1 = array.length / 2;
        int size2 = array.length - size1;
        int[] right = new int[size2];
        System.arraycopy(array, size1, right, 0, size2);
        
        return right;
    }

    //sort the array by merging left and right array positions in the correct order
    public void merge(int[] result, int[] left, int[] right) 
    {
        int l = 0;  //left array position   
        int r = 0;  //right array position
	int i;      //result array position

        for (i = 0; i < result.length; i++) 
        {
            if (r >= right.length || (l < left.length && left[l] <= right[r]))
            {
                result[i] = left[l];   
                l++;
            } else 
            {
                result[i] = right[r];   
                r++;
            }
        }
    }
}