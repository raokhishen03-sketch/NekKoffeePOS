package com.nekkoffee.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

public class OrderScreenController {

    @FXML private TextField searchField;

    @FXML private VBox coffeeCard;
    @FXML private VBox chocolateCard;
    @FXML private VBox whiteChocolateCard;
    @FXML private VBox matchaCard;
    @FXML private VBox snacksCard;


    @FXML
    public void initialize() {

        showAll();

        searchField.textProperty().addListener(
                (obs, oldVal, newVal)->{
                    filterMenu(newVal.trim().toLowerCase());
                }
        );

    }


    private void filterMenu(String text){

        if(text.isEmpty()){
            showAll();
            return;
        }

        hideAll();


        // coffee
        if(
                "coffee".contains(text)
                        ||"americano".contains(text)
                        ||"latte".contains(text)
                        ||"cappuccino".contains(text)
                        ||"extra shot".contains(text)
        ){

            coffeeCard.setVisible(true);
            coffeeCard.setManaged(true);

        }


        // chocolate
        if(
                "chocolate".contains(text)
                        ||"white chocolate".contains(text)
                        ||"hazelnut".contains(text)
                        ||"strawberry".contains(text)
                        ||"caramel".contains(text)
                        ||"berries".contains(text)
        ){

            chocolateCard.setVisible(true);
            chocolateCard.setManaged(true);

            whiteChocolateCard.setVisible(true);
            whiteChocolateCard.setManaged(true);

        }


        // matcha
        if(
                "matcha".contains(text)
                        ||"dirty matcha".contains(text)
                        ||"mango".contains(text)
        ){

            matchaCard.setVisible(true);
            matchaCard.setManaged(true);

        }


        // snacks
        if(
                "snacks".contains(text)
                        ||"pizza".contains(text)
                        ||"toast".contains(text)
                        ||"bread".contains(text)
                        ||"garlic".contains(text)
        ){

            snacksCard.setVisible(true);
            snacksCard.setManaged(true);

        }

    }



    @FXML
    private void showCoffee(){

        hideAll();

        coffeeCard.setVisible(true);
        coffeeCard.setManaged(true);

    }



    @FXML
    private void showChocolate(){

        hideAll();

        chocolateCard.setVisible(true);
        chocolateCard.setManaged(true);

        whiteChocolateCard.setVisible(true);
        whiteChocolateCard.setManaged(true);

    }




    @FXML
    private void showMatcha(){

        hideAll();

        matchaCard.setVisible(true);
        matchaCard.setManaged(true);

    }



    @FXML
    private void showSnacks(){

        hideAll();

        snacksCard.setVisible(true);
        snacksCard.setManaged(true);

    }



    @FXML
    private void showAll(){

        coffeeCard.setVisible(true);
        coffeeCard.setManaged(true);

        chocolateCard.setVisible(true);
        chocolateCard.setManaged(true);

        whiteChocolateCard.setVisible(true);
        whiteChocolateCard.setManaged(true);

        matchaCard.setVisible(true);
        matchaCard.setManaged(true);

        snacksCard.setVisible(true);
        snacksCard.setManaged(true);

    }



    private void hideAll(){

        coffeeCard.setVisible(false);
        coffeeCard.setManaged(false);

        chocolateCard.setVisible(false);
        chocolateCard.setManaged(false);

        whiteChocolateCard.setVisible(false);
        whiteChocolateCard.setManaged(false);

        matchaCard.setVisible(false);
        matchaCard.setManaged(false);

        snacksCard.setVisible(false);
        snacksCard.setManaged(false);

    }



    @FXML
    void hoverIn(MouseEvent e){

        VBox card=(VBox)e.getSource();

        card.setStyle("""
        -fx-background-color:#3b3b4d;
        -fx-background-radius:20;
        -fx-padding:20;
        -fx-effect:dropshadow(gaussian,#f4b56a,20,0.5,0,0);
        """);

    }



    @FXML
    void hoverOut(MouseEvent e){

        VBox card=(VBox)e.getSource();

        card.setStyle("""
        -fx-background-color:#2b2b3c;
        -fx-background-radius:20;
        -fx-padding:20;
        """);

    }

}