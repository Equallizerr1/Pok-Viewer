import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.net.URL;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpClient;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.JFrame;
import java.util.HashMap;

public class PokeViewer implements  ActionListener{
    JFrame frame;
    JTextField textField;
    JButton searchButton;
    JPanel panel;

    Font myFont = new Font("Arial", Font.BOLD, 30);

    String searchTerm;
    static HashMap<String, String> pokemonData = new HashMap<String, String>();

    PokeViewer () {
        MyCanvas m = new MyCanvas();

        frame = new JFrame("PokéViewer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 600);
        frame.setLayout(null);

        textField = new JTextField();
        textField.setBounds(100,25,300,50);
        textField.setEditable(true);
        textField.setFont(myFont);

        searchButton = new JButton("Search");
        searchButton.setBounds(400,25,100,50);
        searchButton.addActionListener(this);

        panel = new JPanel();
        panel.setBounds(100, 100,400,400);
        panel.setBackground(Color.lightGray);

        frame.add(m);
        frame.add(panel);
        frame.add(searchButton);
        frame.add(textField);
        frame.setVisible(true);
    }

    public static void main(String[] args) throws IOException {
        String imageUrl = pokemonData.get("sprite");
        String destinationFile = "image.png";

        saveImage(imageUrl, destinationFile);
        PokeViewer poke = new PokeViewer();

    }
    public Object getPokemon(String searchTerm) {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://pokeapi.co/api/v2/pokemon/"+searchTerm))
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
        JsonObject pokemonObject = new Gson().fromJson(response.body(), JsonObject.class);
        //System.out.println(pokemonObject.getAsJsonObject().get("sprites").getAsJsonObject().get("front_default"));
        pokemonData.put("Name", String.valueOf(pokemonObject.get("name")));
        pokemonData.put("Sprite", String.valueOf(pokemonObject.getAsJsonObject().get("sprites").getAsJsonObject().get("front_default")));
        return pokemonData;
    }

    public static void saveImage(String imageUrl, String destinationFile) throws  IOException{
        URL url = new URL(imageUrl);
        InputStream is = url.openStream();
        OutputStream os = new FileOutputStream(destinationFile);

        byte[] b = new byte[2048];
        int length;

        while ((length = is.read(b)) != -1) {
            os.write(b,0,length);
        }

        is.close();
        os.close();
    }

    public static class MyCanvas extends Canvas {
        public void paint(Graphics g) {
            Toolkit t = Toolkit.getDefaultToolkit();
            Image i = t.getImage();
            g.drawImage(i, 120,100,this);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == searchButton) {
        searchTerm = textField.getText();
        getPokemon(searchTerm);

        System.out.println(pokemonData.get("Sprite"));
        }

    }
}