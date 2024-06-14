import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpClient;
import java.io.IOException;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.JFrame;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class PokeViewer implements  ActionListener{
    JFrame frame;
    JTextField textField;
    JTextField resultField;
    JButton searchButton;
    JPanel panel;

    Font myFont = new Font("Arial", Font.BOLD, 30);

    int searchTerm;
    List<String> pokemon;
    List<String> pokemonNames = new ArrayList<>();
    PokeViewer () {
        getPokemon();
        frame = new JFrame("PokéViewer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 600);
        frame.setLayout(null);
        frame.setResizable(false);

        textField = new JTextField();
        textField.setBounds(100,500,300,50);
        textField.setEditable(true);
        textField.setFont(myFont);

        resultField = new JTextField();
        resultField.setBounds(150,25,300,50);
        resultField.setEditable(false);
        resultField.setFont(myFont);
        resultField.setFocusable(false);

        searchButton = new JButton("Search");
        searchButton.setBounds(400,500,100,50);
        searchButton.addActionListener(this);

        panel = new JPanel();
        panel.setBounds(100, 85,400,400);
        panel.setBackground(Color.lightGray);

        frame.add(panel);
        frame.add(searchButton);
        frame.add(textField);
        frame.add(resultField);
        frame.setVisible(true);
    }

    public static void main(String[] args) throws IOException {
        PokeViewer poke = new PokeViewer();
    }
    public void getPokemon() {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://pokeapi.co/api/v2/pokemon?limit=100000&offset=0"))
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
        String myStr = String.valueOf(pokemonObject.get("results"));;
        String regex = "[\\[{},\\]\\s]";
        List<String> list = new ArrayList<>(Arrays.asList(myStr.split(regex)));
        list.removeAll(Arrays.asList("", null));
        pokemon = list;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == searchButton) {
        searchTerm = Integer.parseInt(textField.getText());
        Object[] pokemonArray = pokemon.toArray();
            for (int i = 0; i < pokemonArray.length; i++) {
                pokemonNames.add(pokemonArray[i].toString().substring(8, pokemonArray[i].toString().length()-1));
                i++;
            }
            if (Objects.equals(pokemonNames.getFirst(), "bulbasaur")) {
                pokemonNames.addFirst("No Pokémon Found");
            }
            resultField.setText(pokemonNames.get(searchTerm));
        }
    }
}