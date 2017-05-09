import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Random;

import static java.lang.System.in;

/**
 * Created by karab on 5/9/2017.
 */
public class ScratchProjectAggregator {
    private static String GET_URL =
            "http://projects.scratch.mit.edu/internalapi/project/";
    private static JSONObject[] projects;

    public static void main(String[] args) throws IOException {

        if (args.length < 1) {
            System.out.print("Usage: ScratchProjectAggregator <sample size>");
            return;
        }

        int sampleSize = Integer.parseInt(args[0]);
        /* time stamped directory for output */
        String timeStamp
                = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
        File dir = new File("Sample" + timeStamp);
        dir.mkdir();
        try{
            projects = getScratchProjects(sampleSize);

            for (int i = 0; i < projects.length; i++) {
                File file = new File(dir, "project" + i + ".json");
                FileWriter fileWriter
                        = new FileWriter(file);
                fileWriter.write(projects[i].toString());
                fileWriter.close();
            }
        }
        catch (JSONException ex)
        {
            ex.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private static JSONObject[] getScratchProjects(int sampleSize) throws IOException, JSONException{
        /* get upper bound for project ids.*/
        int upperBound = getProjIDUpperBound();
        /* Start getting projects. */
        JSONObject[] projects = new JSONObject[sampleSize];
        /* counter: number of successfully retrieved projects*/
        int projCounter = 0;
        Random rand = new Random();
        int projID = 0;
        while (projCounter < sampleSize){
            /* generate a random project ID to try. */
            projID = rand.nextInt(upperBound) + 1;
            URL obj = new URL(GET_URL + projID + "/get/");
            HttpURLConnection connection = (HttpURLConnection) obj.openConnection();
            connection.setRequestMethod("GET");
            int responseCode = connection.getResponseCode();
        /* if you get a 200 - Ok back */
            if (responseCode == HttpURLConnection.HTTP_OK)
            {
                BufferedReader readIn
                        = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();
                while ((inputLine = readIn.readLine()) != null)
                {
                    response.append(inputLine);
                }
                JSONObject jsonObj = new JSONObject(response.toString());
                projects[projCounter] = jsonObj;
                projCounter++;
                System.out.println("added project " + projID + " sucessfully.");
            }
            else {
                System.out.println("id " + projID + " not valid.");
            }
        }
        return projects;
    }

    private static int getProjIDUpperBound() throws IOException, JSONException
    {
        /* Get the total number of shared scratch projects for upper bound of
            random number generator.
        */
        int upperBound = 0;
        URL obj = new URL("https://api.scratch.mit.edu/projects/count/all");
        HttpURLConnection connection = (HttpURLConnection) obj.openConnection();
        connection.setRequestMethod("GET");
        int responseCode = connection.getResponseCode();
        System.out.println("Response to GET request: " + responseCode);
        JSONParser parser = new JSONParser();
        /* if you get a 200 - Ok back */
        if (responseCode == HttpURLConnection.HTTP_OK)
        {
            BufferedReader readIn
                    = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            while ((inputLine = readIn.readLine()) != null)
            {
                JSONObject jsonObj = new JSONObject(inputLine);
                upperBound = jsonObj.getInt("count");
            }
            System.out.println("upper bound: " + upperBound);
        }
        return upperBound;
    }

}
