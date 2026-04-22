xml_content = """<?xml version="1.0" encoding="UTF-8"?>
<?import javafx.scene.control.Label?>
<?import javafx.scene.layout.BorderPane?>
<?import javafx.scene.layout.GridPane?>
<?import javafx.scene.layout.HBox?>
<?import javafx.scene.layout.Pane?>
<?import javafx.scene.layout.Region?>
<?import javafx.scene.layout.StackPane?>
<?import javafx.scene.text.Font?>

<BorderPane style="-fx-background-color: #2b2b2b;" xmlns="http://javafx.com/javafx/8" xmlns:fx="http://javafx.com/fxml/1" fx:controller="com.connectfour.GameController">
   <top>
      <HBox alignment="CENTER" style="-fx-padding: 20px;">
         <children>
            <Label fx:id="statusLabel" text="Player 1's Turn (Red)" textFill="#e74c3c">
               <font>
                  <Font name="Segoe UI Bold" size="24.0" />
               </font>
            </Label>
         </children>
      </HBox>
   </top>
   <center>
      <StackPane fx:id="gameArea">
         <children>
            <Pane fx:id="discRoot" prefWidth="560.0" prefHeight="480.0" />
            
            <GridPane fx:id="boardGrid" prefWidth="560.0" prefHeight="480.0" style="-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.8), 10, 0, 0, 0);">
"""

for r in range(6):
    for c in range(7):
        xml_content += f"""               <Region GridPane.columnIndex="{c}" GridPane.rowIndex="{r}" prefWidth="80" prefHeight="80" style="-fx-background-color: #2980b9; -fx-shape: 'M 0 0 h 80 v 80 h -80 v -80 Z M 40 8 a 32 32 0 1 0 0.0001 0 Z';" />\n"""

xml_content += """            </GridPane>

            <GridPane fx:id="clickGrid" prefWidth="560.0" prefHeight="480.0">
"""

for c in range(7):
    xml_content += f"""               <Pane GridPane.columnIndex="{c}" GridPane.rowIndex="0" GridPane.rowSpan="6" prefWidth="80" prefHeight="480" style="-fx-cursor: hand;" onMouseClicked="#handleColumnClick" onMouseEntered="#handleColumnHover" onMouseExited="#handleColumnExit" userData="{c}" />\n"""

xml_content += """            </GridPane>
         </children>
      </StackPane>
   </center>
</BorderPane>
"""

with open("src/com/connectfour/GameView.fxml", "w") as f:
    f.write(xml_content)
