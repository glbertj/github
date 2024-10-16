package com.svx.github.view;

import com.svx.github.model.Repository;
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
    private MenuItem optionMenu;
    private MenuItem exitMenu;

    private final VBox sideBar;
    private final StackPane mainContent;

    public MainLayoutView() {
        super();
        topBarContainer = new VBox();
        sideBar = new VBox();
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

    // TODO! Custom title bar
    private void initializeTopBar() {
        topBarContainer.getStyleClass().add("top-bar-container");
        topBarContainer.setPrefHeight(60);

        MenuBar menuBar = new MenuBar();

        Menu fileMenu = new Menu("File");

        menuBar.getMenus().add(fileMenu);

        createRepositoryMenu = new MenuItem("New Repository...");
        addRepositoryMenu = new MenuItem("Add Local Repository...");
        optionMenu = new MenuItem("Options...");
        exitMenu = new MenuItem("Exit");

        fileMenu.getItems().addAll(createRepositoryMenu, new SeparatorMenuItem(), addRepositoryMenu,
                new SeparatorMenuItem(), optionMenu, new SeparatorMenuItem(), exitMenu);

        HBox topBar = new HBox();

        ComboBox<Repository> dropdown = getRepositoryComboBox();
        dropdown.getStyleClass().add("top-bar-dropdown");

        topBar.getChildren().add(dropdown);
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
        sideBar.getStyleClass().add("side-bar");
        sideBar.setPrefWidth(200);
    }

    public MenuItem getCreateRepositoryMenu() {
        return createRepositoryMenu;
    }

    public MenuItem getAddRepositoryMenu() {
        return addRepositoryMenu;
    }

    public MenuItem getExitMenu() {
        return exitMenu;
    }
}
