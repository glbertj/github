package com.svx.github.view;

import com.svx.github.manager.RepositoryManager;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.Objects;

public class MainLayoutView extends View<BorderPane> {
    // Top Bar Components
    private final VBox topBarContainer = new VBox();
    private HBox repositoryToggleButton;
    private FontIcon repositoryIsShowingIcon;
    private HBox originButton;

    // Menu
    private MenuBar menuBar;
    private MenuItem createRepositoryMenu;
    private MenuItem addRepositoryMenu;
    private MenuItem cloneRepositoryMenu;
    private MenuItem logoutMenu;
    private MenuItem exitMenu;

    // Sidebar
    private BorderPane sideBar;
    private boolean showingRepositorySidebar = false;

    // Sidebar (Repository)
    private VBox repositorySidebar;

    // Sidebar (Default)
    private BorderPane defaultSidebar;
    private Button changesButton;
    private Button historyButton;
    private VBox changesTab;
    private VBox changedFilesList;
    private Button commitButton;
    private VBox historyTab;
    private VBox historyList;

    // Main Content
    private final StackPane mainContent = new StackPane();
    private TextArea textArea;

    // Methods //////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void initializeView() {
        root = new BorderPane();

        styleReference = Objects.requireNonNull(
                getClass().getResource("/com/svx/github/style/main-layout.css")
        ).toExternalForm();

        initializeTopBar();
        initializeSideBar();
        createTextArea();

        root.setTop(topBarContainer);
        root.setLeft(sideBar);
        root.setCenter(mainContent);
    }

    // Top Bar Methods
    private void initializeTopBar() {
        initializeMenu();

        repositoryToggleButton = createRepositoryToggleButton();
        repositoryToggleButton.getStyleClass().add("top-bar-button");
        repositoryToggleButton.getStyleClass().add("repository-toggle-button");

        switchOriginButton(OriginType.FETCH);

        HBox topBar = new HBox(repositoryToggleButton, originButton);
        topBar.getStyleClass().add("top-bar-container");

        topBarContainer.getChildren().addAll(menuBar, topBar);
    }

    private HBox createRepositoryToggleButton() {
        FontIcon iconView = new FontIcon("fas-laptop-code");
        iconView.getStyleClass().add("top-bar-icon");

        Label currentRepoLabel = new Label("Current Repository");
        currentRepoLabel.getStyleClass().add("top-bar-primary-label");

        Label selectedRepoName = new Label("No repository selected");
        selectedRepoName.getStyleClass().add("top-bar-secondary-label");

        VBox textContent = new VBox(currentRepoLabel, selectedRepoName);
        textContent.setSpacing(2);
        textContent.setAlignment(Pos.CENTER_LEFT);

        HBox leftContent = new HBox(iconView, textContent);
        leftContent.setSpacing(10);
        leftContent.setAlignment(Pos.CENTER_LEFT);

        repositoryIsShowingIcon = new FontIcon("fas-angle-down");
        repositoryIsShowingIcon.getStyleClass().add("top-bar-icon");

        HBox buttonContent = new HBox(leftContent, repositoryIsShowingIcon);
        buttonContent.setSpacing(10);
        buttonContent.setAlignment(Pos.CENTER);
        buttonContent.setStyle("-fx-background-color: #2f363d; -fx-background-radius: 5px;");

        HBox.setHgrow(leftContent, Priority.ALWAYS);
        buttonContent.setStyle("-fx-justify-content: space-between;");

        RepositoryManager.currentRepositoryProperty().addListener((observable, oldRepo, newRepo) -> {
            if (newRepo != null) {
                selectedRepoName.setText(newRepo.getName());
            } else {
                selectedRepoName.setText("No repository selected");
            }
        });

        return buttonContent;
    }

    public void switchOriginButton(OriginType type) {
        switch (type) {
            case FETCH -> switchOriginButton("Fetch Origin", "fas-sync-alt", "some time");
            case PUSH -> switchOriginButton("Push", "fas-arrow-up", "some time");
            case PULL -> switchOriginButton("Pull", "fas-arrow-down", "some time");
        }
    }

    private void switchOriginButton(String originAction, String iconCode, String lastFetched) {
        FontIcon fetchIcon = new FontIcon(iconCode);
        fetchIcon.getStyleClass().add("top-bar-icon");

        Label fetchLabel = new Label(originAction);
        fetchLabel.getStyleClass().add("top-bar-primary-label");
        Label lastFetchedLabel = new Label(lastFetched);
        lastFetchedLabel.getStyleClass().add("top-bar-secondary-label");

        VBox textContent = new VBox(fetchLabel, lastFetchedLabel);
        textContent.setSpacing(2);
        textContent.setAlignment(Pos.CENTER_LEFT);

        originButton = new HBox(fetchIcon, textContent);
        originButton.getStyleClass().add("top-bar-button");
        originButton.setSpacing(10);

    }

    private void initializeMenu() {
        menuBar = new MenuBar();
        menuBar.getStyleClass().add("menu-bar");

        Menu fileMenu = new Menu("File");
        createRepositoryMenu = new MenuItem("New Repository...");
        addRepositoryMenu = new MenuItem("Add Local Repository...");
        cloneRepositoryMenu = new MenuItem("Clone Repository...");
        logoutMenu = new MenuItem("Logout");
        exitMenu = new MenuItem("Exit");

        fileMenu.getItems().addAll(
                createRepositoryMenu, new SeparatorMenuItem(),
                addRepositoryMenu, new SeparatorMenuItem(),
                cloneRepositoryMenu, new SeparatorMenuItem(),
                logoutMenu, exitMenu
        );

        Menu viewMenu = new Menu("View");
        MenuItem changesMenuItem = new MenuItem("Changes");
        MenuItem historyMenuItem = new MenuItem("History");
        MenuItem repositoryListMenuItem = new MenuItem("Repository List");
        MenuItem toggleFullScreenMenuItem = new MenuItem("Toggle Full Screen");

        viewMenu.getItems().addAll(
                changesMenuItem, historyMenuItem, new SeparatorMenuItem(),
                repositoryListMenuItem, new SeparatorMenuItem(),
                toggleFullScreenMenuItem
        );

        Menu repositoryMenu = new Menu("Repository");
        MenuItem pushMenuItem = new MenuItem("Push");
        MenuItem pullMenuItem = new MenuItem("Pull");
        MenuItem fetchMenuItem = new MenuItem("Fetch");
        MenuItem removeRepositoryMenuItem = new MenuItem("Remove Repository...");
        MenuItem showInExplorerMenuItem = new MenuItem("Show in Explorer");

        repositoryMenu.getItems().addAll(
                pushMenuItem, pullMenuItem, fetchMenuItem, new SeparatorMenuItem(),
                removeRepositoryMenuItem, new SeparatorMenuItem(),
                showInExplorerMenuItem
        );

        menuBar.getMenus().addAll(fileMenu, viewMenu, repositoryMenu);
    }

    // Side Bar Methods
    private void initializeSideBar() {
        sideBar = new BorderPane();

        initializeDefaultSideBar();
        initializeRepositorySidebar();

        sideBar.setCenter(defaultSidebar);
    }

    private void initializeDefaultSideBar() {
        defaultSidebar = new BorderPane();
        defaultSidebar.getStyleClass().add("sidebar");

        changesButton = createSidebarButton("Changes");
        changesButton.getStyleClass().add("active");
        historyButton = createSidebarButton("History");

        HBox sideBarHeader = new HBox(changesButton, historyButton);
        sideBarHeader.setSpacing(5);
        sideBarHeader.getStyleClass().add("sidebar-header");

        defaultSidebar.setTop(sideBarHeader);

        initializeChangesTab();
        initializeHistoryTab();

        switchToChangesTab();
    }

    private void initializeRepositorySidebar() {
        repositorySidebar = new VBox();
        repositorySidebar.getStyleClass().add("sidebar");

        Label repositoryLabel = new Label("Repositories");
        repositoryLabel.getStyleClass().add("sidebar-label");

        VBox repositoryList = new VBox();
        repositoryList.setSpacing(5);

        repositorySidebar.getChildren().addAll(repositoryLabel, repositoryList);
    }

    public void switchSideBar() {
        if (showingRepositorySidebar) {
            sideBar.setCenter(defaultSidebar);
            repositoryToggleButton.getStyleClass().remove("active");
            repositoryIsShowingIcon.setIconLiteral("fas-angle-down");
        } else {
            sideBar.setCenter(repositorySidebar);
            repositoryToggleButton.getStyleClass().add("active");
            repositoryIsShowingIcon.setIconLiteral("fas-angle-up");
        }

        showingRepositorySidebar = !showingRepositorySidebar;
    }

    private Button createSidebarButton(String text) {
        Button button = new Button(text);
        button.getStyleClass().add("button");
        button.setOnAction(e -> handleTabSwitch(button));
        return button;
    }

    private void handleTabSwitch(Button activeButton) {
        changesButton.getStyleClass().remove("active");
        historyButton.getStyleClass().remove("active");

        activeButton.getStyleClass().add("active");

        if (activeButton == changesButton) {
            switchToChangesTab();
        } else if (activeButton == historyButton) {
            switchToHistoryTab();
        }
    }

    private void initializeChangesTab() {
        changesTab = new VBox();
        changesTab.getStyleClass().add("tab-content");

        Label changesLabel = new Label("Changes");
        changesLabel.getStyleClass().add("tab-label");

        changedFilesList = new VBox();
        changedFilesList.setSpacing(5);

        commitButton = createAnimatedButton("Commit");
        commitButton.getStyleClass().add("commit-button");

        changesTab.getChildren().addAll(changesLabel, changedFilesList, commitButton);
    }

    private void initializeHistoryTab() {
        historyTab = new VBox();
        historyTab.getStyleClass().add("tab-content");

        Label historyLabel = new Label("History");
        historyLabel.getStyleClass().add("tab-label");

        historyList = new VBox();

        historyTab.getChildren().addAll(historyLabel, historyList);
    }

    private void createTextArea() {
        textArea = new TextArea("Halo");
        textArea.getStyleClass().add("text-area");
        textArea.setWrapText(true);

        mainContent.getChildren().add(textArea);
    }

    public enum OriginType {
        FETCH, PUSH, PULL
    }

    public void switchToChangesTab() {
        defaultSidebar.setCenter(changesTab);
    }
    public void switchToHistoryTab() {
        defaultSidebar.setCenter(historyTab);
    }

    public MenuItem getCreateRepositoryMenu() { return createRepositoryMenu; }
    public MenuItem getAddRepositoryMenu() { return addRepositoryMenu; }
    public MenuItem getCloneRepositoryMenu() { return cloneRepositoryMenu; }
    public MenuItem getLogoutMenu() { return logoutMenu; }
    public MenuItem getExitMenu() { return exitMenu; }
    public HBox getRepositoryToggleButton() { return repositoryToggleButton; }
    public HBox getOriginButton() { return originButton; }
    public Button getChangesButton() { return changesButton; }
    public Button getHistoryButton() { return historyButton; }
    public Button getCommitButton() { return commitButton; }
    public VBox getChangedFilesList() { return changedFilesList; }
    public VBox getHistoryList() { return historyList; }
    public TextArea getTextArea() { return textArea; }
}