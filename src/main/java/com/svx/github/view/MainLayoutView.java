package com.svx.github.view;

import com.svx.github.model.Repository;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.util.Objects;

public class MainLayoutView extends View<BorderPane> {
    private final VBox topBarContainer;
    private MenuItem createRepositoryMenu;
    private MenuItem addRepositoryMenu;
    private MenuItem exitMenu;

    private ComboBox<Repository> repositoryDropdown;

    private final BorderPane sideBar;
    private Button changesButton;
    private Button historyButton;

    private VBox changesTab;
    private Button commitButton;

    private VBox historyTab;

    private final StackPane mainContent;

    public MainLayoutView() {
        super();
        topBarContainer = new VBox();
        sideBar = new BorderPane();
        mainContent = new StackPane();
    }

    @Override
    public void initializeView() {
        root = new BorderPane();

        styleReference = Objects.requireNonNull(getClass().getResource("/com/svx/github/style/main-layout.css")).toExternalForm();

        initializeTopBar();

        initializeSideBar();

        root.setTop(topBarContainer);
        root.setLeft(sideBar);
        root.setCenter(mainContent);
    }

    private void initializeTopBar() {
        topBarContainer.getStyleClass().add("top-bar-container");
        topBarContainer.setPrefHeight(60);

        MenuBar menuBar = new MenuBar();

        Menu fileMenu = new Menu("File");

        menuBar.getMenus().add(fileMenu);

        createRepositoryMenu = new MenuItem("New Repository...");
        addRepositoryMenu = new MenuItem("Add Local Repository...");
        exitMenu = new MenuItem("Exit");

        fileMenu.getItems().addAll(createRepositoryMenu, new SeparatorMenuItem(), addRepositoryMenu,
                new SeparatorMenuItem(), new SeparatorMenuItem(), exitMenu);

        HBox topBar = new HBox();

        repositoryDropdown = getRepositoryComboBox();

        topBar.getChildren().add(repositoryDropdown);
        topBarContainer.getChildren().addAll(menuBar, topBar);
    }

    private static ComboBox<Repository> getRepositoryComboBox() {
        ComboBox<Repository> dropdown = new ComboBox<>(Repository.getRepositories());
        dropdown.setConverter(new StringConverter<>() {
            @Override
            public String toString(Repository repository) {
                return repository != null ? repository.name() : "";
            }

            @Override
            public Repository fromString(String string) {
                return null;
            }
        });

        dropdown.setPrefWidth(200);
        return dropdown;
    }

    private void initializeSideBar() {
        changesButton = new Button("Changes");
        historyButton = new Button("History");

        HBox sideBarHeader = new HBox();
        sideBarHeader.getChildren().addAll(changesButton, historyButton);
        sideBar.setTop(sideBarHeader);

        initializeChangesTab();
        initializeHistoryTab();

        showChangesTab();
    }

    private void initializeChangesTab() {
        changesTab = new VBox();

        Label changesLabel = new Label("Changes");

        commitButton = new Button("Commit");

        changesTab.getChildren().addAll(
                changesLabel, commitButton);
    }

    private void initializeHistoryTab() {
        historyTab = new VBox();

        Label historyLabel = new Label("History");

        historyTab.getChildren().addAll(
                historyLabel);
    }

    // Change sidebar tab
    public void showChangesTab() {
        sideBar.setCenter(changesTab);
    }

    public void showHistoryTab() {
        sideBar.setCenter(historyTab);
    }

    // Menu
    public MenuItem getCreateRepositoryMenu() {
        return createRepositoryMenu;
    }

    public MenuItem getAddRepositoryMenu() {
        return addRepositoryMenu;
    }

    public MenuItem getExitMenu() {
        return exitMenu;
    }

    // Top bar
    public ComboBox<Repository> getRepositoryDropdown() {
        return repositoryDropdown;
    }

    // Sidebar header
    public Button getChangesButton() {
        return changesButton;
    }

    public Button getHistoryButton() {
        return historyButton;
    }

    // Sidebar changes tab
    public Button getCommitButton() {
        return commitButton;
    }
}
