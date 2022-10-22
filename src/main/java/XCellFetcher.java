import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jetbrains.annotations.NotNull;
import scroogemcfawk.Logging;

import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This piece of shit (MS Excel) is finally can be read by my program! <br><br>
 * {@code XCellFetcher} reads the tickers from your Excel workbook and makes an {@code ArrayList<Asset>} of them.
 *
 * @author scroogemcfawk
 * @since v0.0.2
 */

public class XCellFetcher
{
    private static String startCell;
    private static String endCell;
    private static String inputFileAbsolutePath;
    private static boolean isInit = false;

    static
    {
        System.out.println("XCellFetcher is loaded");
    }

    public static void pr()
    {
        System.out.println("Start row: " + startCell.replaceAll("[a-zA-Z]+", ""));
        System.out.println("Start col: " + startCell.replaceAll("\\d+", ""));
        System.out.println("End row: " + endCell.replaceAll("[a-zA-Z]+", ""));
        System.out.println("End col: " + endCell.replaceAll("\\d+", ""));
        System.out.println("Filepath: " + inputFileAbsolutePath);
    }

    public static void initFetcher() throws IOException
    {
        Scanner in = new Scanner(System.in);

        System.out.println("Start cell address (e.g C418): ");
        String start = in.nextLine().strip();

        System.out.println("End cell address (e.g C418): ");
        String end = in.nextLine().strip();

        System.out.println("Absolute path to input file: (e.g " + System.getProperty("user.dir") + "): ");
        String path = in.nextLine().strip();

        try
        {
            initFetcher(start, end, path);
        }
        catch (IOException e)
        {
            System.out.println(Logging.red("Failed to init the fetcher"));
            var rethrow = new IOException("initFetcher(String, String, String) failed");
            rethrow.initCause(e);
            throw rethrow;
        }
    }

    public static void initFetcher(String startCell, String endCell, String inputFileAbsolutePath) throws IOException
    {
        if (startCell.matches("[a-zA-Z]+\\d+") && endCell.matches("[a-zA-Z]+\\d+") && startCell.replaceAll("\\d", "").equals(endCell.replaceAll("\\d", "")))
        {
            XCellFetcher.startCell = startCell;
            XCellFetcher.endCell = endCell;
        }
        else
        {
            throw new IOException("Bad input format given while initializing XCellFetcher");
        }
        try
        {
            new FileInputStream(inputFileAbsolutePath).close();
            XCellFetcher.inputFileAbsolutePath = inputFileAbsolutePath;
            isInit = true;
        }
        catch (FileNotFoundException e)
        {
            throw new IOException("Could not find input file");
        }
    }


    public static @NotNull ArrayList<Asset> fetch(String file) throws FetchingException
    {
        try (FileInputStream in = new FileInputStream(file))
        {
            if (!isInit)
            {
                initFetcher();
            }
            ArrayList<Asset> res = new ArrayList<>(20);
            Workbook wb = new XSSFWorkbook(in);
            Sheet sh = wb.getSheetAt(0);
            for (int i = 7; i < 45; i++)
            {
                Row r = sh.getRow(i);
                Cell c = r.getCell(2);
                //                System.out.printf(c.toString());
                String t = c.toString();
                double p = r.getCell(5).getNumericCellValue();
                //                System.out.printf("" + p);
                if (!t.isBlank())
                {
                    res.add(new Asset(t, p));
                }
            }
            return res;
        }
        catch (IOException e)
        {
            FetchingException rethrow = new FetchingException("File exception in fetcher");
            rethrow.initCause(e);
            throw rethrow;
        }
    }

    public static void main(String[] args) throws IOException
    {
        //        Logger.getGlobal().setLevel(Level.OFF);
        //        ArrayList<Asset> assets = fetch("./io/FinanceWorkbook.xlsx");
        //        for (Asset asset: assets) {
        //            System.out.println(asset + " -> " + Logging.red("" + Parser.parse(asset)));
        //        }
        initFetcher();
        XCellFetcher.pr();
    }
}
