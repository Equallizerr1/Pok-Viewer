import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Objects;

public class PokeViewer implements ActionListener {
    JButton dexButton, imagesButton, searchImagesButton, purgeButton;
    JTextField searchField, resultField;
    JLabel lbl;
    BufferedImage img;
    ImageIcon icon;
    String searchTerm = "";
    String fileName = "";

    File file = new File("assets");
    JsonArray nationalDexEntries = APICalls.GetNationalDexEntries();
    /*
    keySet = [entry_number, pokemon_species]
    String pokemon_Name = nationalDexEntries.get(i).getAsJsonObject().get("pokemon_species").getAsJsonObject().get("name").toString().replaceAll("\"", "");
     JsonElement pokemon_URL = nationalDexEntries.get(i).getAsJsonObject().get("pokemon_species").getAsJsonObject().get("url");
     int entry_Number = nationalDexEntries.get(i).getAsJsonObject().get("entry_number").getAsInt();
    */

    public void main() {
        SwingUtilities.invokeLater(this::apiGUI);
    }
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void purgeDirectory(File dir) {
        // Purges assets folder
        for (File file: Objects.requireNonNull(dir.listFiles())) {
            if (file.isDirectory())
                purgeDirectory(file);
            file.delete();
        }
    }
    public void apiGUI() {
        dexButton = new JButton("Get Dex Entries");
        dexButton.setBounds(500,500,100,50);
        dexButton.addActionListener(this);
        imagesButton = new JButton("Get Images");
        imagesButton.setBounds(300,500,100,50);
        imagesButton.addActionListener(this);
        searchImagesButton = new JButton("Search");
        searchImagesButton.setBounds(100,500,100,50);
        searchImagesButton.addActionListener(this);
        purgeButton = new JButton("Purge");
        purgeButton.setBounds(300,400,100,50);
        purgeButton.addActionListener(this);
        searchField = new JTextField();
        searchField.setBounds(100,400,300,50);
        searchField.setColumns(10);
        searchField.setEditable(true);
        resultField = new JTextField();
        resultField.setBounds(300,400,300,50);
        resultField.setColumns(10);
        resultField.setEditable(false);

        JFrame frame = new JFrame();
        frame.setLayout(new FlowLayout(FlowLayout.CENTER, 5,5));
        frame.setSize(600, 600);
        lbl = new JLabel();
        frame.add(lbl);
        frame.setTitle("PokéViewer");
        frame.setVisible(true);

        frame.add(dexButton);
        frame.add(imagesButton);
        frame.add(purgeButton);
        frame.add(searchImagesButton);
        frame.add(searchField);
        frame.add(resultField);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public static void saveImage(String imageUrl, String destinationFile) throws IOException {
        //noinspection deprecation
        URL url = new URL(imageUrl);
        InputStream is = url.openStream();
        OutputStream os = new FileOutputStream(destinationFile);

        byte[] b = new byte[8192];
        int length;

        while ((length = is.read(b)) != -1) {
            os.write(b, 0, length);
        }

        is.close();
        os.close();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == dexButton) {
            APICalls.GetNationalDexEntries();
            System.out.println(APICalls.GetNationalDexEntries());
        }
        if (e.getSource() == imagesButton) {
            APICalls.getPokemonImages(file, nationalDexEntries);
            //System.out.println(APICalls.getPokemonImages(file, nationalDexEntries));
        }
        if (e.getSource() == searchImagesButton) {
            searchTerm = searchField.getText();
            //noinspection preview
            fileName = STR."assets//\{searchTerm}.png";
            file = new File(fileName);
            try {
                img = ImageIO.read(file);
                icon = new ImageIcon(img);
                lbl.setIcon(icon);
                resultField.setText(fileName.substring(8, fileName.length()-4));
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
        if(e.getSource() == purgeButton) {
            purgeDirectory(file);
        }
    }
}

class APICalls {

    public static void getPokemonImages(File file, JsonArray dexEntries) {
        // Takes nationalDexEntries and downloads Pokémon sprites from "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/"+i+".png"
        //noinspection ResultOfMethodCallIgnored
        file.mkdir();
        int i = 1;
        while (i <= dexEntries.size()) {
            //noinspection preview
            String imageUrl = STR."https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/\{i}.png";
            //noinspection preview
            String destinationFile =
                    STR."Assets\\\{dexEntries.get(i - 1).getAsJsonObject().get("pokemon_species").getAsJsonObject().get(
                            "name").toString().replaceAll("\"", "")}.png";
            try {
                PokeViewer.saveImage(imageUrl, destinationFile);
                i++;
                // Possibly update progress bar here.
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
        System.out.print("Done");
    }

    public static JsonArray GetNationalDexEntries() {
        // Returns pokemon_entries array from https://pokeapi.co/api/v2/pokedex/national as JsonArray

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://pokeapi.co/api/v2/pokedex/national"))
                .method("GET", HttpRequest.BodyPublishers.noBody())
                .build();
        HttpResponse<String> response = null;
        try {
            response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
        assert response != null;
        JsonObject dexObject = new Gson().fromJson(response.body(), JsonObject.class);
        return (JsonArray) dexObject.get("pokemon_entries");
    }
}

