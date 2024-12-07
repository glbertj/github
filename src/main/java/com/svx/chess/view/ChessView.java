package com.svx.chess.view;

import com.svx.chess.model.Chess;
import com.svx.chess.model.ChessBoard;
import com.svx.chess.model.ChessTile;
import com.svx.github.manager.ConnectionManager;
import com.svx.github.view.View;
import javafx.beans.binding.Bindings;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import java.util.Objects;

public class ChessView extends View<StackPane> {
    private Chess.PieceColor playerColor;

    // Left
    private ChessBoard chessBoard;

    private FlowPane capturedWhiteBox;
    private FlowPane capturedBlackBox;
    private Button backToLoginButton;

    @Override
    public void initializeView() {
        root = new StackPane();
        BorderPane contentContainer = new BorderPane();
        contentContainer.getStyleClass().add("game-root");
        styleReference = Objects.requireNonNull(
                getClass().getResource("/com/svx/chess/style/game.css")
        ).toExternalForm();

        HBox leftSection = new HBox();

        playerColor = Math.random() > 0.5 ? Chess.PieceColor.WHITE : Chess.PieceColor.BLACK;
        chessBoard = new ChessBoard(playerColor);

        leftSection.getChildren().add(chessBoard);
        leftSection.setAlignment(Pos.CENTER);

        VBox rightSection = createRightSection();

        Separator verticalSeparator = new Separator();
        verticalSeparator.setOrientation(Orientation.VERTICAL);
        verticalSeparator.setPrefWidth(1);
        verticalSeparator.getStyleClass().add("vertical-separator");

        HBox container = new HBox(20, leftSection, verticalSeparator, rightSection);
        HBox.setHgrow(leftSection, Priority.ALWAYS);
        HBox.setHgrow(rightSection, Priority.ALWAYS);

        leftSection.prefWidthProperty().bind(container.widthProperty().divide(2));
        rightSection.prefWidthProperty().bind(container.widthProperty().divide(2));

        contentContainer.setCenter(container);

        ImageView backgroundImage = new ImageView(new Image(
                Objects.requireNonNull(getClass().getResource("/com/svx/github/image/auth-background.png")).toExternalForm()
        ));
        backgroundImage.setPreserveRatio(true);
        backgroundImage.fitWidthProperty().bind(contentContainer.widthProperty());
        backgroundImage.fitHeightProperty().bind(contentContainer.heightProperty());
        backgroundImage.setMouseTransparent(true);

        root.getChildren().addAll(contentContainer);
    }

    public void showValidMoves(ChessTile selectedTile, int[] validMoves) {
        ChessTile[][] tiles = chessBoard.getTiles();

        for (int move : validMoves) {
            int targetRow = move / 8;
            int targetCol = move % 8;

            ChessTile targetTile = tiles[targetRow][targetCol];
            if (targetTile == null) return;
            targetTile.setIsValidMove(true);

            if (targetTile.getPiece() != null) {
                if (!selectedTile.getPiece().getColor().equals(targetTile.getPiece().getColor())) {
                    targetTile.setIsEatable(true);
                    targetTile.setIsValidMove(false);
                }
            }
        }
    }

    public void hideValidMoves() {
        for (Node node : chessBoard.getChildren()) {
            if (node instanceof ChessTile) {
                ChessTile tile = (ChessTile) node;
                tile.setIsValidMove(false);
                tile.setIsEatable(false);
                tile.setIsEnPassantMove(false);
                tile.setIsCastleMove(false);
            }
        }
    }

    public void clearHighlightedTiles() {
        for (Node node : chessBoard.getChildren()) {
            if (node instanceof ChessTile) {
                ChessTile tile = (ChessTile) node;
                tile.setIsRecentMove(false);
            }
        }
    }

    // Right Section
    private VBox createRightSection() {
        Label opponentName = new Label("Opponent");
        opponentName.getStyleClass().add("primary-text");
        Label playerName = new Label("Player (You)");
        playerName.getStyleClass().add("primary-text");

        capturedWhiteBox = new FlowPane();
        capturedWhiteBox.getStyleClass().add("captured-box");

        capturedBlackBox = new FlowPane();
        capturedBlackBox.getStyleClass().add("captured-box");

        VBox topSection;
        if (playerColor.equals(Chess.PieceColor.WHITE)) {
            topSection = new VBox(opponentName, capturedWhiteBox, playerName, capturedBlackBox);
        } else {
            topSection = new VBox(opponentName, capturedBlackBox, playerName, capturedWhiteBox);
        }
        VBox.setVgrow(topSection, Priority.ALWAYS);

        Label onlineLabel = new Label("Online Status: ");
        onlineLabel.getStyleClass().add("primary-text");

        // Right
        Label onlineStatus = new Label();

        onlineStatus.textProperty().bind(Bindings.createStringBinding(() ->
                        ConnectionManager.isOnlineProperty().get() ? "Online" : "Offline",
                ConnectionManager.isOnlineProperty()));

        onlineStatus.styleProperty().bind(Bindings.createStringBinding(() ->
                        ConnectionManager.isOnlineProperty().get() ? "-fx-text-fill: green;" : "-fx-text-fill: red;",
                ConnectionManager.isOnlineProperty()));

        onlineStatus.getStyleClass().add("primary-text");
        HBox onlineTextBox = new HBox(onlineLabel, onlineStatus);
        backToLoginButton = createBackToLoginButton();

        VBox bottomSection = new VBox(onlineTextBox, backToLoginButton);
        VBox.setVgrow(bottomSection, Priority.NEVER);

        return new VBox(topSection, bottomSection);
    }

    public Button createBackToLoginButton() {
        Button button = new Button("Back to Login");
        button.getStyleClass().add("primary-button");

        button.disableProperty().bind(
                Bindings.createBooleanBinding(
                        () -> !ConnectionManager.isOnlineProperty().get(),
                        ConnectionManager.isOnlineProperty()
                )
        );

        button.setOnAction(e -> {

        });

        return button;
    }

    public Chess.PieceColor getPlayerColor() { return playerColor; }
    public void setPlayerColor(Chess.PieceColor playerColor) { this.playerColor = playerColor; }
    public ChessBoard getChessBoard() { return chessBoard; }
    public FlowPane getCapturedWhiteBox() { return capturedWhiteBox; }
    public FlowPane getCapturedBlackBox() { return capturedBlackBox; }
    public Button getBackToLoginButton() { return backToLoginButton; }
}