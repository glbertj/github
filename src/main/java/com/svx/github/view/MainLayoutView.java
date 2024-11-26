package com.svx.github.view;

import com.svx.github.manager.RepositoryManager;
import javafx.beans.binding.Bindings;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.fxmisc.richtext.InlineCssTextArea;
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
    private MenuItem changesMenuItem;
    private MenuItem historyMenuItem;
    private MenuItem toggleFullScreenMenuItem;
    private MenuItem removeRepositoryMenuItem;
    private MenuItem showInExplorerMenuItem;
    private MenuItem showInVsCodeMenuItem;

    // Sidebar
    private BorderPane sideBar;
    private boolean showingRepositorySidebar = false;

    // Sidebar (Repository)
    private VBox repositorySidebar;
    private VBox repositoryList;

    // Sidebar (Default)
    private BorderPane defaultSidebar;
    private boolean showingHistoryTab = false;
    private Button changesButton;
    private Button historyButton;

    // Sidebar (Changes)
    private VBox changesTab;
    private VBox changedFilesList;
    private VBox commitSection;
    private TextField commitSummaryTextField;
    private TextArea commitDescriptionTextArea;
    private Button commitButton;
    private Label changesLabel;

    // Sidebar (History)
    private VBox historyTab;
    private VBox historyList;
    private Label historyLabel;

    // Main Content
    private final StackPane mainContent = new StackPane();
    private InlineCssTextArea textArea;
    private Pane mainContentOverlay;

    // Methods //////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void initializeView() {
        root = new BorderPane();

        styleReference = Objects.requireNonNull(
                getClass().getResource("/com/svx/github/style/main-layout.css")
        ).toExternalForm();

        initializeTopBar();
        initializeSideBar();
        initializeMainContent();

        root.setTop(topBarContainer);
        root.setLeft(sideBar);
        root.setCenter(mainContent);
    }

    // Top Bar
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
        iconView.getStyleClass().add("icon");

        Label currentRepoLabel = new Label("Current Repository");
        currentRepoLabel.getStyleClass().add("secondary-text");

        Label selectedRepoName = new Label("No repository selected");
        selectedRepoName.getStyleClass().add("primary-text");

        VBox textContent = new VBox(currentRepoLabel, selectedRepoName);
        textContent.setSpacing(2);
        textContent.setAlignment(Pos.CENTER_LEFT);

        HBox leftContent = new HBox(iconView, textContent);
        leftContent.setSpacing(10);
        leftContent.setAlignment(Pos.CENTER_LEFT);

        repositoryIsShowingIcon = new FontIcon("fas-angle-down");
        repositoryIsShowingIcon.getStyleClass().add("icon");

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
        fetchIcon.getStyleClass().add("icon");

        Label fetchLabel = new Label(originAction);
        fetchLabel.getStyleClass().add("primary-text");
        Label lastFetchedLabel = new Label(lastFetched);
        lastFetchedLabel.getStyleClass().add("secondary-text");

        VBox textContent = new VBox(fetchLabel, lastFetchedLabel);
        textContent.setSpacing(2);
        textContent.setAlignment(Pos.CENTER_LEFT);

        if (originButton == null) {
            originButton = new HBox(fetchIcon, textContent);
            originButton.getStyleClass().add("top-bar-button");
            originButton.setSpacing(10);
        } else {
            HBox newOriginButton = new HBox(fetchIcon, textContent);
            newOriginButton.getStyleClass().add("top-bar-button");
            newOriginButton.setSpacing(10);

            Parent parent = originButton.getParent();
            if (parent instanceof Pane pane) {
                int index = pane.getChildren().indexOf(originButton);
                if (index >= 0) {
                    pane.getChildren().set(index, newOriginButton);
                }
            }

            originButton = newOriginButton;
        }
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
        changesMenuItem = new MenuItem("Changes");
        historyMenuItem = new MenuItem("History");
        historyMenuItem.setOnAction(e -> switchToHistoryTab());
        MenuItem repositoryListMenuItem = new MenuItem("Repository List");
        repositoryListMenuItem.setOnAction(e -> {
            if (!showingRepositorySidebar) {
                switchSideBar();
            }
        });
        toggleFullScreenMenuItem = new MenuItem("Toggle Full Screen");

        viewMenu.getItems().addAll(
                changesMenuItem, historyMenuItem, new SeparatorMenuItem(),
                repositoryListMenuItem, new SeparatorMenuItem(),
                toggleFullScreenMenuItem
        );

        Menu repositoryMenu = new Menu("Repository");
        removeRepositoryMenuItem = new MenuItem("Remove Repository");
        showInExplorerMenuItem = new MenuItem("Show in Explorer");
        showInVsCodeMenuItem = new MenuItem("Show in VS Code");

        repositoryMenu.getItems().addAll(
                removeRepositoryMenuItem, new SeparatorMenuItem(),
                showInExplorerMenuItem, showInVsCodeMenuItem
        );

        menuBar.getMenus().addAll(fileMenu, viewMenu, repositoryMenu);
    }

    // Side Bar
    private void initializeSideBar() {
        sideBar = new BorderPane();

        initializeRepositorySidebar();
        initializeDefaultSideBar();

        sideBar.setCenter(defaultSidebar);
    }

    private void initializeRepositorySidebar() {
        repositorySidebar = new VBox();
        repositorySidebar.getStyleClass().add("sidebar");
        repositorySidebar.getStyleClass().add("repository-sidebar");

        TextField searchField = new TextField();
        searchField.setPromptText("Search repositories...");
        searchField.setPrefWidth(230);
        Button addRepositoryButton = new Button("Add");

        HBox repositoryHeader = new HBox(searchField, addRepositoryButton);
        repositoryHeader.getStyleClass().addAll("sidebar-header", "repository-sidebar");
        repositoryHeader.setSpacing(8);
        repositoryHeader.setAlignment(Pos.CENTER_LEFT);

        Label repositoryLabel = new Label("Recent Repositories");
        repositoryLabel.getStyleClass().add("primary-text");

        repositoryList = new VBox();
        repositoryList.setSpacing(5);
        repositoryList.getStyleClass().add("repository-list");
        repositoryList.getChildren().addFirst(repositoryLabel);

        repositorySidebar.getChildren().addAll(repositoryHeader, repositoryList);
    }

    private void initializeDefaultSideBar() {
        defaultSidebar = new BorderPane();
        defaultSidebar.getStyleClass().add("sidebar");
        defaultSidebar.getStyleClass().add("default-sidebar");

        changesButton = new Button("Changes");
        changesButton.getStyleClass().add("active");
        changesButton.setFocusTraversable(false);

        historyButton = new Button("History");
        historyButton.setFocusTraversable(false);

        HBox sideBarHeader = new HBox(changesButton, historyButton);
        sideBarHeader.getStyleClass().add("sidebar-header");

        initializeChangesTab();
        initializeHistoryTab();

        defaultSidebar.setTop(sideBarHeader);
        defaultSidebar.setCenter(changesTab);
        defaultSidebar.setBottom(commitSection);
    }

    private void initializeChangesTab() {
        changesTab = new VBox();
        changesTab.getStyleClass().add("tab-content");

        changesLabel = new Label("0 changed files");
        changesLabel.getStyleClass().add("tab-label");

        changedFilesList = new VBox();

        changesTab = new VBox();
        changesTab.getChildren().addAll(changesLabel, changedFilesList);

        commitSummaryTextField = new TextField();
        commitSummaryTextField.setPromptText("Summary (required)");
        commitDescriptionTextArea = new TextArea();
        commitDescriptionTextArea.setPromptText("Description");
        commitDescriptionTextArea.getStyleClass().add("commit-description-text-area");
        commitButton = new Button("Commit to master");
        commitButton.setMaxWidth(Double.MAX_VALUE);
        commitButton.disableProperty().bind(
                Bindings.createBooleanBinding(
                        () -> commitSummaryTextField.getText().trim().isEmpty(),
                        commitSummaryTextField.textProperty()
                )
        );

        commitSection = new VBox();
        commitSection.getChildren().addAll(commitSummaryTextField, commitDescriptionTextArea, commitButton);
        commitSection.getStyleClass().addAll("commit-section");
    }

    private void initializeHistoryTab() {
        historyTab = new VBox();

        historyLabel = new Label("0 commits");
        historyLabel.getStyleClass().add("tab-label");

        historyList = new VBox();

        historyTab.getChildren().addAll(historyLabel, historyList);
    }

    public void switchSideBar() {
        if (showingRepositorySidebar) {
            sideBar.setCenter(defaultSidebar);
            repositoryToggleButton.getStyleClass().remove("active");
            repositoryIsShowingIcon.setIconLiteral("fas-angle-down");
            mainContentOverlay.setVisible(false);
        } else {
            sideBar.setCenter(repositorySidebar);
            repositoryToggleButton.getStyleClass().add("active");
            repositoryIsShowingIcon.setIconLiteral("fas-angle-up");
            mainContentOverlay.setVisible(true);
        }

        showingRepositorySidebar = !showingRepositorySidebar;
    }

    public void switchToChangesTab() {
        if (showingHistoryTab) {
            defaultSidebar.setCenter(changesTab);
            defaultSidebar.setBottom(commitSection);
            changesButton.getStyleClass().add("active");
            historyButton.getStyleClass().remove("active");
            showingHistoryTab = false;

            getHistoryList().getChildren().forEach(node -> {
                if (node instanceof HBox hbox) {
                    hbox.getStyleClass().remove("active");
                }
            });
        }
    }

    public void switchToHistoryTab() {
        if (!showingHistoryTab) {
            defaultSidebar.setCenter(historyTab);
            defaultSidebar.setBottom(null);
            changesButton.getStyleClass().remove("active");
            historyButton.getStyleClass().add("active");
            showingHistoryTab = true;

            getChangedFilesList().getChildren().forEach(node -> {
                if (node instanceof HBox hbox) {
                    hbox.getStyleClass().remove("active");
                }
            });
        }
    }

    private void initializeMainContent() {
        mainContent.getStyleClass().add("main-content");
        textArea = new InlineCssTextArea();
        textArea.getStyleClass().add("text-area");
        textArea.setStyle(0, textArea.getLength(), "-fx-fill: white;");
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setFocusTraversable(false);

        mainContentOverlay = new Pane();
        mainContentOverlay.getStyleClass().add("overlay");
        mainContentOverlay.setMouseTransparent(true);
        mainContentOverlay.setVisible(false);

        mainContent.getChildren().addAll(textArea, mainContentOverlay);
    }

    public enum OriginType {
        FETCH, PUSH, PULL
    }

    public MenuItem getCreateRepositoryMenu() { return createRepositoryMenu; }
    public MenuItem getAddRepositoryMenu() { return addRepositoryMenu; }
    public MenuItem getCloneRepositoryMenu() { return cloneRepositoryMenu; }
    public MenuItem getLogoutMenu() { return logoutMenu; }
    public MenuItem getExitMenu() { return exitMenu; }
    public MenuItem getChangesMenuItem() { return changesMenuItem; }
    public MenuItem getHistoryMenuItem() { return historyMenuItem; }
    public MenuItem getToggleFullScreenMenuItem() { return toggleFullScreenMenuItem; }
    public MenuItem getRemoveRepositoryMenuItem() { return removeRepositoryMenuItem; }
    public MenuItem getShowInExplorerMenuItem() { return showInExplorerMenuItem; }
    public MenuItem getShowInVsCodeMenuItem() { return showInVsCodeMenuItem; }
    public HBox getRepositoryToggleButton() { return repositoryToggleButton; }
    public VBox getRepositoryList() { return repositoryList; }
    public HBox getOriginButton() { return originButton; }
    public Button getChangesButton() { return changesButton; }
    public Button getHistoryButton() { return historyButton; }
    public TextField getCommitSummaryTextField() { return commitSummaryTextField; }
    public TextArea getCommitDescriptionTextArea() { return commitDescriptionTextArea; }
    public Button getCommitButton() { return commitButton; }
    public Label getChangesLabel() { return changesLabel; }
    public VBox getChangedFilesList() { return changedFilesList; }
    public Label getHistoryLabel() { return historyLabel; }
    public VBox getHistoryList() { return historyList; }
    public InlineCssTextArea getTextArea() { return textArea; }
}